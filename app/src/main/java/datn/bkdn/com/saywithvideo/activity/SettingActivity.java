package datn.bkdn.com.saywithvideo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.model.FirebaseConstant;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView mTvFullName;
    private TextView mTvEmail;
    private static final String FULLNAME = "Add your name ...";
    private static final String EMAIL = "Verify your mail";
    private static final String CHANGE_PASSWORD = "Add your new password ...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();

        mTvFullName.setText(Utils.getCurrentUserName(this));
        mTvEmail.setText(Utils.getCurrentUserEmail(this));
    }

    private void init() {
        LinearLayout mLlFullName = (LinearLayout) findViewById(R.id.llFullName);
        LinearLayout mLlEmail = (LinearLayout) findViewById(R.id.llEmail);
        TextView mTvChangePassword = (TextView) findViewById(R.id.tvChangePassword);
        TextView mTvLogout = (TextView) findViewById(R.id.tvLogout);
        mTvFullName = (TextView) findViewById(R.id.tvFullName);
        mTvEmail = (TextView) findViewById(R.id.tvEmail);

        mLlFullName.setOnClickListener(this);
        mLlEmail.setOnClickListener(this);
        mTvChangePassword.setOnClickListener(this);
        mTvLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llFullName:
                if (showMessage()) {
                    showDialog(FULLNAME);
                }
                break;
            case R.id.llEmail:
                if (showMessage()) {
                    showDialog(EMAIL);
                }
                break;
            case R.id.tvChangePassword:
                if (showMessage()) {
                    showChangePasswordDialog();
                }
                break;
            case R.id.tvLogout:
                Utils.clearPref(SettingActivity.this);
                startActivity(new Intent(SettingActivity.this, LoginActivity.class));
                break;
        }
    }

    private boolean showMessage() {
        if (Tools.isOnline(this) && getCurrentFocus() != null) {
            Snackbar.make(getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showDialog(final String title) {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_setting);
        dialog.setTitle(title);

        TextView mTvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
        final EditText mEdtContent = (EditText) dialog.findViewById(R.id.edtContent);
        TextView mTvSave = (TextView) dialog.findViewById(R.id.tvSave);
        final ImageView mImgClear = (ImageView) dialog.findViewById(R.id.imgClear);

        dialog.show();

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        mTvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (title) {
                    case FULLNAME:
                        Log.d("Tien", "Save fullname");
                        break;
                    case EMAIL:
                        Log.d("Tien", "Save email");
                        break;
                }
                dialog.dismiss();
            }
        });

        mImgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtContent.setText("");
            }
        });

        mEdtContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEdtContent.getText().toString().equals("")) {
                    mImgClear.setVisibility(View.VISIBLE);
                } else {
                    mImgClear.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void showChangePasswordDialog() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_change_password);
        dialog.setTitle(CHANGE_PASSWORD);

        TextView mTvCancel = (TextView) dialog.findViewById(R.id.tvCancel);
        final EditText mEdtOldPass = (EditText) dialog.findViewById(R.id.edtOldPass);
        final EditText mEdtNewPass = (EditText) dialog.findViewById(R.id.edtNewPass);
        TextView mTvSave = (TextView) dialog.findViewById(R.id.tvSave);
        final ImageView mImgClearOld = (ImageView) dialog.findViewById(R.id.imgClearOld);
        final ImageView mImgClearNew = (ImageView) dialog.findViewById(R.id.imgClearNew);

        dialog.show();

        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        mTvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPass = mEdtOldPass.getText().toString();
                String newPass = mEdtNewPass.getText().toString();
                if (oldPass.equals("")) {
                    Toast.makeText(getBaseContext(), "Add your old password", Toast.LENGTH_SHORT).show();
                    datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(SettingActivity.this);
                } else if (newPass.equals("")) {
                    Toast.makeText(getBaseContext(), "Add your new password", Toast.LENGTH_SHORT).show();
                    datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(SettingActivity.this);
                } else {
                    if (showMessage()) {
                        Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL);
                        mFirebase.changePassword(Utils.getCurrentUserEmail(getBaseContext()), oldPass, newPass, new Firebase.ResultHandler() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getBaseContext(), "Change password success.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(FirebaseError firebaseError) {
                                Toast.makeText(getBaseContext(), "Change password error.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    dialog.dismiss();
                }
            }
        });

        mImgClearOld.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtOldPass.setText("");
            }
        });

        mImgClearNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtNewPass.setText("");
            }
        });

        mEdtOldPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEdtOldPass.getText().toString().equals("")) {
                    mImgClearOld.setVisibility(View.VISIBLE);
                } else {
                    mImgClearOld.setVisibility(View.INVISIBLE);
                }
            }
        });

        mEdtNewPass.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEdtNewPass.getText().toString().equals("")) {
                    mImgClearNew.setVisibility(View.VISIBLE);
                } else {
                    mImgClearNew.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
