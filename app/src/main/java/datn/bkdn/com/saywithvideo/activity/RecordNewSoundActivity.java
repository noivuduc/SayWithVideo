package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.AudioRecord;
import android.os.CountDownTimer;
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
import java.util.Date;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.model.User;
import datn.bkdn.com.saywithvideo.utils.Utils;

import static datn.bkdn.com.saywithvideo.R.drawable.selector_button_record_a_sound_pressed;


public class RecordNewSoundActivity extends Activity
{
    private  boolean mStartRecording = true;
    private static final String LOG_TAG = "AudioRecordActivity";
    private static String mFileName = null;
    private AudioRecord audioRecord;
    private String idSound;
    private RelativeLayout buttonRecord;
    private TextView tvStart;
    private TextView tvTime;
    private  CountDownTimer countdown;
    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;


    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_record_new_sound);
        AudioRecordActivity();
        buttonRecord = (RelativeLayout) findViewById(R.id.rlStartRecord);
        tvStart = (TextView) findViewById(R.id.tvStart);
        tvTime = (TextView) findViewById(R.id.tvTime);
        buttonRecord.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStartRecording) {
                    onRecord(mStartRecording);
                    tvTime.setVisibility(View.VISIBLE);
                    clockRecord();
                    buttonRecord.setBackgroundResource(R.drawable.selector_button_record_a_sound_pressed);
                    tvStart.setText("Done");
                    mStartRecording=!mStartRecording;
                }else
                {
                    onRecord(mStartRecording);
                    createSound();
                    //finishRecord();
                    countdown.cancel();
                    //onPlay(true);
                }
            }

        });
    }

    private void createSound(){
        String name = Utils.getCurrentUserName(this);
        String id = Utils.getCurrentUserID(this);
        Sound sound = new Sound(idSound,"sound of "+name,name,"",mFileName,new Date().toString());
        sound.setIdUser(id);
        RealmUtils.getRealmUtils(this).addNewSound(this,sound);
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
        idSound = UUID.randomUUID().toString();
        mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        mFileName += "/"+idSound+".3gp";
    }

    private void clockRecord(){
        countdown = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTime.setText(millisUntilFinished / 1000+"."+millisUntilFinished/10000 + " sec");
            }

            @Override
            public void onFinish() {
               finishRecord();
            }
        }.start();
    }

    private void finishRecord(){
        Intent intent = new Intent(RecordNewSoundActivity.this,EditAudioActivity.class);
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

}

