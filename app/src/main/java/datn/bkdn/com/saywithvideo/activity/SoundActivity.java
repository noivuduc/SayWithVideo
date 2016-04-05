package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.soikonomakis.rxfirebase.RxFirebase;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListMySoundAdapter;
import datn.bkdn.com.saywithvideo.database.ContentAudio;
import datn.bkdn.com.saywithvideo.database.RealmAudioUser;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseAudio;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, RealmChangeListener {
    private ListMySoundAdapter adapter;
    private RelativeLayout rlBack;
    private RelativeLayout rlSort;
    private TextView tvAddSound;
    private EditText tvSearch;
    private MediaPlayer player;
    private ListView listView;
    private ImageView imgSort;
    private Realm realm;
    private RealmAsyncTask asyncTransaction;
    private RealmResults<Sound> mSounds;
    private int mCurrentPos = -1;
    private Firebase mFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        init();
        initAdapter();

    }


    private void initAdapter() {
        getData();
        adapter = new ListMySoundAdapter(this, mSounds);
        listView.setAdapter(adapter);
        adapter.setPlayButtonClicked(new ListMySoundAdapter.OnItemClicked() {

            @Override
            public void onClick(int pos, View v) {
                Sound sound = mSounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        final String audioId = sound.getId();

                        String path = "";
                        ContentAudio contentAudio = AppTools.getContentAudio(audioId, SoundActivity.this);
                        if (contentAudio != null) {
                            sound.setPlays(sound.getPlays() + 1);
                            new AsyncUpdatePlay().execute(audioId, sound.getPlays() + "");
                            if (mCurrentPos != -1 && pos != mCurrentPos) {
                                Sound sound1 = mSounds.get(mCurrentPos);
                                if (sound1.isPlaying()) {
                                    String id = mSounds.get(mCurrentPos).getId();
                                    new AsyncUpdatePlaying().execute(id);
                                    player.stop();
                                    player.reset();
                                }
                            }
                            path = contentAudio.getContent();
                            mCurrentPos = pos;
                            if (sound.isPlaying()) {
                                player.stop();
                                player.reset();
                            } else {
                                playMp3(path);
                            }
                            new AsyncUpdatePlaying().execute(mSounds.get(pos).getId());
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case R.id.llSoundInfor:
                        ContentAudio content = AppTools.getContentAudio(sound.getId(), SoundActivity.this);
                        if (content != null) {
                            String filepath = content.getContent();
                            Intent intent = new Intent(SoundActivity.this, CaptureVideoActivity.class);
                            intent.putExtra("FilePath", filepath);
                            intent.putExtra("FileName", sound.getName());
                            startActivity(intent);
                        }
                        break;
                    case R.id.rlOption:
                        createSoundMenu(v, pos);
                        break;
                }
            }
        });
    }

    public void playMp3(String path) {
        if (player == null) {
            player = new MediaPlayer();
        }
        try {
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                new AsyncUpdatePlaying().execute(mSounds.get(mCurrentPos).getId());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void init() {
        mFirebase = new Firebase(FirebaseConstant.BASE_URL);
        listView = (ListView) findViewById(R.id.lvMySound);
        rlBack = (RelativeLayout) findViewById(R.id.rlBack);
        rlSort = (RelativeLayout) findViewById(R.id.rlSort);
        imgSort = (ImageView) findViewById(R.id.imgSort);
        tvSearch = (EditText) findViewById(R.id.edtSearch);
        tvAddSound = (TextView) findViewById(R.id.tvAddsound);
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

    private void createSoundMenu(View v, final int pos) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.sound_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        final Sound sound = mSounds.get(pos);
                        /*
                        delete audio
                         */
                        Firebase firebase = new Firebase(FirebaseConstant.BASE_URL);
                        Query query = firebase.child(FirebaseConstant.AUDIO_URL).orderByChild(sound.getId());
                        query.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    if (data.getKey().equals(sound.getId())) {
                                        data.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
                        /*
                        delete content
                         */
                        Query query1 = firebase.child(FirebaseConstant.AUDIO_CONTENT_URL).orderByChild(sound.getId());
                        query1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                    if (data.getKey().equals(sound.getId())) {
                                        data.getRef().removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
//                        File file = new File(sound.getLinkOnDisk());
//                       // file.delete();
                        new AsyncTask<String, Void, Void>() {

                            @Override
                            protected Void doInBackground(String... params) {
                                String id = params[0];
                                RealmUtils.getRealmUtils(SoundActivity.this).deleteSound(SoundActivity.this, id);
                                RealmUtils.getRealmUtils(SoundActivity.this).deleteSoundContent(SoundActivity.this, id);
                                return null;
                            }
                        }.execute(sound.getId());

                         /*
                    Dec no_sound
                     */
                        new AsyncTask<Void, Void, Void>() {

                            @Override
                            protected Void doInBackground(Void... params) {
                                String userID = Utils.getCurrentUserID(SoundActivity.this);
                                FirebaseUser f = AppTools.getInfoUser(userID);
                                Firebase ff = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + userID).child("no_sound");
                                ff.setValue(f.getNo_sound() - 1);
                                return null;
                            }
                        }.execute();

                         /*
                    delete all user's favorite has this sound
                     */
                        new AsyncTask<Void,Void,Void>(){
                            @Override
                            protected Void doInBackground(Void... params) {
                                final Firebase f = new Firebase(FirebaseConstant.BASE_URL+FirebaseConstant.USER_URL);
                                RxFirebase.getInstance().
                                        observeValueEvent(f).
                                        subscribeOn(Schedulers.newThread()).
                                        subscribe(new Action1<DataSnapshot>() {
                                            @Override
                                            public void call(DataSnapshot dataSnapshot) {
                                                for (DataSnapshot data : dataSnapshot.getChildren()) {
                                                    String key = data.getKey();
                                                    FirebaseUser fu = AppTools.getInfoUser(key);
                                                    if (fu.getNo_favorite() > 0) {
                                                        f.child(key).child("favorite").child(sound.getId()).removeValue();
                                                        f.child("no_favorite").setValue(fu.getNo_favorite() - 1);
                                                    }

                                                }
                                            }
                                        });
                                return null;
                            }
                        }.execute();
                        adapter.notifyDataSetChanged();
                        break;


                }
                return false;
            }
        });
        menu.show();
    }

    private void finishActivity() {
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            if (player.isPlaying()) {
                player.stop();
            }
        }
        realm.close();
    }

    private void getData() {
        String id = Utils.getCurrentUserID(this);
        realm = RealmManager.getRealm(this);
        mSounds = realm.where(Sound.class).equalTo("idUser", id).findAll();
        mSounds.addChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelAsyncTransaction();
        mSounds = null;
        realm.close();
    }

    private void cancelAsyncTransaction() {
        if (asyncTransaction != null && !asyncTransaction.isCancelled()) {
            asyncTransaction.cancel();
            asyncTransaction = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                finishActivity();
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
                mSounds.sort("name", Sort.ASCENDING);
                adapter.notifyDataSetChanged();
                break;
            case R.id.byDate:
                mSounds.sort("date_create", Sort.ASCENDING);
                adapter.notifyDataSetChanged();
                break;
            case R.id.byPlays:
                mSounds.sort("plays", Sort.ASCENDING);
                adapter.notifyDataSetChanged();
                break;

        }
        return true;
    }

    @Override
    public void onChange() {
        adapter.notifyDataSetChanged();
    }

    private void loadData() {
        mFirebase.child(FirebaseConstant.AUDIO_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RealmUtils.getRealmUtils(SoundActivity.this).deleteAllAudioUser(SoundActivity.this);
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    FirebaseAudio firebaseAudio = data.getValue(FirebaseAudio.class);
                    final String name = firebaseAudio.getName();
                    final String dateCreate = firebaseAudio.getDate_create();
                    final String user_id = firebaseAudio.getUser_id();
                    final String audio_id = data.getKey();
                    final int plays = firebaseAudio.getPlays();
                    Firebase base = new Firebase(Constant.FIREBASE_ROOT + "users/" + user_id + "/name/");
                    RxFirebase.getInstance().
                            observeValueEvent(base).
                            subscribeOn(Schedulers.newThread()).
                            subscribe(new Action1<DataSnapshot>() {
                                @Override
                                public void call(DataSnapshot dataSnapshot) {
                                    RealmAudioUser sound = new RealmAudioUser(name, plays, audio_id, dateCreate);
                                    sound.setPlays(plays);
                                    adapter.notifyDataSetChanged();
                                    new AsyncAddSound().execute(sound);
                                }
                            });
//

                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    class AsyncAddSound extends AsyncTask<RealmAudioUser, Void, Void> {

        @Override
        protected Void doInBackground(RealmAudioUser... sound) {
            RealmUtils.getRealmUtils(SoundActivity.this).addAudioUser(SoundActivity.this, sound[0]);
            return null;
        }
    }

    class AsyncUpdatePlaying extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];
            RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, id);
            return null;
        }
    }

    public class AsyncUpdatePlay extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String audioId = params[0];
            String plays = params[1];
            RealmUtils.getRealmUtils(SoundActivity.this).updatePlays(SoundActivity.this, audioId);
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
            firebase.child(audioId).child("plays").setValue(plays);
            return null;
        }
    }
}
