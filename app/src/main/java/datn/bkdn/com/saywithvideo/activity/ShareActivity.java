package datn.bkdn.com.saywithvideo.activity;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.FacebookSdk;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;

import java.io.File;

import datn.bkdn.com.saywithvideo.R;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    private String mFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        init();
    }

    private void init() {
        mFilePath = getIntent().getStringExtra("filePath");
        TextView mLlMessenger = (TextView) findViewById(R.id.llMessenger);
        TextView mTvShareVideo = (TextView) findViewById(R.id.tvShareVideo);
        if (mLlMessenger != null) mLlMessenger.setOnClickListener(this);
        ViewGroup mRlBack = (ViewGroup) findViewById(R.id.rlBack);
        if (mRlBack != null) mRlBack.setOnClickListener(this);
        mTvShareVideo.setTextColor(Color.WHITE);
    }


    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    private void shareVideo() {
        String mimeType = "video/mp4";
        initFacebook();
        Uri contentUri = Uri.fromFile(new File(mFilePath));
        ShareToMessengerParams shareToMessengerParams = ShareToMessengerParams.newBuilder(contentUri, mimeType).build();

        MessengerUtils.shareToMessenger(this, -1, shareToMessengerParams);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llMessenger:
                shareVideo();
                break;
            case R.id.rlBack:
                finish();
                break;
        }
    }
}
