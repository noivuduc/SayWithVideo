package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import datn.bkdn.com.saywithvideo.R;

public class AddSoundActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout mLlRecord;
    private LinearLayout mLlImport;
    private RelativeLayout mRlBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound);
        mLlRecord = (LinearLayout) findViewById(R.id.llRecord);
        mLlImport = (LinearLayout) findViewById(R.id.llImport);
        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);

        mLlRecord.setOnClickListener(this);
        mLlImport.setOnClickListener(this);
        mRlBack.setOnClickListener(this);
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
