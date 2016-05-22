package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.SoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseGroup;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 *
 */
public class GroupActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener ,View.OnClickListener{
    private int mCurrentPos = -1;
    private MediaPlayer mPlayer;
    private RecyclerView mLvSound;
    private HashMap<String,String> mUrls;
    private Firebase mFirebase;
    File file = null;
    private String mFilePath;
    private String mFileName;
    private ArrayList<Audio> mAdapterItems;
    private ArrayList<String> mAdapterKeys;
    private SoundAdapter mAdapter;
    private SweetAlertDialog mProgressDialog;
    private TextView mTitle;
    private ImageView mImageBack;
    private String mGroupId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_group);
        mLvSound = (RecyclerView) findViewById(R.id.lvSound);
        mTitle = (TextView) findViewById(R.id.tvTitle);
        mImageBack = (ImageView) findViewById(R.id.imgBack);
        mImageBack.setOnClickListener(this);
        ArrayList<String> groups = getIntent().getStringArrayListExtra("data");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setText(groups.get(1));
        mGroupId = groups.get(0);
        Log.d("idss",mGroupId);
        init();
    }

    private Audio convertAudio(Sound sound) {
        return new Audio(sound.getLinkDown(), sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite(),sound.getGroup_id());
    }

    private void initData() {
        if (mAdapterItems == null) {
            mAdapterItems = new ArrayList<>();
        }
        if (mAdapterKeys == null) {
            mAdapterKeys = new ArrayList<>();
        }
        if (mUrls == null){
            mUrls = new HashMap<>();
        }
        new GetData().execute();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addToSoundBoard:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), GroupActivity.this);
                break;
            case R.id.shareIT:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), GroupActivity.this);
                break;
            case R.id.report:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), GroupActivity.this);
                break;
            case R.id.improve:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), GroupActivity.this);
                break;
        }
        return false;
    }

    /**
     * Khởi tạo dữ liệu,
     * đăng ký sự kiện cho adapter
     * <p/>
     */
    private void init() {
        //addGroup();
        mLvSound.setHasFixedSize(true);
        mLvSound.setLayoutManager(new LinearLayoutManager(GroupActivity.this));
        Firebase.setAndroidContext(GroupActivity.this);
        mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
        Query q = mFirebase.orderByChild("group_id").equalTo(mGroupId);
        final Firebase fFavorite = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(GroupActivity.this) + "/favorite/");
        boolean isOnline = Tools.isOnline(GroupActivity.this);
        initData();
        if(isOnline){
            mAdapter = new SoundAdapter(q,mUrls, fFavorite, true, Audio.class, null, null, GroupActivity.this);
            RealmUtils.getRealmUtils(GroupActivity.this).deleteAllSound(GroupActivity.this);
        }else
        {
            mAdapter = new SoundAdapter(q,null, fFavorite, false, Audio.class, mAdapterItems, mAdapterKeys, GroupActivity.this);
        }

        mAdapter.setPlayButtonClicked(new SoundAdapter.OnItemClicked() {
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
                                if (!Tools.isOnline(GroupActivity.this)) {
                                    AppTools.showSnackBar(getString(R.string.internet_connection), GroupActivity.this);
                                    break;
                                }
                                /**
                                 * download sound
                                 */
                                sound.setLoadAudio(true);
                                mAdapter.notifyDataSetChanged();
                                FirebaseStorage storage = FirebaseStorage.getInstance();
                                StorageReference reference = storage.getReferenceFromUrl(FirebaseConstant.STORAGE_BUCKET).child("audios/" + sound.getUrl());
                                file = AppTools.getFile();
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
                                        AppTools.showSnackBar(getResources().getString(R.string.resource_not_found), GroupActivity.this);
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
                        try {
                            final String id = sound.getId();
                            sound.setIsFavorite(!sound.isFavorite());
                            final Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(GroupActivity.this) + "/favorite");
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    RealmUtils.getRealmUtils(GroupActivity.this).updateFavorite(GroupActivity.this, audioId);
                                    favoriteFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)) {
                                                favoriteFirebase.child(id).removeValue();
                                            } else {
                                                Map<String, String> values = new HashMap<>();
                                                values.put("name", sound.getName());
                                                values.put("date_create", sound.getDate_create());
                                                values.put("user_id", sound.getUser_id());
                                                values.put("plays", sound.getPlays() + "");
                                                favoriteFirebase.child(id).setValue(values);
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
                            mFileName = sound.getName();
                            finishActivity();
                        } else {
                            if (!Tools.isOnline(GroupActivity.this)) {
                                AppTools.showSnackBar(getResources().getString(R.string.internet_connection), GroupActivity.this);
                                break;
                            }
                            showProgressDialog();
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            StorageReference reference = storage.getReferenceFromUrl(FirebaseConstant.STORAGE_BUCKET).child("audios/" + sound.getUrl());
                            file = AppTools.getFile();
                            reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    dimissProgressDialog();
                                    mFilePath = file.getPath();
                                    mFileName = sound.getName();
                                    sound.setLink_on_Disk(mFilePath);
                                    new AsyncUpdatePath().execute(sound.getId(), sound.getLink_on_Disk());
                                    finishActivity();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    AppTools.showSnackBar(getResources().getString(R.string.resource_not_found), GroupActivity.this);
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

    private void addGroup(){
        Firebase firebase = new Firebase(FirebaseConstant.BASE_URL+FirebaseConstant.GROUP_URL);
        FirebaseGroup funny = new FirebaseGroup("Hài hước");
        FirebaseGroup love = new FirebaseGroup("Tình yêu");
        FirebaseGroup angry = new FirebaseGroup("Giận dữ");
        FirebaseGroup sad = new FirebaseGroup("Buồn");
        FirebaseGroup laugh = new FirebaseGroup("Cười");
        FirebaseGroup cry = new FirebaseGroup("Khóc");

      ArrayList<FirebaseGroup> firebaseGroups = new ArrayList<>();
        firebaseGroups.add(funny);
        firebaseGroups.add(love);
        firebaseGroups.add(angry);
        firebaseGroups.add(sad);
        firebaseGroups.add(laugh);
        firebaseGroups.add(cry);
        for (FirebaseGroup g: firebaseGroups
             ) {
           Firebase f= firebase.push();
           f.setValue(g);
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new SweetAlertDialog(GroupActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            mProgressDialog.setTitleText(getResources().getString(R.string.please_wait));
            mProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    private void dimissProgressDialog() {
        mProgressDialog.dismiss();
    }

    private void finishActivity() {
        Intent intent = new Intent(GroupActivity.this, CaptureVideoActivity.class);
        intent.putExtra("FilePath", mFilePath);
        intent.putExtra("FileName", mFileName);
        startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
    }

    private void playMp3(String path) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        } else {
            mPlayer.reset();
        }
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
                mAdapter.getItems().get(mCurrentPos).setIsPlaying(false);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(GroupActivity.this, v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgBack:
                this.finish();
                break;
        }

    }

    class GetData extends AsyncTask<Void, Void, ArrayList<Audio>> {

        @Override
        protected ArrayList<Audio> doInBackground(Void... params) {
            Realm realm = RealmManager.getRealm(GroupActivity.this);
            RealmResults<Sound> mSounds = realm.where(Sound.class).findAll();
            ArrayList<Audio> sounds = new ArrayList<>();
            for (Sound sound : mSounds) {
                Audio audio = convertAudio(sound);
                sounds.add(audio);
                mUrls.put(audio.getId(),audio.getLink_on_Disk());
            }
            return sounds;
        }

        @Override
        protected void onPostExecute(ArrayList<Audio> aVoid) {
            super.onPostExecute(aVoid);
            for (Audio audio : aVoid) {
                mAdapterItems.add(audio);
                mAdapterKeys.add(audio.getId());
            }
        }
    }

    /**
     * Cập nhật đường dẫn của file
     */
    private class AsyncUpdatePath extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Realm realm = RealmManager.getRealm(GroupActivity.this);
            realm.beginTransaction();
            Sound sound = realm.where(Sound.class).equalTo("id", params[0]).findFirst();
            sound.setLinkOnDisk(params[1]);
            realm.commitTransaction();
            realm.close();
            return null;
        }
    }

    /**
     * Cập nhật số lượt tải của mỗi audio
     */
    private class AsyncUpdatePlay extends AsyncTask<String, Integer, Void> {

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
