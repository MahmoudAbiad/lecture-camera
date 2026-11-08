package com.mahmoud.lecturecamera.ui.screen.permission

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * شاشة شرح تسبق طلب صلاحية الصور لتوضيح الخصوصية والأمان
 */
@Composable
fun PermissionExplanationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onManualPickAlternative: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "صلاحية الوصول للصور",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "يحتاج التطبيق للوصول إلى معرض الصور لفحص وقت التقاط صور المحاضرات وربطها بجدولك تلقائياً.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                FeaturePoint(
                    icon = Icons.Default.Lock,
                    title = "خصوصية تامة 100%",
                    description = "جميع العمليات تتم داخل هاتفك فقط بدون إنترنت، ولا تُرفع أي صورة."
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeaturePoint(
                    icon = Icons.Default.PhotoLibrary,
                    title = "لا يتم نسخ الصور",
                    description = "يُحفظ معرّف الصورة للرجوع إليها فقط لتوفير مساحة جهازك."
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeaturePoint(
                    icon = Icons.Default.Schedule,
                    title = "مطابقة بالوقت",
                    description = "يتم فحص تاريخ ووقت الالتقاط فقط ومقارنته بمواعيد محاضراتك."
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("متابعة ومنح الصلاحية")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onManualPickAlternative) {
                Text("اختيار صور يدويًا")
            }
        }
    )
}

@Composable
private fun FeaturePoint(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
