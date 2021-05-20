package cn.houkyo.miuidock

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        //Set version
        val textView = findViewById<TextView>(R.id.version)
        val v = "version: " + resources.getString(R.string.version)
        textView.text = v
        val handler = Handler()
        handler.postDelayed({

            if (Utils.isModuleEnable()) {
                val intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            } else {
                Utils.showToast(this, R.string.module_not_enable)
                exitProcess(0)
            }

            finish()
        }, 1500)
    }
}
