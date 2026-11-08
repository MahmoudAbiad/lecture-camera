package com.mahmoud.lecturecamera.ui.screen.setup

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.ui.theme.SubjectColorPalette
import com.mahmoud.lecturecamera.ui.viewmodel.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsSetupScreen(
    viewModel: SetupViewModel,
    onNext: () -> Unit
) {
    val subjects by viewModel.subjects.collectAsState()
    var subjectName by remember { mutableStateOf("") }
    var selectedColorIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("أضف موادك الدراسية") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
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
            // حقل اسم المادة
            OutlinedTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                label = { Text("اسم المادة") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // اختيار اللون
            Text("اختر لونًا:", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(SubjectColorPalette) { index, hex ->
                    val color = Color(AndroidColor.parseColor(hex))
                    val isSelected = index == selectedColorIndex
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(
                                    3.dp,
                                    MaterialTheme.colorScheme.onBackground,
                                    CircleShape
                                )
                                else Modifier
                            )
                            .clickable { selectedColorIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Text("✓", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // زر الإضافة
            Button(
                onClick = {
                    if (subjectName.isNotBlank()) {
                        viewModel.addSubject(
                            subjectName.trim(),
                            SubjectColorPalette[selectedColorIndex]
                        )
                        subjectName = ""
                        selectedColorIndex =
                            (selectedColorIndex + 1) % SubjectColorPalette.size
                    }
                },
                enabled = subjectName.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("أضف المادة")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (subjects.isNotEmpty()) {
                Text(
                    "المواد المضافة (${subjects.size}):",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // قائمة المواد
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjects, key = { it.id }) { subject ->
                    SubjectItem(
                        subject = subject,
                        onDelete = { viewModel.deleteSubject(subject) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // زر التالي
            Button(
                onClick = onNext,
                enabled = subjects.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("التالي \u2190")
            }
        }
    }
}

@Composable
private fun SubjectItem(subject: Subject, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(AndroidColor.parseColor(subject.colorHex)))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = subject.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف")
            }
        }
    }
}
