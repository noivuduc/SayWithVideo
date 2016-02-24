package datn.bkdn.com.saywithvideo.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.regex.Pattern;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.User;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tvLogin;
    private EditText edtEmail;
    private EditText edtPass;
    private ImageView clearPass;
    private ImageView clearEmail;
    private TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(!checkCurrentUser())
        {
            startActivity(new Intent(this,MainActivity.class));
            this.finish();
        }
        init();
    }

    private boolean checkCurrentUser(){
        if(Utils.getCurrentUserEmail(this).equals("null")) return true;
        return false;
    }

    private void init() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPass = (EditText) findViewById(R.id.edtpass);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvRegister = (TextView) findViewById(R.id.tvregister);
        clearEmail = (ImageView) findViewById(R.id.imgClearEmail);
        clearPass = (ImageView) findViewById(R.id.imgClearPass);
        tvLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
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
                    if (s.toString().length() >= 6) {
                        tvLogin.setVisibility(View.VISIBLE);
                    } else {
                        tvLogin.setVisibility(View.GONE);
                    }
                    clearPass.setVisibility(View.VISIBLE);
                }
            }
        });
        clearEmail.setOnClickListener(this);
        clearPass.setOnClickListener(this);
        getEmail();
    }

    private void getEmail() {
       edtEmail.setText(Utils.getPrimaryEmail(this));
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
            case R.id.tvLogin:
                checkisValidAccount(edtEmail.getText().toString(),edtPass.getText().toString());
                break;
            case R.id.tvregister:
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
                break;
        }
    }

    private void checkisValidAccount(String email, String pass) {
        User user =  RealmUtils.getRealmUtils(LoginActivity.this).checkisValidAccount(LoginActivity.this, email, pass);
        if (user == null) {
            Toast.makeText(this,"Email or password is wrong!",Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            Utils.setCurrentUsername(this,user.getName(),user.getEmail(),user.getId());
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

}
