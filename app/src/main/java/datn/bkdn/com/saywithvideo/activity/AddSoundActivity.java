package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import datn.bkdn.com.saywithvideo.R;

public class AddSoundActivity extends AppCompatActivity {
    private LinearLayout llRecord;
    private LinearLayout llImport;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_sound);
        llRecord = (LinearLayout) findViewById(R.id.llRecord);
        llImport = (LinearLayout) findViewById(R.id.llImport);

        llRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddSoundActivity.this,RecordNewSoundActivity.class));
            }
        });
    }
}
