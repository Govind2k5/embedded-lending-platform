package com.lendingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the whole backend. This is a MODULAR MONOLITH: one single
 * Spring Boot application (one JVM process, one deployable jar/container),
 * internally split into feature packages by business domain
 * (borrower, lender, merchant, loanapplication, eligibility, offer, loan,
 * repayment, common) instead of being split into separate microservices.
 *
 * Why one deployable instead of microservices? At this scale there is no
 * need to scale or deploy any single domain independently, so separate
 * services would only add network calls and distributed-transaction
 * complexity (e.g. "approve a loan" touches 3 tables that must commit
 * together) without buying anything back. The package boundaries still give
 * the same organizational clarity microservices would - see each package's
 * service classes, which only ever call each other by plain Long ids, never
 * by reaching into another package's JPA entities.
 */
@SpringBootApplication // enables component scanning, auto-configuration and property support in one annotation
public class EmbeddedLendingPlatformApplication {

    public static void main(String[] args) {
        // Boots the embedded Tomcat server, runs Flyway migrations, wires up
        // every @Service/@Repository/@RestController/@Component bean, then
        // starts listening on the configured port (8080 by default).
        SpringApplication.run(EmbeddedLendingPlatformApplication.class, args);
    }
}
