package com.qwwuyu.file.helper

import android.media.MediaPlayer
import com.qwwuyu.file.R
import com.qwwuyu.file.WApplication
import com.qwwuyu.file.config.ManageConfig
import com.qwwuyu.file.utils.LogUtils

class MediaPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null

    fun play() {
        stop()
        if (ManageConfig.instance.isMedia()) {
            LogUtils.i("play")
            mediaPlayer = MediaPlayer.create(WApplication.context, R.raw.empty).apply {
                setVolume(0.1f, 0.1f)
                setOnCompletionListener { play() }
                setOnErrorListener { mp, what, extra ->
                    LogUtils.i("playError:what=$what extra=$extra")
                    mp.release()
                    true
                }
            }
        }
        mediaPlayer?.start()
    }

    fun stop() {
        if (mediaPlayer != null) LogUtils.i("stop")
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun destroy() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}