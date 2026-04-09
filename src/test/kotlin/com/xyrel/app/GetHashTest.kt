package com.xyrel.app
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.io.File

class GetHashTest {
    @Test
    fun printHash() {
        val hash = BCryptPasswordEncoder().encode("password123")
        File("/home/xyreltenz/Desktop/MobileApp/PrototypeProject/app/final_hash.txt").writeText(hash)
    }
}
