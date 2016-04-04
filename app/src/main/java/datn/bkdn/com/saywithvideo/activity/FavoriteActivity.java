package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import com.firebase.client.Firebase;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.FirebaseConstant;
import datn.bkdn.com.saywithvideo.model.FirebaseUser;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class FavoriteActivity extends AppCompatActivity implements View.OnClickListener {
    private ListSoundAdapter mAdapter;
    private RelativeLayout mRlBack;
    private RelativeLayout mRlSort;
    private EditText mTvSearch;
    private MediaPlayer mPlayer;
    private ListView mLvSound;
    private ImageView mImgSort;
    private int mCurrentPos = -1;
    private RealmResults<Sound> mSounds;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        init();
        mSounds = RealmUtils.getRealmUtils(this).getFavoriteSound(this);
        mAdapter = new ListSoundAdapter(this, mSounds, false);
        mAdapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = mSounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if (mCurrentPos != -1 && pos != mCurrentPos) {
                            Sound sound1 = mSounds.get(mCurrentPos);
                            if (sound1.isPlaying()) {
                                RealmUtils.getRealmUtils(FavoriteActivity.this).updatePlaying(FavoriteActivity.this, mSounds.get(mCurrentPos).getId());
                                mPlayer.stop();
                            }
                        }
                        mCurrentPos = pos;
                        if (sound.isPlaying()) {
                            mPlayer.stop();
                            mPlayer.reset();
                        } else {
                            playMp3(sound.getLinkOnDisk());
                        }
                        RealmUtils.getRealmUtils(FavoriteActivity.this).updatePlaying(FavoriteActivity.this, mSounds.get(pos).getId());
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
//                        if (Tools.isOnline(FavoriteActivity.this)) {
//
//                        }
                        Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(FavoriteActivity.this)).child("favorite");
                        String id = sound.getId();
                        if (user.getFavorite().contains(id)) {
                            user.getFavorite().remove(id);
                        }
                        favoriteFirebase.setValue(user.getFavorite());
                        RealmUtils.getRealmUtils(FavoriteActivity.this).updateFavorite(FavoriteActivity.this, sound.getId());
                        mSounds = RealmUtils.getRealmUtils(FavoriteActivity.this).getFavoriteSound(FavoriteActivity.this);
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        Intent intent = new Intent(FavoriteActivity.this, CaptureVideoActivity.class);
                        intent.putExtra("FilePath", sound.getLinkOnDisk());
                        startActivity(intent);
                        //:TODO
                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }
        });
        mLvSound.setAdapter(mAdapter);

    }

    public void playMp3(String path) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(path);
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                RealmUtils.getRealmUtils(FavoriteActivity.this).updatePlaying(FavoriteActivity.this, mSounds.get(mCurrentPos).getId());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void init() {
        user = Utils.getFavoriteUser(this);
        mLvSound = (ListView) findViewById(R.id.lvSoundFavorite);
        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mRlSort = (RelativeLayout) findViewById(R.id.rlSort);
        mImgSort = (ImageView) findViewById(R.id.imgSort);
        mTvSearch = (EditText) findViewById(R.id.edtSearch);
        setEvent();
    }

    private void setEvent() {
        mRlBack.setOnClickListener(this);
        mRlSort.setOnClickListener(this);
        mTvSearch.setOnClickListener(this);
    }

    private void createPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.show();
    }

    private void createSortMenu(View v) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.sort_favorite_menu, menu.getMenu());
        menu.show();
    }

    private void finishActivity() {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                finishActivity();
                break;
            case R.id.rlSort:
                createSortMenu(mImgSort);
                break;
            case R.id.edtSearch:
                mTvSearch.setFocusable(true);
                mTvSearch.setFocusableInTouchMode(true);
                break;
        }
    }
}
