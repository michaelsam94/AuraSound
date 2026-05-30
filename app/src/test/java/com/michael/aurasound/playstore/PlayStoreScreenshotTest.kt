package com.michael.aurasound.playstore

import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val PHONE = "w360dp-h640dp-xxhdpi"   // 1080×1920
private const val TABLET = "w800dp-h1280dp-xhdpi"  // 1600×2560

@RunWith(RobolectricTestRunner::class)
@Category(PlayStoreScreenshotTests::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class PlayStoreScreenshotTest {

    @Test
    @Config(qualifiers = PHONE)
    fun phone_01_mixer() {
        capturePlayStoreImage("phone/01_mixer.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Mixer)
        }
    }

    @Test
    @Config(qualifiers = PHONE)
    fun phone_02_timer() {
        capturePlayStoreImage("phone/02_timer.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Timer)
        }
    }

    @Test
    @Config(qualifiers = PHONE)
    fun phone_03_presets() {
        capturePlayStoreImage("phone/03_presets.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Presets)
        }
    }

    @Test
    @Config(qualifiers = PHONE)
    fun phone_04_save_preset() {
        capturePlayStoreImage("phone/04_save_preset.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.SavePreset)
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_01_mixer() {
        capturePlayStoreImage("tablet/01_mixer.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Mixer)
        }
    }

    @Test
    @Config(qualifiers = TABLET)
    fun tablet_02_presets() {
        capturePlayStoreImage("tablet/02_presets.png") {
            PlayStoreScreenshotFrame(PlayStoreScene.Presets)
        }
    }
}
