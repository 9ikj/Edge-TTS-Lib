package icu.xmc.ttsex

import android.app.Application
import icu.xmc.edgettslib.TTS
import icu.xmc.edgettslib.TTSVoice
import timber.log.Timber

class App:Application() {

    override fun onCreate() {
        super.onCreate()
        val voiceItem =
            TTSVoice(this).getVoiceList().firstOrNull { it.ShortName == "zh-CN-XiaoyiNeural" }
        if (voiceItem != null) {
            TTS.getInstance().initialize(this, voiceItem)
        }
        Timber.plant(Timber.DebugTree())
    }
}