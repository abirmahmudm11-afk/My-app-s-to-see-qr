package com.example.ui.components

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

/**
 * Plays a short, crisp two-note confirmation beep on a single background executor
 * immediately after a barcode is detected, without blocking the Compose or CameraX threads.
 */
class BarcodeFeedbackPlayer {
  private val audioExecutor = Executors.newSingleThreadExecutor()

  fun playSuccessBeep() {
    runCatching {
      audioExecutor.execute {
        val playedWithTrack = playSynthesizedChime()
        if (!playedWithTrack) {
          playFallbackTone()
        }
      }
    }
  }

  private fun playSynthesizedChime(): Boolean {
    val sampleRate = 22050
    val firstSamples = (sampleRate * 0.058).toInt() // ~58ms C6 (1046.5 Hz)
    val secondSamples = (sampleRate * 0.082).toInt() // ~82ms E6 (1318.5 Hz)
    val totalSamples = firstSamples + secondSamples
    val pcm = ShortArray(totalSamples)

    for (i in 0 until firstSamples) {
      val env = smoothEnvelope(i, firstSamples)
      val sample = sin(2.0 * PI * 1046.5 * i / sampleRate) * env * 0.35
      pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
    }
    for (i in 0 until secondSamples) {
      val env = smoothEnvelope(i, secondSamples)
      val sample = sin(2.0 * PI * 1318.5 * i / sampleRate) * env * 0.38
      pcm[firstSamples + i] = (sample * Short.MAX_VALUE).toInt().toShort()
    }

    val minBuffer = AudioTrack.getMinBufferSize(
      sampleRate,
      AudioFormat.CHANNEL_OUT_MONO,
      AudioFormat.ENCODING_PCM_16BIT,
    )
    if (minBuffer <= 0) return false

    val bufferBytes = maxOf(minBuffer, totalSamples * 2)
    var track: AudioTrack? = null
    return try {
      track = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build(),
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build(),
        )
        .setTransferMode(AudioTrack.MODE_STATIC)
        .setBufferSizeInBytes(bufferBytes)
        .build()

      track.write(pcm, 0, pcm.size)
      track.play()
      Thread.sleep(160L)
      true
    } catch (_: Throwable) {
      false
    } finally {
      runCatching { track?.stop() }
      runCatching { track?.release() }
    }
  }

  private fun playFallbackTone() {
    var toneGen: ToneGenerator? = null
    try {
      toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 75)
      toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 130)
      Thread.sleep(150L)
    } catch (_: Throwable) {
      // Device audio muted or unavailable in test container
    } finally {
      runCatching { toneGen?.release() }
    }
  }

  private fun smoothEnvelope(index: Int, length: Int): Double {
    val ramp = (length * 0.18).coerceAtLeast(1.0)
    return when {
      index < ramp -> index / ramp
      index > length - ramp -> (length - index).coerceAtLeast(0) / ramp
      else -> 1.0
    }
  }

  fun release() {
    runCatching { audioExecutor.shutdownNow() }
  }
}
