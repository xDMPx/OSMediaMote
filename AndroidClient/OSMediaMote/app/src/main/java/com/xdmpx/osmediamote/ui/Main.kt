package com.xdmpx.osmediamote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
        modifier: Modifier = Modifier
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {
            TextField(
                value = ipText,
                onValueChange = { osMediaMoteViewModel.setIpText(it) },
                label = { Text(stringResource(R.string.ip)) })
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                {
                    onClick(ipText)
                }, modifier = Modifier.fillMaxWidth(0.5f)
            ) { Text(stringResource(R.string.confirm)) }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopAppBar(
        onAboutClick: () -> Unit
    ) {
        androidx.compose.material3.TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
            title = { Text(stringResource(R.string.app_name)) },
            actions = { TopAppBarMenu { onAboutClick() } })
    }

    @Composable
    fun TopAppBarMenu(
        onNavigateToAbout: () -> Unit,
    ) {
        var expanded by remember { mutableStateOf(false) }

        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Filled.Menu, contentDescription = "Menu"
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(text = "About") }, onClick = {
                expanded = false
                onNavigateToAbout()
            })
        }

    }

}