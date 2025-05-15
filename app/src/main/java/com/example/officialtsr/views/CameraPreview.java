package com.example.officialtsr.views;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.Surface;

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
                setCameraDisplayOrientation();
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

                // Cập nhật orientation khi thay đổi kích thước
                setCameraDisplayOrientation();

                // Điều chỉnh kích thước SurfaceView
                adjustSurfaceView(optimalSize, width, height);
            }

            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage(), e);
        }
    }    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        if (sizes == null) return null;

        final double ASPECT_TOLERANCE = 0.2; // Tăng tolerance để tìm kích thước phù hợp hơn
        double targetRatio = (double) width / height;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = height;

        Log.d(TAG, "Finding optimal size. Target: " + width + "x" + height + " ratio: " + targetRatio);
        
        // Ưu tiên tìm kích thước có cùng tỉ lệ với màn hình
        for (Camera.Size size : sizes) {
            Log.d(TAG, "Checking size " + size.width + "x" + size.height);
            double ratio = (double) size.width / size.height;
            
            if (Math.abs(ratio - targetRatio) <= ASPECT_TOLERANCE) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                    Log.d(TAG, "Found better size: " + size.width + "x" + size.height);
                }
            }
        }

        // Nếu không tìm được kích thước phù hợp, thử tìm kích thước với chiều cao gần nhất
        if (optimalSize == null) {
            Log.d(TAG, "No optimal size with target ratio, looking for closest match");
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                    Log.d(TAG, "Found size by height: " + size.width + "x" + size.height);
                }
            }
        }
        
        if (optimalSize != null) {
            Log.d(TAG, "Selected optimal size: " + optimalSize.width + "x" + optimalSize.height);
        }
        
        return optimalSize;
    }    private void adjustSurfaceView(Camera.Size previewSize, int viewWidth, int viewHeight) {
        float previewRatio = (float) previewSize.width / previewSize.height;
        float viewRatio = (float) viewWidth / viewHeight;

        Log.d(TAG, "Adjusting surface view. Preview size: " + previewSize.width + "x" + previewSize.height + 
              " View size: " + viewWidth + "x" + viewHeight);
        Log.d(TAG, "Ratios - Preview: " + previewRatio + " View: " + viewRatio);

        // Lưu các kích thước ban đầu để tránh vấn đề khi gọi requestLayout() nhiều lần
        int originalWidth = getLayoutParams().width;
        int originalHeight = getLayoutParams().height;
        
        // Áp dụng tỷ lệ phù hợp để giữ khung hình camera hiển thị đầy đủ và không bị kéo méo
        if (previewRatio > viewRatio) {
            // Preview rộng hơn view, giữ chiều rộng và điều chỉnh chiều cao
            int adjustedHeight = (int) (viewWidth / previewRatio);
            Log.d(TAG, "Preview wider than view, adjusting height to: " + adjustedHeight);
            getLayoutParams().height = adjustedHeight;
            getLayoutParams().width = viewWidth;
        } else {
            // Preview cao hơn hoặc bằng view, giữ chiều cao và điều chỉnh chiều rộng
            int adjustedWidth = (int) (viewHeight * previewRatio);
            Log.d(TAG, "Preview taller than view, adjusting width to: " + adjustedWidth);
            getLayoutParams().width = adjustedWidth;
            getLayoutParams().height = viewHeight;
        }
        
        // Kiểm tra nếu kích thước thay đổi thì mới cần requestLayout
        if (originalWidth != getLayoutParams().width || originalHeight != getLayoutParams().height) {
            Log.d(TAG, "Layout changed, requesting layout update");
            requestLayout();
        }
    }

    private void setCameraDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }

        mCamera.setDisplayOrientation(result);
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}