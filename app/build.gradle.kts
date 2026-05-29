import java.io.File
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.aurasound.amb"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
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
  testOptions { unitTests { isIncludeAndroidResources = true } }
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
    
    // Pink noise approximation (cumulative noise with decay)
    var pinkValue = 0.0
    writeWav("focus", "pink_noise") { _, _ ->
      val white = random.nextDouble() * 2.0 - 1.0
      pinkValue = 0.95 * pinkValue + 0.05 * white
      (pinkValue * 28000).toInt().coerceIn(-32768, 32767).toShort()
    }

    // Brown noise (leaky integration of white noise)
    var brownValue = 0.0
    writeWav("focus", "brown_noise") { _, _ ->
      val white = random.nextDouble() * 2.0 - 1.0
      brownValue = 0.99 * brownValue + 0.01 * white
      (brownValue * 60000).toInt().coerceIn(-32768, 32767).toShort()
    }

    // 2. Nature group
    var rainVal = 0.0
    writeWav("nature", "rain_light") { _, _ ->
      val white = random.nextDouble() * 2.0 - 1.0
      rainVal = 0.93 * rainVal + 0.07 * white
      val click = if (random.nextDouble() > 0.998) (random.nextInt(16000) - 8000) else 0
      (rainVal * 20000 + click).toInt().coerceIn(-32768, 32767).toShort()
    }

    var heavyRainVal = 0.0
    writeWav("nature", "rain_heavy") { _, t ->
      val white = random.nextDouble() * 2.0 - 1.0
      heavyRainVal = 0.98 * heavyRainVal + 0.02 * white
      val rumble = Math.sin(2 * Math.PI * 45 * t) * 2000
      (heavyRainVal * 30000 + rumble).toInt().coerceIn(-32768, 32767).toShort()
    }

    writeWav("nature", "thunder_rumble") { i, t ->
      val strike = Math.sin(2 * Math.PI * 2 * t)
      val amp = Math.pow(Math.sin(2 * Math.PI * 0.2 * t + 0.5), 4.0) * 8000
      val lowNoise = (random.nextInt(3000) - 1500) * amp / 8000
      (Math.sin(2 * Math.PI * 35 * t) * amp + lowNoise).toInt().coerceIn(-32768, 32767).toShort()
    }

    writeWav("nature", "ocean_waves") { _, t ->
      val modulation = 0.5 + 0.5 * Math.sin(2 * Math.PI * 0.4 * t) // 2.5s wave cycle
      val white = random.nextDouble() * 2.0 - 1.0
      (white * 15000 * modulation).toInt().toShort()
    }

    writeWav("nature", "forest_birds") { i, t ->
      val background = (random.nextInt(2000) - 1000)
      val birdActive = (t % 1.2) < 0.25
      val birdSound = if (birdActive) {
        val sweepFreq = 1800.0 + 600.0 * Math.sin(2 * Math.PI * 22 * t)
        val phase = t * sweepFreq
        (Math.sin(phase) * 6000).toInt()
      } else 0
      (background + birdSound).coerceIn(-32768, 32767).toShort()
    }

    writeWav("nature", "river_stream") { _, t ->
      val modulation = 0.8 + 0.2 * Math.sin(2 * Math.PI * 8.0 * t) // rapid flutter
      val white = random.nextDouble() * 2.0 - 1.0
      (white * 12000 * modulation).toInt().toShort()
    }

    // 3. Ambient group
    writeWav("ambient", "campfire") { _, t ->
      val hum = (random.nextInt(3000) - 1500)
      val snap = if (random.nextDouble() > 0.997) (random.nextInt(18000) - 9000) else 0
      (hum + snap).coerceIn(-32768, 32767).toShort()
    }

    writeWav("ambient", "soft_wind") { _, t ->
      val modulation = 0.65 + 0.35 * Math.sin(2 * Math.PI * 0.3 * t)
      val white = random.nextDouble() * 2.0 - 1.0
      (white * 10000 * modulation).toInt().toShort()
    }

    writeWav("ambient", "coffee_shop") { _, t ->
      val voice1 = Math.sin(2 * Math.PI * 180 * t) * 1500
      val voice2 = Math.sin(2 * Math.PI * 250 * t) * 1200
      val clink = if (random.nextDouble() > 0.999) (Math.sin(2 * Math.PI * 2500 * t) * 6000).toInt() else 0
      val noise = (random.nextInt(3000) - 1500)
      (voice1 + voice2 + clink + noise).toInt().coerceIn(-32768, 32767).toShort()
    }

    writeWav("ambient", "train_tracks") { _, t ->
      // Rhythmic train "clack-clack" (approx 1.5 Hz)
      val cycleProgress = t % 0.66
      val isClack = cycleProgress < 0.05 || (cycleProgress in 0.15..0.20)
      val rumble = Math.sin(2 * Math.PI * 55 * t) * 4000
      val clackValue = if (isClack) (random.nextInt(8000) - 4000) else 0
      (rumble + clackValue).toInt().coerceIn(-32768, 32767).toShort()
    }

    writeWav("ambient", "fan_electric") { _, t ->
      val rotor1 = Math.sin(2 * Math.PI * 58 * t) * 6000
      val rotor2 = Math.sin(2 * Math.PI * 116 * t) * 3000
      val airNoise = (random.nextInt(5000) - 2500)
      (rotor1 + rotor2 + airNoise).toInt().coerceIn(-32768, 32767).toShort()
    }

    // 4. Sleep group
    writeWav("sleep", "singing_bowl") { _, t ->
      val decay = Math.max(0.0, 1.0 - t / duration)
      val resonance1 = Math.sin(2 * Math.PI * 293.66 * t) * 8000 // D4
      val resonance2 = Math.sin(2 * Math.PI * 440.0 * t) * 4000  // A4
      val vibrato = 0.8 + 0.2 * Math.sin(2 * Math.PI * 3.0 * t)
      ((resonance1 + resonance2) * decay * vibrato).toInt().toShort()
    }

    writeWav("sleep", "deep_drone") { _, t ->
      val f1 = Math.sin(2 * Math.PI * 65.41 * t) * 6000 // C2
      val f2 = Math.sin(2 * Math.PI * 98.0 * t) * 4000  // G2
      val f3 = Math.sin(2 * Math.PI * 130.81 * t) * 2000 // C3
      (f1 + f2 + f3).toInt().toShort()
    }

    writeWav("sleep", "night_crickets") { _, t ->
      // crickets chirp periodically
      val chirpCycle = t % 0.4
      val isChirp = chirpCycle < 0.12
      val chirp = if (isChirp) {
        val freq = 3800 * (1 + 0.05 * Math.sin(2 * Math.PI * 150 * t))
        (Math.sin(2 * Math.PI * freq * t) * 6000).toInt()
      } else 0
      chirp.toShort()
    }

    writeWav("sleep", "heartbeat_slow") { _, t ->
      val cycleProgress = t % 1.2 // 50 bpm
      val isLub = cycleProgress in 0.0..0.12
      val isDub = cycleProgress in 0.2..0.32
      val amp = if (isLub) {
        val s = Math.sin(2 * Math.PI * (cycleProgress / 0.12))
        s * 15000
      } else if (isDub) {
        val s = Math.sin(2 * Math.PI * ((cycleProgress - 0.2) / 0.12))
        s * 10000
      } else 0.0
      val sound = amp * Math.sin(2 * Math.PI * 48 * t)
      sound.toInt().toShort()
    }
  }
}

// Ensure the generate task runs before compiling the project assets
tasks.named("preBuild") {
  dependsOn("generateAmbientSounds")
}
