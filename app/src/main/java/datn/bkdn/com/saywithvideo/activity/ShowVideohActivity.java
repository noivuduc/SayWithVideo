package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import datn.bkdn.com.saywithvideo.R;

public class ShowVideohActivity extends AppCompatActivity implements View.OnClickListener{

    private RelativeLayout mRlBack;
    private VideoView mVideoView;
    private String mVideoPath;
    private TextView mtvShare;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_videoh);

        mVideoPath=getIntent().getStringExtra("VideoPath");
        init();

        mVideoView.setVideoPath(mVideoPath);
//        mVideoView.setRotation(90);
        mVideoView.start();
    }

    private void init() {
        mRlBack= (RelativeLayout) findViewById(R.id.rlBack);
        mVideoView= (VideoView) findViewById(R.id.videoView);
        mtvShare = (TextView) findViewById(R.id.tvShare);
        mRlBack.setOnClickListener(this);
        mtvShare.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rlBack:
                finish();
                break;
            case R.id.tvShare:
                Intent i = new Intent(ShowVideohActivity.this,ShareActivity.class);
                i.putExtra("filePath",mVideoPath);
                startActivity(i);
                break;
        }
    }
}
