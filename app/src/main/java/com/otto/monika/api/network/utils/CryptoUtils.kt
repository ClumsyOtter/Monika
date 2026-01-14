package com.otto.monika.api.network.utils

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    val randomString: String
        get() {
            val random = SecureRandom()
            return random.nextLong().toString()
        }

    fun getRandomBytes(size: Int): ByteArray {
        val random = SecureRandom()
        val bytes = ByteArray(size)
        random.nextBytes(bytes)
        return bytes
    }

    fun getRawBytes(text: String): ByteArray? {
        return text.toByteArray(StandardCharsets.UTF_8)
    }

    fun getString(data: ByteArray?): String {
        return String(data!!, StandardCharsets.UTF_8)
    }

    fun base64Decode(text: String?): ByteArray? {
        return Base64.decode(text, Base64.NO_WRAP)
    }

    fun base64Encode(data: ByteArray?): String? {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    object AES {
        const val ITERATION_COUNT_DEFAULT: Int = 100
        const val KEY_SIZE_DEFAULT: Int = 256
        const val IV_SIZE_DEFAULT: Int = 16
        const val KEY_AES_SPEC: String = "AES/CBC/PKCS7Padding"

        @JvmOverloads
        fun encrypt(
            text: String, password: String = simplePassword, salt: ByteArray? = simpleSalt,
            iv: ByteArray? = simpleIV
        ): String? {
            val data = getRawBytes(text)
            val encryptedData = encrypt(
                data, password, salt, iv,
                KEY_SIZE_DEFAULT, ITERATION_COUNT_DEFAULT
            )
            return base64Encode(encryptedData)
        }

        @JvmOverloads
        fun decrypt(
            text: String?, password: String = simplePassword, salt: ByteArray? = simpleSalt,
            iv: ByteArray? = simpleIV
        ): String {
            val encryptedData = base64Decode(text)
            val data = decrypt(
                encryptedData, password, salt, iv,
                KEY_SIZE_DEFAULT, ITERATION_COUNT_DEFAULT
            )
            return getString(data)
        }

        @JvmOverloads
        fun encrypt(
            data: ByteArray?,
            password: String = simplePassword,
            salt: ByteArray? = simpleSalt,
            iv: ByteArray? = simpleIV,
            keySize: Int = KEY_SIZE_DEFAULT,
            iterationCount: Int = ITERATION_COUNT_DEFAULT
        ): ByteArray? {
            return process(
                data, Cipher.ENCRYPT_MODE, password, salt, iv,
                keySize, iterationCount
            )
        }

        @JvmOverloads
        fun decrypt(
            data: ByteArray?,
            password: String = simplePassword,
            salt: ByteArray? = simpleSalt,
            iv: ByteArray? = simpleIV,
            keySize: Int = KEY_SIZE_DEFAULT,
            iterationCount: Int = ITERATION_COUNT_DEFAULT
        ): ByteArray? {
            return process(
                data, Cipher.DECRYPT_MODE, password, salt, iv,
                keySize, iterationCount
            )
        }

        /**
         * AES encrypt function
         *
         * @param original
         * @param key      16, 24, 32 bytes available
         * @param iv       initial vector (16 bytes) - if null: ECB mode, otherwise:
         * CBC mode
         * @return
         */
        fun encrypt(original: ByteArray?, key: ByteArray?, iv: ByteArray?): ByteArray? {
            if (key == null
                || (key.size != 16 && key.size != 24 && key.size != 32)
            ) {
                return null
            }
            if (iv != null && iv.size != 16) {
                return null
            }

            try {
                var keySpec: SecretKeySpec? = null
                var cipher: Cipher? = null
                if (iv != null) {
                    keySpec = SecretKeySpec(key, KEY_AES_SPEC)
                    cipher = Cipher.getInstance(KEY_AES_SPEC)
                    cipher.init(
                        Cipher.ENCRYPT_MODE, keySpec,
                        IvParameterSpec(iv)
                    )
                } else  // if(iv == null)
                {
                    keySpec = SecretKeySpec(key, KEY_AES_SPEC)
                    cipher = Cipher.getInstance(KEY_AES_SPEC)
                    cipher.init(Cipher.ENCRYPT_MODE, keySpec)
                }

                return cipher.doFinal(original)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        /**
         * AES decrypt function
         *
         * @param encrypted
         * @param key       16, 24, 32 bytes available
         * @param iv        initial vector (16 bytes) - if null: ECB mode, otherwise:
         * CBC mode
         * @return
         */
        fun decrypt(encrypted: ByteArray?, key: ByteArray?, iv: ByteArray?): ByteArray? {
            if (key == null
                || (key.size != 16 && key.size != 24 && key.size != 32)
            ) {
                return null
            }
            if (iv != null && iv.size != 16) {
                return null
            }

            try {
                var keySpec: SecretKeySpec?
                var cipher: Cipher?
                if (iv != null) {
                    keySpec = SecretKeySpec(key, "AES/CBC/PKCS7Padding") // AES/ECB/PKCS5Padding
                    cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
                    cipher.init(
                        Cipher.DECRYPT_MODE, keySpec,
                        IvParameterSpec(iv)
                    )
                } else  // if(iv == null)
                {
                    keySpec = SecretKeySpec(key, "AES/ECB/PKCS7Padding")
                    cipher = Cipher.getInstance("AES/ECB/PKCS7Padding")
                    cipher.init(Cipher.DECRYPT_MODE, keySpec)
                }

                return cipher.doFinal(encrypted)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        fun process(
            data: ByteArray?, mode: Int, password: String,
            salt: ByteArray?, iv: ByteArray?, keySize: Int, iterationCount: Int
        ): ByteArray? {
            val keySpec: KeySpec = PBEKeySpec(
                password.toCharArray(), salt,
                iterationCount, keySize
            )
            try {
                val keyFactory = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA1")
                val keyBytes = keyFactory.generateSecret(keySpec)
                    .getEncoded()
                val key: SecretKey = SecretKeySpec(keyBytes, "AES")
                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                val ivParams = IvParameterSpec(iv)
                cipher.init(mode, key, ivParams)
                return cipher.doFinal(data)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        val simplePassword: String
            get() = "GZ9Gn2U5nhpea8hw"

        val simpleSalt: ByteArray
            get() = "rUiey8D2GNzV69Mp".toByteArray()

        val simpleIV: ByteArray
            get() {
                val iv =
                    ByteArray(IV_SIZE_DEFAULT)
                Arrays.fill(iv, 5.toByte())
                return iv
            }
    }

    class AESCrypto {
        private var password: String? = null
        private var salt: ByteArray? = null
        private var iv: ByteArray? = null
        private var keySize = 0
        private var iterCount = 0

        constructor(password: String) {
            initialize(
                password,
                AES.simpleSalt,
                AES.simpleIV,
                KEY_SIZE_DEFAULT, ITERATION_COUNT_DEFAULT
            )
        }

        constructor(password: String, salt: ByteArray?) {
            initialize(
                password, salt,
                AES.simpleIV, KEY_SIZE_DEFAULT,
                ITERATION_COUNT_DEFAULT
            )
        }

        constructor(password: String, keySize: Int, salt: ByteArray?, iv: ByteArray?) {
            initialize(password, salt, iv, keySize, ITERATION_COUNT_DEFAULT)
        }

        private fun initialize(
            password: String, salt: ByteArray?, iv: ByteArray?,
            keySize: Int, iterCount: Int
        ) {
            this.password = password
            this.salt = salt
            this.iv = iv
            this.keySize = keySize
            this.iterCount = iterCount
        }

        fun encrypt(text: String): String? {
            val data = getRawBytes(text)
            val encryptedData = encrypt(data)
            return base64Encode(encryptedData)
        }

        fun encrypt(data: ByteArray?): ByteArray? {
            return process(data, Cipher.ENCRYPT_MODE)
        }

        fun decrypt(text: String?): String {
            val encryptedData = base64Decode(text)
            val data = decrypt(encryptedData)
            return getString(data)
        }

        fun decrypt(encryptedData: ByteArray?): ByteArray? {
            return process(encryptedData, Cipher.DECRYPT_MODE)
        }

        private fun process(data: ByteArray?, mode: Int): ByteArray? {
            return AES.process(
                data, mode, password!!, salt, iv, keySize,
                iterCount
            )
        }

        companion object {
            private const val ITERATION_COUNT_DEFAULT = 100
            private const val ITERATION_COUNT_MIN = 10
            private const val ITERATION_COUNT_MAX = 5000
            private const val KEY_SIZE_DEFAULT = 256
            private const val KEY_SIZE_MIN = 64
            private const val KEY_SIZE_MAX = 1024
            private const val IV_SIZE = 16
        }
    }

    object HEX {
        private val HEX_DIGITS = charArrayOf(
            '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        )

        private val FIRST_CHAR = CharArray(256)
        private val SECOND_CHAR = CharArray(256)

        init {
            for (i in 0..255) {
                FIRST_CHAR[i] = HEX_DIGITS[(i shr 4) and 0xF]
                SECOND_CHAR[i] = HEX_DIGITS[i and 0xF]
            }
        }

        private val DIGITS = ByteArray('f'.code + 1)

        init {
            run {
                var i = 0
                while (i <= 'F'.code) {
                    HEX.DIGITS[i] = -1
                    i++
                }
            }
            for (i in 0..9) {
                DIGITS['0'.code.toByte() + i] = i.toByte()
            }
            for (i in 0..5) {
                DIGITS['A'.code.toByte() + i] = (10 + i).toByte()
                DIGITS['a'.code.toByte() + i] = (10 + i).toByte()
            }
        }

        /**
         * Quickly converts a byte array to a hexadecimal string representation.
         *
         * @param array byte array, possibly zero-terminated.
         */
        fun encodeHex(array: ByteArray, zeroTerminated: Boolean): String {
            val cArray = CharArray(array.size * 2)

            var j = 0
            for (i in array.indices) {
                val index = array[i].toInt() and 0xFF
                if (index == 0 && zeroTerminated) {
                    break
                }

                cArray[j++] = FIRST_CHAR[index]
                cArray[j++] = SECOND_CHAR[index]
            }

            return String(cArray, 0, j)
        }

        /**
         * Quickly converts a hexadecimal string to a byte array.
         */
        fun decodeHex(hexString: String): ByteArray {
            val length = hexString.length

            require((length and 0x01) == 0) { "Odd number of characters." }

            var badHex = false
            val out = ByteArray(length shr 1)
            var i = 0
            var j = 0
            while (j < length) {
                val c1 = hexString.get(j++).code
                if (c1 > 'f'.code) {
                    badHex = true
                    break
                }

                val d1: Byte = DIGITS[c1]
                if (d1.toInt() == -1) {
                    badHex = true
                    break
                }

                val c2 = hexString.get(j++).code
                if (c2 > 'f'.code) {
                    badHex = true
                    break
                }

                val d2: Byte = DIGITS[c2]
                if (d2.toInt() == -1) {
                    badHex = true
                    break
                }

                out[i] = (d1.toInt() shl 4 or d2.toInt()).toByte()
                i++
            }

            require(!badHex) { "Invalid hexadecimal digit: " + hexString }

            return out
        }
    }

    object HASH {
        private const val MD5 = "MD5"
        private const val SHA_1 = "SHA-1"
        private const val SHA_256 = "SHA-256"
        private val DIGITS_LOWER = charArrayOf(
            '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
        )
        private val DIGITS_UPPER = charArrayOf(
            '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
        )

        fun md5(data: ByteArray): String {
            return String(encodeHex(md5Bytes(data)))
        }

        fun md5(text: String): String {
            return String(encodeHex(md5Bytes(getRawBytes(text)!!)))
        }


        fun md5Bytes(data: ByteArray): ByteArray {
            return getDigest(MD5).digest(data)
        }

        fun sha1(data: ByteArray): String {
            return String(encodeHex(sha1Bytes(data)))
        }

        fun sha1(text: String): String {
            return String(encodeHex(sha1Bytes(getRawBytes(text)!!)))
        }

        fun sha1Bytes(data: ByteArray): ByteArray {
            return getDigest(SHA_1).digest(data)
        }

        fun sha256(data: ByteArray): String {
            return String(encodeHex(sha256Bytes(data)))
        }

        fun sha256(text: String): String {
            return String(encodeHex(sha256Bytes(getRawBytes(text)!!)))
        }

        fun sha256Bytes(data: ByteArray): ByteArray {
            return getDigest(SHA_256).digest(data)
        }

        private fun getDigest(algorithm: String): MessageDigest {
            try {
                return MessageDigest.getInstance(algorithm)
            } catch (e: NoSuchAlgorithmException) {
                throw IllegalArgumentException(e)
            }
        }

        private fun encodeHex(data: ByteArray, toLowerCase: Boolean = true): CharArray {
            return encodeHex(data, if (toLowerCase) DIGITS_LOWER else DIGITS_UPPER)
        }

        private fun encodeHex(data: ByteArray, toDigits: CharArray): CharArray {
            val l = data.size
            val out = CharArray(l shl 1)
            var i = 0
            var j = 0
            while (i < l) {
                out[j++] = toDigits[(0xF0 and data[i].toInt()) ushr 4]
                out[j++] = toDigits[0x0F and data[i].toInt()]
                i++
            }
            return out
        }
    }
}