package com.michael.aurasound.playstore

import androidx.compose.runtime.Composable
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziComposeOptions
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.activityTheme
import com.github.takahirom.roborazzi.captureRoboImage
import com.michael.aurasound.R

private val playStoreCaptureOptions = RoborazziOptions(
    captureType = RoborazziOptions.CaptureType.Screenshot(),
)

/**
 * Captures a composable to `play-store/<outputPath>` at the active `@Config(qualifiers)` size.
 *
 * Uses the composable `captureRoboImage { }` API (not `onRoot().captureRoboImage()`), which
 * renders real pixels via a real activity instead of a Robolectric semantics dump.
 * The output dir is configured to `${rootProject.projectDir}/play-store`, so paths are
 * relative to the app module: `../play-store/<outputPath>`.
 */
@OptIn(ExperimentalRoborazziApi::class)
fun capturePlayStoreImage(
    outputPath: String,
    content: @Composable () -> Unit,
) {
    captureRoboImage(
        filePath = "../play-store/$outputPath",
        roborazziOptions = playStoreCaptureOptions,
        roborazziComposeOptions = RoborazziComposeOptions {
            activityTheme(R.style.Theme_MyApplication)
        },
        content = content,
    )
}
