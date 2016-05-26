package datn.bkdn.com.saywithvideo.activity;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.custom.MarkerView;
import datn.bkdn.com.saywithvideo.custom.WaveformView;
import datn.bkdn.com.saywithvideo.database.Group;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.firebase.FirebaseAudio;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseGroup;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;
import datn.bkdn.com.saywithvideo.soundfile.SoundFile;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class EditAudioActivity extends Activity implements MarkerView.MarkerListener,
        WaveformView.WaveformListener, View.OnClickListener {

    private static final int MIN_SECOND = 1;
    private static final int MAX_SECOND = 20;
    private ImageView mImgPlay;
    private String mFilename;
    private String mOutputPath;
    private Firebase mFirebase;
    private String mType;
    private TextView mTvStart;
    private TextView mTvEnd;
    private ProgressDialog mProgressDialog;
    private String fileName;
    private String audioName;
    private WaveformView mWaveformView;
    private MarkerView mStartMarker;
    private MarkerView mEndMarker;
    private int mWidth;
    private int mMaxPos;
    private int mStartPos;
    private int mEndPos;
    private boolean mStartVisible;
    private boolean mEndVisible;
    private int mLastDisplayedStartPos;
    private int mLastDisplayedEndPos;
    private int mOffset;
    private int mOffsetGoal;
    private int mFlingVelocity;
    private int mPlayStartMsec;
    private int mPlayEndMsec;
    private Handler mHandler;
    private String mGroupId;
    private ArrayList<FirebaseGroup> mGroups;
    private final Runnable mTimerRunnable = new Runnable() {
        public void run() {
            if (mStartPos != mLastDisplayedStartPos) {
                mTvStart.setText(formatTime(mStartPos));
                mLastDisplayedStartPos = mStartPos;
            }

            if (mEndPos != mLastDisplayedEndPos) {
                mTvEnd.setText(formatTime(mEndPos));
                mLastDisplayedEndPos = mEndPos;
            }

            mHandler.postDelayed(mTimerRunnable, 100);
        }
    };
    private boolean mIsPlaying;
    private MediaPlayer mPlayer;
    private boolean mTouchDragging;
    private float mTouchStart;
    private int mTouchInitialOffset;
    private int mTouchInitialStartPos;
    private int mTouchInitialEndPos;
    private long mWaveformTouchStartMsec;
    private float mDensity;
    private int mMarkerLeftInset;
    private int mMarkerRightInset;
    private int mMarkerTopOffset;
    private int mMarkerBottomOffset;
    private TextView mInfo;
    private String mCaption = "";
    private SoundFile mSoundFile;
    private File mFile;
    private boolean mKeyDown;
    private long mLoadingLastUpdateTime;
    private boolean mLoadingKeepGoing;
    private boolean mIsKeepActivity;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_edit_audio);

        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(FirebaseConstant.BASE_URL);

        mFilename = getIntent().getStringExtra("FileName");
        mType = getIntent().getStringExtra("Type");
        if (mFilename == null) {
            Toast.makeText(getBaseContext(), "Audio file error", Toast.LENGTH_SHORT).show();
            finish();
        }

        mHandler = new Handler();
        loadGui();
        mHandler.postDelayed(mTimerRunnable, 100);
        loadFromFile();
    }

    private void loadGui() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mDensity = metrics.density;

        mMarkerLeftInset = (int) (46 * mDensity);
        mMarkerRightInset = (int) (48 * mDensity);
        mMarkerTopOffset = (int) (10 * mDensity);
        mMarkerBottomOffset = (int) (10 * mDensity);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        TextView mTvNext = (TextView) findViewById(R.id.tvNext);
        mImgPlay = (ImageView) findViewById(R.id.imgPlay);
        mTvStart = (TextView) findViewById(R.id.tvStart);
        mTvEnd = (TextView) findViewById(R.id.tvEnd);
        mWaveformView = (WaveformView) findViewById(R.id.waveform);
        mInfo = (TextView) findViewById(R.id.tvInfor);

        mRlBack.setOnClickListener(this);
        mTvNext.setOnClickListener(this);
        mImgPlay.setOnClickListener(this);
        mWaveformView.setListener(this);
        mInfo.setText(mCaption);

        mMaxPos = 0;
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;
        mIsKeepActivity = false;

        mStartMarker = (MarkerView) findViewById(R.id.startmarker);
        mStartMarker.setListener(this);
        mStartMarker.setAlpha(1f);
        mStartMarker.setFocusable(true);
        mStartMarker.setFocusableInTouchMode(true);
        mStartVisible = true;

        mEndMarker = (MarkerView) findViewById(R.id.endmarker);
        mEndMarker.setListener(this);
        mEndMarker.setAlpha(1f);
        mEndMarker.setFocusable(true);
        mEndMarker.setFocusableInTouchMode(true);
        mEndVisible = true;
        enableDisableButtons();

        updateDisplay();
    }

    @Override
    public void markerTouchStart(MarkerView marker, float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialStartPos = mStartPos;
        mTouchInitialEndPos = mEndPos;
    }

    @Override
    public void markerTouchMove(MarkerView marker, float x) {
        float delta = x - mTouchStart;

        if (marker == mStartMarker) {
            mStartPos = trap((int) (mTouchInitialStartPos + delta));
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
        } else {
            mEndPos = trap((int) (mTouchInitialEndPos + delta));
            if (mEndPos < mStartPos)
                mEndPos = mStartPos;
        }

        updateDisplay();
    }

    @Override
    public void markerTouchEnd(MarkerView marker) {
        mTouchDragging = false;
        if (marker == mStartMarker) {
            setOffsetGoalStart();
        } else {
            setOffsetGoalEnd();
        }
    }

    @Override
    public void markerFocus(MarkerView marker) {
        mKeyDown = false;
        if (marker == mStartMarker) {
            setOffsetGoalStartNoUpdate();
        } else {
            setOffsetGoalEndNoUpdate();
        }

        mHandler.postDelayed(new Runnable() {
            public void run() {
                updateDisplay();
            }
        }, 100);
    }

    @Override
    public void markerLeft(MarkerView marker, int velocity) {
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos = trap(mStartPos - velocity);
            mEndPos = trap(mEndPos - (saveStart - mStartPos));
            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            if (mEndPos == mStartPos) {
                mStartPos = trap(mStartPos - velocity);
                mEndPos = mStartPos;
            } else {
                mEndPos = trap(mEndPos - velocity);
            }

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    @Override
    public void markerRight(MarkerView marker, int velocity) {
        Log.d("marker", "markerRight");
        mKeyDown = true;

        if (marker == mStartMarker) {
            int saveStart = mStartPos;
            mStartPos += velocity;
            if (mStartPos > mMaxPos)
                mStartPos = mMaxPos;
            mEndPos += (mStartPos - saveStart);
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalStart();
        }

        if (marker == mEndMarker) {
            mEndPos += velocity;
            if (mEndPos > mMaxPos)
                mEndPos = mMaxPos;

            setOffsetGoalEnd();
        }

        updateDisplay();
    }

    @Override
    public void markerEnter(MarkerView marker) {

    }

    @Override
    public void markerKeyUp() {
        mKeyDown = false;
        updateDisplay();
    }

    @Override
    public void markerDraw() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgPlay:
                onPlay(mStartPos);
                break;
            case R.id.tvNext:
                AppTools.hideKeyboard(EditAudioActivity.this);
                if (!datn.bkdn.com.saywithvideo.network.Tools.isOnline(this)) {
                    Snackbar.make(findViewById(R.id.root), getResources().getString(R.string.internet_connection), Snackbar.LENGTH_LONG).show();
                    return;
                }

                float start = Float.parseFloat(mTvStart.getText().toString());
                float end = Float.parseFloat(mTvEnd.getText().toString());
                if (end - start > MAX_SECOND) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.length_audio_over) + " " + MAX_SECOND + " " + getResources().getString(R.string.second), Toast.LENGTH_SHORT).show();
                    return;
                } else if (end - start < MIN_SECOND) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.length_audio_less_than) + " "
                            + MAX_SECOND + " " + getResources().getString(R.string.second), Toast.LENGTH_SHORT).show();
                } else {
                    createDialog();
                }
                break;
            case R.id.rlBack:
                mIsKeepActivity = true;
                finish();

        }
    }

//    public byte[] fileToByte(String path) {
//        File file = new File(path);
//        int size = (int) file.length();
//        byte[] bytes = new byte[size];
//        try {
//            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
//            buf.read(bytes, 0, bytes.length);
//            buf.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return bytes;
//    }

    private void uploadFile(final String name) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(FirebaseConstant.STORAGE_BUCKET);
        Uri file = Uri.fromFile(new File(mOutputPath));
        StorageReference store = storageRef.child("audios/" + file.getLastPathSegment());
        store.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //Uri uri = taskSnapshot.getDownloadUrl();
                createSound(name, audioName);
            }
        });
    }

    private void createSound(String name, String url) {
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        final String id = Utils.getCurrentUserID(EditAudioActivity.this);

        //  send to server
        FirebaseAudio mAudio = new FirebaseAudio(name, id, ft.format(date),url,mGroupId,0);
        Firebase firebase = mFirebase.child(FirebaseConstant.AUDIO_URL).push();
        firebase.setValue(mAudio, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.audio_coundnt_save), Toast.LENGTH_SHORT).show();
                }
            }
        });

        FirebaseUser f = AppTools.getInfoUser(id);
        mFirebase.child(FirebaseConstant.USER_URL).child(id).child("no_sound").setValue((f.getNo_sound() + 1) + "");
    }

    private void getData() {
        new AsyncTask<Void, Void, ArrayList<FirebaseGroup>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected ArrayList<FirebaseGroup> doInBackground(Void... params) {
                ArrayList<FirebaseGroup> mGroups = new ArrayList<>();
                RealmResults<Group> groups = RealmUtils.getRealmUtils(EditAudioActivity.this).getGroup(EditAudioActivity.this);
                for (Group g : groups) {
                    FirebaseGroup group = new FirebaseGroup(g.getName(), g.getId());
                    mGroups.add(group);
                }
                return mGroups;
            }

            @Override
            protected void onPostExecute(ArrayList<FirebaseGroup> aVoid) {
                super.onPostExecute(aVoid);
                mGroups = aVoid;
                Log.d("sizess",mGroups.size()+"");
                String[] group = new String[mGroups.size()];
                for (int i = 0; i < mGroups.size(); i++) {
                    group[i] = mGroups.get(i).getName();
                }
                createGroupDialog(group);
            }
        }.execute();
    }

    private void createGroupDialog(final String[] groups) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(EditAudioActivity.this);
        alert.setTitle(getString(R.string.alert_group_title));
        alert.setItems(groups, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGroupId = mGroups.get(which).getId();
                dialog.dismiss();
                new EditAudio().execute();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    private void createDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_name_audio);
        dialog.setTitle(getResources().getString(R.string.pick_name_audio));

        TextView tvOK = (TextView) dialog.findViewById(R.id.tvOK);
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
        final EditText edtName = (EditText) dialog.findViewById(R.id.edtnewName);

        tvOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileName = edtName.getText().toString();
                dialog.dismiss();
                getData();
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

    private long getCurrentTime() {
        return System.nanoTime() / 1000000;
    }

    @Override
    public void onBackPressed() {
        mIsKeepActivity = true;
        if (mType.equals("Record")) {
            File file = new File(mFilename);
            file.deleteOnExit();
        }
        super.onBackPressed();
    }

    @Override
    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = getCurrentTime();
    }

    @Override
    public void waveformTouchMove(float x) {
        mOffset = trap((int) (mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    @Override
    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mOffset;

        long elapsedMsec = getCurrentTime() - mWaveformTouchStartMsec;
        if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                        (int) (mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                        seekMsec < mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int) (mTouchStart + mOffset));
            }
        }
    }

    @Override
    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mOffset;
        mFlingVelocity = (int) (-vx);
        updateDisplay();
    }

    @Override
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    private void loadFromFile() {
        mFile = new File(mFilename);

        mLoadingLastUpdateTime = getCurrentTime();
        mLoadingKeepGoing = true;
        mProgressDialog = new ProgressDialog(EditAudioActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle(R.string.progress_dialog_loading);
        mProgressDialog.setCancelable(true);


        mProgressDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        mLoadingKeepGoing = false;
                    }
                });
        mProgressDialog.show();

        final SoundFile.ProgressListener listener =
                new SoundFile.ProgressListener() {
                    public boolean reportProgress(double fractionComplete) {
                        long now = getCurrentTime();
                        if (now - mLoadingLastUpdateTime > 100) {
                            mProgressDialog.setProgress(
                                    (int) (mProgressDialog.getMax() * fractionComplete));
                            mLoadingLastUpdateTime = now;
                        }
                        return mLoadingKeepGoing;
                    }
                };

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(mFilename);
                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepare();
                } catch (IOException e) {
                    mPlayer = null;
                }
            }
        }).start();

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
        mLastDisplayedStartPos = -1;
        mLastDisplayedEndPos = -1;

        mTouchDragging = false;

        mOffset = 0;
        mOffsetGoal = 0;
        mFlingVelocity = 0;
        resetPositions();
        if (mEndPos > mMaxPos)
            mEndPos = mMaxPos;

        mCaption =
                mSoundFile.getFiletype() + ", " +
                        mSoundFile.getSampleRate() + " Hz, " +
                        mSoundFile.getAvgBitrateKbps() + " kbps, " +
                        formatTime(mMaxPos) + " " +
                        getResources().getString(R.string.time_seconds);
        mInfo.setText(mCaption);

        mProgressDialog.dismiss();
        updateDisplay();
    }

    private synchronized void updateDisplay() {
        if (mIsKeepActivity) return;
        if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition();
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mOffset += offsetDelta;

                if (mOffset + mWidth / 2 > mMaxPos) {
                    mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mOffset < 0) {
                    mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mOffset;
            } else {
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
            }
        }

        mWaveformView.setParameters(mStartPos, mEndPos, mOffset);
        mWaveformView.invalidate();

        mStartMarker.setContentDescription(
                getResources().getText(R.string.start_marker) + " " +
                        formatTime(mStartPos));
        mEndMarker.setContentDescription(
                getResources().getText(R.string.end_marker) + " " +
                        formatTime(mEndPos));

        int startX = mStartPos - mOffset - mMarkerLeftInset;
        if (startX + mStartMarker.getWidth() >= 0) {
            if (!mStartVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mStartVisible = true;
                        mStartMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mStartVisible) {
                mStartMarker.setAlpha(0f);
                mStartVisible = false;
            }
            startX = 0;
        }

        int endX = mEndPos - mOffset - mEndMarker.getWidth() + mMarkerRightInset;
        if (endX + mEndMarker.getWidth() >= 0) {
            if (!mEndVisible) {
                // Delay this to avoid flicker
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        mEndVisible = true;
                        mEndMarker.setAlpha(1f);
                    }
                }, 0);
            }
        } else {
            if (mEndVisible) {
                mEndMarker.setAlpha(0f);
                mEndVisible = false;
            }
            endX = 0;
        }

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                startX,
                mMarkerTopOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mStartMarker.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(
                endX,
                mWaveformView.getMeasuredHeight() - mEndMarker.getHeight() - mMarkerBottomOffset,
                -mStartMarker.getWidth(),
                -mStartMarker.getHeight());
        mEndMarker.setLayoutParams(params);
    }

    private void enableDisableButtons() {
        if (mIsPlaying) {
            mImgPlay.setImageResource(android.R.drawable.ic_media_pause);
            mImgPlay.setContentDescription(getResources().getText(R.string.stop));
        } else {
            mImgPlay.setImageResource(android.R.drawable.ic_media_play);
            mImgPlay.setContentDescription(getResources().getText(R.string.play));
        }
    }

    private void resetPositions() {
        mStartPos = mWaveformView.secondsToPixels(0.0);
        mEndPos = mWaveformView.secondsToPixels(15.0);
    }

    private int trap(int pos) {
        if (pos < 0)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        return pos;
    }

    private void setOffsetGoalStart() {
        setOffsetGoal(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalStartNoUpdate() {
        Log.d("111", mStartPos + " " + mWidth);
        setOffsetGoalNoUpdate(mStartPos - mWidth / 2);
    }

    private void setOffsetGoalEnd() {
        setOffsetGoal(mEndPos - mWidth / 2);
    }

    private void setOffsetGoalEndNoUpdate() {
        setOffsetGoalNoUpdate(mEndPos - mWidth / 2);
    }

    private void setOffsetGoal(int offset) {
        setOffsetGoalNoUpdate(offset);
        updateDisplay();
    }

    private void setOffsetGoalNoUpdate(int offset) {
        if (mTouchDragging) {
            return;
        }

        mOffsetGoal = offset;
        if (mOffsetGoal + mWidth / 2 > mMaxPos)
            mOffsetGoal = mMaxPos - mWidth / 2;
        if (mOffsetGoal < 0)
            mOffsetGoal = 0;
    }

    private String formatTime(int pixels) {
        if (mWaveformView != null && mWaveformView.isInitialized()) {
            return formatDecimal(mWaveformView.pixelsToSeconds(pixels));
        } else {
            return "";
        }
    }

    private String formatDecimal(double x) {
        int xWhole = (int) x;
        int xFrac = (int) (100 * (x - xWhole) + 0.5);

        if (xFrac >= 100) {
            xWhole++; //Round up
            xFrac -= 100; //Now we need the remainder after the round up
            if (xFrac < 10) {
                xFrac *= 10; //we need a fraction that is 2 digits long
            }
        }

        if (xFrac < 10)
            return xWhole + ".0" + xFrac;
        else
            return xWhole + "." + xFrac;
    }

    private synchronized void handlePause() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mWaveformView.setPlayback(-1);
        mIsPlaying = false;
        enableDisableButtons();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsKeepActivity = true;
        if (mPlayer.isPlaying()) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    private synchronized void onPlay(int startPosition) {
        if (mIsPlaying) {
            handlePause();
            return;
        }

        if (mPlayer == null) {
            return;
        }

        try {
            mPlayStartMsec = mWaveformView.pixelsToMillisecs(startPosition);
            if (startPosition < mStartPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mStartPos);
            } else if (startPosition > mEndPos) {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mMaxPos);
            } else {
                mPlayEndMsec = mWaveformView.pixelsToMillisecs(mEndPos);
            }
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    handlePause();
                }
            });
            mIsPlaying = true;

            mPlayer.seekTo(mPlayStartMsec);
            mPlayer.start();
            updateDisplay();
            enableDisableButtons();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class EditAudio extends AsyncTask<Void, Void, Void> {

        private float start;
        private float end;
        private SweetAlertDialog mProgressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mProgressBar = new ProgressDialog(EditAudioActivity.this);
//            mProgressBar.setMessage(getResources().getString(R.string.please_wait));
//            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//            mProgressBar.show();

            mProgressBar = new SweetAlertDialog(EditAudioActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            mProgressBar.setTitleText(getResources().getString(R.string.please_wait));
            mProgressBar.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            mProgressBar.setCancelable(false);
            mProgressBar.show();
            start = Float.parseFloat(mTvStart.getText().toString());
            end = Float.parseFloat(mTvEnd.getText().toString());
        }

        @Override
        protected Void doInBackground(Void... params) {
            String folderPath = Constant.DIRECTORY_PATH + Constant.AUDIO;
            AppTools.createFolder(folderPath);
            audioName = "AUDIO_" + AppTools.getDate() + ".m4a";
            mOutputPath = folderPath + audioName;
            try {
                SoundFile soundFile = SoundFile.create(mFilename, null);
                if (soundFile != null) {
                    soundFile.WriteFile(new File(mOutputPath), start, end);
                }
            } catch (IOException | SoundFile.InvalidInputException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mType.equals("Record")) {
                File file = new File(mFilename);
                file.deleteOnExit();
            }
            mProgressBar.dismiss();
            uploadFile(fileName);
            finish();

        }
    }
}
