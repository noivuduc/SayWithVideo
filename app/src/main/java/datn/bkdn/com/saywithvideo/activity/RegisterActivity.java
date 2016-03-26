package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
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

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEmail;
    private EditText edtName;
    private EditText edtPass;
    private TextView tvregister;
    private TextView tvLogin;
    private ImageView clearPass;
    private ImageView clearEmail;
    private ImageView clearName;
    private Firebase mUserFire;

    private static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Firebase.setAndroidContext(this);
        init();
    }

    private void init() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPass = (EditText) findViewById(R.id.edtpass);
        edtName = (EditText) findViewById(R.id.edtName);

        tvregister = (TextView) findViewById(R.id.tvRegisterUser);
        tvLogin = (TextView) findViewById(R.id.tvhaveaccount);

        clearEmail = (ImageView) findViewById(R.id.imgClearEmail);
        clearName = (ImageView) findViewById(R.id.imgClearName);
        clearPass = (ImageView) findViewById(R.id.imgClearPass);

        edtEmail.setText(Utils.getPrimaryEmail(this));
        tvregister.setOnClickListener(this);
        tvLogin.setOnClickListener(this);

        clearEmail.setOnClickListener(this);
        clearName.setOnClickListener(this);
        clearPass.setOnClickListener(this);

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtEmail.getText().toString().equals("")) {
                    clearEmail.setVisibility(View.GONE);
                } else {
                    if (checkisValidInfo()) {
                        tvregister.setVisibility(View.VISIBLE);
                    } else {
                        tvregister.setVisibility(View.GONE);
                    }
                    clearEmail.setVisibility(View.VISIBLE);
                }
            }
        });
        edtPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    clearPass.setVisibility(View.GONE);
                } else {
                    if (checkisValidInfo()) {
                        tvregister.setVisibility(View.VISIBLE);
                    } else {
                        tvregister.setVisibility(View.GONE);
                    }
                    clearPass.setVisibility(View.VISIBLE);
                }
            }
        });
        edtName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")) {
                    clearName.setVisibility(View.GONE);
                } else {
                    if (checkisValidInfo()) {
                        tvregister.setVisibility(View.VISIBLE);
                    } else {
                        tvregister.setVisibility(View.GONE);
                    }
                    clearName.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgClearEmail:
                edtEmail.setText("");
                break;
            case R.id.imgClearPass:
                edtPass.setText("");
                break;
            case R.id.imgClearName:
                edtName.setText("");
                break;
            case R.id.tvRegisterUser:
                if (!Tools.isOnline(getBaseContext())) {
                    Snackbar.make(getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                    return;
                }
                final String name = edtName.getText().toString();
                final String email = edtEmail.getText().toString();
                final String pass = edtPass.getText().toString();

                mUserFire = new Firebase(Constant.FIREBASE_ROOT);
                mUserFire.createUser(email, pass, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> stringObjectMap) {
                        Intent i = new Intent(RegisterActivity.this, RegisterSuccessActivity.class);
                        mUserFire.authWithPassword(email, pass, new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(AuthData authData) {
                                Utils.setCurrentUsername(RegisterActivity.this, name, email, authData.getUid());
                                HashMap<String, String> map = new HashMap<String, String>();
                                map.put("name", name);
                                mUserFire.child("users").child(authData.getUid()).setValue(map);
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {

                            }
                        });

                        startActivity(i);
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        switch (firebaseError.getCode()) {
                            case FirebaseError.EMAIL_TAKEN:
                                Toast.makeText(RegisterActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.INVALID_EMAIL:
                                Toast.makeText(RegisterActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                break;
                            case FirebaseError.INVALID_PASSWORD:
                                Toast.makeText(RegisterActivity.this, firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(RegisterActivity.this);
                break;
            case R.id.tvhaveaccount:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                break;
        }
    }

    private boolean checkisValidInfo() {
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String pass = edtPass.getText().toString().trim();

        if (isEmailValid(email) && pass.length() > 6 && name.length() > 0) return true;
        return false;

    }

}
