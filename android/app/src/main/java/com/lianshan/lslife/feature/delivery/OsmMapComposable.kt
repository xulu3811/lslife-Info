package com.lianshan.lslife.feature.delivery

import android.graphics.Color
import android.graphics.Paint
import android.preference.PreferenceManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OsmMapComposable(
    modifier: Modifier = Modifier,
    riderLat: Float,
    riderLng: Float,
    userLat: Float? = null,
    userLng: Float? = null,
    progress: Float
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember {
        // Required by osmdroid
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        MapView(context).apply {
            setMultiTouchControls(true)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            view.overlays.clear()

            val riderPoint = GeoPoint(riderLat.toDouble(), riderLng.toDouble())
            
            // Marker for rider
            val riderMarker = Marker(view).apply {
                position = riderPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "骑手当前位置"
                snippet = "正全速向您奔来"
            }
            view.overlays.add(riderMarker)

            // Marker for user and polyline if user location is available
            if (userLat != null && userLng != null) {
                val userPoint = GeoPoint(userLat.toDouble(), userLng.toDouble())
                val userMarker = Marker(view).apply {
                    position = userPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = "收货地址"
                }
                view.overlays.add(userMarker)

                // Simple straight line between rider and user
                val line = Polyline().apply {
                    addPoint(riderPoint)
                    addPoint(userPoint)
                    outlinePaint.color = Color.parseColor("#3b82f6") // tailwind blue-500
                    outlinePaint.strokeWidth = 10f
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                }
                view.overlays.add(line)
            }

            // Move camera
            view.controller.setZoom(16.0)
            view.controller.setCenter(riderPoint)
            
            view.invalidate()
        }
    )
}
