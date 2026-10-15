package com.calendariolaboral.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calendariolaboral.app.R
import com.calendariolaboral.core.Region

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionPicker(selected: Region?, onPick: (Region) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.choose_region)) }) }) { padding ->
        val regions = Region.entries.map { it to regionName(it) }.sortedBy { it.second }
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
            item {
                Text(
                    stringResource(R.string.choose_region_text),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            items(regions, key = { it.first.name }) { (region, name) ->
                ListItem(
                    headlineContent = { Text(name) },
                    leadingContent = { RadioButton(selected = region == selected, onClick = null) },
                    modifier = Modifier.clickable { onPick(region) },
                )
            }
        }
    }
}
