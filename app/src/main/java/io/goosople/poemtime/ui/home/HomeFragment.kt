package io.goosople.poemtime.ui.home

import android.content.SharedPreferences
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.microsoft.cognitiveservices.speech.SpeechConfig
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer
import com.microsoft.cognitiveservices.speech.audio.AudioConfig
import io.goosople.poemtime.PoemTimeUtils
import io.goosople.poemtime.PoemTimeUtils.Companion.getPoemContent
import io.goosople.poemtime.PoemTimeUtils.Companion.getPoemDetail
import io.goosople.poemtime.PoemTimeUtils.Companion.setEditTextRange
import io.goosople.poemtime.databinding.FragmentHomeBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

@Suppress("DEPRECATION")
class HomeFragment : Fragment(), TextToSpeech.OnInitListener {
    private val poemTotalNum = PoemTimeUtils.poemTotalNumber

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        mTextToSpeech = TextToSpeech(activity, this)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val onlineService = sharedPreferences.getBoolean("online_service", false)
        if (onlineService) {
            binding.poemLocal.visibility = View.GONE
            binding.poemLocalDetail.visibility = View.GONE
            binding.con.visibility = View.GONE
            binding.pauseFAB.visibility = View.GONE
            binding.autoPlayFAB.visibility = View.GONE
        } else {
            val tTaskHandler = Handler {
                when (it.what) {
                    1234 -> {
                        var poemNum = sharedPreferences.getInt("poemNum", 0)
                        if (poemNum < poemTotalNum) {
                            poemNum += 1
                            with(sharedPreferences.edit()) {
                                putInt("poemNum", poemNum)
                                commit()
                            }
                            poemInit(poemNum)
                            poemDetailNum(poemNum)
                            tts(sharedPreferences, binding.poemLocal.text.toString())
                        }
                    }
                    else -> {
                        Log.d("Handler", "Unexpected message")
                    }
                }
                false
            }

            var isAutoPlay: Boolean
            binding.pauseFAB.visibility = View.GONE
            binding.autoPlayFAB.setOnClickListener {
                binding.pauseFAB.visibility = View.VISIBLE
                binding.autoPlayFAB.visibility = View.GONE
                isAutoPlay = true
                val time = sharedPreferences.getString("delay", "10")!!.toLong()
                Log.d("time", time.toString())
                Thread {
                    while (isAutoPlay) {
                        val updateTimeMessage: Message = Message.obtain()
                        updateTimeMessage.what = 1234
                        tTaskHandler.sendMessage(updateTimeMessage)
                        Thread.sleep(time * 1000)
                    }
                }.start()
            }
            binding.pauseFAB.setOnClickListener {
                isAutoPlay = false
                binding.pauseFAB.visibility = View.GONE
                binding.autoPlayFAB.visibility = View.VISIBLE
            }

            binding.poem.visibility = View.GONE
            var poemNum = sharedPreferences.getInt("poemNum", 0)
            with(sharedPreferences.edit()) {
                putInt("poemNum", poemNum)
                commit()
            }
            poemDetailNum(poemNum)
            poemInit(poemNum)
            setEditTextRange(binding.poemNum, 1, 2362)

            binding.buttonLast.setOnClickListener {
                poemNum = sharedPreferences.getInt("poemNum", 0)
                if (poemNum > 0) {
                    poemNum -= 1
                    with(sharedPreferences.edit()) {
                        putInt("poemNum", poemNum)
                        commit()
                    }
                    poemDetailNum(poemNum)
                    poemInit(poemNum)
                }
            }
            binding.buttonNext.setOnClickListener {
                poemNum = sharedPreferences.getInt("poemNum", 0)
                if (poemNum < poemTotalNum) {
                    poemNum += 1
                    with(sharedPreferences.edit()) {
                        putInt("poemNum", poemNum)
                        commit()
                    }
                    poemInit(poemNum)
                    poemDetailNum(poemNum)
                }
            }

            binding.poemNum.doAfterTextChanged {
                if (binding.poemNum.text.toString().isNotBlank()) {
                    var num = binding.poemNum.text.toString().toInt()
                    num -= 1
                    with(sharedPreferences.edit()) {
                        putInt("poemNum", num)
                        commit()
                    }
                    poemInit(num)
                }
            }
        }
        return binding.root
    }

    private fun poemInit(num: Int) {
        binding.poemLocal.text = getPoemContent(num, resources)
        binding.poemLocalDetail.text = getPoemDetail(num, resources)
    }

    private fun poemDetailNum(num: Int) {
        val poemNum = num + 1
        binding.poemNum.setText(poemNum.toString())
    }

    private fun tts(sharedPreferences: SharedPreferences, text: String) {
        if (sharedPreferences.getBoolean("aztts", false)) {
            val azureKey = sharedPreferences.getString("key", "")
            if (azureKey!!.isNotBlank()) azureTTS(azureKey, text) else sysTTS(text)
        } else sysTTS(text)
    }

    private fun azureTTS(azureKey: String, text: String) {
        GlobalScope.launch {
            val speechConfig = SpeechConfig.fromSubscription(azureKey, "eastasia")
            SpeechSynthesizer(speechConfig, AudioConfig.fromDefaultSpeakerOutput()).SpeakSsml(
                textToSsml(text)
            )
        }
    }

    private fun textToSsml(text: String): String {
        return "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\" xmlns:mstts=\"http://www.w3.org/2001/mstts\" xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"zh-CN\"><voice name=\"zh-CN-XiaoxiaoNeural\"><mstts:express-as style=\"lyrical\" ><prosody rate=\"-10%\" pitch=\"5%\">$text</prosody></mstts:express-as></voice></speak>"
    }

    private var isSupport = true
    private lateinit var mTextToSpeech: TextToSpeech
    private fun sysTTS(text: String) {
        if (!isSupport) {
            Toast.makeText(
                activity,
                "Your phone don't support TTS, \nplease use Azure TTS.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        //设置播报语音音量（跟随手机音量调节而改变）
        val myHashAlarm = hashMapOf<String, String>()
        myHashAlarm[TextToSpeech.Engine.KEY_PARAM_STREAM] = AudioManager.STREAM_MUSIC.toString()
        /**语音播报
         *QUEUE_ADD：播放完之前的语音任务后才播报本次内容
         *QUEUE_FLUSH：丢弃之前的播报任务，立即播报本次内容
         */
        mTextToSpeech.speak(text, TextToSpeech.QUEUE_ADD, myHashAlarm)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = mTextToSpeech.setLanguage(Locale.SIMPLIFIED_CHINESE)
            // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
            mTextToSpeech.setPitch(1.0f) //(这里推荐默认,不然不同手机可能发声不同，并且异常)
            mTextToSpeech.setSpeechRate(1.0f)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                //系统不支持中文播报
                isSupport = false
            }
            Log.d("TTS", isSupport.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}