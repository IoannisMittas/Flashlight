package com.mittas.flashlight;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

public class CameraUtility {
    private static Camera camera;

    public static void initCamera() {
        if (camera == null) {
            camera = Camera.open();
        }
    }

    public static void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    public static void turnOnOff(Context context, boolean isOn) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                String cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, isOn);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M &&
                camera != null) {
            Camera.Parameters params = camera.getParameters();
            if (isOn) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
            } else {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
            }
        }
    }

}
