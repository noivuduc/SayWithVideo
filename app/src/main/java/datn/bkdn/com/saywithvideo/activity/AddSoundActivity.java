package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import datn.bkdn.com.saywithvideo.R;

public class AddSoundActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* noi oc cho*/
        setContentView(R.layout.activity_add_sound);
        LinearLayout mLlRecord = (LinearLayout) findViewById(R.id.llRecord);
        LinearLayout mLlImport = (LinearLayout) findViewById(R.id.llImport);
        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);

        TextView tvImport = (TextView) findViewById(R.id.tvimport);
        TextView tvrecord = (TextView) findViewById(R.id.tvrecord);
        TextView tvadd = (TextView) findViewById(R.id.tvtitleaddsound);

        tvImport.setTextColor(Color.WHITE);
        tvrecord.setTextColor(Color.WHITE);
        tvadd.setTextColor(Color.WHITE);
        if (mLlRecord != null) mLlRecord.setOnClickListener(this);
        if (mLlImport != null) mLlImport.setOnClickListener(this);
        if (mRlBack != null) mRlBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llRecord:
                startActivity(new Intent(AddSoundActivity.this, RecordNewSoundActivity.class));
                finish();
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
            case R.id.llImport:
                startActivity(new Intent(AddSoundActivity.this, ImportSoundActivity.class));
                finish();
                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                break;
            case R.id.rlBack:
                finish();
                break;
        }
    }
}
