package datn.bkdn.com.saywithvideo.activity;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.custom.MarkerView;
import datn.bkdn.com.saywithvideo.custom.VisualizerView;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.soundfile.SoundFile;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Tools;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class EditAudioActivity extends Activity implements MarkerView.CustomListener,
        MediaPlayer.OnCompletionListener, View.OnClickListener {

    private MarkerView mMarkerLeft;
    private MarkerView mMarkerRight;
    private RelativeLayout mRlBack;
    private VisualizerView mVisualizerView;
    private TextView mTvNext;
    private ImageView mImgPlay;
    private static final int MIN_SECOND = 2;
    private float pixelPerSecond;
    private String filePath;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private int wRight;
    private String outputPath;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_audio);

        filePath = getIntent().getStringExtra("FileName");
        if (filePath == null) {
            finish();
        }
        init();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            mMediaPlayer = null;
        }
        mMediaPlayer.setOnCompletionListener(this);
        wRight = getResources().getDisplayMetrics().widthPixels;
    }

    private void init() {
        mMarkerLeft = (MarkerView) findViewById(R.id.markerLeft);
        mMarkerRight = (MarkerView) findViewById(R.id.markerRight);
        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        mTvNext = (TextView) findViewById(R.id.tvNext);
        mImgPlay = (ImageView) findViewById(R.id.imgPlay);

        mMarkerLeft.setListener(this);
        mMarkerRight.setListener(this);
        mRlBack.setOnClickListener(this);
        mTvNext.setOnClickListener(this);
        mImgPlay.setOnClickListener(this);
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


    @Override
    public void markerDraw() {

    }

    @Override
    public void markerTouchStart(MarkerView customImageView) {
        pixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
    }

    @Override
    public void markerTouchEnd(MarkerView customImageView, float x) {

    }

    @Override
    public void markerMove(MarkerView customImageView, float x) {
        float min_pixel = MIN_SECOND * pixelPerSecond * 1000;
        if (customImageView.getId() == R.id.markerLeft) {
            if (mMarkerRight.getX() - x < min_pixel) {
                return;
            }
        } else {
            int xr = (int) x + mMarkerRight.getWidth();
            if (xr > wRight) {
                return;
            }
            if (x - mMarkerLeft.getX() < min_pixel) {
                return;
            }
        }
        customImageView.setX(x);
    }

    @Override
    public void onClick(View v) {
        float left = 0;
        float right = 0;
        switch (v.getId()) {
            case R.id.imgPlay:
                if (mVisualizer != null && mVisualizer.getEnabled()) {
                    mVisualizer.setEnabled(false);
                }

                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.stop();
                    }
                    try {
                        mMediaPlayer.reset();
                        mMediaPlayer.setDataSource(filePath);
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setupVisualizerFxAndUI();
                    mVisualizer.setEnabled(true);
                    if (pixelPerSecond == 0) {
                        pixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
                    }
                    left = (mMarkerLeft.getX() + mMarkerLeft.getWidth() / 2 - mVisualizerView.getLeft()) / pixelPerSecond;
                    right = (mMarkerRight.getX() + mMarkerRight.getWidth() / 2 - mVisualizerView.getLeft()) / pixelPerSecond;
                    mVisualizerView.setStartPosition(left);
                    start((long) left, (long) (right - left));
                }
                break;
            case R.id.tvNext:
                if (pixelPerSecond == 0) {
                    pixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
                }
                left = (mMarkerLeft.getX() + mMarkerLeft.getWidth() / 2 - mVisualizerView.getLeft()) / pixelPerSecond;
                right = (mMarkerRight.getX() + mMarkerRight.getWidth() / 2 - mVisualizerView.getLeft()) / pixelPerSecond;
                new EditAudio(left, right).execute();
                break;
            case R.id.rlBack:
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                finish();

        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        complete();
    }

    private void complete() {
        mImgPlay.setImageResource(R.mipmap.ic_play);
        mVisualizer.setEnabled(false);
        mVisualizerView.reset();
    }

    public void start(long mStart, long mDuration) {
        CountDownTimer c = new CountDownTimer(mDuration, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                complete();
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
            }
        };
        mMediaPlayer.seekTo((int) mStart);
        mMediaPlayer.start();
        c.start();
        mImgPlay.setImageResource(R.mipmap.ic_pause);
    }

    private class EditAudio extends AsyncTask<Void, Void, Void> {

        private float left;
        private float right;

        public EditAudio(float left, float right) {
            this.left = left;
            this.right = right;
        }

        @Override
        protected Void doInBackground(Void... params) {
            outputPath = Constant.AUDIO_DIRECTORY_PATH + "AUDIO_" + Tools.getDate() + ".m4a";
            try {
                SoundFile soundFile = SoundFile.create(filePath, null);
                soundFile.WriteFile(new File(outputPath), left / 1000, right / 1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SoundFile.InvalidInputException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String idSound;

        private void createSound() {
            idSound = UUID.randomUUID().toString();
            String id = Utils.getCurrentUserID(EditAudioActivity.this);
            Sound sound = new Sound(idSound, outputPath, "noi", outputPath, outputPath, new Date().toString());
            sound.setIdUser(id);
            RealmUtils.getRealmUtils(EditAudioActivity.this).addNewSound(EditAudioActivity.this, sound);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            File file = new File(filePath);
            file.delete();
            createSound();
            finish();
        }
    }
}
