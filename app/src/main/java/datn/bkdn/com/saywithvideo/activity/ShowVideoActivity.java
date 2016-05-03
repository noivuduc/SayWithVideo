package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;

public class ShowVideoActivity extends AppCompatActivity implements View.OnClickListener, TextureView.SurfaceTextureListener {

    private static final String TAG = "Tien";
    private String mVideoPath;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        mVideoPath = getIntent().getStringExtra("VideoPath");
        init();
    }

    private void init() {
        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        TextureView mTextureView = (TextureView) findViewById(R.id.playback_video);
        TextView mTvShare = (TextView) findViewById(R.id.tvShare);

        if (mRlBack != null) mRlBack.setOnClickListener(this);
        if (mTvShare != null) mTvShare.setOnClickListener(this);
        if (mTextureView != null) mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                finish();
                break;
            case R.id.tvShare:
                Intent i = new Intent(ShowVideoActivity.this, ShareActivity.class);
                i.putExtra("filePath", mVideoPath);
                startActivity(i);
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Surface surface = new Surface(surfaceTexture);

        try {
            mMediaPlayer = new MediaPlayer();
            try {
                mMediaPlayer.setDataSource(mVideoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setSurface(surface);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();

            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });

        } catch (SecurityException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mVideoPath);
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
