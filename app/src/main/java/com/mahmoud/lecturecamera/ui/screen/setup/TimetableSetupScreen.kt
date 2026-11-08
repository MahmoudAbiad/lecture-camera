package com.mahmoud.lecturecamera.ui.screen.setup

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.data.entity.TimetableSlot
import com.mahmoud.lecturecamera.ui.viewmodel.SetupViewModel

private val DAYS = listOf(
    6 to "السبت",
    7 to "الأحد",
    1 to "الاثنين",
    2 to "الثلاثاء",
    3 to "الأربعاء",
    4 to "الخميس",
    5 to "الجمعة"
)

private data class TimeOption(val minuteOfDay: Int, val label: String)

private val TIME_OPTIONS = buildList {
    for (h in 7..21) {
        add(TimeOption(h * 60, "%02d:00".format(h)))
        if (h < 21) add(TimeOption(h * 60 + 30, "%02d:30".format(h)))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableSetupScreen(
    viewModel: SetupViewModel,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    val slots by viewModel.slots.collectAsState()

    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var selectedSubjectIndex by remember { mutableIntStateOf(0) }
    var startTimeIndex by remember { mutableIntStateOf(2) }  // 08:00
    var endTimeIndex by remember { mutableIntStateOf(5) }    // 09:30

    // تأكد أن الفهرس ضمن الحدود
    LaunchedEffect(subjects.size) {
        if (selectedSubjectIndex >= subjects.size) selectedSubjectIndex = 0
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("أضف جدولك الأسبوعي") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // اليوم والمادة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Selector(
                    label = "اليوم",
                    options = DAYS.map { it.second },
                    selectedIndex = selectedDayIndex,
                    onSelect = { selectedDayIndex = it },
                    modifier = Modifier.weight(1f)
                )
                Selector(
                    label = "المادة",
                    options = subjects.map { it.name },
                    selectedIndex = selectedSubjectIndex,
                    onSelect = { selectedSubjectIndex = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // الوقت
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Selector(
                    label = "من",
                    options = TIME_OPTIONS.map { it.label },
                    selectedIndex = startTimeIndex,
                    onSelect = { startTimeIndex = it },
                    modifier = Modifier.weight(1f)
                )
                Selector(
                    label = "إلى",
                    options = TIME_OPTIONS.map { it.label },
                    selectedIndex = endTimeIndex,
                    onSelect = { endTimeIndex = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (subjects.isNotEmpty()) {
                        val startMin = TIME_OPTIONS[startTimeIndex].minuteOfDay
                        val endMin = TIME_OPTIONS[endTimeIndex].minuteOfDay
                        if (endMin > startMin) {
                            viewModel.addSlot(
                                subjectId = subjects[selectedSubjectIndex].id,
                                dayOfWeek = DAYS[selectedDayIndex].first,
                                startMinute = startMin,
                                endMinute = endMin
                            )
                        }
                    }
                },
                enabled = subjects.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("أضف الحصة")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (slots.isNotEmpty()) {
                Text(
                    "الحصص المضافة (${slots.size}):",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // قائمة الحصص مجمّعة حسب اليوم
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val grouped = slots.groupBy { slot ->
                    DAYS.find { it.first == slot.dayOfWeek }?.second ?: ""
                }
                val sortedDays = DAYS.map { it.second }.filter { it in grouped.keys }

                sortedDays.forEach { dayName ->
                    item(key = "header_$dayName") {
                        Text(
                            dayName,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    items(
                        grouped[dayName].orEmpty(),
                        key = { it.id }
                    ) { slot ->
                        SlotItem(
                            slot = slot,
                            subject = subjects.find { it.id == slot.subjectId },
                            onDelete = { viewModel.deleteSlot(slot) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onFinish,
                enabled = slots.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("إنهاء الإعداد ✓")
            }
        }
    }
}

@Composable
private fun SlotItem(slot: TimetableSlot, subject: Subject?, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (subject != null) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color(AndroidColor.parseColor(subject.colorHex)))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = subject.name,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Text(
                    text = "مادة محذوفة",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = "${fmtTime(slot.startMinuteOfDay)} \u2013 ${fmtTime(slot.endMinuteOfDay)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "حذف",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun fmtTime(minuteOfDay: Int): String {
    val h = minuteOfDay / 60
    val m = minuteOfDay % 60
    return "%02d:%02d".format(h, m)
}

@Composable
private fun Selector(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = options.getOrElse(selectedIndex) { "\u2014" },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(index)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
