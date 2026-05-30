package com.michael.aurasound.data.presets

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.michael.aurasound.core.data.model.MixChannel
import com.michael.aurasound.core.data.model.Preset
import com.michael.aurasound.core.data.repository.PresetRepository
import com.michael.aurasound.core.data.repository.SoundRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.JsonClass
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aurasound_presets")

class PresetRepositoryImpl(
    private val context: Context,
    private val soundRepository: SoundRepository
) : PresetRepository {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val keyPresets = stringPreferencesKey("saved_presets")
    
    private val listType = Types.newParameterizedType(List::class.java, PresetEntity::class.java)
    private val adapter = moshi.adapter<List<PresetEntity>>(listType)

    override fun getPresets(): Flow<List<Preset>> {
        return context.dataStore.data.map { preferences ->
            try {
                val json = preferences[keyPresets] ?: "[]"
                val entities = adapter.fromJson(json) ?: emptyList()
                entities.map { entity ->
                    Preset(
                        id = entity.id,
                        name = entity.name,
                        channels = entity.channels.mapNotNull { ch ->
                            val track = soundRepository.getAllSounds().find { it.id == ch.trackId }
                            if (track != null) MixChannel(track, ch.volume) else null
                        },
                        createdAt = entity.createdAt
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }

    override suspend fun savePreset(name: String, channels: List<MixChannel>) {
        try {
            context.dataStore.edit { preferences ->
                val json = preferences[keyPresets] ?: "[]"
                val entities = adapter.fromJson(json)?.toMutableList() ?: mutableListOf()
                
                val newEntity = PresetEntity(
                    id = UUID.randomUUID().toString(),
                    name = name,
                    channels = channels.map { ChannelEntity(it.soundTrack.id, it.volume) },
                    createdAt = System.currentTimeMillis()
                )
                entities.add(newEntity)
                
                preferences[keyPresets] = adapter.toJson(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deletePreset(id: String) {
        try {
            context.dataStore.edit { preferences ->
                val json = preferences[keyPresets] ?: "[]"
                val entities = adapter.fromJson(json)?.toMutableList() ?: return@edit
                entities.removeAll { it.id == id }
                preferences[keyPresets] = adapter.toJson(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@JsonClass(generateAdapter = true)
data class PresetEntity(
    val id: String,
    val name: String,
    val channels: List<ChannelEntity>,
    val createdAt: Long
)

@JsonClass(generateAdapter = true)
data class ChannelEntity(
    val trackId: String,
    val volume: Float
)
