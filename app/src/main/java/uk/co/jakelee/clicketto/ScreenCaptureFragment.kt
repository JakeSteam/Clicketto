package uk.co.jakelee.clicketto

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.nio.ByteBuffer


class ScreenCaptureFragment : Fragment(), View.OnClickListener {
    private var mResultCode = 0
    private var mResultData: Intent? = null
    private var mMediaProjection: MediaProjection? = null
    private var mImageReader: ImageReader? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionManager: MediaProjectionManager? = null
    private var mButtonToggle: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE)
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_screen_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mButtonToggle = view.findViewById<View>(R.id.toggle) as Button
        mButtonToggle!!.setOnClickListener(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mMediaProjectionManager = requireActivity().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode)
            outState.putParcelable(STATE_RESULT_DATA, mResultData)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.toggle -> if (mVirtualDisplay == null) {
                startScreenCapture()
            } else {
                stopScreenCapture()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_SHORT).show()
                return
            }
            mResultCode = resultCode
            mResultData = data
            setUpMediaProjection()
            setUpVirtualDisplay()
        }
    }

    override fun onPause() {
        super.onPause()
        stopScreenCapture()
    }

    override fun onDestroy() {
        super.onDestroy()
        tearDownMediaProjection()
    }

    private fun setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager!!.getMediaProjection(
            mResultCode,
            mResultData!!
        )
    }

    private fun tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

    private fun startScreenCapture() {
        if (mMediaProjection != null) {
            setUpVirtualDisplay()
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection()
            setUpVirtualDisplay()
        } else {
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                mMediaProjectionManager!!.createScreenCaptureIntent(),
                REQUEST_MEDIA_PROJECTION
            )
        }
    }

    private fun setUpVirtualDisplay() {
        val density = resources.displayMetrics.densityDpi
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        mVirtualDisplay = mMediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader!!.surface, null, null
        )
        Log.d("logero", "Let's go")
        var i = 0
        var lastRun = 0L
        mImageReader!!.setOnImageAvailableListener({ imageReader ->
                imageReader?.acquireLatestImage()?.let {
                    val timeSince = System.currentTimeMillis() - lastRun
                    if (timeSince > 60) {
                        lastRun = System.currentTimeMillis()
                        Log.d("logero", "Received image #${++i}: $timeSince since last")
                        val planes: Array<Image.Plane> = it.planes
                        val pixelStride: Int = planes[0].pixelStride
                        val rowPadding: Int = planes[0].rowStride - pixelStride * width
                        val bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                        bitmap.copyPixelsFromBuffer(planes[0].buffer)
                        Log.d("logero", "Image converted to bitmap")
                    } else {
                        Log.d("logero", "Ignoring image")
                    }
                    it.close()
            }
        }, null)
        mButtonToggle!!.setText("Stop")
    }

    private fun stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mVirtualDisplay = null
        mButtonToggle!!.setText("Start")
    }

    companion object {
        private const val STATE_RESULT_CODE = "result_code"
        private const val STATE_RESULT_DATA = "result_data"
        private const val REQUEST_MEDIA_PROJECTION = 1
    }
}