package datn.bkdn.com.saywithvideo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


/**
 * Created by Admin on 2/20/2016.
 */
import android.app.Activity;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.os.Environment;
import android.view.ViewGroup;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.Context;
import android.util.Log;
import android.media.MediaRecorder;
import android.media.MediaPlayer;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;

import static datn.bkdn.com.saywithvideo.R.drawable.selector_button_record_a_sound_pressed;


public class RecordNewSoundActivity extends Activity
{
    private  boolean mStartRecording = true;
    private static final String LOG_TAG = "AudioRecordActivity";
    private static String mFileName = null;

    private RelativeLayout buttonRecord;
    private TextView tvStart;
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_record_new_sound);
        AudioRecordActivity();
        buttonRecord = (RelativeLayout) findViewById(R.id.rlStartRecord);
        tvStart = (TextView) findViewById(R.id.tvStart);

        buttonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStartRecording) {
                    onRecord(mStartRecording);
                    buttonRecord.setBackgroundResource(R.drawable.selector_button_record_a_sound_pressed);
                    tvStart.setText("Done");
                    mStartRecording=!mStartRecording;
                }else
                {
                    onRecord(mStartRecording);
                    onPlay(true);
                }
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

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

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

    public void  AudioRecordActivity() {
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/audiorecordtest.3gp";
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
}

