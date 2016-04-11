package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.SoundAdapter;
import datn.bkdn.com.saywithvideo.database.ContentAudio;
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
import io.realm.RealmAsyncTask;
import io.realm.RealmResults;

public class SoundFragment extends Fragment {
    private int mCurrentPos = -1;
    private List<Audio> mSounds = new ArrayList<>();
    private MediaPlayer mPlayer;
    private RecyclerView mLvSound;
    private Firebase mFirebase;
    private Realm realm;
    private SoundAdapter mAdapter;
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
        mLvSound = (RecyclerView) v.findViewById(R.id.lvSound);
        mLvSound.setLayoutManager(new LinearLayoutManager(getContext()));
        init();
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    private Audio convertAudio(Sound sound) {
        Audio audio = new Audio(sound.getId(), sound.getName(), sound.getAuthor(), false, sound.isFavorite(), sound.getPlays(), sound.getDateOfCreate());
        return audio;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void init() {
        Firebase.setAndroidContext(getContext());
        mFirebase = new Firebase(FirebaseConstant.BASE_URL+FirebaseConstant.AUDIO_URL);
        mAdapter = new SoundAdapter(mFirebase,Audio.class,getContext());
        mAdapter.setPlayButtonClicked(new SoundAdapter.OnItemClicked() {
            @Override
            public void onClick(Audio sound, View v, int pos) {
                switch (v.getId()) {
                    case R.id.imgPlay:
                        final String audioId = sound.getId();
                        String path = "";
                        ContentAudio contentAudio = AppTools.getContentAudio(audioId, getActivity());
                        if (contentAudio != null) {
                            new AsyncUpdatePlay().execute(audioId, sound.getPlays() + 1 + "");
                            path = contentAudio.getContent();
                            if (mCurrentPos != -1 && pos != mCurrentPos) {
                                Audio sound1 = mSounds.get(mCurrentPos);
                                if (sound1.isPlaying()) {
                                    sound1.setIsPlaying(!sound1.isPlaying());
                                    String id = mSounds.get(mCurrentPos).getId();
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
                            sound.setIsPlaying(!sound.isPlaying());
                        }
                        break;
                    case R.id.rlFavorite:
                        try {
                            final String id = sound.getId();
                            sound.setIsFavorite(!sound.isFavorite());
                            final FirebaseUser f = AppTools.getInfoUser(Utils.getCurrentUserID(getContext()));
                            final Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(getContext()) + "/favorite");
                            new AsyncTask<Void,Void,Void>(){

                                @Override
                                protected Void doInBackground(Void... params) {
                                    favoriteFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final Firebase ff = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(getContext()) + "/");
                                            if(dataSnapshot.hasChild(id)){
                                                favoriteFirebase.child(id).removeValue();
                                                ff.child("no_favorite").setValue(f.getNo_favorite()- 1);
                                            }else
                                            {
                                                favoriteFirebase.child(id).setValue("true");
                                                ff.child("no_favorite").setValue(f.getNo_favorite() + 1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                    return null;
                                }
                            }.execute();

                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        mAdapter.notifyDataSetChanged();
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
        mLvSound.setAdapter(mAdapter);
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
        if (!Tools.isOnline(getContext())) {
            mSounds.clear();
            realm = RealmManager.getRealm(getContext());
            RealmResults<Sound> Sounds = realm.where(Sound.class).findAll();
            for (Sound sound : Sounds) {
                Audio audio = convertAudio(sound);
                mSounds.add(audio);
            }

        } else {
            mSounds.clear();
            RealmUtils.getRealmUtils(getContext()).deleteAllSound(getContext());
        }
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
                new AsyncUpdatePlaying().execute(mSounds.get(mCurrentPos).getId());
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
            Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
            firebase.child(audioId).child("plays").setValue(plays);
            return null;
        }
    }


}
