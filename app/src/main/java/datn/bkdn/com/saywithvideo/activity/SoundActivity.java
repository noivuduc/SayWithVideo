package datn.bkdn.com.saywithvideo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListMySoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;


public class SoundActivity extends AppCompatActivity implements View.OnClickListener,
        PopupMenu.OnMenuItemClickListener {
    private ListMySoundAdapter mAdapter;
    private RelativeLayout rlBack;
    private RelativeLayout rlSort;
    private TextView tvAddSound;
    private EditText tvSearch;
    private MediaPlayer mPlayer;
    private String mFilePath;
    private RecyclerView mRecycle;
    private ImageView imgSort;
    private ArrayList<Audio> mAdapterItems;
    private ArrayList<String> mAdapterKeys;
    private int mCurrentPos = -1;
    private Firebase mFirebase;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        init();
        initAdapter();

    }

    private void showMessage() {
        View v = findViewById(R.id.root);
        if (v != null) {
            Snackbar.make(v, getResources().getString(R.string.internet_connection), Snackbar.LENGTH_LONG).show();
        }
    }

    private void initAdapter() {
        Query query = mFirebase.orderByChild("user_id").equalTo(Utils.getCurrentUserID(this));
        getData();
        mAdapter = new ListMySoundAdapter(this, query, Audio.class, mAdapterItems, mAdapterKeys);
        mRecycle.setAdapter(mAdapter);
        mAdapter.setPlayButtonClicked(new ListMySoundAdapter.OnItemClicked() {

            @Override
            public void onClick(final int pos, View v, final Audio sound) {
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
                                if (!Tools.isOnline(SoundActivity.this)) {
                                    showMessage();
                                    break;
                                }
                                /**
                                 * download sound
                                 */
                                new AsyncTask<Void, String, String>() {
                                    @Override
                                    protected void onPreExecute() {
                                        super.onPreExecute();
                                        sound.setLoadAudio(true);
                                        mAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    protected String doInBackground(Void... params) {
                                        return AppTools.getContentAudio(audioId, SoundActivity.this);
                                    }

                                    @Override
                                    protected void onPostExecute(String aVoid) {
                                        super.onPostExecute(aVoid);
                                        mFilePath = aVoid;
                                        sound.setLink_on_Disk(mFilePath);
                                        new AsyncUpdatePath().execute(sound.getId(), sound.getLink_on_Disk());
                                        if (mCurrentPos == pos) {
                                            sound.setLoadAudio(false);
                                            sound.setIsPlaying(!sound.isPlaying());
                                            playMp3(mFilePath);
                                        }
                                        mAdapter.notifyDataSetChanged();

                                    }
                                }.execute();

                            } else {
                                mFilePath = sound.getLink_on_Disk();
                                sound.setIsPlaying(!sound.isPlaying());
                                mAdapter.notifyDataSetChanged();
                                playMp3(mFilePath);
                            }
                            new AsyncUpdatePlay().execute(audioId, sound.getPlays() + 1 + "");

                        }
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
                            if (!Tools.isOnline(SoundActivity.this)) {
                                showMessage();
                                break;
                            }
                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    if (mProgressDialog == null) {
                                        mProgressDialog = new ProgressDialog(SoundActivity.this);
                                    }
                                    mProgressDialog.show();
                                }

                                @Override
                                protected String doInBackground(Void... params) {
                                    return AppTools.getContentAudio(audioId, SoundActivity.this);
                                }

                                @Override
                                protected void onPostExecute(String aVoid) {
                                    super.onPostExecute(aVoid);
                                    mProgressDialog.dismiss();
                                    mFilePath = aVoid;
                                    sound.setLink_on_Disk(mFilePath);
                                    new AsyncUpdatePath().execute(sound.getId(), sound.getLink_on_Disk());
                                    finishActivity();
                                }
                            }.execute();
                        }

                        break;
                    case R.id.rlOption:
                        createSoundMenu(v, sound);
                        break;
                }
            }
        });
    }

    public class AsyncUpdatePath extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Realm realm = RealmManager.getRealm(SoundActivity.this);
            realm.beginTransaction();
            Sound sound = realm.where(Sound.class).equalTo("id", params[0]).findFirst();
            sound.setLinkOnDisk(params[1]);
            realm.commitTransaction();
            realm.close();
            return null;
        }
    }

    class AsyncUpdatePlay extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String audioId = params[0];
            String plays = params[1];
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
            firebase.child(audioId).child("plays").setValue(plays);
            return null;
        }
    }

    public void playMp3(String path) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.reset();
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

    private void init() {
        Firebase.setAndroidContext(this);
        mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
        mRecycle = (RecyclerView) findViewById(R.id.recycleViewMySound);
        rlBack = (RelativeLayout) findViewById(R.id.rlBack);
        rlSort = (RelativeLayout) findViewById(R.id.rlSort);
        imgSort = (ImageView) findViewById(R.id.imgSort);
        tvSearch = (EditText) findViewById(R.id.edtSearch);
        tvAddSound = (TextView) findViewById(R.id.tvAddsound);
        mRecycle.setHasFixedSize(true);
        mRecycle.setLayoutManager(new LinearLayoutManager(this));
        setEvent();
    }

    private void setEvent() {
        rlBack.setOnClickListener(this);
        rlSort.setOnClickListener(this);
        tvAddSound.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
    }

    private void createSortMenu(View v) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.sort_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();
    }

    private void createSoundMenu(View v, final Audio sound) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.sound_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(
                new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                new DecNoSoundUser().execute();
                                new DeleteContentFirebase().execute(sound.getId());
                                new DeleteFavoriteUserFirebase().execute(sound.getId());
                                new DeleteSoundFirebase().execute(sound.getId());
                                RealmUtils.getRealmUtils(SoundActivity.this).deleteSound(SoundActivity.this, sound.getId());
                                mAdapter.notifyDataSetChanged();
                                break;


                        }
                        return false;
                    }
                }

        );
        menu.show();
    }

    private Firebase firebase = new Firebase(FirebaseConstant.BASE_URL);

    private class DeleteSoundFirebase extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(final String... params) {
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
            firebase.child(params[0]).removeValue();
            return null;
        }
    }

    private class DeleteContentFirebase extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_CONTENT_URL);
            firebase.child(params[0]).removeValue();
            return null;
        }
    }

    private class DecNoSoundUser extends AsyncTask<Void, Void, Void>

    {

        @Override
        protected Void doInBackground
                (Void... params) {
            String userID = Utils.getCurrentUserID(SoundActivity.this);
            FirebaseUser f = AppTools.getInfoUser(userID);
            if (f.getNo_sound() > 0) {
                Firebase ff = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + userID).child("no_sound");
                ff.setValue(f.getNo_sound() - 1);
            }
            return null;
        }
    }

    private class DeleteFavoriteUserFirebase extends android.os.AsyncTask<String, Void, Void>

    {
        @Override
        protected Void doInBackground(final String... params) {
            final String id = params[0];
            final Firebase f = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL);
            f.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String key = data.getKey();
                        f.child(key).child("favorite").child(id).removeValue();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
            return null;
        }
    }

    private void finishActivity() {
        Intent intent = new Intent(SoundActivity.this, CaptureVideoActivity.class);
        intent.putExtra("FilePath", mFilePath);
        startActivity(intent);
        // this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.stop();
            }
        }
    }

    private void getData() {
        if (mAdapterItems == null) {
            mAdapterItems = new ArrayList<>();
        }
        if (mAdapterKeys == null) {
            mAdapterKeys = new ArrayList<>();
        }

        String id = Utils.getCurrentUserID(this);
        Realm realm = RealmManager.getRealm(this);
        RealmResults<Sound> mSounds = realm.where(Sound.class).equalTo("idUser", id).findAll();
        for (Sound s : mSounds) {
            Audio audio = convertAudio(s);
            mAdapterItems.add(audio);
            mAdapterKeys.add(audio.getId());
        }

    }

    private Audio convertAudio(Sound sound) {
        return new Audio(sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                this.finish();
                break;
            case R.id.rlSort:
                createSortMenu(imgSort);
                break;
            case R.id.edtSearch:
                tvSearch.setFocusable(true);
                tvSearch.setFocusableInTouchMode(true);
                break;
            case R.id.tvAddsound:
                startActivity(new Intent(SoundActivity.this, AddSoundActivity.class));
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.byName:
                // mSounds.sort("name", Sort.ASCENDING);
                mAdapter.sortByName();
                break;
            case R.id.byDate:
//                mSounds.sort("date_create", Sort.ASCENDING);
//                adapter.notifyDataSetChanged();
                mAdapter.sortByDateUpload();
                break;
            case R.id.byPlays:
//                mSounds.sort("plays", Sort.ASCENDING);
//                adapter.notifyDataSetChanged();
                mAdapter.sortByPlays();
                break;

        }
        return true;
    }
}
