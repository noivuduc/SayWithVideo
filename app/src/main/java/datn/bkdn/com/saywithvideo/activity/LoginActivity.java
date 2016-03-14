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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Arrays;

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
    private TextView tvLoginFacebook;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                String email = "";
                                String name = "";
                                String id = "";
                                try {
                                    email = object.getString("email");
                                    name = object.getString("name");
                                    id = object.getString("id");
                                } catch (JSONException e) {
                                    email = id;
                                }
                                if (RealmUtils.getRealmUtils(LoginActivity.this).checkExistsEmail(LoginActivity.this,email)) {
                                    finish();
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                } else {
                                    RealmUtils.getRealmUtils(LoginActivity.this).addUser(LoginActivity.this, name, "", email);
                                    Utils.setCurrentUsername(LoginActivity.this, name, email, RealmUtils.getRealmUtils(LoginActivity.this).getUserWithEmail(LoginActivity.this, email).get(0).getId());
                                    startActivity(new Intent(LoginActivity.this, RegisterSuccessActivity.class));
                                    finish();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Login facebook failed", Toast.LENGTH_SHORT).show();
            }
        });

        if (!checkCurrentUser()) {
            startActivity(new Intent(this, MainActivity.class));
            this.finish();
        }
        init();
    }

    private boolean checkCurrentUser() {
        if (Utils.getCurrentUserEmail(this).equals("null")) return true;
        return false;
    }

    private void init() {
        edtEmail = (EditText) findViewById(R.id.edtEmail);
        edtPass = (EditText) findViewById(R.id.edtpass);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        tvRegister = (TextView) findViewById(R.id.tvregister);
        clearEmail = (ImageView) findViewById(R.id.imgClearEmail);
        clearPass = (ImageView) findViewById(R.id.imgClearPass);
        tvLoginFacebook = (TextView) findViewById(R.id.tvLoginFacebook);
        tvLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvLoginFacebook.setOnClickListener(this);
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
                checkisValidAccount(edtEmail.getText().toString(), edtPass.getText().toString());
                break;
            case R.id.tvregister:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                break;
            case R.id.tvLoginFacebook:
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile", "user_friends", "email"));
                break;
        }
    }

    private void checkisValidAccount(String email, String pass) {
        User user = RealmUtils.getRealmUtils(LoginActivity.this).checkisValidAccount(LoginActivity.this, email, pass);
        if (user == null) {
            Toast.makeText(this, "Email or password is wrong!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Utils.setCurrentUsername(this, user.getName(), user.getEmail(), user.getId());
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
