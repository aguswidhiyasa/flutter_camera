package com.bebena.flutter_camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.Camera.Size
import android.media.CamcorderProfile
import android.os.Environment
import android.util.Log
import android.view.OrientationEventListener
import android.view.Surface
import androidx.core.app.ActivityCompat
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import io.flutter.view.TextureRegistry
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class Camera(
        private var activity: Activity,
        private var flutterTexture: TextureRegistry.SurfaceTextureEntry,
        private var dartMessenger: DarMessenger,
        private var resolutionPreset: String
) : PluginRegistry.RequestPermissionsResultListener {

    private var camera: Camera? = null

    val REQUEST_CAMERA_PERMISSION_REQ = 1808
    var cameraPermissionState: Int = 0

    private var orientationEventListener: OrientationEventListener? = null
    private var currentOrientation: Int = 90
    private var camcoderProfile: CamcorderProfile? = null
    private var previewSize: com.bebena.flutter_camera.Size? = null

    private var cameraId: Int = 0

    val MEDIA_TYPE_IMAGE = 1
    val MEDIA_TYPE_VIDEO = 2

    init {
        orientationEventListener = object : OrientationEventListener(activity.applicationContext) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) {
                    return
                }

                if (camera == null) {
                    Log.d("flutter_camera", "Camera NULL")
                    return
                }

                when (orientation) {
                    in 45..134 -> changeOrientation(0) // -
                    in 135..224-> changeOrientation(270) // |
                    in 225..314 -> changeOrientation(180) // -
                    else -> changeOrientation(90)
                }
            }
        }
        orientationEventListener!!.enable()
    }

    fun changeOrientation(degree: Int) {
        camera!!.setDisplayOrientation(degree);
    }

    fun open(call: MethodCall, result: MethodChannel.Result) {
        val cameraFacing = call.argument<String>("cameraLens")

        var getCameraFacing = 0
        if (cameraFacing != null) {
            getCameraFacing = when(cameraFacing) {
                "front" -> Camera.CameraInfo.CAMERA_FACING_FRONT
                "back" -> Camera.CameraInfo.CAMERA_FACING_BACK
                else -> Camera.CameraInfo.CAMERA_FACING_BACK
            }
        }

        var cameraCount: Int = 0
        val cameraInfo = Camera.CameraInfo()
        cameraCount = Camera.getNumberOfCameras()
        for (x in 0 .. cameraCount) {
            Camera.getCameraInfo(x, cameraInfo)
            if (cameraInfo.facing == getCameraFacing) {
                this.camera = initCamera(result, x)
                cameraId = x
                break
            }
        }

        val resolutionPreset = ResolutionPreset.valueOf(this.resolutionPreset)
        camcoderProfile = CameraUtils.getCameraProfilePreset(cameraId, resolutionPreset)
        previewSize = Size(camcoderProfile!!.videoFrameWidth, camcoderProfile!!.videoFrameHeight)

        // Checking camera is added or not a
            this.camera!!.setDisplayOrientation(currentOrientation)
            startPreview()
            val res = HashMap<String, Any>()
            res["textureId"] = flutterTexture.id()
            result.success(res)
    }

    private fun initCamera(result: MethodChannel.Result, cameraInfo: Int = 0): Camera? {
        return try {
            Camera.open(cameraInfo)
        } catch (e: Exception) {
            result.error("errorCamera", e.message.toString(), null)
            null
        }
    }

    fun takePicture(result: MethodChannel.Result) {
        var path: String = ""
        val mPicture = Camera.PictureCallback { data, _ ->
            val pictureFile: File = getOutputMediaFile(MEDIA_TYPE_IMAGE) ?: run {
                Log.d("flutter_camera", "Error creating media File, check storage permission")
                return@PictureCallback
            }

            path = pictureFile.path

            try {
                val fos = FileOutputStream(pictureFile)
                fos.write(data)
                fos.close()
            } catch (e: FileNotFoundException) {
                Log.d("flutter_camera", "File not found")
            } catch (e: IOException) {
                Log.d("flutter_camera", "Error accessing file")
            }
        }

        camera!!.takePicture(null, null, mPicture)

        val res: MutableMap<String, Any> = HashMap()
        res["filePath"] = path

        result.success( res)
    }

    fun close() {
        camera?.also {
            it.release()
            camera = null
        }
    }

    @SuppressLint("Recycle")
    fun startPreview() {
        var texture = this.flutterTexture.surfaceTexture()
        texture.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)


        camera!!.setPreviewTexture(texture)
        camera!!.startPreview()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getOutputMediaFile(type: Int): File? {

        Log.d("flutter_camera", "StState: ${Environment.getExternalStorageState()}")

        val mediaStorageDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Latihan"
        )

        Log.d("flutter_camera", mediaStorageDir.path)

        mediaStorageDir.apply {
            if (!exists()) {
                if (!mkdir()) {
                    Log.d("flutter_camera", "Failed to create Directory")
                    return null
                }
            }
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when(type) {
            MEDIA_TYPE_IMAGE -> {
                File("${mediaStorageDir.path}${File.separator}IMG_$timestamp.jpg")
            }
            else -> null
        }
    }

    fun requestPermission() {
        if (checkPermission() && checkExternalStoragePermission() && checkWriteStoragePermission()) {
            // Success
            return
        }

//        if (checkExternalStoragePermission()) {
//            return
//        }
//
//        if (checkWriteStoragePermission()) {
//            return
//        }

        ActivityCompat.requestPermissions(activity, arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ), REQUEST_CAMERA_PERMISSION_REQ)
    }

    private fun checkPermission(): Boolean {
        this.cameraPermissionState = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        )
        return this.cameraPermissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun checkExternalStoragePermission(): Boolean {
        val storagePermission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        return storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun checkWriteStoragePermission(): Boolean {
        val storagePermission = ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return storagePermission == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>?, grantResults: IntArray): Boolean {
        return when (requestCode) {
            REQUEST_CAMERA_PERMISSION_REQ -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // start camera
                } else {
                    // When NOt granted
                }
                false
            }
            else -> {
                false
            }
        }
    }
}