package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Button bt = (Button) findViewById(R.id.logout);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.clearPref(SettingActivity.this);
                startActivity(new Intent(SettingActivity.this,LoginActivity.class));
            }
        });
    }
}
