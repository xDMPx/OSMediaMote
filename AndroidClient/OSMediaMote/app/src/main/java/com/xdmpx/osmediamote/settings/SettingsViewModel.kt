package com.xdmpx.osmediamote.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

val Context.settingsDataStore: DataStore<SettingsState> by dataStore(
    fileName = "settings.json",
    serializer = SettingsSerializer,
)

@Serializable
data class SettingsState(
    val usePureDark: Boolean = false,
    val useDynamicColor: Boolean = true,
)

class SettingsViewModel : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    fun toggleUsePureDark() {
        _settingsState.value.let {
            _settingsState.value = it.copy(usePureDark = !it.usePureDark)
        }
    }

    fun toggleUseDynamicColor() {
        _settingsState.value.let {
            _settingsState.value = it.copy(useDynamicColor = !it.useDynamicColor)
        }
    }

    suspend fun loadSettings(context: Context) {
        val settingsData = context.settingsDataStore.data.first()
        _settingsState.value.let {
            _settingsState.value = it.copy(
                usePureDark = settingsData.usePureDark,
                useDynamicColor = settingsData.useDynamicColor,
            )
        }
    }

    suspend fun saveSettings(context: Context) {
        context.settingsDataStore.updateData {
            it.copy(
                usePureDark = this@SettingsViewModel._settingsState.value.usePureDark,
                useDynamicColor = this@SettingsViewModel._settingsState.value.useDynamicColor
            )
        }
    }

}
