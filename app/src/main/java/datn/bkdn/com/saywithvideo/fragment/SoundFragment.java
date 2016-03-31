package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.soikonomakis.rxfirebase.RxFirebase;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.DemoAdapter;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.ContentAudio;
import datn.bkdn.com.saywithvideo.model.FirebaseAudio;
import datn.bkdn.com.saywithvideo.model.FirebaseConstant;
import datn.bkdn.com.saywithvideo.model.FirebaseUser;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class SoundFragment extends Fragment implements RealmChangeListener {
    private int mCurrentPos = -1;
    private RealmResults<Sound> mSounds;
    private MediaPlayer mPlayer;
    private ListView mLvSound;
    private ListSoundAdapter mAdapter;
    private Firebase mFirebase;
    private FirebaseUser user;
    private Realm realm;
    private DemoAdapter dotAdapter;
    private RealmAsyncTask asyncTransaction;
    public static SoundFragment newInstance() {

        Bundle args = new Bundle();

        SoundFragment fragment = new SoundFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getContext()).inflate(R.layout.fragment_sound, container, false);
        mLvSound = (ListView) v.findViewById(R.id.lvSound);
        init();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        realm = RealmManager.getRealm(getContext());
        mSounds = realm.where(Sound.class).findAll();
        mSounds.addChangeListener(this);
        dotAdapter.updateList(mSounds);

    }

    @Override
    public void onStop() {
        super.onStop();
        cancelAsyncTransaction();
        mSounds.removeChangeListeners();
        mSounds = null;
        realm.close();
    }

    @Override
    public void onChange() {
        dotAdapter.notifyDataSetChanged();
    }

    private void cancelAsyncTransaction() {
        if (asyncTransaction != null && !asyncTransaction.isCancelled()) {
            asyncTransaction.cancel();
            asyncTransaction = null;
        }
    }

    private void loadData() {
        mFirebase.child(FirebaseConstant.AUDIO_URL).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RealmUtils.getRealmUtils(getContext()).deleteAllSound(getContext());
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
                                    String userName = dataSnapshot.getValue().toString();
                                    Sound sound = new Sound(audio_id, name, userName, dateCreate);
                                    sound.setPlays(plays);
                                    if (user.getFavorite() != null) {
                                        if (user.getFavorite().contains(audio_id))
                                            sound.setIsFavorite(true);
                                    }
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

    private void init() {
        Firebase.setAndroidContext(getContext());
        mFirebase = new Firebase(FirebaseConstant.BASE_URL);
        user = Utils.getFavoriteUser(getContext());
        mAdapter = new ListSoundAdapter(getContext(), mSounds);
        dotAdapter = new DemoAdapter(getContext());
        loadData();
        dotAdapter.setPlayButtonClicked(new DemoAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = mSounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        Log.d("1112", "1112");
                        final String audioId = sound.getId();
                        String path = "";
                        ContentAudio contentAudio = AppTools.getContentAudio(audioId, getActivity());
                        if (contentAudio != null) {
                            new AsyncUpdatePlay().execute(audioId, sound.getPlays() + 1 + "");
                            path = contentAudio.getContent();
                            if (mCurrentPos != -1 && pos != mCurrentPos) {
                                Sound sound1 = mSounds.get(mCurrentPos);
                                if (sound1.isPlaying()) {
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
                                playMp3(path);
                            }
                            new AsyncUpdatePlaying().execute(sound.getId());
                            dotAdapter.notifyDataSetChanged();
                        }
                        break;
                    case R.id.rlFavorite:
                        try {
                            Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(getContext())).child("favorite");
                            String id = sound.getId();

                            if (user.getFavorite().contains(id)) {
                                user.getFavorite().remove(id);
                            } else {
                                user.getFavorite().add(id);
                            }
                            favoriteFirebase.setValue(user.getFavorite());
                        } catch (Exception e) {

                        }
                        new AsyncTask<String, Void, Void>() {
                            @Override
                            protected Void doInBackground(String... params) {
                                RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(), params[0]);
                                return null;
                            }
                        }.execute(sound.getId());

                        dotAdapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        ContentAudio content = AppTools.getContentAudio(sound.getId(), getActivity());
                        if (content != null) {
                            String filePath = content.getContent();
                            Intent intent = new Intent(getContext(), CaptureVideoActivity.class);
                            intent.putExtra("FilePath", filePath);
                            startActivity(intent);
                        }
                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }
        });
        mLvSound.setAdapter(dotAdapter);
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

    @Override
    public void onResume() {
        super.onResume();
    }

    public void playMp3(String path) {
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
                RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), mSounds.get(mCurrentPos).getId());
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(getContext(), v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.show();

    }

    class AsyncAddSound extends AsyncTask<Sound, Void, Void> {

        @Override
        protected Void doInBackground(Sound... sound) {
            RealmUtils.getRealmUtils(getContext()).addNewSound(getContext(), sound[0]);
            return null;
        }
    }

    class AsyncUpdatePlaying extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String id = params[0];
            RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), id);
            return null;
        }
    }

    public class AsyncUpdatePlay extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String audioId = params[0];
            String plays = params[1];
            RealmUtils.getRealmUtils(getContext()).updatePlays(getContext(), audioId);
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
            firebase.child(audioId).child("plays").setValue(plays);
            return null;
        }
    }


}
