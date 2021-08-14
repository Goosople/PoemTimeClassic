package io.goosople.poemtime

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.preference.PreferenceManager
import io.goosople.poemtime.databinding.FragmentPoemBinding
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


private var _binding: FragmentPoemBinding? = null

// This property is only valid between onCreateView and
// onDestroyView.
private val binding get() = _binding!!


class PoemFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPoemBinding.inflate(inflater, container, false)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val onlineService = sharedPreferences.getBoolean("online_service", true)
        if (onlineService) {
            binding.poemLocal.visibility = View.GONE
            binding.poemLocalDetail.visibility = View.GONE
            binding.con.visibility = View.GONE
        }
        else{
            binding.poem.visibility = View.GONE
            val poemNum = sharedPreferences.getInt("poemNum", 0)
            with(sharedPreferences.edit()) {
                putInt("poemNum", poemNum)
                commit()
            }
            poemDetailNum(poemNum)
            poemInit(poemNum)
            setEditTextRange(binding.poemNum,1,2362)
        }

        binding.buttonLast.setOnClickListener {
            var poemNum = sharedPreferences.getInt("poemNum", 0)
            if (poemNum > 0){
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
            var poemNum = sharedPreferences.getInt("poemNum", 0)
            if (poemNum < 2361){
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

        return binding.root
    }

    private fun setEditTextRange(editText: EditText, min: Int, max: Int) {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                Log.d("beforeTextChanged", "s=$s; start=$start; count=$count; after=$after")
            }

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.d("onTextChanged", "s=$s; start=$start; before=$before; count=$count")
                // 拿到数字
                if (s.toString().isNotEmpty()) {
                    var value = Integer.valueOf(s.toString())
                    if (s.toString().substring(0, 1).contains("0") && value != 0) {
                        // 第一个字为0
                        binding.poemNum.setText(value.toString())
                        return
                    }
                    // 判断大小
                    if (value < min) {
                        binding.poemNum.setText(min.toString())
                    } else if (value > max) {
                        // 移除最老的第一个数试试看
                        var ads = s.toString().substring(1)
                        if (ads.isEmpty()) ads = "0"
                        val ad = ads.toInt()
                        if (ad in min..max) {
                            binding.poemNum.setText(ad.toString())
                        } else {
                            // 排除输入的最后一个是0
                            var ssr = s.toString().substring(0, s.toString().length - 1)
                            if (ssr.contains("0")) {
                                // 移除最老的0
                                ssr = (s.toString().substring(0, s.toString().indexOf("0"))
                                        + s.toString().substring(s.toString().indexOf("0") + 1))
                                value = Integer.valueOf(ssr)
                                if (value in min..max) {
                                    binding.poemNum.setText(value.toString())
                                    return
                                }
                            }
                            binding.poemNum.setText(max.toString())
                        }
                    }
                    return
                }
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable) {
                Log.d("afterTextChanged", "s=$s")
            }
        }
        editText.addTextChangedListener(watcher)
    }

    private fun getKeyString(num: Int, jsonString: String, key: String): String {
        var jsonObj = JSONObject(jsonString)
        val array = jsonObj.getJSONArray("poems")
        jsonObj = array.getJSONObject(num)
        return jsonObj.getString(key)
    }

    private fun getPoemData(): String {
        val input = resources.openRawResource(R.raw.poemdata)
        val inputStreamReader = BufferedReader(InputStreamReader(input))
        return inputStreamReader.readText()
    }

    private fun getPoemContent(num: Int): String {
        return getKeyString(num, getPoemData(), "poem")
    }
    private fun getPoemDetail(num: Int): String {
        val poet = getKeyString(num, getPoemData(), "poet")
        val title = getKeyString(num, getPoemData(), "title")
        return "《$title》$poet"
    }

    private fun poemInit(num: Int){
        binding.poemLocal.text = getPoemContent(num)
        binding.poemLocalDetail.text = getPoemDetail(num)
    }

    private fun poemDetailNum(num: Int){
        val poemNum = num + 1
        binding.poemNum.setText(poemNum.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}