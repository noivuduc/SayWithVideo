package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import datn.bkdn.com.saywithvideo.database.FavoriteAudio;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class FavoriteActivity extends AppCompatActivity implements View.OnClickListener, RealmChangeListener {
    private ListSoundAdapter mAdapter;
    private RelativeLayout mRlBack;
    private RelativeLayout mRlSort;
    private EditText mTvSearch;
    private MediaPlayer mPlayer;
    private ListView mLvSound;
    private Realm realm;
    private String mFilePath;
    private Firebase mFirebaseFavorite;
    private ImageView mImgSort;
    private int mCurrentPos = -1;
    private RealmResults<Sound> mSounds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        init();

    }

    @Override
    protected void onPause() {
        super.onPause();
        realm.close();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    private void loadData() {
        realm = RealmManager.getRealm(this);
        mSounds = realm.where(Sound.class).equalTo("isFavorite", true).findAll();
        mSounds.addChangeListener(this);
    }

    @Override
    public void onChange() {
        mAdapter.notifyDataSetChanged();
    }

    private void init() {
        loadData();
        mAdapter = new ListSoundAdapter(this, mSounds);
        mFirebaseFavorite = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(this) + "/favorite/");
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
        mAdapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                final Sound sound = mSounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        final String audioId = sound.getId();


                        if (mCurrentPos != -1 && pos != mCurrentPos) {
                            Sound sound1 = mSounds.get(mCurrentPos);
                            if (sound1.isPlaying()) {
                                //sound1.setIsPlaying(!sound1.isPlaying());
                                String id = mSounds.get(mCurrentPos).getId();
                                new AsyncUpdatePlaying().execute(id);
                                mPlayer.stop();
                            }
                        }
                        mCurrentPos = pos;
                        if (sound.isPlaying()) {
                            mPlayer.stop();
                            mPlayer.reset();
                        } else {
                            if (sound.getLinkOnDisk() == null) {
                                getPath(audioId);
                                sound.setLinkOnDisk(mFilePath);
                            } else {
                                mFilePath = sound.getLinkOnDisk();
                                playMp3(mFilePath);
                            }
                            new AsyncUpdatePlay().execute(audioId, sound.getPlays() + 1 + "");
//                                playMp3(mFilePath);
                        }
                        new AsyncUpdatePlaying().execute(sound.getId());
                        // sound.setIsPlaying(!sound.isPlaying());

                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                        if (Tools.isOnline(FavoriteActivity.this)) {

                        }
                        String id = Utils.getCurrentUserID(FavoriteActivity.this);
                        final FirebaseUser f = AppTools.getInfoUser(Utils.getCurrentUserID(FavoriteActivity.this));
                        Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + id).child("favorite");
                        favoriteFirebase.child(sound.getId()).removeValue();
                        final Firebase ff = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + id + "/");
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                ff.child("no_favorite").setValue(f.getNo_favorite() - 1);
                                return null;
                            }
                        }.execute();
                        new AsyncTask<String, Void, Void>() {
                            @Override
                            protected Void doInBackground(String... params) {
                                RealmUtils.getRealmUtils(FavoriteActivity.this).updateFavorite(FavoriteActivity.this, params[0]);
                                return null;
                            }
                        }.execute(sound.getId());
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        if ((sound.getLinkOnDisk()) != null) {
                            mFilePath = sound.getLinkOnDisk();

                            Intent intent = new Intent(FavoriteActivity.this, CaptureVideoActivity.class);
                            intent.putExtra("FilePath", mFilePath);
                            startActivity(intent);
                        } else {
                            new AsyncTask<Void, Void, String>() {

                                @Override
                                protected String doInBackground(Void... params) {
                                    String f = AppTools.getContentAudio(sound.getId(), FavoriteActivity.this);
                                    return f;
                                }

                                @Override
                                protected void onPostExecute(String aVoid) {
                                    super.onPostExecute(aVoid);
                                    mFilePath = aVoid;
                                    sound.setLinkOnDisk(mFilePath);
                                    Intent intent = new Intent(FavoriteActivity.this, CaptureVideoActivity.class);
                                    intent.putExtra("FilePath", mFilePath);
                                    startActivity(intent);
                                }
                            }.execute();
                        }
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

    private void getPath(final String audioId) {
        new AsyncTask<Void, String, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String path = AppTools.getContentAudio(audioId, FavoriteActivity.this);
                return path;
            }

            @Override
            protected void onPostExecute(String aVoid) {
                super.onPostExecute(aVoid);
                mFilePath = aVoid;
                playMp3(mFilePath);
            }
        }.execute();
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

    class AsyncAddSound extends AsyncTask<FavoriteAudio, Void, Void> {

        @Override
        protected Void doInBackground(FavoriteAudio... sound) {
            RealmUtils.getRealmUtils(FavoriteActivity.this).addNewFavorite(FavoriteActivity.this, sound[0]);
            return null;
        }
    }

    class AsyncUpdatePlaying extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];
            RealmUtils.getRealmUtils(FavoriteActivity.this).updatePlaying(FavoriteActivity.this, id);
            return null;
        }
    }

    public class AsyncUpdatePlay extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String audioId = params[0];
            String plays = params[1];
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
            firebase.child(audioId).child("plays").setValue(plays);
            return null;
        }
    }
}
