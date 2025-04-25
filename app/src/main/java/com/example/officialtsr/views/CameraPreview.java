package com.example.officialtsr.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error setting camera preview: " + e.getMessage(), e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mHolder.getSurface() == null || mCamera == null) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // Ignore: tried to stop a non-existent preview
        }

        try {
            Camera.Parameters parameters = mCamera.getParameters();
            Camera.Size optimalSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), width, height);
            if (optimalSize != null) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                mCamera.setParameters(parameters);

                // Adjust SurfaceView to maintain aspect ratio
                adjustSurfaceViewSize(optimalSize, width, height);
            }

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage(), e);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.01;
        double targetRatio = (double) width / height;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;

            double diff = Math.abs(size.height - height);
            if (diff < minDiff) {
                optimalSize = size;
                minDiff = diff;
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                double diff = Math.abs(size.height - height);
                if (diff < minDiff) {
                    optimalSize = size;
                    minDiff = diff;
                }
            }
        }

        return optimalSize;
    }

    private void adjustSurfaceViewSize(Camera.Size optimalSize, int width, int height) {
        float aspectRatio = (float) optimalSize.width / optimalSize.height;

        int newWidth = width;
        int newHeight = (int) (width / aspectRatio);

        if (newHeight > height) {
            newHeight = height;
            newWidth = (int) (height * aspectRatio);
        }

        getLayoutParams().width = newWidth;
        getLayoutParams().height = newHeight;
        requestLayout();
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
