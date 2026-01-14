package com.otto.monika.common.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.otto.monika.api.model.user.Token
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import kotlin.io.readBytes
import kotlin.text.decodeToString
import kotlin.text.encodeToByteArray


val Context.TokenDataStore: DataStore<Token> by dataStore(
    fileName = "userInfo.json",
    serializer = TokenSerializer
)


object TokenSerializer : Serializer<Token> {

    override val defaultValue: Token = Token()

    override suspend fun readFrom(input: InputStream): Token =
        try {
            Json.decodeFromString<Token>(input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read userInfo", serialization)
        }

    override suspend fun writeTo(t: Token, output: OutputStream) {
        output.write(Json.encodeToString(t).encodeToByteArray())
    }
}