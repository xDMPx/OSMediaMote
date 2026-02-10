package com.xdmpx.osmediamote.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream


@Serializable
data class Settings(val useDynamicColor: Boolean = true)

object SettingsSerializer : Serializer<Settings> {

    override val defaultValue: Settings = Settings()

    override suspend fun readFrom(input: InputStream): Settings = try {
        Json.decodeFromString<Settings>(
            input.readBytes().decodeToString()
        )
    } catch (serialization: SerializationException) {
        throw CorruptionException("Unable to read Settings", serialization)
    }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
