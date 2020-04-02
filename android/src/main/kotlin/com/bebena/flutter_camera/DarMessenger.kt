package com.bebena.flutter_camera

import android.text.TextUtils
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler

class DarMessenger(binaryMessenger: BinaryMessenger, eventChannelId: Long) {

    enum class EventType {
        ERROR,
        CAMERA_CLOSING
    }

    private var eventSink: EventChannel.EventSink? = null

    init {
        EventChannel(binaryMessenger, "bebena/flutter_camera/cameraEvents$eventChannelId").setStreamHandler(
            object: StreamHandler {
                override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                    eventSink = events
                }

                override fun onCancel(arguments: Any?) {
                    eventSink = null
                }
            }
        )
    }

    fun sendCameraClosingEvent() {
        send(EventType.CAMERA_CLOSING, null)
    }

    fun send(eventType: EventType, description: String?) {
        if (eventSink == null) return

        var event: MutableMap<String, String> = HashMap()
        event["eventType"] = eventType.toString().toLowerCase()

        if (eventType == EventType.ERROR && !TextUtils.isEmpty(description)) {
            event["errorDescription"] = description!!
        }

        eventSink!!.success(event)
    }
}