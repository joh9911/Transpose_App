package com.myFile.transpose.data.model

data class AudioEffectsDataModel(
    val pitchValue: Int,
    val tempoValue: Int,
    val bassBoostValue: Int,
    val loudnessEnhancerValue: Int,
    val virtualizerValue: Int,
    val presetReverbIndexValue: Int,
    val presetReverbSendLevel: Int,
    val isPresetReverbEnabled: Boolean,
    val equalizerIndexValue: Int,
    val isEqualizerEnabled: Boolean
)