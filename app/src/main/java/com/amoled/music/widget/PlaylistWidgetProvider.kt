package com.amoled.music.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.RemoteViews
import com.amoled.music.MainActivity
import com.amoled.music.R
import com.amoled.music.playback.MusicPlaybackService

class PlaylistWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "Amoled Music", "Tap to open player", null, false)
        }
    }

    companion object {
        const val ACTION_PLAY_PLAYLIST = "com.amoled.music.ACTION_PLAY_PLAYLIST"
        const val ACTION_TOGGLE_PLAY = "com.amoled.music.ACTION_TOGGLE_PLAY"
        const val ACTION_NEXT_TRACK = "com.amoled.music.ACTION_NEXT_TRACK"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            title: String,
            subtitle: String,
            bannerUriString: String?,
            isPlaying: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_playlist_layout)

            views.setTextViewText(R.id.widget_title, title)
            views.setTextViewText(R.id.widget_subtitle, subtitle)

            // Play / Pause Icon
            views.setImageViewResource(
                R.id.widget_btn_play,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )

            // Banner Image
            if (!bannerUriString.isNullOrBlank()) {
                try {
                    val uri = Uri.parse(bannerUriString)
                    val stream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(stream)
                    stream?.close()
                    if (bitmap != null) {
                        views.setImageViewBitmap(R.id.widget_banner, bitmap)
                    } else {
                        views.setImageViewResource(R.id.widget_banner, R.drawable.ic_banner_placeholder)
                    }
                } catch (e: Exception) {
                    views.setImageViewResource(R.id.widget_banner, R.drawable.ic_banner_placeholder)
                }
            } else {
                views.setImageViewResource(R.id.widget_banner, R.drawable.ic_banner_placeholder)
            }

            // Click on widget body opens MainActivity
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

            // Play / Pause PendingIntent
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

            // Next Track PendingIntent
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

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(
            context: Context,
            title: String,
            subtitle: String,
            bannerUriString: String?,
            isPlaying: Boolean
        ) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, PlaylistWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            for (id in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, id, title, subtitle, bannerUriString, isPlaying)
            }
        }
    }
}
