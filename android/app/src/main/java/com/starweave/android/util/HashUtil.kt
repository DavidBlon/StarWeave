package com.starweave.android.util

import java.security.MessageDigest

object HashUtil {
    /**
     * Mulberry32 PRNG - deterministic pseudo-random from a seed.
     * Returns a function that generates floats in [0, 1).
     */
    fun mulberry32(seed: Int): () -> Float {
        var state = seed
        return {
            state = state + 0x6D2B79F5
            var t = state
            t = (t xor (t ushr 15)) * t xor (t xor (t ushr 15))
            t = t xor (t + (t xor (t ushr 7)) * t)
            ((t xor (t ushr 14)) and 0x7FFFFFFF) / 0x7FFFFFFF.toFloat()
        }
    }

    /**
     * Hash a string to a 32-bit integer seed (matches web hashCode).
     */
    fun hashCode(text: String): Int {
        var hash = 0
        for (ch in text) {
            hash = (31 * hash + ch.code) and 0xFFFFFFFF.toInt()
        }
        return hash
    }

    /**
     * SHA-256 hex string.
     */
    fun sha256(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
