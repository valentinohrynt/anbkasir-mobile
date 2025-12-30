package com.anekabaru.anbkasir.ui.components

import android.graphics.Rect
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScanner(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // [UBAH DI SINI] Definisi ukuran Persegi Panjang untuk Barcode
    // Dibuat lebih lebar daripada tingginya
    val scanBoxWidth = 320.dp
    val scanBoxHeight = 150.dp

    // Konversi ke Pixel
    val scanBoxWidthPx = with(LocalDensity.current) { scanBoxWidth.toPx() }
    val scanBoxHeightPx = with(LocalDensity.current) { scanBoxHeight.toPx() }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    this.scaleType = PreviewView.ScaleType.FILL_CENTER
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraExecutor = Executors.newSingleThreadExecutor()

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val mediaImage = imageProxy.image
                        if (mediaImage != null) {
                            // Analisis tetap menggunakan area crop persegi agar aman di berbagai device
                            // ML Kit tetap akan bisa membaca barcode di dalam area visual persegi panjang
                            val width = mediaImage.width
                            val height = mediaImage.height

                            val cropSize = (if (width < height) width else height) * 0.7 // Sedikit diperbesar areanya
                            val cx = width / 2
                            val cy = height / 2

                            val cropRect = Rect(
                                (cx - cropSize / 2).toInt(),
                                (cy - cropSize / 2).toInt(),
                                (cx + cropSize / 2).toInt(),
                                (cy + cropSize / 2).toInt()
                            )

                            imageProxy.setCropRect(cropRect)

                            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                            val scanner = BarcodeScanning.getClient()

                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    for (barcode in barcodes) {
                                        barcode.rawValue?.let { code ->
                                            onCodeScanned(code)
                                        }
                                    }
                                }
                                .addOnFailureListener {
                                    Log.e("Scanner", "Detection failed", it)
                                }
                                .addOnCompleteListener {
                                    imageProxy.close()
                                }
                        } else {
                            imageProxy.close()
                        }
                    }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        Log.e("Scanner", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            }
        )

        // --- OVERLAY VISUAL (DIUBAH JADI PERSEGI PANJANG) ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            // Gunakan dimensi lebar dan tinggi yang terpisah
            val boxWidth = scanBoxWidthPx
            val boxHeight = scanBoxHeightPx

            // Gambar background gelap full
            drawRect(
                color = Color.Black.copy(alpha = 0.6f),
                size = Size(canvasWidth, canvasHeight)
            )

            // "Hapus" bagian tengah agar transparan (clear mode) - PERSEGI PANJANG
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(
                    (canvasWidth - boxWidth) / 2,
                    (canvasHeight - boxHeight) / 2
                ),
                size = Size(boxWidth, boxHeight), // Ukuran persegi panjang
                blendMode = BlendMode.Clear
            )

            // Gambar Border Putih di sekeliling kotak - PERSEGI PANJANG
            drawRect(
                color = Color.White,
                topLeft = Offset(
                    (canvasWidth - boxWidth) / 2,
                    (canvasHeight - boxHeight) / 2
                ),
                size = Size(boxWidth, boxHeight), // Ukuran persegi panjang
                style = Stroke(width = 4.dp.toPx())
            )
        }

        DisposableEffect(Unit) {
            onDispose {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }
    }
}