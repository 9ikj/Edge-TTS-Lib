package icu.xmc.edgettslib

import android.content.Context
import android.media.MediaPlayer
import icu.xmc.edgettslib.entity.VoiceItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


class TTS private constructor(){

    companion object{
        private var sInstance: TTS? = null
            get() {
                if (field == null) {
                    field = TTS()
                }
                return field
            }

        fun getInstance(): TTS{
            return sInstance!!
        }

    }

    private val voiceList = arrayListOf<VoiceItem>()

    private var headers: HashMap<String, String>? = null
    private var voice: VoiceItem? = null
    private var format = "audio-24khz-48kbitrate-mono-mp3"
    private var findHeadHook = false
    private var voicePitch = "+0Hz"
    private var voiceRate = "+0%"
    private var voiceVolume = "+0%"
    private var storage = ""
    private var mediaPlayer: MediaPlayer? = null
    private var lastPlayMp3:File ?= null

    fun initialize(context: Context,voice:VoiceItem){
        this.voice = voice
        if (voiceList.isEmpty()){
            voiceList.addAll(TTSVoice(context).getVoiceList())
        }
        storage = context.cacheDir.absolutePath
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setOnCompletionListener {
            lastPlayMp3?.delete()
        }
    }

    fun setVoicePitch(voicePitch: Int): TTS {
        this.voicePitch = "+${voicePitch}Hz"
        return this
    }

    fun setVoiceRate(voiceRate: Int): TTS {
        this.voiceRate = "+${voiceRate}%"
        return this
    }

    fun setVoiceVolume(voiceVolume: Int): TTS {
        this.voiceVolume = "+${voiceVolume}%"
        return this
    }

    fun formatMp3(): TTS {
        format = "audio-24khz-48kbitrate-mono-mp3"
        return this
    }

//    fun formatOpus(): TTS {
//        format = "webm-24khz-16bit-mono-opus"
//        return this
//    }

    fun findHeadHook(): TTS {
        findHeadHook = true
        return this
    }

    fun fixHeadHook(): TTS {
        findHeadHook = false
        return this
    }

    fun storage(storage: String): TTS {
        this.storage = storage
        return this
    }
    fun headers(headers: HashMap<String, String>): TTS {
        this.headers = headers
        return this
    }

    fun speak(content: String) {
        if (voice == null) {
            throw RuntimeException("please set voice")
        }
        val str = removeIncompatibleCharacters(content)
        if (str.isNullOrBlank()) {
            throw RuntimeException("invalid content")
        }
        val storageFolder = File(storage)
        if (!storageFolder.exists()) {
            storageFolder.mkdirs()
        }
        val dateStr = dateToString(Date())
        val reqId = uuid()
        val audioFormat = TTSUtil.mkAudioFormat(dateStr,format)
        val ssml = TTSUtil.mkssml(voice!!.Locale, voice!!.Name,content,voicePitch,voiceRate,voiceVolume)
        val ssmlHeadersPlusData = TTSUtil.ssmlHeadersPlusData(reqId, dateStr, ssml)
        if (headers == null) {
            headers = HashMap()
            headers?.put("Origin", UrlConstant.EDGE_ORIGIN)
            headers?.put("Pragma", "no-cache")
            headers?.put("Cache-Control", "no-cache")
            headers?.put("User-Agent", UrlConstant.EDGE_UA)
        }
        var fileName = reqId
        if (format == "audio-24khz-48kbitrate-mono-mp3") {
            fileName += ".mp3"
        }
        else if (format == "webm-24khz-16bit-mono-opus") {
            fileName += ".opus"
        }
        val storageFile = File(storage)
        if (!storageFile.exists()){
            storageFile.mkdirs()
        }
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = TTSWebsocket(
                    UrlConstant.EDGE_URL, headers!!, storage,
                    fileName, findHeadHook
                )
                client.connect()
                while (!client.isOpen) {
                    // wait open
                    Thread.sleep(100)
                }
                client.send(audioFormat)
                client.send(ssmlHeadersPlusData)
                while (client.isOpen) {
                    // wait close
                }
                val file = File(storage,fileName)
                if (file.exists()){
                    mediaPlayer?.reset()
                    mediaPlayer?.setDataSource(file.absolutePath)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                }
                lastPlayMp3 = file
            } catch (e:Throwable){
                e.printStackTrace()
            }
        }

    }

    private fun dateToString(date: Date): String {
        val sdf = SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z (zzzz)",Locale.getDefault())
        return sdf.format(date)
    }

    private fun uuid(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
    private fun removeIncompatibleCharacters(input: String): String? {
        if (input.isBlank()){
            return null
        }
        val output = StringBuilder()
        for (element in input) {
            val code = element.code
            if (code in 0..8 || code in 11..12 || code in 14..31) {
                output.append(" ")
            } else {
                output.append(element)
            }
        }
        return output.toString()
    }
}