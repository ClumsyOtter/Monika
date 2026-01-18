package com.otto.network.network.utils

import android.content.res.AssetManager
import android.text.TextUtils
import android.util.Base64
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import kotlin.concurrent.Volatile
import kotlin.math.min

/**
 * RSA加密解密工具里
 * 已处理线程安全问题
 * 建议在线程中使用
 */
class RSAHelper {
    private var assetManager: AssetManager? = null

    /*公钥*/
    @Volatile
    private var publicKey: PublicKey? = null
        get() {
            if (field == null && !isInited) {
                synchronized(keyLock) {
                    if (field == null && !isInited) {
                        isInited = true
                        var inputStream: InputStream? = null
                        try {
                            if (!TextUtils.isEmpty(publicKeyAssetName) && assetManager != null) {
                                inputStream = assetManager!!.open(publicKeyAssetName!!)
                            } else if (!TextUtils.isEmpty(publicKeyFilePath)) {
                                inputStream = FileInputStream(publicKeyFilePath)
                            }
                            if (inputStream != null) {
                                field = loadPublicKey(inputStream)
                            }
                        } catch (_: Exception) {
                        } finally {
                            try {
                                inputStream?.close()
                            } catch (_: IOException) {
                            }
                        }
                    }
                }
            }
            return field
        }
    private var publicKeyFilePath: String? = null
    private var publicKeyAssetName: String? = null
    private val keyLock = Any()

    @Volatile
    private var isInited = false

    /**
     * RSA加密辅助类的构造方法
     * 从assets中读取公钥
     *
     * @param assetManager  [AssetManager]
     * @param assetFileName asset文件中公钥文件的文件名
     */
    constructor(assetManager: AssetManager?, assetFileName: String) {
        this.assetManager = assetManager
        publicKeyAssetName = assetFileName
    }

    /**
     * RSA加密辅助类的构造方法
     * 从普通文件中读取公钥
     *
     * @param publicKeyFilePath 公钥文件的path
     */
    constructor(publicKeyFilePath: String?) {
        this.publicKeyFilePath = publicKeyFilePath
    }

    /**
     * 加密数据
     *
     * @param str 待加密数据
     * @return 加密后数据, 如果加密过程出错或者没有公钥, 则会返回原来的值
     */
    @Throws(GeneralSecurityException::class)
    fun encrypt(str: String): String {
        val data = str.toByteArray(StandardCharsets.UTF_8)
        val length = data.size
        if (length <= ENCRYPT_BLOCK_SIZE) {
            return Base64.encodeToString(encryptInternal(data), Base64.DEFAULT)
        }
        val sb = StringBuilder()
        var index = 0
        var buffer = ByteArray(ENCRYPT_BLOCK_SIZE)
        var loop = 0
        for (i in 0..<length) {
            buffer[index] = data[i]
            if (++index == ENCRYPT_BLOCK_SIZE || i == length - 1) {
                loop++
                if (loop != 1) {
                    sb.append(SPLIT)
                }
                sb.append(Base64.encodeToString(encryptInternal(buffer), Base64.DEFAULT))
                index = 0
                if (i != length - 1) {
                    buffer = ByteArray(min(ENCRYPT_BLOCK_SIZE, (length - i - 1)))
                }
            }
        }
        return sb.toString()
    }

    @Throws(GeneralSecurityException::class)
    private fun encryptInternal(data: ByteArray?): ByteArray? {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey)
        return cipher.doFinal(data)
    }

    /**
     * 加载公钥
     */
    @Throws(IOException::class, NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    private fun loadPublicKey(publicKeyStream: InputStream?): PublicKey? {
        val keyStr = readKey(publicKeyStream)
        val factory = KeyFactory.getInstance("RSA")
        val keySpec: KeySpec = X509EncodedKeySpec(Base64.decode(keyStr, Base64.DEFAULT))
        return factory.generatePublic(keySpec)
    }

    /**
     * 从流中读取公钥
     *
     * @param inputStream 输入流
     * @return 公钥的String
     */
    @Throws(IOException::class)
    private fun readKey(inputStream: InputStream?): String {
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val sb = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            // 排除-开头的行, 比如----PUBLIC KEY-----这样的
            if (line[0] != '-') {
                sb.append(line)
                    .append('\r')
            }
            line = bufferedReader.readLine()
        }
        return sb.toString()
    }

    companion object {
        private const val ALGORITHM = "RSA/ECB/PKCS1Padding"

        /*加密块的大小, 与服务端约定*/
        private const val ENCRYPT_BLOCK_SIZE = 245

        /*加密块之间的分隔符*/
        private const val SPLIT = '|'
    }
}