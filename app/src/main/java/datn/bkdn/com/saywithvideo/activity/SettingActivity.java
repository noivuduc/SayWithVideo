package datn.bkdn.com.saywithvideo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String FULLNAME = "Add your name ...";
    private static final String EMAIL = "Verify your mail";
    private static final String CHANGE_PASSWORD = "Add your new password ...";
    private TextView mTvFullName;
    private TextView mTvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();
    }

    private void init() {
        if (Tools.isOnline(SettingActivity.this)) {
            Firebase.setAndroidContext(SettingActivity.this);
        }
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

        String email = Utils.getCurrentUserEmail(this);
        if (email.equals("")) {
            mLlEmail.setEnabled(false);
            mTvChangePassword.setEnabled(false);
        } else {
            mTvEmail.setText(email);
        }
        mTvFullName.setText(Utils.getCurrentUserName(this));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llFullName:
                if (showMessage()) {
                    showChangeFullNameDialog();
                }
                break;
            case R.id.llEmail:
                if (showMessage()) {
                    showChangeEmailDialog();
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
        if (!Tools.isOnline(SettingActivity.this)) {
            Snackbar.make(findViewById(R.id.root), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showChangeFullNameDialog() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_setting);
        dialog.setTitle(FULLNAME);

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
                String fullName = mEdtContent.getText().toString().trim();
                if (fullName.equals("")) {
                    Snackbar.make(findViewById(R.id.root), "Add your name", Snackbar.LENGTH_LONG).show();
                    datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(SettingActivity.this, dialog.getCurrentFocus());
                } else {
                    changeFullName(mEdtContent.getText().toString());
                    dialog.dismiss();
                }
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

    private void changeFullName(final String content) {
        Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL);
        FirebaseUser user = new FirebaseUser();
        user.setName(content);
        mFirebase.child(Utils.getCurrentUserID(this)).setValue(user, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError == null) {
                    mTvFullName.setText(content);
                    Utils.updateCurrentUserName(SettingActivity.this, content);
                    Toast.makeText(SettingActivity.this, "Change full name success.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(SettingActivity.this, "Change full name error.", Toast.LENGTH_LONG).show();
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

    private void showChangeEmailDialog() {
        final Dialog dialog = new Dialog(SettingActivity.this);
        dialog.setContentView(R.layout.dialog_change_email);
        dialog.setTitle(EMAIL);

        final EditText mEdtPassword = (EditText) dialog.findViewById(R.id.edtPassword);
        final EditText mEdtNewMail = (EditText) dialog.findViewById(R.id.edtNewMail);
        final ImageView mImgClearPassword = (ImageView) dialog.findViewById(R.id.imgClearPassword);
        final ImageView mImgClearNewMail = (ImageView) dialog.findViewById(R.id.imgClearNewMail);
        TextView mTvSave = (TextView) dialog.findViewById(R.id.tvSave);
        TextView mTvCancel = (TextView) dialog.findViewById(R.id.tvCancel);

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
                String password = mEdtPassword.getText().toString().trim();
                final String newEmail = mEdtNewMail.getText().toString().trim();
                if (password.equals("")) {
                    Snackbar.make(findViewById(R.id.root), "Add your password", Snackbar.LENGTH_SHORT).show();
                } else if (newEmail.equals("")) {
                    Snackbar.make(findViewById(R.id.root), "Add your new email", Snackbar.LENGTH_SHORT).show();
                } else {
                    Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL);
                    mFirebase.changeEmail(Utils.getCurrentUserEmail(SettingActivity.this), password, newEmail, new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            Utils.updateCurrentEmail(SettingActivity.this, newEmail);
                            mTvEmail.setText(newEmail);
                            Toast.makeText(getBaseContext(), "Change email success.", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getBaseContext(), "Change email error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                }
                datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(SettingActivity.this, dialog.getCurrentFocus());
            }
        });

        mImgClearPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtPassword.setText("");
            }
        });
        mImgClearNewMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdtNewMail.setText("");
            }
        });

        mEdtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEdtPassword.getText().toString().equals("")) {
                    mImgClearPassword.setVisibility(View.VISIBLE);
                } else {
                    mImgClearPassword.setVisibility(View.INVISIBLE);
                }
            }
        });
        mEdtNewMail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mEdtNewMail.getText().toString().equals("")) {
                    mImgClearNewMail.setVisibility(View.VISIBLE);
                } else {
                    mImgClearNewMail.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}
