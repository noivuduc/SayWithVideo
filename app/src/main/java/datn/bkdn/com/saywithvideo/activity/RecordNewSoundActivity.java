package datn.bkdn.com.saywithvideo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.soundfile.SoundFile;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Tools;

public class RecordNewSoundActivity extends Activity implements OnClickListener {
    private static String mFileName = null;
    private RelativeLayout mRlRecord;
    private TextView mTvStart;
    private TextView mTvTime;
    private TextView mTvInfor;
    private static final int MAX_RECORD = 20;
    private long mRecordingLastUpdateTime;
    private double mRecordingTime;
    private boolean mRecordingKeepGoing;
    private boolean mIsRecord = false;
    private SoundFile mSoundFile;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_record_new_sound);

        init();
    }

    private void init() {
        mRlRecord = (RelativeLayout) findViewById(R.id.rlStartRecord);
        ViewGroup vgBack = (ViewGroup) findViewById(R.id.rlBack);
        mTvStart = (TextView) findViewById(R.id.tvStart);
        mTvTime = (TextView) findViewById(R.id.tvTime);
        mTvInfor = (TextView) findViewById(R.id.tvInfor);

        String idSound = AppTools.getDate();
        String folderPath = Constant.DIRECTORY_PATH + Constant.AUDIO;
        Tools.createFolder(folderPath);
        mFileName = folderPath + "AUDIO_" + idSound + ".aac";

        vgBack.setOnClickListener(this);
        mRlRecord.setOnClickListener(this);
    }

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    private void record() {
        mIsRecord = !mIsRecord;
        mRecordingLastUpdateTime = getCurrentTime();
        mRecordingKeepGoing = mIsRecord;

        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double elapsedTime) {
                        long now = getCurrentTime();
                        if (now - mRecordingLastUpdateTime > 5) {
                            mRecordingTime = elapsedTime;
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    int min = (int) (mRecordingTime / 60);
                                    float sec = (float) (mRecordingTime - 60 * min);
                                    float time = MAX_RECORD - sec;
                                    if (time < 0) time = 0;
                                    if (sec >= MAX_RECORD) {
                                        mRecordingKeepGoing = false;
                                    }
                                    mTvTime.setText(String.format("%d:%05.2f", min, time));
                                }
                            });
                            mRecordingLastUpdateTime = now;
                        }
                        return mRecordingKeepGoing;
                    }
                };

        // Record the audio stream in a background thread
        new Thread() {
            public void run() {
                try {
                    mSoundFile = SoundFile.record(listener);
                    if (mSoundFile == null) {
                        Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                    mSoundFile.WriteFile(new File(mFileName), 0, mSoundFile.getNumFrames());
                    finishRecord();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void finishRecord() {
        Intent intent = new Intent(RecordNewSoundActivity.this, EditAudioActivity.class);
        intent.putExtra("FileName", mFileName);
        intent.putExtra("Type", "Record");
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlStartRecord:
                if (!mIsRecord) {
                    mTvTime.setVisibility(View.VISIBLE);
                    mRlRecord.setBackgroundResource(R.drawable.selector_button_record_a_sound_pressed);
                    String s = "Done";
                    String s2 = "Tap when you are done!";
                    mTvStart.setText(s);
                    mTvInfor.setText(s2);
                }
                record();
                break;
            case R.id.rlBack:
                finish();
                break;
        }
    }
}

