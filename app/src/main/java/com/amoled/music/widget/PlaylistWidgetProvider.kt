package com.amoled.music.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.amoled.music.MainActivity
import com.amoled.music.R
import com.amoled.music.playback.MusicPlaybackService

class PlaylistWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString("title", "Music") ?: "Music"
        val subtitle = prefs.getString("subtitle", "Tap to play") ?: "Tap to play"
        val bannerUri = prefs.getString("banner_uri", null)
        val isPlaying = prefs.getBoolean("is_playing", false)

        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, title, subtitle, bannerUri, isPlaying)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val title = prefs.getString("title", "Music") ?: "Music"
        val subtitle = prefs.getString("subtitle", "Tap to play") ?: "Tap to play"
        val bannerUri = prefs.getString("banner_uri", null)
        val isPlaying = prefs.getBoolean("is_playing", false)

        updateAppWidget(context, appWidgetManager, appWidgetId, title, subtitle, bannerUri, isPlaying)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action ?: return

        when (action) {
            ACTION_TOGGLE_PLAY, ACTION_NEXT_TRACK, ACTION_PREV_TRACK, ACTION_PLAY_PLAYLIST -> {
                val serviceIntent = Intent(context, MusicPlaybackService::class.java).apply {
                    this.action = action
                    intent.extras?.let { putExtras(it) }
                }
                try {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } catch (e: Exception) {
                    try {
                        context.startService(serviceIntent)
                    } catch (_: Exception) {}
                }
            }
        }
    }

    companion object {
        const val PREFS_NAME = "music_widget_prefs"
        const val ACTION_PLAY_PLAYLIST = "com.amoled.music.ACTION_PLAY_PLAYLIST"
        const val ACTION_TOGGLE_PLAY = "com.amoled.music.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT_TRACK = "com.amoled.music.ACTION_NEXT_TRACK"
        const val ACTION_PREV_TRACK = "com.amoled.music.ACTION_PREV_TRACK"
        const val EXTRA_PLAYLIST_ID = "extra_playlist_id"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            title: String,
            subtitle: String,
            bannerUriString: String?,
            isPlaying: Boolean
        ) {
            val roundedBanner = loadRoundedBitmap(context, bannerUriString)

            val compactViews = buildCompactViews(context, title, subtitle, roundedBanner, isPlaying)
            val expandedViews = buildExpandedViews(context, title, subtitle, roundedBanner, isPlaying)

            val finalViews = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val viewMap = mapOf(
                    SizeF(120f, 55f) to compactViews,
                    SizeF(180f, 105f) to expandedViews
                )
                RemoteViews(viewMap)
            } else {
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
                if (minHeight >= 95) expandedViews else compactViews
            }

            appWidgetManager.updateAppWidget(appWidgetId, finalViews)
        }

        private fun buildCompactViews(
            context: Context,
            title: String,
            subtitle: String,
            bannerBitmap: Bitmap?,
            isPlaying: Boolean
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_playlist_layout)

            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_subtitle, subtitle)

            if (bannerBitmap != null) {
                views.setImageViewBitmap(R.id.widget_banner, bannerBitmap)
            } else {
                views.setImageViewResource(R.id.widget_banner, R.drawable.ic_banner_placeholder)
            }

            views.setImageViewResource(
                R.id.widget_btn_play,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            // Click body to open App
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingAppIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)

            // Play / Pause in background
            val toggleIntent = Intent(context, PlaylistWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PLAY
            }
            val togglePending = PendingIntent.getBroadcast(
                context,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play, togglePending)

            // Next Track in background
            val nextIntent = Intent(context, PlaylistWidgetProvider::class.java).apply {
                action = ACTION_NEXT_TRACK
            }
            val nextPending = PendingIntent.getBroadcast(
                context,
                2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            return views
        }

        private fun buildExpandedViews(
            context: Context,
            title: String,
            subtitle: String,
            bannerBitmap: Bitmap?,
            isPlaying: Boolean
        ): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_playlist_expanded_layout)

            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_subtitle, subtitle)

            if (bannerBitmap != null) {
                views.setImageViewBitmap(R.id.widget_banner, bannerBitmap)
            } else {
                views.setImageViewResource(R.id.widget_banner, R.drawable.ic_banner_placeholder)
            }

            views.setImageViewResource(
                R.id.widget_btn_play,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            // Click body to open App
            val appIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingAppIntent = PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingAppIntent)

            // Previous in background
            val prevIntent = Intent(context, PlaylistWidgetProvider::class.java).apply {
                action = ACTION_PREV_TRACK
            }
            val prevPending = PendingIntent.getBroadcast(
                context,
                3,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_prev, prevPending)

            // Play / Pause in background
            val toggleIntent = Intent(context, PlaylistWidgetProvider::class.java).apply {
                action = ACTION_TOGGLE_PLAY
            }
            val togglePending = PendingIntent.getBroadcast(
                context,
                1,
                toggleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_play, togglePending)

            // Next Track in background
            val nextIntent = Intent(context, PlaylistWidgetProvider::class.java).apply {
                action = ACTION_NEXT_TRACK
            }
            val nextPending = PendingIntent.getBroadcast(
                context,
                2,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_next, nextPending)

            return views
        }

        private fun loadRoundedBitmap(context: Context, bannerUriString: String?): Bitmap? {
            if (bannerUriString.isNullOrBlank()) return null
            return try {
                val uri = Uri.parse(bannerUriString)
                val stream = context.contentResolver.openInputStream(uri)
                val original = BitmapFactory.decodeStream(stream)
                stream?.close()
                if (original != null) {
                    val size = 256
                    val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(output)
                    val paint = Paint().apply { isAntiAlias = true }
                    val rect = Rect(0, 0, original.width, original.height)
                    val rectF = RectF(0f, 0f, size.toFloat(), size.toFloat())

                    canvas.drawRoundRect(rectF, 40f, 40f, paint)
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                    canvas.drawBitmap(original, rect, rectF, paint)
                    output
                } else null
            } catch (e: Exception) {
                null
            }
        }

        fun updateAllWidgets(
            context: Context,
            title: String,
            subtitle: String,
            bannerUriString: String?,
            isPlaying: Boolean
        ) {
            // Save state in SharedPreferences for subsequent system re-renders
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("title", title)
                .putString("subtitle", subtitle)
                .putString("banner_uri", bannerUriString)
                .putBoolean("is_playing", isPlaying)
                .apply()

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PlaylistWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id, title, subtitle, bannerUriString, isPlaying)
            }
        }
    }
}
