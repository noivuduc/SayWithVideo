package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class RegisterSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        final Firebase ref = new Firebase(Constant.FIREBASE_ROOT);
        setContentView(R.layout.activity_register_success);
        TextView tvContinue = (TextView) findViewById(R.id.tvContinue);
        tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=getIntent();
                final String name = intent.getStringExtra("name");
                final String email = intent.getStringExtra("email");
                String pass = intent.getStringExtra("pass");

                ref.authWithPassword(email, pass, new Firebase.AuthResultHandler() {
                    @Override
                    public void onAuthenticated(AuthData authData) {
                        Utils.setCurrentUsername(RegisterSuccessActivity.this, name, email, authData.getUid());
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put("name", name);
                        ref.child("users").child(authData.getUid()).setValue(map);
                    }

                    @Override
                    public void onAuthenticationError(FirebaseError firebaseError) {

                    }
                });
                startActivity(new Intent(RegisterSuccessActivity.this, MainActivity.class));
            }
        });
    }
}
