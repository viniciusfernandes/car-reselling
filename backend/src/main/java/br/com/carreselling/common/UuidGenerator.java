package br.com.carreselling.common;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Generates UUID version 7 (time-ordered) as specified by RFC 9562.
 *
 * Layout (128 bits):
 * ┌─────────────────────────────────────────────────────────────────────┐
 * │  0-47   │ 48-51  │  52-63  │ 64-65   │           66-127            │
 * │ unix_ms │ ver=7  │ rand_a  │ var=10  │           rand_b            │
 * └─────────────────────────────────────────────────────────────────────┘
 *
 * Compared to UUID v4 (pure random), v7 UUIDs are monotonically increasing
 * within the same millisecond window, which makes them safe as primary keys
 * in databases — no index fragmentation, natural sort order.
 */
public final class UuidGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private UuidGenerator() {}

    /**
     * Returns a new UUID v7.
     */
    public static UUID generate() {
        long now = System.currentTimeMillis();

        // MSB: 48-bit timestamp | 4-bit version (0x7) | 12-bit rand_a
        long msb = (now << 16)
                 | 0x7000L
                 | (RANDOM.nextLong() & 0x0FFFL);

        // LSB: 2-bit variant (10) | 62-bit rand_b
        long lsb = 0x8000000000000000L
                 | (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL);

        return new UUID(msb, lsb);
    }
}
