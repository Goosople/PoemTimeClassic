package io.goosople.poemtime

import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController


class FullscreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        when (val fragment = intent.getIntExtra(EXTRA_FRAGMENT,2131296563)) {
            2131296563 -> {
                setContentView(R.layout.activity_fullscreen)
            }
            2131296602 -> {
                setContentView(R.layout.activity_fullscreen_poem)
            }
            2131296566 -> {
                Toast.makeText(this, "Setting CANNOT be fullscreen!\nPlease contact the developer to fix it.", Toast.LENGTH_SHORT).show()
                onBackPressed()
            }
            else -> {
                setContentView(R.layout.activity_fullscreen)
                Toast.makeText(this, "Error: id $fragment is invalid.", Toast.LENGTH_SHORT).show()
            }
        }
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }
}