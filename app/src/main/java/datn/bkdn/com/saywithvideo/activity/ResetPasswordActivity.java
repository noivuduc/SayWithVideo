package datn.bkdn.com.saywithvideo.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.network.Tools;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEdtEmail;
    private TextView mTvResetPassword;
    private ImageView mImgClearEmail;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        init();
        setListener();
    }

    private void setListener() {
        mTvResetPassword.setOnClickListener(this);
        mImgClearEmail.setOnClickListener(this);

        mEdtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = mEdtEmail.getText().toString().trim();
                if (text.equals("")) {
                    mTvResetPassword.setVisibility(View.GONE);
                    mImgClearEmail.setVisibility(View.GONE);
                } else {
                    mImgClearEmail.setVisibility(View.VISIBLE);
                    if (text.length() >= 2) {
                        mTvResetPassword.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    private void init() {
        Firebase.setAndroidContext(this);
        mEdtEmail = (EditText) findViewById(R.id.edtEmail);
        mTvResetPassword = (TextView) findViewById(R.id.tvResetPassword);
        mImgClearEmail = (ImageView) findViewById(R.id.imgClearEmail);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getResources().getString(R.string.please_wait));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvResetPassword:
                datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(ResetPasswordActivity.this);
                if (!Tools.isOnline(getBaseContext())) {
                    if (getCurrentFocus() != null) {
                        Snackbar.make(getCurrentFocus(), getResources().getString(R.string.internet_connection), Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    mProgressDialog.show();
                    Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL);
                    mFirebase.resetPassword(mEdtEmail.getText().toString().trim(), new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            mProgressDialog.dismiss();
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.reset_pass_success), Toast.LENGTH_LONG).show();
                            finish();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.reset_pass_fail), Toast.LENGTH_LONG).show();
                        }
                    });
                }
                break;
            case R.id.imgClearEmail:
                mEdtEmail.setText("");
                break;
        }
    }
}
