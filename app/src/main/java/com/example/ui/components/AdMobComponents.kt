package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import androidx.compose.runtime.Stable
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
import com.example.domain.EarningsEngine
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private fun isRobolectricEnvironment(): Boolean =
  Build.FINGERPRINT.contains("robolectric", ignoreCase = true)

tailrec fun Context.findActivity(): Activity? = when (this) {
  is Activity -> this
  is ContextWrapper -> baseContext.findActivity()
  else -> null
}

sealed interface RewardedAdLoadState {
  data object Idle : RewardedAdLoadState
  data object Loading : RewardedAdLoadState
  data object Ready : RewardedAdLoadState
  data object Showing : RewardedAdLoadState
  data class Unavailable(val message: String) : RewardedAdLoadState
}

data class RewardedAdPaidResult(
  val adUnitId: String,
  val triggerSource: String,
  val scannedCode: String,
  val adValueMicros: Long,
  val userShareMicros: Long,
  val currencyCode: String,
  val precisionType: Int,
  val rewardType: String,
  val rewardAmount: Int,
)

private class ActiveImpressionSession(
  val adUnitId: String,
  val triggerSource: String,
  val scannedCode: String,
  val onRewardCompleted: (RewardedAdPaidResult) -> Unit,
  val onNotCompleted: (String) -> Unit,
) {
  var adValueMicros: Long? = null
  var currencyCode: String = "USD"
  var precisionType: Int = AdValue.PrecisionType.UNKNOWN
  var earnedReward: Boolean = false
  var rewardType: String = "reward"
  var rewardAmount: Int = 1
  var settled: Boolean = false

  fun tryFinalize(forceIfMissingPaidEvent: Boolean = false) {
    if (settled) return
    if (!earnedReward) return
    val valueMicros = adValueMicros
    if (valueMicros == null && !forceIfMissingPaidEvent) return

    settled = true
    val finalAdMicros = (valueMicros ?: 0L).coerceAtLeast(0L)
    val userShare = EarningsEngine.calculateTenPercentShareMicros(finalAdMicros)
    onRewardCompleted(
      RewardedAdPaidResult(
        adUnitId = adUnitId,
        triggerSource = triggerSource,
        scannedCode = scannedCode,
        adValueMicros = finalAdMicros,
        userShareMicros = userShare,
        currencyCode = currencyCode,
        precisionType = precisionType,
        rewardType = rewardType,
        rewardAmount = rewardAmount,
      ),
    )
  }
}

@Stable
class RewardedAdController(
  private val appContext: Context,
  val adUnitId: String,
) {
  private val mainHandler = Handler(Looper.getMainLooper())
  private var loadedAd: RewardedAd? = null
  private var isDisposed = false

  private val _loadState = MutableStateFlow<RewardedAdLoadState>(RewardedAdLoadState.Idle)
  val loadState: StateFlow<RewardedAdLoadState> = _loadState.asStateFlow()

  fun preload() {
    if (isDisposed || isRobolectricEnvironment()) return
    if (loadedAd != null || _loadState.value is RewardedAdLoadState.Loading) return

    _loadState.value = RewardedAdLoadState.Loading
    runCatching {
      RewardedAd.load(
        appContext,
        adUnitId,
        AdRequest.Builder().build(),
        object : RewardedAdLoadCallback() {
          override fun onAdLoaded(ad: RewardedAd) {
            if (isDisposed) return
            loadedAd = ad
            _loadState.value = RewardedAdLoadState.Ready
          }

          override fun onAdFailedToLoad(error: LoadAdError) {
            if (isDisposed) return
            loadedAd = null
            _loadState.value = RewardedAdLoadState.Unavailable(
              "Ad unavailable (${error.code}): ${error.message}",
            )
          }
        },
      )
    }.onFailure { err ->
      loadedAd = null
      _loadState.value = RewardedAdLoadState.Unavailable(
        err.message ?: "Unable to initialize rewarded ad SDK.",
      )
    }
  }

  /**
   * Displays the loaded RewardedAd and attaches both OnPaidEventListener (impression-level ad value)
   * and OnUserEarnedRewardListener. Regardless of callback arrival order, credits 10% of AdValue
   * micros upon successful completion.
   */
  fun showRewardedAd(
    activityContext: Context,
    triggerSource: String,
    scannedCode: String,
    onRewardCompleted: (RewardedAdPaidResult) -> Unit,
    onUnavailableOrDismissed: (String) -> Unit,
  ) {
    if (isRobolectricEnvironment()) {
      onUnavailableOrDismissed("Rewarded ads require Google Play Services on an Android device.")
      return
    }
    val activity = activityContext.findActivity()
    if (activity == null) {
      onUnavailableOrDismissed("Unable to find foreground Activity to display ad.")
      return
    }

    val ad = loadedAd
    if (ad == null) {
      preload()
      val msg = when (val state = _loadState.value) {
        is RewardedAdLoadState.Unavailable -> "${state.message}. Retrying ad load..."
        is RewardedAdLoadState.Loading -> "Loading rewarded ad, please try again in a moment..."
        else -> "Rewarded ad is loading. Please tap again in a second."
      }
      onUnavailableOrDismissed(msg)
      return
    }

    loadedAd = null
    _loadState.value = RewardedAdLoadState.Showing

    val session = ActiveImpressionSession(
      adUnitId = adUnitId,
      triggerSource = triggerSource,
      scannedCode = scannedCode,
      onRewardCompleted = onRewardCompleted,
      onNotCompleted = onUnavailableOrDismissed,
    )

    ad.onPaidEventListener = OnPaidEventListener { adValue ->
      session.adValueMicros = adValue.valueMicros
      session.currencyCode = adValue.currencyCode.ifBlank { "USD" }
      session.precisionType = adValue.precisionType
      session.tryFinalize(forceIfMissingPaidEvent = false)
    }

    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
      override fun onAdDismissedFullScreenContent() {
        _loadState.value = RewardedAdLoadState.Idle
        preload()
        if (!session.settled) {
          if (session.earnedReward) {
            // Wait briefly for late OnPaidEventListener before settling with whatever value arrived
            mainHandler.postDelayed(
              { session.tryFinalize(forceIfMissingPaidEvent = true) },
              600L,
            )
          } else {
            session.onNotCompleted("Ad closed before completion. Watch the full ad to earn your 10% share.")
          }
        }
      }

      override fun onAdFailedToShowFullScreenContent(adError: AdError) {
        _loadState.value = RewardedAdLoadState.Unavailable(adError.message)
        preload()
        if (!session.settled) {
          session.settled = true
          session.onNotCompleted("Could not show rewarded ad: ${adError.message}")
        }
      }
    }

    ad.show(activity) { rewardItem ->
      session.earnedReward = true
      session.rewardType = rewardItem.type
      session.rewardAmount = rewardItem.amount
      session.tryFinalize(forceIfMissingPaidEvent = false)
    }
  }

  fun dispose() {
    isDisposed = true
    loadedAd = null
    mainHandler.removeCallbacksAndMessages(null)
  }
}

@Composable
fun rememberRewardedAdController(
  adUnitId: String? = null,
): RewardedAdController {
  val context = LocalContext.current
  val isInspection = LocalInspectionMode.current
  val resolvedUnitId = remember(adUnitId, context) {
    adUnitId?.takeIf { it.isNotBlank() } ?: context.getString(R.string.admob_rewarded_unit_id)
  }
  val controller = remember(resolvedUnitId, context.applicationContext) {
    RewardedAdController(context.applicationContext, resolvedUnitId)
  }
  DisposableEffect(controller, isInspection) {
    if (!isInspection && !isRobolectricEnvironment()) {
      controller.preload()
    }
    onDispose {
      controller.dispose()
    }
  }
  return controller
}

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
