package com.mahmoud.lecturecamera.ui.screen.review

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mahmoud.lecturecamera.data.entity.Photo
import com.mahmoud.lecturecamera.data.entity.Subject
import com.mahmoud.lecturecamera.ui.viewmodel.PendingGroup
import com.mahmoud.lecturecamera.ui.viewmodel.ReviewViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewPendingScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit
) {
    val groupedList by viewModel.groupedPending.collectAsState()
    val selectedIds by viewModel.selectedPhotoIds.collectAsState()
    val allSubjects by viewModel.subjects.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("مراجعة الصور المقترحة") },
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
        if (groupedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "رائع! تمت مراجعة جميع الصور",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد صور مقترحة تنتظر المراجعة حاليًا.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(onClick = onBack) {
                        Text("العودة للرئيسية")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(groupedList, key = { "${it.subject.id}_${it.date}" }) { group ->
                    GroupReviewCard(
                        group = group,
                        selectedIds = selectedIds,
                        allSubjects = allSubjects,
                        onTogglePhoto = { viewModel.togglePhotoSelection(it) },
                        onAcceptGroup = { viewModel.acceptGroup(group) },
                        onRejectPhoto = { viewModel.rejectPhoto(it) },
                        onSelectAll = { viewModel.selectAllInGroup(group.photos) },
                        onChangeSubject = { newSubId -> viewModel.changeGroupSubject(group.photos, newSubId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupReviewCard(
    group: PendingGroup,
    selectedIds: Set<Long>,
    allSubjects: List<Subject>,
    onTogglePhoto: (Long) -> Unit,
    onAcceptGroup: () -> Unit,
    onRejectPhoto: (Long) -> Unit,
    onSelectAll: () -> Unit,
    onChangeSubject: (Long) -> Unit
) {
    var subjectMenuExpanded by remember { mutableStateOf(false) }

    val subjectColor = runCatching {
        Color(AndroidColor.parseColor(group.subject.colorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    val dateFormatted = group.date.format(DateTimeFormatter.ofPattern("EEEE، d MMMM yyyy"))
    val selectedCountInGroup = group.photos.count { selectedIds.contains(it.id) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // رأس المجموعة: اسم المادة وتاريخ الحصة
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(subjectColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.subject.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // زر تغيير المادة المقترحة
                Box {
                    OutlinedButton(
                        onClick = { subjectMenuExpanded = true },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تغيير المادة", style = MaterialTheme.typography.labelSmall)
                    }

                    DropdownMenu(
                        expanded = subjectMenuExpanded,
                        onDismissRequest = { subjectMenuExpanded = false }
                    ) {
                        allSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub.name) },
                                onClick = {
                                    onChangeSubject(sub.id)
                                    subjectMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // شبكة الصور داخل الحصة
            val gridHeight = when {
                group.photos.size <= 3 -> 110.dp
                group.photos.size <= 6 -> 220.dp
                else -> 330.dp
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(group.photos, key = { it.id }) { photo ->
                    val isSelected = selectedIds.contains(photo.id)
                    PhotoReviewThumbnail(
                        photo = photo,
                        isSelected = isSelected,
                        onToggle = { onTogglePhoto(photo.id) },
                        onReject = { onRejectPhoto(photo.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // أزرار اتخاذ القرار: اعتماد أو نقل لغير مصنف
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAcceptGroup,
                    enabled = selectedCountInGroup > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("قبول كجلسة ($selectedCountInGroup)")
                }

                OutlinedButton(
                    onClick = onSelectAll,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تحديد الكل")
                }
            }
        }
    }
}

@Composable
private fun PhotoReviewThumbnail(
    photo: Photo,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onToggle() }
    ) {
        AsyncImage(
            model = photo.mediaStoreUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // شارة الاختيار (Checkmark)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }

        // زر استبعاد مباشر (X)
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onReject() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "استبعاد لغير مصنف",
                modifier = Modifier.size(14.dp),
                tint = Color.White
            )
        }
    }
}
