package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.CamcorderProfile;
import android.os.CountDownTimer;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Tools;


public class RecordNewSoundActivity extends Activity {
    private boolean mStartRecording = true;
    private static final String LOG_TAG = "AudioRecordActivity";
    private static String mFileName = null;
    private RelativeLayout buttonRecord;
    private ViewGroup vgBack;
    private TextView tvStart;
    private TextView tvTime;
    private TextView tvInfor;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private ClockRecord mClockRecord;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_record_new_sound);
        AudioRecordActivity();
        buttonRecord = (RelativeLayout) findViewById(R.id.rlStartRecord);
        vgBack = (ViewGroup) findViewById(R.id.rlBack);
        tvStart = (TextView) findViewById(R.id.tvStart);
        tvTime = (TextView) findViewById(R.id.tvTime);
        tvInfor = (TextView) findViewById(R.id.tvInfor);

        buttonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mStartRecording) {
                    tvTime.setVisibility(View.VISIBLE);
                    buttonRecord.setBackgroundResource(R.drawable.selector_button_record_a_sound_pressed);
                    tvStart.setText("Done");
                    tvInfor.setText("Tap when you are done!");
                    clockRecord();
                    mStartRecording = !mStartRecording;
                } else {
                    onRecord(mStartRecording);
                    finishRecord();
                    mClockRecord.cancel();
                }
            }
        });

        vgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(camcorderProfile.audioChannels);
        mRecorder.setAudioEncodingBitRate(camcorderProfile.audioBitRate);
        mRecorder.setAudioSamplingRate(camcorderProfile.audioSampleRate);
        mRecorder.setOutputFile(mFileName);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    public void AudioRecordActivity() {
        String idSound = Tools.getDate();
        mFileName = Constant.AUDIO_DIRECTORY_PATH + "AUDIO_" + idSound + ".aac";
    }

    private void clockRecord() {
        mClockRecord = new ClockRecord(10000, 100);
        mClockRecord.startClock();
    }

    private void finishRecord() {
        Intent intent = new Intent(RecordNewSoundActivity.this, EditAudioActivity.class);
        intent.putExtra("FileName", mFileName);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private class ClockRecord extends CountDownTimer {
        public ClockRecord(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            tvTime.setText(millisUntilFinished / 1000 + "." + (millisUntilFinished / 100) % 10 + " sec");
        }

        @Override
        public void onFinish() {
            finishRecord();
        }

        public void startClock() {
            start();
            onRecord(mStartRecording);
        }
    }
}

