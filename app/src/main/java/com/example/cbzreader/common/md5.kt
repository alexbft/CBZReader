package com.example.cbzreader.common

import java.math.BigInteger
import java.security.MessageDigest

fun md5(s: String): String {
    val bytes = MessageDigest.getInstance("MD5").digest(s.toByteArray(Charsets.US_ASCII))
    return String.format("%032x", BigInteger(1, bytes))
}