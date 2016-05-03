package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import datn.bkdn.com.saywithvideo.R;

public class AddSoundActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound);
        LinearLayout mLlRecord = (LinearLayout) findViewById(R.id.llRecord);
        LinearLayout mLlImport = (LinearLayout) findViewById(R.id.llImport);
        RelativeLayout mRlBack = (RelativeLayout) findViewById(R.id.rlBack);

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
                break;
            case R.id.llImport:
                startActivity(new Intent(AddSoundActivity.this, ImportSoundActivity.class));
                finish();
                break;
            case R.id.rlBack:
                finish();
                break;
        }
    }
}
