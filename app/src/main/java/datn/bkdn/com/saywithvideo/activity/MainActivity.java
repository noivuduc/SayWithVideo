package datn.bkdn.com.saywithvideo.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.MainPagerAdapter;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.PagerSlidingTabStrip;
import datn.bkdn.com.saywithvideo.utils.Utils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ViewPager mPager;
    private NavigationView mNavigationView;
    private static final String FULLNAME = "Add your name ...";
    private static final String EMAIL = "Verify your mail";
    private static final String CHANGE_PASSWORD = "Add your new password ...";
    private TextView mTvName;
    private TextView mTvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase.setAndroidContext(this);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationView != null) mNavigationView.setNavigationItemSelectedListener(this);

        mPager = (ViewPager) findViewById(R.id.pager);
        PagerSlidingTabStrip mTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tab);
        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(adapter);
        if (mTabStrip != null) mTabStrip.setViewPager(mPager);

        initNavigation();
    }

    private void initNavigation() {
        View headerView = mNavigationView.getHeaderView(0);
        mTvName = (TextView) headerView.findViewById(R.id.tvName);
        mTvEmail = (TextView) headerView.findViewById(R.id.tvEmail);
        ImageView mImgMenu = (ImageView) findViewById(R.id.imgMenu);

        mTvName.setText(Utils.getCurrentUserName(this));
        if (Utils.getCurrentUserEmail(this).equals("")) {
            mTvEmail.setText(Utils.getCurrentUserID(this));
        } else {
            mTvEmail.setText(Utils.getCurrentUserEmail(this));
        }
        if (mImgMenu != null) mImgMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer == null) return;
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }
            }
        });
    }

    public void showSounds() {
        mPager.setCurrentItem(0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer == null) return;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_email) {
            if (Utils.getCurrentUserEmail(this).equals("")) {
                showMessage(getResources().getString(R.string.cant_change_email));
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            if (showMessage()) {
                showChangeEmailDialog();
            }
        } else if (id == R.id.nav_favourites) {
            startActivity(new Intent(MainActivity.this, FavoriteActivity.class));
        } else if (id == R.id.nav_fullname) {
            if (showMessage()) {
                showChangeFullNameDialog();
            }
        } else if (id == R.id.nav_logout) {
            Utils.clearPref(MainActivity.this);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (id == R.id.nav_password) {
            if (Utils.getCurrentUserEmail(this).equals("")) {
                showMessage(getResources().getString(R.string.cant_change_password));
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                if (drawer != null) drawer.closeDrawer(GravityCompat.START);
                return true;
            }
            if (showMessage()) {
                showChangePasswordDialog();
            }
        } else if (id == R.id.nav_sounds) {
            startActivity(new Intent(MainActivity.this, SoundActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean showMessage() {
        if (!Tools.isOnline(MainActivity.this)) {
            showMessage(getResources().getString(R.string.internet_connection));
            return false;
        }
        return true;
    }

    private void showChangeFullNameDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
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
                final String fullName = mEdtContent.getText().toString().trim();
                if (fullName.equals("")) {
                    showMessage(getResources().getString(R.string.add_name));
                    datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(MainActivity.this, dialog.getCurrentFocus());
                } else {
                    Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(MainActivity.this) + "/");
                    mFirebase.child("name/").setValue(fullName, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError == null) {
                                mTvName.setText(fullName);
                                Utils.updateCurrentUserName(MainActivity.this, fullName);
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.change_name_success), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.change_name_error), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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

    private void showChangePasswordDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
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
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.add_old_password), Toast.LENGTH_SHORT).show();
                    datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(MainActivity.this);
                } else if (newPass.equals("")) {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.add_new_password), Toast.LENGTH_SHORT).show();
                    datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(MainActivity.this);
                } else {
                    if (showMessage()) {
                        Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL);
                        mFirebase.changePassword(Utils.getCurrentUserEmail(getBaseContext()), oldPass, newPass, new Firebase.ResultHandler() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_success), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(FirebaseError firebaseError) {
                                Toast.makeText(getBaseContext(), getResources().getString(R.string.change_password_error), Toast.LENGTH_SHORT).show();
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

    private void showMessage(String msg) {
        View v = findViewById(R.id.root);
        if (v != null) {
            Snackbar.make(v, msg, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showChangeEmailDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
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
                    showMessage(getResources().getString(R.string.add_password));
                } else if (newEmail.equals("")) {
                    showMessage(getResources().getString(R.string.add_new_email));
                } else {
                    Firebase mFirebase = new Firebase(FirebaseConstant.BASE_URL);
                    mFirebase.changeEmail(Utils.getCurrentUserEmail(MainActivity.this), password, newEmail, new Firebase.ResultHandler() {
                        @Override
                        public void onSuccess() {
                            Utils.updateCurrentEmail(MainActivity.this, newEmail);
                            mTvEmail.setText(newEmail);
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.change_email_success), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.change_email_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                }
                datn.bkdn.com.saywithvideo.utils.Tools.hideKeyboard(MainActivity.this, dialog.getCurrentFocus());
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
