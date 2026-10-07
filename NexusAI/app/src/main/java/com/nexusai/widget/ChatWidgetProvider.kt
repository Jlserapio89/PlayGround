package com.nexusai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.nexusai.MainActivity
import com.nexusai.R

/** Widget/acceso rápido: abre el chat de NexusAI desde el launcher. */
class ChatWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(ctx: Context, mgr: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val intent = Intent(ctx, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW; data = Uri.parse("nexusai://chat")
            }
            val pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            mgr.updateAppWidget(id, RemoteViews(ctx.packageName, R.layout.widget_chat).apply {
                setOnClickPendingIntent(R.id.widget_root, pi)
            })
        }
    }
}
