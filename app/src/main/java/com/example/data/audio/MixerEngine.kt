package com.example.data.audio

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MixerEngine(private val context: Context) {
    private val handlerThread = HandlerThread("AuraMixerThread").also { it.start() }
    private val handler = Handler(handlerThread.looper)
    
    private val players = mutableMapOf<String, ExoPlayer>()
    private val channelVolumes = mutableMapOf<String, Float>()
    
    private var masterVolume = 1.0f
    private var isPlayingState = false

    fun addChannel(track: SoundTrackData, volume: Float) {
        handler.post {
            if (players.containsKey(track.id)) return@post
            
            try {
                val player = ExoPlayer.Builder(context)
                    .setLooper(handlerThread.looper) // Force ExoPlayer onto the background thread
                    .build()
                    .apply {
                        val uri = Uri.parse("asset:///sounds/${track.assetPath}")
                        setMediaItem(MediaItem.fromUri(uri))
                        repeatMode = Player.REPEAT_MODE_ONE // Perfect gapless looping!
                        this.volume = volume * masterVolume
                        prepare()
                        if (isPlayingState) {
                            play()
                        }
                    }
                players[track.id] = player
                channelVolumes[track.id] = volume
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun removeChannel(trackId: String) {
        handler.post {
            try {
                players[trackId]?.apply {
                    stop()
                    release()
                }
                players.remove(trackId)
                channelVolumes.remove(trackId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVolume(trackId: String, volume: Float) {
        handler.post {
            channelVolumes[trackId] = volume
            players[trackId]?.volume = volume * masterVolume
        }
    }

    fun setMasterVolume(volume: Float) {
        handler.post {
            masterVolume = volume.coerceIn(0f, 1f)
            players.forEach { (id, player) ->
                val trackVol = channelVolumes[id] ?: 1.0f
                player.volume = trackVol * masterVolume
            }
        }
    }

    fun playAll() {
        handler.post {
            isPlayingState = true
            players.values.forEach { it.play() }
        }
    }

    fun pauseAll() {
        handler.post {
            isPlayingState = false
            players.values.forEach { it.pause() }
        }
    }

    fun clearMix() {
        handler.post {
            players.values.forEach {
                try {
                    it.stop()
                    it.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            players.clear()
            channelVolumes.clear()
        }
    }

    fun release() {
        handler.post {
            players.values.forEach {
                try {
                    it.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            players.clear()
            channelVolumes.clear()
            handlerThread.quitSafely()
        }
    }
}

// Inline representation to prevent dependency cycle in compilation
data class SoundTrackData(
    val id: String,
    val assetPath: String
)
