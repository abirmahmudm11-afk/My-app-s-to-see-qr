package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

private fun isRobolectricEnvironment(): Boolean =
  Build.FINGERPRINT.contains("robolectric", ignoreCase = true)

@Composable
fun AdMobBannerSlot(
  modifier: Modifier = Modifier,
  adUnitId: String? = null,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val isInspection = LocalInspectionMode.current
  val isTestEnv = remember { isRobolectricEnvironment() }
  val resolvedUnitId = remember(adUnitId, context) {
    adUnitId?.takeIf { it.isNotBlank() } ?: context.getString(R.string.admob_banner_unit_id)
  }

  var adLoaded by remember(resolvedUnitId) { mutableStateOf(false) }
  var adFailed by remember(resolvedUnitId) { mutableStateOf(false) }

  if (isInspection || isTestEnv || adFailed) {
    return
  }

  BoxWithConstraints(
    modifier = modifier
      .fillMaxWidth()
      .testTag("admob_banner_slot"),
  ) {
    val widthDp = maxWidth.value.toInt().coerceIn(320, 728)
    val adView = remember(resolvedUnitId, widthDp) {
      runCatching {
        AdView(context).apply {
          this.adUnitId = resolvedUnitId
          val adaptiveSize = runCatching {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
          }.getOrElse { AdSize.BANNER }
          setAdSize(adaptiveSize)
          adListener = object : AdListener() {
            override fun onAdLoaded() {
              adLoaded = true
              adFailed = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
              adLoaded = false
              adFailed = true
            }
          }
          loadAd(AdRequest.Builder().build())
        }
      }.getOrNull()
    }

    if (adView != null) {
      DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
          when (event) {
            Lifecycle.Event.ON_RESUME -> runCatching { adView.resume() }
            Lifecycle.Event.ON_PAUSE -> runCatching { adView.pause() }
            else -> Unit
          }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
          lifecycleOwner.lifecycle.removeObserver(observer)
          runCatching { adView.destroy() }
        }
      }

      Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          if (adLoaded) {
            Text(
              text = "Sponsored",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .wrapContentHeight(),
            contentAlignment = Alignment.Center,
          ) {
            AndroidView(
              factory = { adView },
              modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            )
          }
        }
      }
    }
  }
}
