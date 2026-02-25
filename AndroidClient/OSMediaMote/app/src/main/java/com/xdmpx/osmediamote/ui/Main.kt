package com.xdmpx.osmediamote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.xdmpx.osmediamote.OSMediaMote
import com.xdmpx.osmediamote.R

object Main {

    @Composable
    fun IpInputScreen(
        ipText: String,
        osMediaMoteViewModel: OSMediaMote,
        onClick: (ipText: String) -> Unit,
        enabled: Boolean,
        modifier: Modifier = Modifier
    ) {
        val focusManager = LocalFocusManager.current

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            TextField(
                value = ipText,
                onValueChange = { osMediaMoteViewModel.setIpText(it) },
                label = { Text(stringResource(R.string.ip)) },
                enabled = enabled,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                {
                    focusManager.clearFocus()
                    onClick(ipText)
                }, enabled = enabled, modifier = Modifier.fillMaxWidth(0.5f)
            ) { Text(stringResource(R.string.confirm)) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBar(
        onAboutClick: () -> Unit, onNavigateToSettings: () -> Unit
    ) {
        androidx.compose.material3.TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
            title = { Text(stringResource(R.string.app_name)) },
            actions = { TopAppBarMenu(onAboutClick, onNavigateToSettings) })
    }

    @Composable
    fun TopAppBarMenu(
        onNavigateToAbout: () -> Unit, onNavigateToSettings: () -> Unit
    ) {
        var expanded by remember { mutableStateOf(false) }

        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                painterResource(R.drawable.sharp_menu_24),
                contentDescription = stringResource(R.string.menu)
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.settings_screen)) },
                onClick = {
                    expanded = false
                    onNavigateToSettings()
                })
            DropdownMenuItem(
                text = { Text(text = stringResource(id = R.string.about_screen)) },
                onClick = {
                    expanded = false
                    onNavigateToAbout()
                })
        }

    }

}