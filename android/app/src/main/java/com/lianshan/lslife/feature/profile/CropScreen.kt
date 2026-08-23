package com.lianshan.lslife.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lianshan.lslife.core.data.LsRepository
import com.lianshan.lslife.ui.components.LoadingBox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import kotlin.math.max
import coil.imageLoader

data class CropUiState(
    val loading: Boolean = false,
    val uploading: Boolean = false,
    val bitmap: ImageBitmap? = null,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val message: String? = null,
    val uploadedUrl: String? = null
)

@HiltViewModel
class CropViewModel @Inject constructor(
    private val lsRepository: LsRepository
) : ViewModel() {
    private val _state = MutableStateFlow(CropUiState())
    val state: StateFlow<CropUiState> = _state

    fun loadBitmap(context: Context, uriString: String) {
        if (_state.value.bitmap != null) return
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(loading = true) }
            try {
                val file = java.io.File(context.cacheDir, "avatar_temp.jpg")
                if (!file.exists()) {
                    throw Exception("文件不存在或保存失败")
                }

                // Use Coil to load the image robustly, handling Exif, orientation, and hardware bitmap issues
                val request = coil.request.ImageRequest.Builder(context)
                    .data(file)
                    .allowHardware(false) // Force software bitmap so we can draw it later in cropAndUpload
                    .size(2048) // Limit size to avoid OOM
                    .build()
                
                val result = context.imageLoader.execute(request)
                if (result is coil.request.SuccessResult) {
                    val drawable = result.drawable
                    if (drawable is android.graphics.drawable.BitmapDrawable) {
                        val bitmap = drawable.bitmap
                        val imageBitmap = bitmap.asImageBitmap()
                        _state.update { 
                            it.copy(
                                loading = false, 
                                bitmap = imageBitmap,
                                originalWidth = bitmap.width,
                                originalHeight = bitmap.height
                            ) 
                        }
                    } else {
                        throw Exception("图片格式不受支持")
                    }
                } else {
                    throw Exception("图片加载失败")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                _state.update { it.copy(loading = false, message = "无法加载图片: ${e.message}") }
            }
        }
    }

    fun cropAndUpload(context: Context, bitmap: ImageBitmap, offset: Offset, scale: Float, cropSize: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(uploading = true) }
            try {
                // 1. Convert ImageBitmap to Android Bitmap
                val androidBitmap = bitmap.asAndroidBitmap()
                
                // 2. Calculate crop rect
                // The image is drawn at center of the screen, with pan/zoom.
                // We need to reverse the transform to find what part of original image is in the crop circle.
                // Let's use a simpler approach: Draw the original bitmap into a new canvas with the transform, and extract the center.
                val resultSize = 500
                val resultBmp = Bitmap.createBitmap(resultSize, resultSize, Bitmap.Config.ARGB_8888)
                val canvas = android.graphics.Canvas(resultBmp)
                canvas.drawColor(android.graphics.Color.WHITE)
                
                val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
                
                // We need to map the center 500x500 box from the screen coordinates to the bitmap coordinates.
                // It is easier to just apply the same scale/translate to the canvas.
                // cropSize is the size of the square on screen.
                // The ratio from resultSize to cropSize is `resultSize / cropSize`
                val ratio = resultSize / cropSize
                
                canvas.scale(ratio, ratio)
                
                // Move to center of result 
                canvas.translate(cropSize / 2f, cropSize / 2f)
                
                // Apply the user's offset & scale
                canvas.translate(offset.x, offset.y)
                canvas.scale(scale, scale)
                
                // Draw the original image centered without density scaling
                canvas.translate(-androidBitmap.width / 2f, -androidBitmap.height / 2f)
                
                val srcRect = android.graphics.Rect(0, 0, androidBitmap.width, androidBitmap.height)
                val dstRect = android.graphics.RectF(0f, 0f, androidBitmap.width.toFloat(), androidBitmap.height.toFloat())
                canvas.drawBitmap(androidBitmap, srcRect, dstRect, paint)

                // 3. Compress using AvatarCompressor
                val compressedBytes = AvatarCompressor.compressAvatar(resultBmp, 50 * 1024, 500)
                resultBmp.recycle()

                // 4. Upload
                val reqFile = compressedBytes.toRequestBody("image/*".toMediaTypeOrNull())
                val part = okhttp3.MultipartBody.Part.createFormData("image", "avatar.jpg", reqFile)
                
                val res = lsRepository.uploadImage(part)
                if (res.isSuccess) {
                    val url = res.getOrNull()?.url
                    if (url != null) {
                        withContext(Dispatchers.Main) {
                            _state.update { it.copy(uploading = false, uploadedUrl = url) }
                        }
                    } else {
                        throw Exception("Upload returned null url")
                    }
                } else {
                    throw Exception("Upload failed")
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _state.update { it.copy(uploading = false, message = "裁剪上传失败: ${e.message ?: "未知错误"}") }
                }
            }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
    uriString: String,
    onBack: () -> Unit,
    onCropped: (String) -> Unit,
    viewModel: CropViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(uriString) {
        viewModel.loadBitmap(context, uriString)
    }

    LaunchedEffect(state.uploadedUrl) {
        state.uploadedUrl?.let { onCropped(it) }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("裁剪头像") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "取消")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Black
    ) { padding ->
        if (state.loading) {
            LoadingBox(Modifier.padding(padding).fillMaxSize())
            return@Scaffold
        }

        val bitmap = state.bitmap
        if (bitmap == null) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("无法加载图片", color = Color.White)
            }
            return@Scaffold
        }

        var scale by remember { mutableFloatStateOf(0f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var viewportSize by remember { mutableStateOf(IntSize.Zero) }
        var isInitialized by remember { mutableStateOf(false) }

        // The size of the circular crop hole
        val cropSize = if (viewportSize.width > 0) (viewportSize.width * 0.8f) else 0f

        // 居中与缩放重新计算函数 (在重组初期及加载成功回调时触发)
        fun recalculateCenterAndFit(bmp: ImageBitmap, size: IntSize) {
            if (size.width > 0 && size.height > 0 && bmp.width > 0 && bmp.height > 0) {
                val holeSize = size.width * 0.8f
                val minDim = minOf(bmp.width, bmp.height).toFloat()
                if (minDim > 0f) {
                    scale = maxOf(holeSize / minDim, 0.1f)
                    offset = Offset.Zero
                    isInitialized = true
                }
            }
        }

        // 1. 在重组（Recomposition）初期检查并立即触发居中对齐与初始缩放计算
        if (!isInitialized && viewportSize.width > 0 && viewportSize.height > 0) {
            recalculateCenterAndFit(bitmap, viewportSize)
        }

        // 2. 在尺寸变更或异步加载完成回调时保障触发计算
        LaunchedEffect(bitmap, viewportSize) {
            if (!isInitialized && viewportSize.width > 0 && viewportSize.height > 0) {
                recalculateCenterAndFit(bitmap, viewportSize)
            }
        }

        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .onSizeChanged { newSize ->
                    val oldSize = viewportSize
                    viewportSize = newSize
                    if (oldSize == IntSize.Zero && newSize.width > 0 && !isInitialized) {
                        recalculateCenterAndFit(bitmap, newSize)
                    }
                }
        ) {
            Canvas(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (isInitialized) {
                            scale = (scale * zoom).coerceIn(0.1f, 10f)
                            offset += pan
                        }
                    }
                }) {
                if (!isInitialized) return@Canvas

                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw the image with mathematically perfect transform matching CropViewModel
                withTransform({
                    translate(canvasWidth / 2f + offset.x, canvasHeight / 2f + offset.y)
                    scale(scale, scale, pivot = Offset.Zero)
                    translate(-bitmap.width / 2f, -bitmap.height / 2f)
                }) {
                    drawImage(image = bitmap)
                }

                // Draw the mask
                val path = Path().apply {
                    addRect(Rect(0f, 0f, canvasWidth, canvasHeight))
                    addOval(
                        Rect(
                            center = Offset(canvasWidth / 2, canvasHeight / 2),
                            radius = cropSize / 2
                        )
                    )
                    fillType = PathFillType.EvenOdd
                }
                drawPath(path = path, color = Color.Black.copy(alpha = 0.6f))
                
                // Draw crop circle outline
                drawCircle(
                    color = Color.White,
                    radius = cropSize / 2,
                    center = Offset(canvasWidth / 2, canvasHeight / 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                )
            }

            // Bottom Confirm Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(32.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (state.uploading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    FloatingActionButton(
                        onClick = {
                            viewModel.cropAndUpload(context, bitmap, offset, scale, cropSize)
                        },
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "确认裁剪")
                    }
                }
            }
        }
    }
}
