package datn.bkdn.com.saywithvideo.activity;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.FacebookSdk;
import com.facebook.messenger.MessengerUtils;
import com.facebook.messenger.ShareToMessengerParams;

import java.io.File;

import datn.bkdn.com.saywithvideo.R;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout llMessenger;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        init();
    }

    private void init() {
        filePath = getIntent().getStringExtra("filePath");
        llMessenger = (LinearLayout) findViewById(R.id.llMessenger);
        llMessenger.setOnClickListener(this);
    }


    private void initFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
    }

    private String mimeType = "video/mp4";

    private void shareVideo() {
        initFacebook();
        Uri contentUri = Uri.fromFile(new File(filePath));
        ShareToMessengerParams shareToMessengerParams = ShareToMessengerParams.newBuilder(contentUri, mimeType).build();

        MessengerUtils.shareToMessenger(this,-1,shareToMessengerParams);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.llMessenger:
                shareVideo();
                break;
        }
    }
}
