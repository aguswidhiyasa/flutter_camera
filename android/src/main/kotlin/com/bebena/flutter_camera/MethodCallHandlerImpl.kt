package com.bebena.flutter_camera

import android.app.Activity
import android.hardware.Camera
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.view.TextureRegistry
import java.lang.Exception

final class MethodCallHandlerImpl(
        val activity: Activity,
        val messenger: BinaryMessenger,
        val textureRegistry: TextureRegistry
): MethodChannel.MethodCallHandler {

    private var methodChannel: MethodChannel = MethodChannel(messenger, "bebena/flutter_camera")
    private var imageStreamChannel: EventChannel = EventChannel(messenger, "bebena/flutter_camera/stream")

    private var flutterCamera: com.bebena.flutter_camera.Camera? = null

    init {
        methodChannel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "init" -> {
                initCamera(call, result)
            }
            "takePickture" -> {
                flutterCamera!!.takePicture(result)
            }
            "close" -> {
                flutterCamera!!.close()
                result.success(null)
            }
            else -> result.notImplemented()
        }
    }

    private fun initCamera(call: MethodCall, result: MethodChannel.Result) {
        val flutterSurfaceTexture: TextureRegistry.SurfaceTextureEntry = textureRegistry.createSurfaceTexture()
        val dartMessenger = DarMessenger(messenger, flutterSurfaceTexture.id())

        val resolutionPreset: String? = call.argument("cameraPreset")

        flutterCamera = Camera(
                activity,
                flutterSurfaceTexture,
                dartMessenger,
                resolutionPreset!!
        )
        flutterCamera!!.requestPermission()
        flutterCamera!!.open(call, result)
    }

    fun stopListening() {
        methodChannel.setMethodCallHandler(null)
    }

}