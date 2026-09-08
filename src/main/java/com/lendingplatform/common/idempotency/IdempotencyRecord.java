package com.lendingplatform.common.idempotency;

/**
 * What actually gets stored in Redis under "idempotency:{key}" (see
 * IdempotencyService). Bundles everything needed to either detect a
 * conflicting reuse of the same key, or to replay the exact original HTTP
 * response on a legitimate retry:
 *
 * @param requestHash  SHA-256 hex digest of the original request body's JSON -
 *                      compared against a retry's hash to tell "same intent,
 *                      safe to replay" apart from "different body, reject".
 * @param responseBody the original controller response object (e.g. a
 *                      LoanApplicationResponse) - stored as Object since this
 *                      record is reused for any endpoint's response type.
 * @param statusCode   the original HTTP status (e.g. 201) to replay verbatim.
 */
public record IdempotencyRecord(
        String requestHash,
        Object responseBody,
        int statusCode
) {
}
