package icu.xmc.ttsex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import icu.xmc.edgettslib.TTS
import icu.xmc.ttsex.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=  ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.speak.setOnClickListener {
            TTS.getInstance().findHeadHook().speak("你好啊")
        }
    }
}