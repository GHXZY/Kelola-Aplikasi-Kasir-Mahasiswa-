package com.example.ui.settings

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.KelolaRadius
import com.example.ui.theme.KelolaSpacing
import kotlin.math.max

@Composable
fun QrisCropDialog(
    rawBitmap: Bitmap,
    onConfirmCrop: (Bitmap) -> Unit,
    onDismiss: () -> Unit
) {
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp),
            shape = KelolaRadius.ShapeLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
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
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Crop,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Potong Gambar QRIS (1:1)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Posisikan agar fokus pada kode QR saja",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", modifier = Modifier.size(20.dp))
                    }
                }

                // Square Crop Viewport with Grid Lines
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .aspectRatio(1f)
                        .clip(KelolaRadius.ShapeCard)
                        .background(Color.Black)
                        .border(2.dp, MaterialTheme.colorScheme.primary, KelolaRadius.ShapeCard)
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                panX += dragAmount.x
                                panY += dragAmount.y
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height

                        val baseScale = max(canvasW / rawBitmap.width, canvasH / rawBitmap.height)
                        val effectiveScale = baseScale * zoom

                        val drawW = rawBitmap.width * effectiveScale
                        val drawH = rawBitmap.height * effectiveScale

                        val drawX = (canvasW - drawW) / 2f + panX
                        val drawY = (canvasH - drawH) / 2f + panY

                        drawIntoCanvas { composeCanvas ->
                            val nativeCanvas = composeCanvas.nativeCanvas
                            val matrix = Matrix().apply {
                                postScale(effectiveScale, effectiveScale)
                                postTranslate(drawX, drawY)
                            }
                            val paint = Paint().apply {
                                isFilterBitmap = true
                                isAntiAlias = true
                            }
                            nativeCanvas.drawBitmap(rawBitmap, matrix, paint)
                        }

                        // Rule of Thirds Overlay Grid Lines
                        val colStep = canvasW / 3f
                        val rowStep = canvasH / 3f
                        val gridColor = Color.White.copy(alpha = 0.25f)
                        drawLine(gridColor, Offset(colStep, 0f), Offset(colStep, canvasH), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, Offset(colStep * 2, 0f), Offset(colStep * 2, canvasH), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, Offset(0f, rowStep), Offset(canvasW, rowStep), strokeWidth = 1.dp.toPx())
                        drawLine(gridColor, Offset(0f, rowStep * 2), Offset(canvasW, rowStep * 2), strokeWidth = 1.dp.toPx())
                    }

                    // Drag hint banner
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(8.dp),
                        shape = KelolaRadius.ShapeSmall,
                        color = Color.Black.copy(alpha = 0.65f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.OpenWith, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Text("Sentuh & geser gambar untuk mengatur posisi", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                // Zoom Control Slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.ZoomIn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Text("Skala / Zoom", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        Text(String.format("%.1fx", zoom), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Slider(
                            value = zoom,
                            onValueChange = { zoom = it },
                            valueRange = 0.8f..3.5f,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Icon(Icons.Default.ZoomIn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                }

                // Quick Reset Center Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            zoom = 1.0f
                            panX = 0f
                            panY = 0f
                        },
                        shape = KelolaRadius.ShapeSmall,
                        modifier = Modifier.height(KelolaSpacing.ButtonHeightSmall)
                    ) {
                        Icon(Icons.Default.RotateLeft, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pusatkan", fontSize = 11.sp)
                    }

                    Text(
                        text = "${rawBitmap.width} x ${rawBitmap.height} px",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // CTA Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = KelolaRadius.ShapeInput,
                        modifier = Modifier
                            .weight(1f)
                            .height(KelolaSpacing.ButtonHeightCta)
                    ) {
                        Text("Batal")
                    }

                    Button(
                        onClick = {
                            // Produce high resolution 600x600 px cropped square Bitmap
                            val exportSize = 600
                            val cropped = Bitmap.createBitmap(exportSize, exportSize, Bitmap.Config.ARGB_8888)
                            val canvas = Canvas(cropped)
                            canvas.drawColor(AndroidColor.WHITE)

                            val baseScale = max(exportSize.toFloat() / rawBitmap.width, exportSize.toFloat() / rawBitmap.height)
                            val effectiveScale = baseScale * zoom

                            // Scale pan offset from preview viewport (260dp) to export canvas (600px)
                            // In preview: canvas is 260dp, so scale factor is exportSize / previewCanvasSizePx
                            val previewViewportPx = 260f * 2.5f // nominal estimate or ratio
                            val scaleFactor = exportSize.toFloat() / 260f

                            val drawW = rawBitmap.width * effectiveScale
                            val drawH = rawBitmap.height * effectiveScale

                            val drawX = (exportSize - drawW) / 2f + panX * (exportSize / 260f)
                            val drawY = (exportSize - drawH) / 2f + panY * (exportSize / 260f)

                            val matrix = Matrix().apply {
                                postScale(effectiveScale, effectiveScale)
                                postTranslate(drawX, drawY)
                            }
                            val paint = Paint().apply {
                                isFilterBitmap = true
                                isAntiAlias = true
                            }
                            canvas.drawBitmap(rawBitmap, matrix, paint)

                            onConfirmCrop(cropped)
                        },
                        shape = KelolaRadius.ShapeInput,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .weight(1f)
                            .height(KelolaSpacing.ButtonHeightCta)
                            .testTag("button_confirm_qris_crop")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Terapkan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
