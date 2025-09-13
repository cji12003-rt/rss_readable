package com.example.rssreadernews

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NewsReaderService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    // TODO: Implement foreground service logic
}
