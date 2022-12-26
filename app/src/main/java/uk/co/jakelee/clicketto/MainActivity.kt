package uk.co.jakelee.clicketto

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
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

    private val DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE = 1222

    fun createFloatingWidget(view: View?) {
        //Check if the application has draw over other apps permission or not?
        //This permission is by default available for API<23. But for API > 23
        //you have to ask for the permission in runtime.
        if (!Settings.canDrawOverlays(this)) {
            //If the draw over permission is not available open the settings screen
            //to grant the permission.
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE)
        } else  //If permission is granted start floating widget service
            startFloatingWidgetService()
    }

    /*  Start Floating widget service and finish current activity */
    private fun startFloatingWidgetService() {
        startService(Intent(this@MainActivity, FloatingWidgetService::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == DRAW_OVER_OTHER_APP_PERMISSION_REQUEST_CODE) {
            //Check if the permission is granted or not.
            if (resultCode == RESULT_OK) //If permission granted start floating widget service
                startFloatingWidgetService() else  //Permission is not available then display toast
                Toast.makeText(
                    this,
                    "Permission denied :(",
                    Toast.LENGTH_SHORT
                ).show()
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}