package icu.xmc.edgettslib

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import icu.xmc.edgettslib.entity.VoiceItem
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class TTSVoice(context: Context) {

    private val voiceList = arrayListOf<VoiceItem>()

    init {
        fun getJsonFromAsset(context: Context, filename: String): String {
            val stringBuilder = StringBuilder()
            try {
                val inputStream = context.assets.open(filename)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }

                bufferedReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return stringBuilder.toString()
        }

        val voiceListStr = getJsonFromAsset(context, "voicesList.json")
        val type = object : TypeToken<MutableList<VoiceItem>>() {}.type
        val mutableList = Gson().fromJson<MutableList<VoiceItem>>(voiceListStr, type)
        voiceList.addAll(mutableList)
    }


    fun getVoiceList() = voiceList
}