package datn.bkdn.com.saywithvideo.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import datn.bkdn.com.saywithvideo.custom.WaveformView;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.soundfile.SoundFile;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.CameraPreview;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Tools;
import datn.bkdn.com.saywithvideo.utils.Utils;

@SuppressWarnings("deprecation")
public class CaptureVideoActivity extends AppCompatActivity implements View.OnClickListener,
        WaveformView.WaveformListener, MediaPlayer.OnCompletionListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private RelativeLayout mRlCaptureVideo;
    private RelativeLayout mRlSwitchCamera;
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    private String mFilePath;
    private String mFileName;
    private boolean mCameraFront = false;
    private String mVideoOutPut;
    private WaveformView mWaveformView;
    private Handler mHandler;
    private SoundFile mSoundFile;
    private File mFile;
    private float mDensity;
    private boolean mLoadingKeepGoing;
    private ProgressDialog mProgressDialog;
    private long mLoadingLastUpdateTime;
    private int mMaxPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mStartPos;
    private int mEndPos;
    private int mPlayEndMsec;
    private boolean mIsPlaying;
    private int mWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_video);
        init();
    }

    private void init() {
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mRlCaptureVideo = (RelativeLayout) findViewById(R.id.rlCaptureVideo);
        mRlSwitchCamera = (RelativeLayout) findViewById(R.id.rlSwitchCamera);
        mWaveformView = (WaveformView) findViewById(R.id.waveform);
        FrameLayout mFlPreview = (FrameLayout) findViewById(R.id.cameraPreview);

        if (mRlBack != null) mRlBack.setOnClickListener(this);
        mRlCaptureVideo.setOnClickListener(this);
        mRlSwitchCamera.setOnClickListener(this);
        mWaveformView.setListener(this);

        mHandler = new Handler();
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        if (mFlPreview != null) mFlPreview.addView(mPreview);

        initData();
    }

    private void initData() {
        mFileName = getIntent().getStringExtra("FileName");
        mFilePath = getIntent().getStringExtra("FilePath");
        mMaxPos = 0;
        mStartPos = 0;
        mDensity = getResources().getDisplayMetrics().density;
        loadFile();
    }

    private void loadFile() {
        mFile = new File(mFilePath);
        mLoadingLastUpdateTime = System.currentTimeMillis();
        mLoadingKeepGoing = true;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mLoadingKeepGoing = false;
            }
        });
        mProgressDialog.show();

        final SoundFile.ProgressListener listener = new SoundFile.ProgressListener() {
            @Override
            public boolean reportProgress(double fractionComplete) {
                long now = System.currentTimeMillis();
                if (now - mLoadingLastUpdateTime > 100) {
                    mProgressDialog.setProgress(
                            (int) (mProgressDialog.getMax() * fractionComplete));
                    mLoadingLastUpdateTime = now;
                }
                return mLoadingKeepGoing;
            }
        };

        //  mediaplayer
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(mFilePath);
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mMediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //  soundfile
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSoundFile = SoundFile.create(mFile.getAbsolutePath(), listener);
                } catch (final Exception e) {
                    mProgressDialog.dismiss();
                    return;
                }
                if (mLoadingKeepGoing) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mProgressDialog.dismiss();
                            finishOpeningSoundFile();
                        }
                    });
                }
            }
        }).start();

    }

    private void finishOpeningSoundFile() {
        mWaveformView.setSoundFile(mSoundFile);
        mWaveformView.recomputeHeights(mDensity);

        mMaxPos = mWaveformView.maxPos();
        mOffset = 0;
        mOffsetGoal = 0;

        resetPositions();
        updateDisplay();
    }

    protected void resetPositions() {
        mStartPos = 0;
        mEndPos = mMaxPos;
    }

    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mMediaPlayer == null) {
            return;
        }

        try {
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }
            mIsPlaying = true;
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(this);
            updateDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected synchronized void handlePause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
    }

    protected synchronized void handleRelease() {
        releasePlayer();
        releaseMediaRecorder();
        try {
            File file = new File(mVideoOutPut);
            file.deleteOnExit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
    }

    protected void setOffsetGoalNoUpdate(int offset) {
        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    protected synchronized void updateDisplay() {

        if (mIsPlaying) {
            int now = mMediaPlayer.getCurrentPosition();
            Log.d("Tien", now + " " + mMediaPlayer.getDuration());
            if (now == mMediaPlayer.getDuration()) {
                complete();
                return;
            }
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        int offsetDelta;
        offsetDelta = mOffsetGoal - mOffset;

        if (offsetDelta > 10)
            offsetDelta = offsetDelta / 10;
        else if (offsetDelta > 0)
            offsetDelta = 1;
        else if (offsetDelta < -10)
            offsetDelta = offsetDelta / 10;
        else if (offsetDelta < 0)
            offsetDelta = -1;
        else
            offsetDelta = 0;

        mOffset += offsetDelta;

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();
    }

    public Camera getCameraInstance() {
        Camera camera;
        try {
            camera = Camera.open(findFrontFacingCamera());
        } catch (Exception e) {
            camera = null;
        }
        return camera;
    }

    private boolean hasCamera(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private boolean prepareMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();

        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
        CamcorderProfile cpHigh = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mMediaRecorder.setVideoSize(cpHigh.videoFrameWidth, cpHigh.videoFrameHeight);
        mMediaRecorder.setVideoFrameRate(cpHigh.videoFrameRate);
        mMediaRecorder.setVideoEncodingBitRate(cpHigh.videoBitRate);
        if (mCameraFront) {
            mMediaRecorder.setOrientationHint(270);
        } else {
            mMediaRecorder.setOrientationHint(90);
        }

        Tools.createFolder(Constant.VIDEO_DIRECTORY_PATH);
        mVideoOutPut = Constant.VIDEO_DIRECTORY_PATH + "VIDEO_" + AppTools.getDate() + ".mp4";
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
            mMediaRecorder.stop();
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
        if (!hasCamera(this)) {
            Toast toast = Toast.makeText(this, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (mCamera == null) {
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                mRlSwitchCamera.setVisibility(View.GONE);
                mCamera = Camera.open(findBackFacingCamera());
            } else {
                mCamera = Camera.open(findFrontFacingCamera());
            }
            mPreview.refreshCamera(mCamera);
        }
        super.onResume();
    }

    public void chooseCamera() {
        if (mCameraFront) {
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
                mCameraFront = true;
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
                mCameraFront = false;
                break;
            }
        }
        return cameraId;
    }

    private void releasePlayer() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                releaseCamera();
                handleRelease();
                finish();
                break;
            case R.id.rlSwitchCamera:
                int camerasNumber = Camera.getNumberOfCameras();
                if (camerasNumber > 1) {
                    releaseCamera();
                    chooseCamera();
                } else {
                    Toast toast = Toast.makeText(this, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
                    toast.show();
                }
                break;
            case R.id.rlCaptureVideo:
                mRlCaptureVideo.setEnabled(false);
                mRlSwitchCamera.setEnabled(false);
                if (!prepareMediaRecorder()) {
                    Toast.makeText(CaptureVideoActivity.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                    finish();
                }
                onPlay(mStartPos);
                mMediaRecorder.start();
                break;
        }
    }

    @Override
    public void waveformTouchStart(float x) {

    }

    @Override
    public void waveformTouchMove(float x) {

    }

    @Override
    public void waveformTouchEnd() {

    }

    @Override
    public void waveformFling(float x) {

    }

    @Override
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        }
    }

    private void complete() {
        Log.d("Tien", "onCompletion");
        mIsPlaying = false;
        mMediaPlayer.release();
        releaseMediaRecorder();
        mWaveformView.setPlayback(-1);
        new MuxVideo().execute();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        complete();
    }

    private class MuxVideo extends AsyncTask<Void, Void, Void> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(CaptureVideoActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle("Muxing...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Movie inputVideo = MovieCreator.build(mVideoOutPut);
                Movie inputAudio = MovieCreator.build(mFilePath);
                Movie outputVideo = new Movie();

                for (Track t : inputAudio.getTracks()) {
                    outputVideo.addTrack(t);
                }

                for (Track t : inputVideo.getTracks()) {
                    outputVideo.addTrack(t);
                }

                Container container = new DefaultMp4Builder().build(outputVideo);
                String folderPath = Constant.VIDEO_DIRECTORY_PATH;
                Tools.createFolder(folderPath);
                outputPath = folderPath + "VIDEO_" + AppTools.getDate() + ".mp4";
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
            file.deleteOnExit();
            String userId = Utils.getCurrentUserID(CaptureVideoActivity.this);
            RealmUtils.getRealmUtils(CaptureVideoActivity.this).addVideo(CaptureVideoActivity.this, mFileName, outputPath, userId);
            sendBroadcast(new Intent("AddVideo"));
            Intent intent = new Intent(CaptureVideoActivity.this, ShowVideoActivity.class);
            intent.putExtra("VideoPath", outputPath);
            startActivity(intent);
            finish();
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        handleRelease();
        super.onBackPressed();
    }
}