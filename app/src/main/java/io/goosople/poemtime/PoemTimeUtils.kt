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
            editText.addTextChangedListener(object : TextWatcher {
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
                        val value = Integer.valueOf(s.toString())
                        if (s.toString().substring(0, 1).contains("0") && value != 0) {
                            // 第一个字为0
                            editText.setText(value.toString())
                            return
                        }
                        // 判断大小
                        if (value < min) {
                            editText.setText(min.toString())
                        } else if (value > max) {
                            editText.setText(max.toString())
                        }
                        return
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun afterTextChanged(s: Editable) {
                    Log.d("afterTextChanged", "s=$s")
                }
            })
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
            return "《${getPoemTitle(num, resources)}》$poet"
        }

        private fun getPoemTitle(num: Int, resources: Resources): String {
            return getKeyString(num, getPoemData(resources), "title")
        }

        fun search(query: String, resources: Resources): Int {
            Log.d("search", query)
            for (i in 0..poemTotalNumber)
                if (getPoemTitle(i, resources).contains(query)) {
                    Log.d("search", i.toString())
                    return i
                }
            return -1
        }
    }
}