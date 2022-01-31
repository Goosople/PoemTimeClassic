package io.goosople.poemtime

import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class PoemTimeUtils {
    companion object {
        const val poemTotalNumber = 2369

        fun setEditTextRange(editText: EditText, min: Int, max: Int) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
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
                            editText.setText(value.toString())
                            return
                        }
                        // 判断大小
                        if (value < min) {
                            editText.setText(min.toString())
                        } else if (value > max) {
                            // 移除最老的第一个数试试看
                            var ads = s.toString().substring(1)
                            if (ads.isEmpty()) ads = "0"
                            val ad = ads.toInt()
                            if (ad in min..max) {
                                editText.setText(ad.toString())
                            } else {
                                // 排除输入的最后一个是0
                                var ssr = s.toString().substring(0, s.toString().length - 1)
                                if (ssr.contains("0")) {
                                    // 移除最老的0
                                    ssr = (s.toString().substring(0, s.toString().indexOf("0"))
                                            + s.toString().substring(s.toString().indexOf("0") + 1))
                                    value = Integer.valueOf(ssr)
                                    if (value in min..max) {
                                        editText.setText(value.toString())
                                        return
                                    }
                                }
                                editText.setText(max.toString())
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

        private fun getPoemData(resources: Resources): String {
            val input = resources.openRawResource(R.raw.poemdata)
            val inputStreamReader = BufferedReader(InputStreamReader(input))
            return inputStreamReader.readText()
        }

        fun getPoemContent(num: Int, resources: Resources): String {
            return getKeyString(num, getPoemData(resources), "poem")
        }

        fun getPoemDetail(num: Int, resources: Resources): String {
            val poet = getKeyString(num, getPoemData(resources), "poet")
            val title = getKeyString(num, getPoemData(resources), "title")
            return "《$title》$poet"
        }
    }
}