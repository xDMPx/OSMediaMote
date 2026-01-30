package com.xdmpx.osmediamote.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.coroutineScope
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
    ) {
        androidx.compose.material3.TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), title = { Text(stringResource(R.string.app_name)) }, actions = {})
    }

}