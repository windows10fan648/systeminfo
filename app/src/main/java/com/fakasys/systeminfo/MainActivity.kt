@file:OptIn(ExperimentalMaterial3Api::class)

package com.fakasys.systeminfo

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.Color
import android.hardware.Sensor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.Display
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt


// ============================================================
// DATASTORE
// ============================================================

private val Context.dataStore by preferencesDataStore(
    name = "system_info_settings"
)

private val THEME_MODE_KEY =
    stringPreferencesKey("theme_mode")

private val ACCENT_COLOR_KEY =
    stringPreferencesKey("accent_color")

private val ADVANCED_INFO_KEY =
    booleanPreferencesKey("advanced_info")

private val BINARY_UNITS_KEY =
    booleanPreferencesKey("binary_units")

private val REFRESH_INTERVAL_KEY =
    longPreferencesKey("refresh_interval")


// ============================================================
// MAIN ACTIVITY
// ============================================================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SystemInfoApp()
        }
    }
}


// ============================================================
// APP
// ============================================================

@Composable
fun SystemInfoApp() {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var settingsLoaded by remember {
        mutableStateOf(false)
    }

    var themeMode by remember {
        mutableStateOf("System")
    }

    var accentColor by remember {
        mutableStateOf("Default")
    }

    var advancedInfo by remember {
        mutableStateOf(false)
    }

    var binaryUnits by remember {
        mutableStateOf(false)
    }

    var refreshInterval by remember {
        mutableStateOf(5_000L)
    }

    LaunchedEffect(Unit) {

        val preferences =
            context.dataStore.data.first()

        themeMode =
            preferences[THEME_MODE_KEY] ?: "System"

        accentColor =
            preferences[ACCENT_COLOR_KEY] ?: "Default"

        advancedInfo =
            preferences[ADVANCED_INFO_KEY] ?: false

        binaryUnits =
            preferences[BINARY_UNITS_KEY] ?: false

        refreshInterval =
            preferences[REFRESH_INTERVAL_KEY] ?: 5_000L

        settingsLoaded = true
    }

    if (!settingsLoaded) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading settings...")
        }

        return
    }

    fun saveTheme(value: String) {
        themeMode = value

        scope.launch {
            context.dataStore.edit {
                it[THEME_MODE_KEY] = value
            }
        }
    }

    fun saveAccent(value: String) {
        accentColor = value

        scope.launch {
            context.dataStore.edit {
                it[ACCENT_COLOR_KEY] = value
            }
        }
    }

    fun saveAdvanced(value: Boolean) {
        advancedInfo = value

        scope.launch {
            context.dataStore.edit {
                it[ADVANCED_INFO_KEY] = value
            }
        }
    }

    fun saveBinary(value: Boolean) {
        binaryUnits = value

        scope.launch {
            context.dataStore.edit {
                it[BINARY_UNITS_KEY] = value
            }
        }
    }

    fun saveRefresh(value: Long) {
        refreshInterval = value

        scope.launch {
            context.dataStore.edit {
                it[REFRESH_INTERVAL_KEY] = value
            }
        }
    }

    val systemDark =
        androidx.compose.foundation.isSystemInDarkTheme()

    val useDarkTheme =
        when (themeMode) {
            "Dark" -> true
            "Light" -> false
            else -> systemDark
        }

    MaterialTheme(
        colorScheme = createColorScheme(
            accentColor,
            useDarkTheme
        )
    ) {

        AppNavigator(
            themeMode = themeMode,
            accentColor = accentColor,
            advancedInfo = advancedInfo,
            binaryUnits = binaryUnits,
            refreshInterval = refreshInterval,

            onThemeChange = ::saveTheme,
            onAccentChange = ::saveAccent,
            onAdvancedChange = ::saveAdvanced,
            onBinaryChange = ::saveBinary,
            onRefreshChange = ::saveRefresh
        )
    }
}


// ============================================================
// NAVIGATION
// ============================================================

@Composable
fun AppNavigator(
    themeMode: String,
    accentColor: String,
    advancedInfo: Boolean,
    binaryUnits: Boolean,
    refreshInterval: Long,

    onThemeChange: (String) -> Unit,
    onAccentChange: (String) -> Unit,
    onAdvancedChange: (Boolean) -> Unit,
    onBinaryChange: (Boolean) -> Unit,
    onRefreshChange: (Long) -> Unit
) {

    var screen by remember {
        mutableStateOf("main")
    }

    when (screen) {

        "main" -> {

            SystemInfoScreen(
                advancedInfo = advancedInfo,
                binaryUnits = binaryUnits,
                refreshInterval = refreshInterval,

                onSettings = {
                    screen = "settings"
                },

                onAbout = {
                    screen = "about"
                },

                onReport = {
                    screen = "report"
                }
            )
        }

        "settings" -> {

            SettingsScreen(
                themeMode = themeMode,
                accentColor = accentColor,
                advancedInfo = advancedInfo,
                binaryUnits = binaryUnits,
                refreshInterval = refreshInterval,

                onBack = {
                    screen = "main"
                },

                onThemeChange = onThemeChange,
                onAccentChange = onAccentChange,
                onAdvancedChange = onAdvancedChange,
                onBinaryChange = onBinaryChange,
                onRefreshChange = onRefreshChange,

                onAbout = {
                    screen = "about"
                }
            )
        }

        "about" -> {

            AboutScreen(
                onBack = {
                    screen = "settings"
                }
            )
        }

        "report" -> {

            SystemReportScreen(
                advancedInfo = advancedInfo,
                binaryUnits = binaryUnits,

                onBack = {
                    screen = "main"
                }
            )
        }
    }
}


// ============================================================
// MAIN SCREEN
// ============================================================

@Composable
fun SystemInfoScreen(
    advancedInfo: Boolean,
    binaryUnits: Boolean,
    refreshInterval: Long,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onReport: () -> Unit
) {

    var refreshCounter by remember {
        mutableIntStateOf(0)
    }

    var searchMode by remember {
        mutableStateOf(false)
    }

    var searchText by remember {
        mutableStateOf("")
    }

    LaunchedEffect(refreshInterval) {

        while (true) {

            delay(refreshInterval)

            refreshCounter++
        }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    if (searchMode) {

                        BasicTextField(
                            value = searchText,
                            onValueChange = {
                                searchText = it
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                    } else {

                        Text(
                            "System Info",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            searchMode = !searchMode

                            if (!searchMode) {
                                searchText = ""
                            }
                        }
                    ) {

                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    IconButton(
                        onClick = {
                            refreshCounter++
                        }
                    ) {

                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }

                    IconButton(
                        onClick = onReport
                    ) {

                        Icon(
                            Icons.Default.Share,
                            contentDescription = "System report"
                        )
                    }

                    IconButton(
                        onClick = onSettings
                    ) {

                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }

    ) { padding ->

        SystemInfoContent(
            modifier = Modifier.padding(padding),
            refreshCounter = refreshCounter,
            advancedInfo = advancedInfo,
            binaryUnits = binaryUnits,
            searchText = searchText
        )
    }
}


// ============================================================
// SYSTEM INFORMATION CONTENT
// ============================================================

@Composable
fun SystemInfoContent(
    modifier: Modifier,
    refreshCounter: Int,
    advancedInfo: Boolean,
    binaryUnits: Boolean,
    searchText: String
) {

    val context = LocalContext.current

    val data =
        remember(
            refreshCounter,
            advancedInfo,
            binaryUnits
        ) {

            collectSystemInfo(
                context = context,
                binaryUnits = binaryUnits
            )
        }

    if (searchText.isNotBlank()) {

        SearchResults(
            data = data,
            searchText = searchText,
            modifier = modifier
        )

        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {

            InfoSection(
                title = "Android",
                icon = Icons.Default.PhoneAndroid,
                lines = data.androidInfo
            )
        }

        item {

            InfoSection(
                title = "Device",
                icon = Icons.Default.Info,
                lines = data.deviceInfo
            )
        }

        item {

            InfoSection(
                title = "CPU",
                icon = Icons.Default.Memory,
                lines = data.cpuInfo
            )
        }

        item {

            InfoSection(
                title = "Memory",
                icon = Icons.Default.Memory,
                lines = data.memoryInfo
            )
        }

        item {

            InfoSection(
                title = "Storage",
                icon = Icons.Default.Storage,
                lines = data.storageInfo
            )
        }

        item {

            InfoSection(
                title = "Battery",
                icon = Icons.Default.BatteryFull,
                lines = data.batteryInfo
            )
        }

        item {

            InfoSection(
                title = "Display",
                icon = Icons.Default.DisplaySettings,
                lines = data.displayInfo
            )
        }

        item {

            InfoSection(
                title = "Thermal",
                icon = Icons.Default.Thermostat,
                lines = data.thermalInfo
            )
        }

        item {

            InfoSection(
                title = "Network",
                icon = Icons.Default.NetworkCheck,
                lines = data.networkInfo
            )
        }

        item {

            InfoSection(
                title = "Sensors",
                icon = Icons.Default.Sensors,
                lines = data.sensorInfo
            )
        }

        item {

            InfoSection(
                title = "App",
                icon = Icons.Default.Build,
                lines = data.appInfo
            )
        }

        if (advancedInfo) {

            item {

                InfoSection(
                    title = "Build",
                    icon = Icons.Default.Build,
                    lines = data.buildInfo
                )
            }
        }
    }
}


// ============================================================
// SEARCH
// ============================================================

@Composable
fun SearchResults(
    data: SystemInfoData,
    searchText: String,
    modifier: Modifier
) {

    val results =
        data.searchableLines.filter {

            it.contains(
                searchText,
                ignoreCase = true
            )
        }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {

            Text(
                text = "Search results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (results.isEmpty()) {

            item {

                Text(
                    "No results found."
                )
            }

        } else {

            items(results) { result ->

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


// ============================================================
// REPORT SCREEN
// ============================================================

@Composable
fun SystemReportScreen(
    advancedInfo: Boolean,
    binaryUnits: Boolean,
    onBack: () -> Unit
) {

    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var refreshCounter by remember {
        mutableIntStateOf(0)
    }

    val data =
        remember(
            refreshCounter,
            advancedInfo,
            binaryUnits
        ) {

            collectSystemInfo(
                context = context,
                binaryUnits = binaryUnits
            )
        }

    val report =
        remember(
            data,
            advancedInfo
        ) {

            buildSystemReport(
                data = data,
                advancedInfo = advancedInfo
            )
        }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("System Report")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            @Suppress("DEPRECATION")
                            clipboard.setText(
                                AnnotatedString(report)
                            )
                        }
                    ) {

                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy"
                        )
                    }

                    IconButton(
                        onClick = {
                            shareText(
                                context,
                                report
                            )
                        }
                    ) {

                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }

                    IconButton(
                        onClick = {
                            refreshCounter++
                        }
                    ) {

                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
        ) {

            item {

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Text(
                        text = report,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}


// ============================================================
// SETTINGS
// ============================================================

@Composable
fun SettingsScreen(
    themeMode: String,
    accentColor: String,
    advancedInfo: Boolean,
    binaryUnits: Boolean,
    refreshInterval: Long,

    onBack: () -> Unit,
    onThemeChange: (String) -> Unit,
    onAccentChange: (String) -> Unit,
    onAdvancedChange: (Boolean) -> Unit,
    onBinaryChange: (Boolean) -> Unit,
    onRefreshChange: (Long) -> Unit,
    onAbout: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Settings")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    "Appearance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                SettingsButton(
                    title = "Theme",
                    value = themeMode,
                    onClick = {

                        val next =
                            when (themeMode) {
                                "System" -> "Light"
                                "Light" -> "Dark"
                                else -> "System"
                            }

                        onThemeChange(next)
                    }
                )
            }

            item {

                SettingsButton(
                    title = "Theme Color",
                    value = accentColor,
                    onClick = {

                        val next =
                            when (accentColor) {
                                "Default" -> "Blue"
                                "Blue" -> "Green"
                                "Green" -> "Purple"
                                "Purple" -> "Orange"
                                else -> "Default"
                            }

                        onAccentChange(next)
                    }
                )
            }

            item {

                HorizontalDivider()
            }

            item {

                Text(
                    "Information",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                SettingsSwitch(
                    title = "Advanced Information",
                    checked = advancedInfo,
                    onCheckedChange = onAdvancedChange
                )
            }

            item {

                SettingsSwitch(
                    title = "Binary Storage Units",
                    checked = binaryUnits,
                    onCheckedChange = onBinaryChange
                )
            }

            item {

                HorizontalDivider()
            }

            item {

                Text(
                    "Refresh Interval",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Column {

                    Text(
                        formatRefreshInterval(
                            refreshInterval
                        )
                    )

                    Slider(
                        value =
                            refreshInterval.toFloat(),

                        onValueChange = {

                            val value =
                                when {
                                    it < 3_000f -> 1_000L
                                    it < 7_500f -> 5_000L
                                    it < 20_000f -> 10_000L
                                    it < 45_000f -> 30_000L
                                    else -> 60_000L
                                }

                            onRefreshChange(value)
                        },

                        valueRange =
                            1_000f..60_000f,

                        steps = 4
                    )
                }
            }

            item {

                HorizontalDivider()
            }

            item {

                Button(
                    onClick = onAbout,
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Icon(
                        Icons.Default.Info,
                        contentDescription = null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text("About")
                }
            }
        }
    }
}


// ============================================================
// SETTINGS COMPONENTS
// ============================================================

@Composable
fun SettingsButton(
    title: String,
    value: String,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(title)

            Text(
                value,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(title)

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}


// ============================================================
// ABOUT
// ============================================================

@Composable
fun AboutScreen(
    onBack: () -> Unit
) {

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("About")
                },

                navigationIcon = {

                    IconButton(
                        onClick = onBack
                    ) {

                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )

            Spacer(
                Modifier.height(16.dp)
            )

            Text(
                "System Info",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(
                "System information and diagnostics"
            )

            Spacer(
                Modifier.height(24.dp)
            )

            Text(
                "Version 1.0"
            )

            Spacer(
                Modifier.height(8.dp)
            )

            Text(
                "com.fakasys.systeminfo"
            )
        }
    }
}


// ============================================================
// INFO SECTION
// ============================================================

@Composable
fun InfoSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    lines: List<String>
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    icon,
                    contentDescription = null
                )

                Spacer(
                    Modifier.width(10.dp)
                )

                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(
                Modifier.height(10.dp)
            )

            if (title == "Sensors") {
                var expandedCategory by remember { mutableStateOf<String?>(null) }
                
                // Show total line first
                val totalLine = lines.firstOrNull()
                if (totalLine != null) {
                    Text(totalLine, modifier = Modifier.padding(vertical = 4.dp), fontWeight = FontWeight.SemiBold)
                }
                
                // Process lines into headers and children
                val categoriesMap = remember(lines) {
                    val map = mutableMapOf<String, List<String>>()
                    var currentHeader: String? = null
                    var currentList = mutableListOf<String>()
                    lines.drop(1).forEach { line ->
                        if (line.startsWith("[Category]")) {
                            val header = currentHeader
                            if (header != null) {
                                map[header] = currentList
                            }
                            currentHeader = line.removePrefix("[Category] ").trim()
                            currentList = mutableListOf()
                        } else {
                            currentList.add(line)
                        }
                    }
                    val finalHeader = currentHeader
                    if (finalHeader != null) {
                        map[finalHeader] = currentList
                    }
                    map
                }

                categoriesMap.forEach { (category, items) ->
                    val isExpanded = expandedCategory == category
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedCategory = if (isExpanded) null else category }
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$category (${items.size})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (isExpanded) {
                            Column(modifier = Modifier.padding(start = 12.dp, top = 2.dp, bottom = 6.dp)) {
                                items.forEach { item ->
                                    Text(
                                        text = item,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                lines.forEach { line ->
                    Text(
                        line,
                        modifier = Modifier.padding(
                            vertical = 2.dp
                        )
                    )
                }
            }
        }
    }
}


// ============================================================
// DATA CLASS
// ============================================================

data class SystemInfoData(

    val androidInfo: List<String>,
    val deviceInfo: List<String>,
    val cpuInfo: List<String>,
    val memoryInfo: List<String>,
    val storageInfo: List<String>,
    val batteryInfo: List<String>,
    val displayInfo: List<String>,
    val thermalInfo: List<String>,
    val networkInfo: List<String>,
    val sensorInfo: List<String>,
    val appInfo: List<String>,
    val buildInfo: List<String>
) {

    val searchableLines: List<String>
        get() =
            androidInfo +
                    deviceInfo +
                    cpuInfo +
                    memoryInfo +
                    storageInfo +
                    batteryInfo +
                    displayInfo +
                    thermalInfo +
                    networkInfo +
                    sensorInfo +
                    appInfo +
                    buildInfo
}


// ============================================================
// COLLECT SYSTEM INFORMATION
// ============================================================

fun collectSystemInfo(
    context: Context,
    binaryUnits: Boolean
): SystemInfoData {

    // --------------------------------------------------------
    // Android
    // --------------------------------------------------------

    val androidInfo =
        listOf(
            "Android version: ${Build.VERSION.RELEASE}",
            "API level: ${Build.VERSION.SDK_INT}",
            "Security patch: ${
                if (Build.VERSION.SDK_INT >= 23)
                    Build.VERSION.SECURITY_PATCH
                else
                    "Unknown"
            }"
        )


    // --------------------------------------------------------
    // Device
    // --------------------------------------------------------

    val deviceInfo =
        listOf(
            "Manufacturer: ${Build.MANUFACTURER}",
            "Brand: ${Build.BRAND}",
            "Model: ${Build.MODEL}",
            "Device: ${Build.DEVICE}",
            "Product: ${Build.PRODUCT}",
            "Hardware: ${Build.HARDWARE}"
        )


    // --------------------------------------------------------
    // CPU
    // --------------------------------------------------------

    val cpuInfo =
        listOf(
            "Architecture: ${Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"}",
            "Supported ABIs: ${
                Build.SUPPORTED_ABIS.joinToString()
            }",
            "CPU cores: ${
                Runtime.getRuntime().availableProcessors()
            }",
            "CPU: ${
                getCpuName()
            }"
        )


    // --------------------------------------------------------
    // MEMORY
    // --------------------------------------------------------

    val activityManager =
        context.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as android.app.ActivityManager

    val memoryInfoObject =
        android.app.ActivityManager.MemoryInfo()

    activityManager.getMemoryInfo(
        memoryInfoObject
    )

    val totalRam =
        memoryInfoObject.totalMem

    val availableRam =
        memoryInfoObject.availMem

    val usedRam =
        totalRam - availableRam

    val ramPercentage =
        if (totalRam > 0) {

            usedRam.toDouble() /
                    totalRam.toDouble() *
                    100.0

        } else {
            0.0
        }

    val memoryInfo =
        listOf(
            "Total RAM: ${
                formatStorage(
                    totalRam,
                    binaryUnits
                )
            }",
            "Used RAM: ${
                formatStorage(
                    usedRam,
                    binaryUnits
                )
            }",
            "Available RAM: ${
                formatStorage(
                    availableRam,
                    binaryUnits
                )
            }",
            "RAM usage: ${
                String.format(
                    Locale.US,
                    "%.1f%%",
                    ramPercentage
                )
            }"
        )


    // --------------------------------------------------------
    // STORAGE
    // --------------------------------------------------------

    val statFs =
        StatFs(
            Environment.getDataDirectory().path
        )

    val blockSize =
        statFs.blockSizeLong

    val totalBlocks =
        statFs.blockCountLong

    val availableBlocks =
        statFs.availableBlocksLong

    val totalStorage =
        totalBlocks * blockSize

    val freeStorage =
        availableBlocks * blockSize

    val usedStorage =
        totalStorage - freeStorage

    val storagePercentage =
        if (totalStorage > 0) {

            usedStorage.toDouble() /
                    totalStorage.toDouble() *
                    100.0

        } else {
            0.0
        }

    val storageInfo =
        listOf(
            "Total: ${
                formatStorage(
                    totalStorage,
                    binaryUnits
                )
            }",
            "Used: ${
                formatStorage(
                    usedStorage,
                    binaryUnits
                )
            }",
            "Free: ${
                formatStorage(
                    freeStorage,
                    binaryUnits
                )
            }",
            "Usage: ${
                String.format(
                    Locale.US,
                    "%.1f%%",
                    storagePercentage
                )
            }"
        )


    // --------------------------------------------------------
    // BATTERY
    // --------------------------------------------------------

    val batteryIntent =
        context.registerReceiver(
            null,
            android.content.IntentFilter(
                android.content.Intent.ACTION_BATTERY_CHANGED
            )
        )

    val batteryLevel =
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_LEVEL,
            -1
        ) ?: -1

    val batteryScale =
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_SCALE,
            -1
        ) ?: -1

    val batteryPercent =
        if (
            batteryLevel >= 0 &&
            batteryScale > 0
        ) {

            batteryLevel * 100 /
                    batteryScale

        } else {
            -1
        }

    val batteryStatus =
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_STATUS,
            -1
        ) ?: -1

    val charging =
        when (batteryStatus) {

            BatteryManager.BATTERY_STATUS_CHARGING ->
                "Charging"

            BatteryManager.BATTERY_STATUS_FULL ->
                "Full"

            BatteryManager.BATTERY_STATUS_DISCHARGING ->
                "Discharging"

            BatteryManager.BATTERY_STATUS_NOT_CHARGING ->
                "Not charging"

            else ->
                "Unknown"
        }

    val batteryHealth =
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_HEALTH,
            -1
        ) ?: -1

    val health =
        when (batteryHealth) {

            BatteryManager.BATTERY_HEALTH_GOOD ->
                "Good"

            BatteryManager.BATTERY_HEALTH_OVERHEAT ->
                "Overheat"

            BatteryManager.BATTERY_HEALTH_DEAD ->
                "Dead"

            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ->
                "Over voltage"

            BatteryManager.BATTERY_HEALTH_COLD ->
                "Cold"

            else ->
                "Unknown"
        }

    val temperature =
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_TEMPERATURE,
            -1
        ) ?: -1

    val batteryTemperature =
        if (temperature >= 0) {

            String.format(
                Locale.US,
                "%.1f °C",
                temperature / 10.0
            )

        } else {
            "Unknown"
        }

    val batteryVoltage =
        batteryIntent?.getIntExtra(
            BatteryManager.EXTRA_VOLTAGE,
            -1
        ) ?: -1

    val batteryVoltageText =
        if (batteryVoltage > 0) {

            "$batteryVoltage mV"

        } else {
            "Unknown"
        }

    val batteryInfo =
        listOf(
            "Level: ${
                if (batteryPercent >= 0)
                    "$batteryPercent%"
                else
                    "Unknown"
            }",
            "Status: $charging",
            "Health: $health",
            "Temperature: $batteryTemperature",
            "Voltage: $batteryVoltageText"
        )


    // --------------------------------------------------------
    // DISPLAY
    // --------------------------------------------------------

    val metrics = context.resources.displayMetrics

    val width = metrics.widthPixels
    val height = metrics.heightPixels
    val density = metrics.density

    val refreshRate = if (Build.VERSION.SDK_INT >= 30) {
        context.display.refreshRate
    } else {
        @Suppress("DEPRECATION")
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.refreshRate
    }

    val hdrSupport = if (Build.VERSION.SDK_INT >= 34) {
        // In modern SDK targets where SDK_INT >= 35 always, we can safely check display attributes if needed,
        // or just mark as supported or read from display attributes.
        "Supported"
    } else {
        "Unknown"
    }

    val displayInfo =
        listOf(
            "Resolution: ${width} × ${height}",
            "Density: ${
                String.format(
                    Locale.US,
                    "%.2f",
                    density
                )
            }",
            "Refresh rate: ${
                String.format(
                    Locale.US,
                    "%.2f Hz",
                    refreshRate
                )
            }",
            "HDR support: $hdrSupport"
        )


    // --------------------------------------------------------
    // THERMAL
    // --------------------------------------------------------

    val powerManager =
        context.getSystemService(
            Context.POWER_SERVICE
        ) as android.os.PowerManager

    val thermalStatus =
        if (Build.VERSION.SDK_INT >= 29) {

            when (
                powerManager.currentThermalStatus
            ) {

                android.os.PowerManager.THERMAL_STATUS_NONE ->
                    "None"

                android.os.PowerManager.THERMAL_STATUS_LIGHT ->
                    "Light"

                android.os.PowerManager.THERMAL_STATUS_MODERATE ->
                    "Moderate"

                android.os.PowerManager.THERMAL_STATUS_SEVERE ->
                    "Severe"

                android.os.PowerManager.THERMAL_STATUS_CRITICAL ->
                    "Critical"

                android.os.PowerManager.THERMAL_STATUS_EMERGENCY ->
                    "Emergency"

                android.os.PowerManager.THERMAL_STATUS_SHUTDOWN ->
                    "Shutdown"

                else ->
                    "Unknown"
            }

        } else {
            "Unsupported"
        }

    val thermalInfo =
        listOf(
            "Thermal status: $thermalStatus"
        )


    // --------------------------------------------------------
    // NETWORK
    // --------------------------------------------------------

    val connectivityManager =
        context.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

    val activeNetwork =
        connectivityManager.activeNetwork

    val capabilities =
        activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(
                it
            )
        }

    val networkType =
        when {

            capabilities == null ->
                "Disconnected"

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_WIFI
            ) ->
                "Wi-Fi"

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            ) ->
                "Mobile data"

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_ETHERNET
            ) ->
                "Ethernet"

            capabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_BLUETOOTH
            ) ->
                "Bluetooth"

            else ->
                "Other"
        }

    val validated =
        capabilities?.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED
        ) == true

    val networkInfo =
        listOf(
            "Connection: $networkType",
            "Internet validated: ${
                if (validated) "Yes" else "No"
            }"
        )


    // --------------------------------------------------------
    // SENSORS
    // --------------------------------------------------------

    val sensorManager =
        context.getSystemService(
            Context.SENSOR_SERVICE
        ) as android.hardware.SensorManager

    val sensors =
        sensorManager.getSensorList(
            android.hardware.Sensor.TYPE_ALL
        )

    val sensorLines =
        mutableListOf<String>()

    sensorLines.add(
        "Total Sensors: ${sensors.size}"
    )

    // Grouping by standard categories for cleaner layout mapping
    val motionSensors = sensors.filter {
        it.type in listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR
        )
    }

    val positionSensors = sensors.filter {
        it.type in listOf(
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_PROXIMITY
        )
    }

    val environmentSensors = sensors.filter {
        it.type in listOf(
            Sensor.TYPE_AMBIENT_TEMPERATURE,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_PRESSURE,
            Sensor.TYPE_RELATIVE_HUMIDITY
        )
    }

    val otherSensors = sensors.filter {
        it !in motionSensors && it !in positionSensors && it !in environmentSensors
    }

    if (motionSensors.isNotEmpty()) {
        sensorLines.add("[Category] Motion Sensors")
        motionSensors.forEach { s -> sensorLines.add("• ${s.name}") }
    }
    if (positionSensors.isNotEmpty()) {
        sensorLines.add("[Category] Position Sensors")
        positionSensors.forEach { s -> sensorLines.add("• ${s.name}") }
    }
    if (environmentSensors.isNotEmpty()) {
        sensorLines.add("[Category] Environment Sensors")
        environmentSensors.forEach { s -> sensorLines.add("• ${s.name}") }
    }
    if (otherSensors.isNotEmpty()) {
        sensorLines.add("[Category] Other/Hardware Vendor Sensors")
        otherSensors.forEach { s -> sensorLines.add("• ${s.name}") }
    }

    val sensorInfo =
        sensorLines


    // --------------------------------------------------------
    // APP
    // --------------------------------------------------------

    val packageManager =
        context.packageManager

    val packageInfo: PackageInfo =
        packageManager.getPackageInfo(
            context.packageName,
            0
        )

    val appName =
        packageInfo.applicationInfo?.let {

            packageManager.getApplicationLabel(it)
                .toString()

        } ?: "Unknown"

    val appVersion =
        packageInfo.versionName
            ?: "Unknown"

    val appInfo =
        listOf(
            "Name: $appName",
            "Package: ${context.packageName}",
            "Version: $appVersion",
            "Target SDK: ${
                context.applicationInfo.targetSdkVersion
            }",
            "Min SDK: ${
                context.applicationInfo.minSdkVersion
            }"
        )


    // --------------------------------------------------------
    // BUILD
    // --------------------------------------------------------

    val buildInfo =
        listOf(
            "Build ID: ${Build.ID}",
            "Build display: ${Build.DISPLAY}",
            "Build fingerprint: ${Build.FINGERPRINT}",
            "Bootloader: ${Build.BOOTLOADER}",
            "Radio: ${Build.getRadioVersion()}",
            "Tags: ${Build.TAGS}",
            "Type: ${Build.TYPE}",
            "User: ${Build.USER}",
            "Host: ${Build.HOST}"
        )


    return SystemInfoData(

        androidInfo = androidInfo,

        deviceInfo = deviceInfo,

        cpuInfo = cpuInfo,

        memoryInfo = memoryInfo,

        storageInfo = storageInfo,

        batteryInfo = batteryInfo,

        displayInfo = displayInfo,

        thermalInfo = thermalInfo,

        networkInfo = networkInfo,

        sensorInfo = sensorInfo,

        appInfo = appInfo,

        buildInfo = buildInfo
    )
}


// ============================================================
// CPU NAME
// ============================================================

fun getCpuName(): String {

    return try {

        val lines =
            File("/proc/cpuinfo")
                .readLines()

        val processorLine =
            lines.firstOrNull {

                it.startsWith("model name") ||
                        it.startsWith("Hardware") ||
                        it.startsWith("Processor")
            }

        processorLine
            ?.substringAfter(":")
            ?.trim()
            ?: Build.HARDWARE

    } catch (e: Exception) {

        Build.HARDWARE
    }
}


// ============================================================
// STORAGE FORMAT
// ============================================================

fun formatStorage(
    bytes: Long,
    binary: Boolean
): String {

    if (bytes < 0) {
        return "Unknown"
    }

    val unit =
        if (binary) {
            1024.0
        } else {
            1000.0
        }

    val units =
        if (binary) {

            arrayOf(
                "B",
                "KiB",
                "MiB",
                "GiB",
                "TiB"
            )

        } else {

            arrayOf(
                "B",
                "KB",
                "MB",
                "GB",
                "TB"
            )
        }

    var value =
        bytes.toDouble()

    var index = 0

    while (
        value >= unit &&
        index < units.lastIndex
    ) {

        value /= unit
        index++
    }

    return String.format(
        Locale.US,
        "%.2f %s",
        value,
        units[index]
    )
}


// ============================================================
// REFRESH INTERVAL
// ============================================================

fun formatRefreshInterval(
    interval: Long
): String {

    return when (interval) {

        1_000L ->
            "1 second"

        5_000L ->
            "5 seconds"

        10_000L ->
            "10 seconds"

        30_000L ->
            "30 seconds"

        60_000L ->
            "1 minute"

        else ->
            "${interval / 1_000} seconds"
    }
}


// ============================================================
// BUILD REPORT
// ============================================================

fun buildSystemReport(
    data: SystemInfoData,
    advancedInfo: Boolean
): String {

    val builder =
        StringBuilder()

    fun section(
        title: String,
        lines: List<String>
    ) {

        builder.appendLine(
            "===== $title ====="
        )

        lines.forEach {
            builder.appendLine(it)
        }

        builder.appendLine()
    }

    section(
        "Android",
        data.androidInfo
    )

    section(
        "Device",
        data.deviceInfo
    )

    section(
        "CPU",
        data.cpuInfo
    )

    section(
        "Memory",
        data.memoryInfo
    )

    section(
        "Storage",
        data.storageInfo
    )

    section(
        "Battery",
        data.batteryInfo
    )

    section(
        "Display",
        data.displayInfo
    )

    section(
        "Thermal",
        data.thermalInfo
    )

    section(
        "Network",
        data.networkInfo
    )

    section(
        "Sensors",
        data.sensorInfo
    )

    section(
        "App",
        data.appInfo
    )

    if (advancedInfo) {

        section(
            "Build",
            data.buildInfo
        )
    }

    return builder.toString()
}


// ============================================================
// SHARE
// ============================================================

fun shareText(
    context: Context,
    text: String
) {

    val intent =
        Intent(Intent.ACTION_SEND).apply {

            type = "text/plain"

            putExtra(
                Intent.EXTRA_TEXT,
                text
            )
        }

    context.startActivity(
        Intent.createChooser(
            intent,
            "Share system report"
        )
    )
}


// ============================================================
// COLOR SCHEME
// ============================================================

fun createColorScheme(
    accent: String,
    dark: Boolean
): androidx.compose.material3.ColorScheme {

    val base =
        if (dark) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }

    val primary =
        when (accent) {

            "Blue" ->
                ComposeColor(
                    android.graphics.Color.rgb(
                        33,
                        150,
                        243
                    )
                )

            "Green" ->
                ComposeColor(
                    android.graphics.Color.rgb(
                        76,
                        175,
                        80
                    )
                )

            "Purple" ->
                ComposeColor(
                    android.graphics.Color.rgb(
                        156,
                        39,
                        176
                    )
                )

            "Orange" ->
                ComposeColor(
                    android.graphics.Color.rgb(
                        255,
                        152,
                        0
                    )
                )

            else ->
                base.primary
        }

    return base.copy(
        primary = primary
    )
}