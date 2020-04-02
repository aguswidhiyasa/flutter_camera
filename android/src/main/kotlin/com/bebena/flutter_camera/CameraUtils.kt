package com.bebena.flutter_camera

import android.media.CamcorderProfile
import android.os.Build

enum class ResolutionPreset {
    low, medium, high, veryHigh, ultraHigh, max
}

class CameraUtils {
    companion object {
        fun getCameraProfilePreset(cameraId: Int, resolutionPreset: ResolutionPreset): CamcorderProfile? {
            when (resolutionPreset) {
                ResolutionPreset.low -> {
                    if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_QVGA)) {
                        return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_QVGA)
                    }
                }
                ResolutionPreset.medium -> {
                    if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_480P)) {
                        return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_480P)
                    }
                }
                ResolutionPreset.high -> {
                    if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_720P)) {
                        return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_720P)
                    }
                }
                ResolutionPreset.veryHigh -> {
                    if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
                        return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P)
                    }
                }
                ResolutionPreset.ultraHigh -> {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_2160P)) {
                            return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_2160P)
                        }
                    } else {
                        if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_1080P)) {
                            return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_1080P)
                        }
                    }
                }
                ResolutionPreset.max -> {
                    if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_HIGH)) {
                        return CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_HIGH)
                    }
                }
                else -> {
                    return if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
                        CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW)
                    } else {
                        throw IllegalArgumentException(
                                "No capture session available for current capture session.")
                    }
                }
            }

            return if (CamcorderProfile.hasProfile(cameraId, CamcorderProfile.QUALITY_LOW)) {
                CamcorderProfile.get(cameraId, CamcorderProfile.QUALITY_LOW)
            } else {
                throw IllegalArgumentException(
                        "No capture session available for current capture session.")
            }
        }
    }
}

data class Size(
        val width: Int,
        val height: Int
) {

}