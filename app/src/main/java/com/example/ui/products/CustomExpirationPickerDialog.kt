package com.example.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BorderLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.FormatUtils
import java.util.Calendar

@Composable
fun CustomExpirationPickerDialog(
    currentExpiration: Long?,
    onConfirm: (Long) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    // Initialize Calendar with current expiry or default to current date and time (tanggal saat itu)
    val initialCal = remember(currentExpiration) {
        Calendar.getInstance().apply {
            if (currentExpiration != null && currentExpiration > 0) {
                timeInMillis = currentExpiration
            }
        }
    }

    var selectedYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH) + 1) } // 1-12
    var selectedDay by remember { mutableIntStateOf(initialCal.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(initialCal.get(Calendar.HOUR_OF_DAY)) } // 0-23
    var selectedMinute by remember { mutableIntStateOf(initialCal.get(Calendar.MINUTE)) } // 0-59

    // State input teks manual untuk Hari, Bulan, Tahun (diisi langsung via keyboard)
    var dayInput by remember { mutableStateOf(selectedDay.toString()) }
    var monthInput by remember { mutableStateOf(selectedMonth.toString()) }
    var yearInput by remember { mutableStateOf(selectedYear.toString()) }

    // Active tab: 0 = "Atur Tanggal", 1 = "Atur Jam / Waktu"
    var activeTab by remember { mutableIntStateOf(0) }

    // Helper to calculate computed timestamp in real time
    val computedTimestamp = remember(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute, dayInput, monthInput, yearInput) {
        val d = dayInput.toIntOrNull()?.coerceIn(1, 31) ?: selectedDay
        val m = monthInput.toIntOrNull()?.coerceIn(1, 12) ?: selectedMonth
        val rawYear = yearInput.toIntOrNull()
        val y = when {
            rawYear == null -> selectedYear
            rawYear in 2020..2099 -> rawYear
            rawYear in 0..99 -> 2000 + rawYear
            else -> selectedYear
        }
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, y)
        cal.set(Calendar.MONTH, (m - 1).coerceIn(0, 11))
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        cal.set(Calendar.DAY_OF_MONTH, d.coerceIn(1, maxDay))
        cal.set(Calendar.HOUR_OF_DAY, selectedHour.coerceIn(0, 23))
        cal.set(Calendar.MINUTE, selectedMinute.coerceIn(0, 59))
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.timeInMillis
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = KelolaRadius.ShapeLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Atur Kadaluarsa Kustom",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pilih tanggal atau jam kadaluarsa",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Current Result Preview Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = KelolaRadius.ShapeInput,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Jadwal Kadaluarsa Produk:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = FormatUtils.formatDateTime(computedTimestamp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Tabs: Pilih Tanggal vs Pilih Jam
                Surface(
                    shape = KelolaRadius.ShapeInput,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    TabRow(
                        selectedTabIndex = activeTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        indicator = {},
                        divider = {}
                    ) {
                        Tab(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Pilih Tanggal", fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Tab(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Pilih Jam", fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal)
                                }
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (activeTab == 0) {
                    // --- TAB TANGGAL ---
                    Text(
                        text = "Pilihan Tanggal Cepat:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickDays = listOf(
                            "Hari Ini" to 0,
                            "Besok" to 1,
                            "+3 Hari" to 3,
                            "+7 Hari" to 7,
                            "+1 Bulan" to 30,
                            "+3 Bulan" to 90,
                            "+1 Tahun" to 365
                        )
                        items(quickDays) { (label, days) ->
                            FilterChip(
                                selected = false,
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.DAY_OF_YEAR, days)
                                    selectedYear = cal.get(Calendar.YEAR)
                                    selectedMonth = cal.get(Calendar.MONTH) + 1
                                    selectedDay = cal.get(Calendar.DAY_OF_MONTH)
                                    dayInput = selectedDay.toString()
                                    monthInput = selectedMonth.toString()
                                    yearInput = selectedYear.toString()
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                shape = KelolaRadius.ShapeInput,
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Atur Tanggal / Bulan / Tahun:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day (Diisi manual via keyboard tanpa icon +/-)
                        ManualDateInputField(
                            label = "Hari",
                            value = dayInput,
                            placeholder = "1-31",
                            helperText = "Tgl 1-31",
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(2)
                                dayInput = clean
                                clean.toIntOrNull()?.let { d ->
                                    if (d in 1..31) selectedDay = d
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_custom_day"
                        )

                        // Month (Diisi manual via keyboard tanpa icon +/-)
                        ManualDateInputField(
                            label = "Bulan",
                            value = monthInput,
                            placeholder = "1-12",
                            helperText = "Bln 1-12",
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(2)
                                monthInput = clean
                                clean.toIntOrNull()?.let { m ->
                                    if (m in 1..12) selectedMonth = m
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "input_custom_month"
                        )

                        // Year (Diisi manual via keyboard tanpa icon +/-)
                        ManualDateInputField(
                            label = "Tahun",
                            value = yearInput,
                            placeholder = "YYYY",
                            helperText = "Tahun",
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }.take(4)
                                yearInput = clean
                                val rawY = clean.toIntOrNull()
                                if (rawY != null) {
                                    if (rawY in 2020..2099) {
                                        selectedYear = rawY
                                    } else if (clean.length == 2 && rawY in 0..99) {
                                        selectedYear = 2000 + rawY
                                    }
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            testTag = "input_custom_year"
                        )
                    }
                } else {
                    // --- TAB JAM & WAKTU ---
                    Text(
                        text = "Pilihan Jam Cepat:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val quickHours = listOf(
                            "Sekarang" to Pair(Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE)),
                            "+1 Jam" to Pair((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24, Calendar.getInstance().get(Calendar.MINUTE)),
                            "+3 Jam" to Pair((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 3) % 24, Calendar.getInstance().get(Calendar.MINUTE)),
                            "+6 Jam" to Pair((Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 6) % 24, 0),
                            "Siang (12:00)" to Pair(12, 0),
                            "Sore (17:00)" to Pair(17, 0),
                            "Malam (21:00)" to Pair(21, 0),
                            "Akhir Hari (23:59)" to Pair(23, 59)
                        )
                        items(quickHours) { (label, time) ->
                            val isSelected = selectedHour == time.first && selectedMinute == time.second
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedHour = time.first
                                    selectedMinute = time.second
                                },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                shape = KelolaRadius.ShapeInput,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Atur Jam & Menit Presisi:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hour
                        NumberStepperField(
                            label = "Jam (0-23)",
                            value = selectedHour,
                            minValue = 0,
                            maxValue = 23,
                            formatTwoDigits = true,
                            onValueChange = { selectedHour = it },
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Minute
                        NumberStepperField(
                            label = "Menit (0-59)",
                            value = selectedMinute,
                            minValue = 0,
                            maxValue = 59,
                            formatTwoDigits = true,
                            onValueChange = { selectedMinute = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Actions Bottom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentExpiration != null) {
                        TextButton(
                            onClick = {
                                onClear()
                                onDismiss()
                            }
                        ) {
                            Text("Hapus Expired", color = DangerRed, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = onDismiss) {
                        Text("Batal", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = {
                            onConfirm(computedTimestamp)
                            onDismiss()
                        },
                        shape = KelolaRadius.ShapeInput,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("button_apply_custom_expiry")
                    ) {
                        Text("Terapkan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NumberStepperField(
    label: String,
    value: Int,
    minValue: Int,
    maxValue: Int,
    formatTwoDigits: Boolean = false,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = KelolaRadius.ShapeInput,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = {
                        val next = if (value - 1 < minValue) maxValue else value - 1
                        onValueChange(next)
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Kurang",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = if (formatTwoDigits) String.format("%02d", value) else "$value",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = {
                        val next = if (value + 1 > maxValue) minValue else value + 1
                        onValueChange(next)
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ManualDateInputField(
    label: String,
    value: String,
    placeholder: String,
    helperText: String? = null,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        supportingText = helperText?.let { { Text(it, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        ),
        shape = KelolaRadius.ShapeInput,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier.testTag(testTag)
    )
}

