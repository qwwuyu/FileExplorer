package com.qwwuyu.file.helper

import android.media.MediaPlayer
import com.qwwuyu.file.R
import com.qwwuyu.file.WApplication
import com.qwwuyu.file.config.ManageConfig

class MediaPlayerHelper {
    private val mediaPlayer: MediaPlayer = MediaPlayer.create(WApplication.context, R.raw.empty).apply {
        setVolume(0.1f, 0.1f)
        setOnCompletionListener { play() }
    }

    fun play() {
        if (!mediaPlayer.isPlaying && ManageConfig.instance.isMedia()) {
            mediaPlayer.start()
        }
    }

    fun stop() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
    }

    fun destroy() {
        mediaPlayer.stop()
        mediaPlayer.release()
    }
}