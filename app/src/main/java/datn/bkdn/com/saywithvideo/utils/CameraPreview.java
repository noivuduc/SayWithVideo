package datn.bkdn.com.saywithvideo.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("surfaceCreated", "surfaceCreated");
        try {
            if (mCamera == null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d(VIEW_LOG_TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        Log.d("refreshCamera", "refreshCamera");
        if (mHolder.getSurface() == null) {
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
        }
        setCamera(camera);
        try {
            Camera.Parameters mParameters = camera.getParameters();
            List<Camera.Size> i = mParameters.getSupportedPreviewSizes();
            Camera.Size mBestSize = i.get(0);
            List<int[]> fps = mParameters.getSupportedPreviewFpsRange();
            int[] best = fps.get(0);
            Log.d("size", mBestSize.width + " " + mBestSize.height);
            mParameters.setPreviewSize(mBestSize.width, mBestSize.height);
            mParameters.setPreviewFpsRange(best[0], best[1]);
            List<String> focusModes = mParameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
                mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            mCamera.setParameters(mParameters);
            camera.setParameters(mParameters);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            Log.d(VIEW_LOG_TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        refreshCamera(mCamera);
    }

    public void setCamera(Camera camera) {
        mCamera = camera;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.release();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);
        int h = MeasureSpec.getSize(heightMeasureSpec);

        if (w > h) {
            w = h;
        } else {
            h = w;
        }
        setMeasuredDimension(w, h);
        Log.d("preview", getWidth() + " " + getHeight() + " " + getMeasuredWidth() + " " + getMeasuredHeight());
    }
}