package com.xdmpx.osmediamote.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

abstract class Settings {
    companion object {
        @Volatile
        private var INSTANCE: SettingsViewModel? = null

        fun getInstance(): SettingsViewModel {
            synchronized(this) {
                return INSTANCE ?: SettingsViewModel(
                ).also {
                    INSTANCE = it
                }
            }
        }
    }
}

object SettingsSerializer : Serializer<SettingsState> {

    override val defaultValue: SettingsState = SettingsState()

    override suspend fun readFrom(input: InputStream): SettingsState = try {
        Json.decodeFromString<SettingsState>(
            input.readBytes().decodeToString()
        )
    } catch (serialization: SerializationException) {
        throw CorruptionException("Unable to read Settings", serialization)
    }

    override suspend fun writeTo(t: SettingsState, output: OutputStream) {
        output.write(
            Json.encodeToString(t).encodeToByteArray()
        )
    }
}
