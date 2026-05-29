package com.example.data.audio

import com.example.core.data.model.SoundTrack
import com.example.core.data.repository.SoundRepository

class SoundRepositoryImpl : SoundRepository {
    private val catalog = listOf(
        // Focus
        SoundTrack("white_noise", "White Noise", "Focus", "focus/white_noise.ogg", "Waves"),
        SoundTrack("pink_noise", "Pink Noise", "Focus", "focus/pink_noise.ogg", "Hearing"),
        SoundTrack("brown_noise", "Brown Noise", "Focus", "focus/brown_noise.ogg", "Waves"),
        
        // Nature
        SoundTrack("rain_light", "Light Rain", "Nature", "nature/rain_light.ogg", "WaterDrop"),
        SoundTrack("rain_heavy", "Heavy Rain", "Nature", "nature/rain_heavy.ogg", "Thunderstorm"),
        SoundTrack("thunder_rumble", "Distant Thunder", "Nature", "nature/thunder_rumble.ogg", "Thunderstorm"),
        SoundTrack("ocean_waves", "Ocean Waves", "Nature", "nature/ocean_waves.ogg", "BeachAccess"),
        SoundTrack("forest_birds", "Forest & Birds", "Nature", "nature/forest_birds.ogg", "Forest"),
        SoundTrack("river_stream", "River Stream", "Nature", "nature/river_stream.ogg", "Water"),
        
        // Ambient
        SoundTrack("campfire", "Campfire Crackle", "Ambient", "ambient/campfire.ogg", "LocalFireDepartment"),
        SoundTrack("soft_wind", "Soft Wind", "Ambient", "ambient/soft_wind.ogg", "Air"),
        SoundTrack("coffee_shop", "Coffee Shop Hum", "Ambient", "ambient/coffee_shop.ogg", "LocalCafe"),
        SoundTrack("train_tracks", "Train on Tracks", "Ambient", "ambient/train_tracks.ogg", "DirectionsRailway"),
        SoundTrack("fan_electric", "Electric Fan", "Ambient", "ambient/fan_electric.ogg", "Toys"),
        
        // Sleep
        SoundTrack("singing_bowl", "Tibetan Bowl", "Sleep", "sleep/singing_bowl.ogg", "Bedtime"),
        SoundTrack("deep_drone", "Deep Space Drone", "Sleep", "sleep/deep_drone.ogg", "Nightlight"),
        SoundTrack("night_crickets", "Night Crickets", "Sleep", "sleep/night_crickets.ogg", "BugReport"),
        SoundTrack("heartbeat_slow", "Slow Heartbeat", "Sleep", "sleep/heartbeat_slow.ogg", "Favorite")
    )

    override fun getAllSounds(): List<SoundTrack> = catalog

    override fun getSoundsByCategory(category: String): List<SoundTrack> {
        return catalog.filter { it.category.equals(category, ignoreCase = true) }
    }
}
