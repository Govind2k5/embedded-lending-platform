package com.lendingplatform.lender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Cache-aside wrapper around lender eligibility rules.
 * PostgreSQL is the source of truth; Redis just saves repeated reads
 * of rules that rarely change but get checked on every application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LenderCacheService {

    private static final String KEY_PREFIX = "lender:";
    private static final String KEY_SUFFIX = ":rules";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final LenderRepository lenderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public List<LenderRules> getActiveLenderRules() {
        List<Long> activeLenderIds = lenderRepository.findByActiveTrue().stream()
                .map(Lender::getId)
                .toList();
        return activeLenderIds.stream().map(this::getRules).toList();
    }

    public LenderRules getRules(Long lenderId) {
        String key = cacheKey(lenderId);
        Object raw = redisTemplate.opsForValue().get(key);
        LenderRules cached = raw != null ? objectMapper.convertValue(raw, LenderRules.class) : null;
        if (cached != null) {
            log.debug("Lender rules cache hit for lender {}", lenderId);
            return cached;
        }

        log.debug("Lender rules cache miss for lender {}, loading from database", lenderId);
        Lender lender = lenderRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lender not found: " + lenderId));
        LenderRules rules = LenderRules.from(lender);
        redisTemplate.opsForValue().set(key, rules, TTL);
        return rules;
    }

    public void evict(Long lenderId) {
        redisTemplate.delete(cacheKey(lenderId));
    }

    private String cacheKey(Long lenderId) {
        return KEY_PREFIX + lenderId + KEY_SUFFIX;
    }
}
