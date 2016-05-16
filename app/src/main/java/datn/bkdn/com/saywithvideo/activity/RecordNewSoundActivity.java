package datn.bkdn.com.saywithvideo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.skyfishjy.library.RippleBackground;

import java.io.File;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.soundfile.SoundFile;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Constant;

public class RecordNewSoundActivity extends Activity implements OnClickListener {
    private static final int MAX_RECORD = 20;
    private static String mFileName = null;
    private RelativeLayout mRlRecord;
    private TextView mTvStart;
    private TextView mTvTime;
    private TextView mTvInfor;
    private RippleBackground rippleBackground;
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
        rippleBackground = (RippleBackground) findViewById(R.id.content);
        ViewGroup vgBack = (ViewGroup) findViewById(R.id.rlBack);
        mTvStart = (TextView) findViewById(R.id.tvStart);
        mTvTime = (TextView) findViewById(R.id.tvTime);
        mTvInfor = (TextView) findViewById(R.id.tvInfor);

        String idSound = AppTools.getDate();
        String folderPath = Constant.DIRECTORY_PATH + Constant.AUDIO;
        AppTools.createFolder(folderPath);
        mFileName = folderPath + "AUDIO_" + idSound + ".aac";
        vgBack.setOnClickListener(this);
        mRlRecord.setOnClickListener(this);
    }

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    private void record() {
        Log.d("running","running 0");
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
                    mRlRecord.setBackgroundResource(R.color.transparent);
                    mTvStart.setText(getResources().getString(R.string.record_done));
                    mTvInfor.setText(getResources().getString(R.string.hint_record_done));
                    rippleBackground.startRippleAnimation();
                } else {
                    rippleBackground.stopRippleAnimation();
                }
                record();
                break;
            case R.id.rlBack:
                finish();
                overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
    }
}

