package uk.co.jakelee.clicketto

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ViewAnimator
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = ScreenCaptureFragment()
            transaction.replace(R.id.sample_content_fragment, fragment)
            transaction.commit()
        }
    }
}