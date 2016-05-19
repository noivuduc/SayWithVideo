package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListFavoriteAdapter;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class FavoriteActivity extends AppCompatActivity implements View.OnClickListener, RealmChangeListener {
    private ArrayList<Audio> mAdapterItems;
    private ArrayList<String> mAdapterKeys;
    private ListFavoriteAdapter mAdapter;
    private RelativeLayout mRlBack;
    private RelativeLayout mRlSort;
    private TextView mTvSearch;
    private MediaPlayer mPlayer;
    private RecyclerView mLvSound;
    private Realm realm;
    private String mFilePath;
    private Firebase mFirebaseFavorite;
    private ImageView mImgSort;
    private int mCurrentPos = -1;
    private SweetAlertDialog mProgressDialog;
    private RealmResults<Sound> mSounds;
    private Intent mIntentUnFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        init();

    }


    private void initData() {
        if (mAdapterItems == null) {
            mAdapterItems = new ArrayList<>();
        }
        if (mAdapterKeys == null) {
            mAdapterKeys = new ArrayList<>();
        }

        realm = RealmManager.getRealm(this);
        mSounds = realm.where(Sound.class).equalTo("isFavorite", true).findAll();
        for (Sound s : mSounds) {
            Audio audio = convertAudio(s);
            mAdapterItems.add(audio);
            mAdapterKeys.add(audio.getId());
        }
    }

    private Audio convertAudio(Sound sound) {
        return new Audio(sound.getLinkDown(), sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite());
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

    private void playMp3(String path) {
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
        initData();
        boolean isOnline = Tools.isOnline(this);
        mFirebaseFavorite = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(this) + "/favorite");
        mAdapter = new ListFavoriteAdapter(mFirebaseFavorite, null, isOnline, Audio.class, mAdapterItems, mAdapterKeys, this);
        mLvSound = (RecyclerView) findViewById(R.id.lvSoundFavorite);
        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mRlSort = (RelativeLayout) findViewById(R.id.rlSort);
        mImgSort = (ImageView) findViewById(R.id.imgSort);
        mTvSearch = (TextView) findViewById(R.id.edtSearch);
        mTvSearch.setTextColor(Color.WHITE);
        mLvSound.setHasFixedSize(true);
        mLvSound.setLayoutManager(new LinearLayoutManager(this));
        setEvent();
    }

    private void setEvent() {
        mRlBack.setOnClickListener(this);
        mRlSort.setOnClickListener(this);
        mTvSearch.setOnClickListener(this);
        mAdapter.setPlayButtonClicked(new ListFavoriteAdapter.OnItemClicked() {
            @Override
            public void onClick(final Audio sound, View v, final int pos) {
                final String audioId = sound.getId();
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if (mCurrentPos != -1 && pos != mCurrentPos) {
                            Audio sound1 = mAdapter.getItems().get(mCurrentPos);
                            if (sound1.isPlaying()) {
                                sound1.setIsPlaying(!sound1.isPlaying());
                                if (sound1.isLoadAudio()) {
                                    sound1.setLoadAudio(false);
                                }
                                mAdapter.notifyDataSetChanged();
                                if (mPlayer != null) {
                                    mPlayer.stop();
                                }
                            }
                        }
                        mCurrentPos = pos;
                        if (sound.isPlaying()) {
                            sound.setIsPlaying(false);
                            mPlayer.stop();
                            mPlayer.reset();
                            mAdapter.notifyDataSetChanged();
                        } else {
                            if (sound.getLink_on_Disk() == null) {
                                if (!Tools.isOnline(FavoriteActivity.this)) {
                                    Snackbar.make(getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                                    break;
                                }
                                /**
                                 * download sound
                                 */
                                sound.setLoadAudio(true);
                                mAdapter.notifyDataSetChanged();
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference reference = storage.getReferenceFromUrl(FirebaseConstant.STORAGE_BUCKET).child("audios/" + sound.getUrl());
                                final File file = AppTools.getFile();
                                FileDownloadTask downloadTask = reference.getFile(file);
                                downloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        mFilePath = file.getPath();
                                        sound.setLink_on_Disk(mFilePath);
                                        new AsyncUpdatePath().execute(sound.getId(), sound.getLink_on_Disk());
                                        if (mCurrentPos == pos) {
                                            sound.setLoadAudio(false);
                                            sound.setIsPlaying(!sound.isPlaying());
                                            playMp3(mFilePath);
                                        }
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        AppTools.showSnackBar(getResources().getString(R.string.resource_not_found), FavoriteActivity.this);
                                        sound.setLoadAudio(false);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });

                            } else {
                                mFilePath = sound.getLink_on_Disk();
                                sound.setIsPlaying(!sound.isPlaying());
                                mAdapter.notifyDataSetChanged();
                                playMp3(mFilePath);
                            }
                            new AsyncUpdatePlay().execute(audioId, sound.getPlays() + 1 + "");

                        }

                        //  mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                        if (mIntentUnFavorite == null) {
                            mIntentUnFavorite = new Intent();
                        }
                        try {
                            final String id = sound.getId();
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    sound.setLoadFavorite(true);
                                    mAdapter.notifyDataSetChanged();
                                }

                                @Override
                                protected Void doInBackground(Void... params) {
                                    RealmUtils.getRealmUtils(FavoriteActivity.this).updateFavorite(FavoriteActivity.this, audioId);
                                    mFirebaseFavorite.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)) {
                                                mFirebaseFavorite.child(id).removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    sound.setLoadFavorite(false);
                                    mAdapter.notifyDataSetChanged();
                                }
                            }.execute();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        if (mCurrentPos != -1 && pos != mCurrentPos) {
                            Audio sound1 = mAdapter.getItems().get(mCurrentPos);
                            if (sound1.isPlaying()) {
                                sound1.setIsPlaying(!sound1.isPlaying());
                                mAdapter.notifyDataSetChanged();
                                if (mPlayer != null) {
                                    mPlayer.stop();
                                }
                            }
                        }

                        if ((sound.getLink_on_Disk()) != null) {
                            mFilePath = sound.getLink_on_Disk();
                            finishActivity();
                        } else {
                            if (!Tools.isOnline(FavoriteActivity.this)) {
                                AppTools.showSnackBar(getResources().getString(R.string.internet_connection), FavoriteActivity.this);
                                break;
                            }
                            showProgressDialog();
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference reference = storage.getReferenceFromUrl(FirebaseConstant.STORAGE_BUCKET).child("audios/" + sound.getUrl());
                            final File file = AppTools.getFile();
                            reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    dimissProgressDialog();
                                    mFilePath = file.getPath();
                                    sound.setLink_on_Disk(mFilePath);
                                    new AsyncUpdatePath().execute(sound.getId(), sound.getLink_on_Disk());
                                    finishActivity();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    AppTools.showSnackBar(getResources().getString(R.string.resource_not_found), FavoriteActivity.this);
                                    dimissProgressDialog();
                                }
                            });
                        }

                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }

        });

        mLvSound.setAdapter(mAdapter);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(FavoriteActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            mProgressDialog.setTitleText(getResources().getString(R.string.please_wait));
            mProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    private void dimissProgressDialog() {
        mProgressDialog.dismiss();
    }

    @Override
    public void onChange(Object element) {

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

    private class AsyncUpdatePath extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Realm realm = RealmManager.getRealm(FavoriteActivity.this);
            realm.beginTransaction();
            Sound sound = realm.where(Sound.class).equalTo("id", params[0]).findFirst();
            sound.setLinkOnDisk(params[1]);
            realm.commitTransaction();
            realm.close();
            return null;
        }
    }

    private class AsyncUpdatePlay extends AsyncTask<String, Void, Void> {

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