package datn.bkdn.com.saywithvideo.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.custom.VisualizerView;
import datn.bkdn.com.saywithvideo.utils.CameraPreview;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Tools;

public class CaptureVideoActivity extends AppCompatActivity implements View.OnClickListener, MediaPlayer.OnCompletionListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private RelativeLayout mRlBack;
    private RelativeLayout mRlCaptureVideo;
    private RelativeLayout mRlSwitchCamera;
    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    private String filePath;
    private boolean cameraFront = false;
    private boolean recording = false;
    private String mVideoOutPut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_video);

        init();

        filePath = getIntent().getStringExtra("FilePath");
        Log.d("filePath", filePath);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mRlCaptureVideo = (RelativeLayout) findViewById(R.id.rlCaptureVideo);
        mRlSwitchCamera = (RelativeLayout) findViewById(R.id.rlSwitchCamera);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);

        mRlBack.setOnClickListener(this);
        mRlCaptureVideo.setOnClickListener(this);
        mRlSwitchCamera.setOnClickListener(this);

        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        Log.d("mPreview", mPreview.getWidth() + " " + mPreview.getHeight());
    }

    private void setupVisualizerFxAndUI() {
        mVisualizerView.start();
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
        mVisualizerView.setDuration(mMediaPlayer.getDuration());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(
                new Visualizer.OnDataCaptureListener() {
                    public void onWaveFormDataCapture(Visualizer visualizer,
                                                      byte[] bytes, int samplingRate) {
                        int current;
                        try {
                            current = mMediaPlayer.getCurrentPosition();
                            mVisualizerView.updateVisualizer(bytes, current);
                        } catch (Exception e) {
                            visualizer.setEnabled(false);
                        }

                    }

                    public void onFftDataCapture(Visualizer visualizer,
                                                 byte[] bytes, int samplingRate) {
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false);
    }

    private void complete() {
        mVisualizer.setEnabled(false);
        mVisualizerView.reset();
    }

    public static Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
            camera.setDisplayOrientation(90);
//            Camera.Parameters mParameters = camera.getParameters();
//            List<Camera.Size> i = mParameters.getSupportedPreviewSizes();
//            Camera.Size mBestSize = i.get(0);
//            List<int[]> fps = mParameters.getSupportedPreviewFpsRange();
//            int[] best = fps.get(0);
//            mParameters.setPreviewSize(mBestSize.width, mBestSize.height);
//            mParameters.setPreviewFpsRange(best[0], best[1]);
//            camera.setParameters(mParameters);
        } catch (Exception e) {
        }
        return camera;
    }

    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        mMediaRecorder.setVideoSize(1080, 1080);
        mMediaRecorder.setVideoFrameRate(24);
        mMediaRecorder.setVideoEncodingBitRate(3000000);
        mMediaRecorder.setOrientationHint(90);

        mVideoOutPut = Constant.VIDEO_DIRECTORY_PATH + "VIDEO_" + Tools.getDate() + ".mp4";
        mMediaRecorder.setOutputFile(mVideoOutPut);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(this)) {
            Toast toast = Toast.makeText(this, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                mRlSwitchCamera.setVisibility(View.GONE);
            }
            mCamera = Camera.open(findFrontFacingCamera());
            mPreview.refreshCamera(mCamera);
        }
    }

    public void chooseCamera() {
        if (cameraFront) {
            int cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        } else {
            int cameraId = findFrontFacingCamera();
            if (cameraId >= 0) {
                mCamera = Camera.open(cameraId);
                mPreview.refreshCamera(mCamera);
            }
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }
        return cameraId;
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                cameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    private void releasePlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                releaseCamera();
                releasePlayer();
                finish();
                break;
            case R.id.rlSwitchCamera:
                if (!recording) {
                    int camerasNumber = Camera.getNumberOfCameras();
                    if (camerasNumber > 1) {
                        releaseCamera();
                        chooseCamera();
                    } else {
                        Toast toast = Toast.makeText(this, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                break;
            case R.id.rlCaptureVideo:
                if (!recording) {
                    recording = !recording;
                    mRlCaptureVideo.setEnabled(false);
                    setupVisualizerFxAndUI();
                    if (!prepareMediaRecorder()) {
                        Toast.makeText(CaptureVideoActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                mMediaPlayer.start();
                                mMediaRecorder.start();
                                mVisualizer.setEnabled(true);
                            } catch (final Exception ex) {
                            }
                        }
                    });
                }
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        complete();
        mMediaRecorder.stop();
        releaseMediaRecorder();
        new MuxVideo().execute();
    }

    private class MuxVideo extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Movie inputVideo = MovieCreator.build(mVideoOutPut);
                Movie inputAudio = MovieCreator.build(filePath);
                Movie outputVideo = new Movie();

                for (Track t : inputAudio.getTracks()) {
                    outputVideo.addTrack(t);
                }

                for (Track t : inputVideo.getTracks()) {
                    outputVideo.addTrack(t);
                }

                Container container = new DefaultMp4Builder().build(outputVideo);
                outputPath = Constant.VIDEO_DIRECTORY_PATH + "VIDEO_" + Tools.getDate() + ".mp4";
                FileChannel fileChannel = new FileOutputStream(new File(outputPath)).getChannel();
                container.writeContainer(fileChannel);
                fileChannel.close();
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Mux video error", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        private String outputPath;

        @Override
        protected void onPostExecute(Void aVoid) {
            File file = new File(mVideoOutPut);
            file.delete();
            Toast.makeText(getBaseContext(), "Mux video success", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(CaptureVideoActivity.this, ShowVideohActivity.class);
            intent.putExtra("VideoPath", outputPath);
            startActivity(intent);
            finish();
        }
    }
}
