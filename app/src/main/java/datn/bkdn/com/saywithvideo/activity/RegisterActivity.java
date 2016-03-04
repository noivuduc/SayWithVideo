package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.User;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText edtEmail;
    private EditText edtName;
    private EditText edtPass;
    private TextView tvregister;
    private TextView tvLogin;
    private ImageView clearPass;
    private ImageView clearEmail;
    private ImageView clearName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
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
                    if (s.toString().length() > 6) {
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
                }
                clearPass.setVisibility(View.VISIBLE);
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
                if (checkisValidInfo()) {
                    if (RealmUtils.getRealmUtils(this).checkExistsEmail(this, edtEmail.getText().toString())) {
                        Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        String name = edtName.getText().toString();
                        String email = edtEmail.getText().toString();
                        String pass = edtPass.getText().toString();
                        RealmUtils.getRealmUtils(this).addUser(this, name, pass, email);
                        Utils.setCurrentUsername(this, name, email, RealmUtils.getRealmUtils(RegisterActivity.this).getUserWithEmail(RegisterActivity.this, email).get(0).getId());
                        startActivity(new Intent(RegisterActivity.this, RegisterSuccessActivity.class));
                    }
                }
                break;
            case R.id.tvhaveaccount:
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                break;
        }
    }

    private boolean checkisValidInfo() {
        String name = edtName.getText().toString();
        String email = edtEmail.getText().toString();
        String pass = edtPass.getText().toString();

        if (isEmailValid(email) && pass.length() > 6 && name.length() > 0) return true;
        return false;

    }

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

}
