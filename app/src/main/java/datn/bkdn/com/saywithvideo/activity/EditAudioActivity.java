package datn.bkdn.com.saywithvideo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
    private VisualizerView mVisualizerView;
    private ImageView mImgPlay;
    private static final int MIN_SECOND = 1;
    private float mPixelPerSecond;
    private String mFilePath;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private int mWidth;
    private String mOutputPath;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_audio);

        mFilePath = getIntent().getStringExtra("FileName");
        if (mFilePath == null) {
            finish();
        }

        init();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(mFilePath);
            mMediaPlayer.prepare();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
            mMediaPlayer = null;
        }

        mWidth = getResources().getDisplayMetrics().widthPixels;
        mPixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
    }

    private void init() {
        mMarkerLeft = (MarkerView) findViewById(R.id.markerLeft);
        mMarkerRight = (MarkerView) findViewById(R.id.markerRight);
        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mVisualizerView = (VisualizerView) findViewById(R.id.visualizerView);
        TextView mTvNext = (TextView) findViewById(R.id.tvNext);
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
        mPixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
    }

    @Override
    public void markerTouchEnd(MarkerView customImageView, float x) {

    }

    @Override
    public void markerMove(MarkerView customImageView, float x) {
        float min_pixel = MIN_SECOND * mPixelPerSecond * 1000;
        if (customImageView.getId() == R.id.markerLeft) {
            if (mMarkerRight.getX() - x < min_pixel) {
                return;
            }
        } else {
            int xr = (int) x + mMarkerRight.getWidth();
            if (xr > mWidth) {
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
        float left;
        float right;
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
                        mMediaPlayer.setDataSource(mFilePath);
                        mMediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    setupVisualizerFxAndUI();
                    mVisualizer.setEnabled(true);
                    if (mPixelPerSecond == 0) {
                        mPixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
                    }
                    left = (mMarkerLeft.getX() + mMarkerLeft.getWidth() / 2 - mVisualizerView.getLeft()) / mPixelPerSecond;
                    right = (mMarkerRight.getX() + mMarkerRight.getWidth() / 2 - mVisualizerView.getLeft()) / mPixelPerSecond;
                    mVisualizerView.setStartPosition(left);
                    mVisualizerView.setEndPosition(right);
                    start((long) left, (long) (right - left));
                }
                break;
            case R.id.tvNext:
                if (mPixelPerSecond == 0) {
                    mPixelPerSecond = mVisualizerView.getWidth() * 1.0f / mMediaPlayer.getDuration();
                }
                left = (mMarkerLeft.getX() + mMarkerLeft.getWidth() / 2 - mVisualizerView.getLeft()) / mPixelPerSecond;
                right = (mMarkerRight.getX() + mMarkerRight.getWidth() / 2 - mVisualizerView.getLeft()) / mPixelPerSecond;
                new EditAudio(left, right).execute();
                createDialog();
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
            mOutputPath = Constant.AUDIO_DIRECTORY_PATH + "AUDIO_" + Tools.getDate() + ".m4a";
            try {
                SoundFile soundFile = SoundFile.create(mFilePath, null);
                soundFile.WriteFile(new File(mOutputPath), left / 1000, right / 1000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SoundFile.InvalidInputException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            File file = new File(mFilePath);
            file.delete();
        }
    }

    private String idSound;

    private void createSound(String name) {
        idSound = UUID.randomUUID().toString();
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        String id = Utils.getCurrentUserID(EditAudioActivity.this);
        Sound sound = new Sound(idSound, name, Utils.getCurrentUserName(EditAudioActivity.this), mOutputPath, mOutputPath, ft.format(date).toString());
        sound.setIdUser(id);
        RealmUtils.getRealmUtils(EditAudioActivity.this).addNewSound(EditAudioActivity.this, sound);
    }

    public void createDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_name_audio);
        dialog.setTitle("Pick a name");

        TextView tvOK = (TextView) dialog.findViewById(R.id.tvOK);
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
        final EditText edtName = (EditText) dialog.findViewById(R.id.edtnewName);

        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                createSound(edtName.getText().toString());
                finish();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
