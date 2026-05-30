import java.io.File
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Properties
import java.util.Random

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

// Enable the Play Store screenshot test category only for Roborazzi / asset-generation
// tasks, so normal `testDebugUnitTest` stays fast and never runs screenshot tests.
if (gradle.startParameter.taskNames.any {
    it.equals("generatePlayStoreAssets", ignoreCase = true) ||
      it.contains("Roborazzi", ignoreCase = true)
  }) {
  extra["screenshot"] = true
}

android {
  namespace = "com.michael.aurasound"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.michael.aurasound"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      // Prefer key.properties (archived in playstore-keys); fall back to env vars for CI.
      val keystorePropertiesFile = rootProject.file("key.properties")
      if (keystorePropertiesFile.exists()) {
        val keystoreProperties = Properties().apply {
          FileInputStream(keystorePropertiesFile).use { load(it) }
        }
        storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
        storePassword = keystoreProperties.getProperty("storePassword")
        keyAlias = keystoreProperties.getProperty("keyAlias")
        keyPassword = keystoreProperties.getProperty("keyPassword")
      } else {
        val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      all {
        val screenshotTests = project.hasProperty("screenshot")
        it.inputs.property("screenshotTestsEnabled", screenshotTests)
        // Heavy settings ONLY for screenshot runs — do not slow normal testDebugUnitTest.
        if (screenshotTests) {
          it.maxParallelForks = 1
          it.maxHeapSize = "2048m"
          it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
        }
        it.useJUnit {
          if (screenshotTests) {
            includeCategories("com.michael.aurasound.playstore.PlayStoreScreenshotTests")
          } else {
            excludeCategories("com.michael.aurasound.playstore.PlayStoreScreenshotTests")
          }
        }
      }
    }
  }
}

roborazzi {
  outputDir.set(file("${rootProject.projectDir}/play-store"))
}

tasks.register("generatePlayStoreAssets") {
  group = "publishing"
  description = "Generate Play Store screenshots, feature graphic, and app icon via Roborazzi"
  dependsOn("recordRoborazziDebug")
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.media3.exoplayer)
  implementation(libs.media3.session)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

val soundsOutputDir = layout.projectDirectory.dir("src/main/assets/sounds")

tasks.register("generateAmbientSounds") {
  val targetDir = soundsOutputDir
  doFirst {
    val assetsDir = targetDir.asFile
    val categories = listOf("focus", "nature", "ambient", "sleep")
    categories.forEach { File(assetsDir, it).mkdirs() }

    val sampleRate = 11025
    val duration = 2.5
    val numSamples = (sampleRate * duration).toInt()
    val random = Random(1337)

    fun writeWav(category: String, name: String, generator: (Int, Double) -> Short) {
      val outputFile = File(assetsDir, "$category/$name.ogg")
      val bos = ByteArrayOutputStream()
      val subChunk2Size = numSamples * 2
      val chunkSize = 36 + subChunk2Size

      // RIFF header
      bos.write("RIFF".toByteArray())
      bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(chunkSize).array())
      bos.write("WAVE".toByteArray())
      bos.write("fmt ".toByteArray())
      bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(16).array())
      bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array())
      bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(1).array())
      bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate).array())
      bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(sampleRate * 2).array())
      bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(2).array())
      bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(16).array())
      bos.write("data".toByteArray())
      bos.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(subChunk2Size).array())

      for (i in 0 until numSamples) {
        val t = i.toDouble() / sampleRate
        val pcm = generator(i, t)
        bos.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(pcm).array())
      }

      outputFile.writeBytes(bos.toByteArray())
    }

    // 1. Focus group
    writeWav("focus", "white_noise") { _, _ ->
      (random.nextInt(12000) - 6000).toShort()
    }
    
    // Pink noise: Paul Kellet's filter (true ~ -3 dB/octave rolloff)
    var pb0 = 0.0
    var pb1 = 0.0
    var pb2 = 0.0
    writeWav("focus", "pink_noise") { _, _ ->
      val white = random.nextDouble() * 2.0 - 1.0
      pb0 = 0.99765 * pb0 + white * 0.0990460
      pb1 = 0.96300 * pb1 + white * 0.2965164
      pb2 = 0.57000 * pb2 + white * 1.0526913
      val pink = pb0 + pb1 + pb2 + white * 0.1848
      (pink * 9000).toInt().coerceIn(-32768, 32767).toShort()
    }

    // Brown noise (leaky integration of white noise)
    var brownValue = 0.0
    writeWav("focus", "brown_noise") { _, _ ->
      val white = random.nextDouble() * 2.0 - 1.0
      brownValue = 0.99 * brownValue + 0.01 * white
      (brownValue * 60000).toInt().coerceIn(-32768, 32767).toShort()
    }

    // The remaining 15 tracks (nature / ambient / sleep) are real CC0 field
    // recordings fetched from Freesound by scripts/fetch_sounds.py — synthesized
    // approximations sounded artificial, so they are no longer generated here.
    // White/pink/brown noise stay synthesized: that is mathematically exact and
    // cleaner than any recording.
  }
}

// Ensure the generate task runs before compiling the project assets
tasks.named("preBuild") {
  dependsOn("generateAmbientSounds")
}
