package com.bebena.flutter_camera

import android.app.Activity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry.Registrar
import io.flutter.view.TextureRegistry

/** FlutterCameraPlugin */
public class FlutterCameraPlugin: FlutterPlugin, ActivityAware {

  private final val TAG: String = "FlutterCamera"

  private lateinit var channel : MethodChannel

  private var flutterPluginBinding: FlutterPlugin.FlutterPluginBinding? = null
  private var methodCallHandler: MethodCallHandlerImpl? = null

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val cameraPlugin = FlutterCameraPlugin()
      cameraPlugin.starMethodCall(
              registrar.activity(),
              registrar.messenger(),
              registrar.view()
      )
    }
  }

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    this.flutterPluginBinding = null
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    starMethodCall(
            binding.activity,
            flutterPluginBinding!!.binaryMessenger,
            flutterPluginBinding!!.flutterEngine.renderer
    )
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }

  override fun onDetachedFromActivity() {
    if (methodCallHandler == null) {
      return
    }

    methodCallHandler!!.stopListening()
    methodCallHandler = null
  }

  private fun starMethodCall(
          activity: Activity,
          binaryMessengger: BinaryMessenger,
          textureRegistry: TextureRegistry
  ) {
    methodCallHandler = MethodCallHandlerImpl(
            activity,
            binaryMessengger,
            textureRegistry
    )
  }
}
