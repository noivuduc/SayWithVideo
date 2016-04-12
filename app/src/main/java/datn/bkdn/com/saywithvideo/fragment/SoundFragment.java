package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.SoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmManager;
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
    private RealmResults<Sound> mSounds;
    private MediaPlayer mPlayer;
    private RecyclerView mLvSound;
    private Firebase mFirebase;
    private Realm realm;
    private FirebaseUser mFirebaseUser;
    private String mFilePath;
    private ArrayList<Audio> mAdapterItems;
    private ArrayList<String> mAdapterKeys;
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
        mLvSound.setHasFixedSize(true);
        mLvSound.setLayoutManager(new LinearLayoutManager(getContext()));
        init();
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    private Audio convertAudio(Sound sound) {
        Audio audio = new Audio(sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite());
        return audio;
    }

    public void initData() {
        if (mAdapterItems == null) {
            mAdapterItems = new ArrayList<>();
        }
        if (mAdapterKeys == null) {
            mAdapterKeys = new ArrayList<>();
        }

        realm = RealmManager.getRealm(getContext());
        mSounds = realm.where(Sound.class).findAll();

        for (Sound s : mSounds) {
            Log.d("SoundFragment.initData",s.isFavorite()+"");
            Audio audio = convertAudio(s);
            mAdapterItems.add(audio);
            mAdapterKeys.add(audio.getId());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void init() {
        initData();
        Firebase.setAndroidContext(getContext());
        mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
        mAdapter = new SoundAdapter(mFirebase, Audio.class, mAdapterItems, mAdapterKeys, getContext());
        mAdapter.setPlayButtonClicked(new SoundAdapter.OnItemClicked() {
            @Override
            public void onClick(Audio sound, View v, int pos) {
                final String audioId = sound.getId();
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if(!Tools.isOnline(getContext())){
                            Snackbar.make( getActivity().getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                            break;
                        }
                        if (sound.getLink_on_Disk() == null)
                        {
                            getPath(audioId);
                            sound.setLink_on_Disk(mFilePath);
                        }
                        else
                        {
                            mFilePath = sound.getLink_on_Disk();

                        }
                        new AsyncUpdatePlay().execute(audioId, sound.getPlays() + 1 + "");
                        if (mCurrentPos != -1 && pos != mCurrentPos) {
                            Audio sound1 = mAdapter.getItems().get(mCurrentPos);
                            if (sound1.isPlaying()) {
                                sound1.setIsPlaying(!sound1.isPlaying());
                                mAdapter.notifyDataSetChanged();
                                mPlayer.stop();
                            }
                        }
                        mCurrentPos = pos;
                        if (sound.isPlaying()) {
                            mPlayer.stop();
                            mPlayer.reset();
                        } else {
                            try {
                                playMp3(mFilePath);
                            }catch (Exception e){

                            }
                        }
                        sound.setIsPlaying(!sound.isPlaying());
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                        try {
                            final String id = sound.getId();
                            sound.setIsFavorite(!sound.isFavorite());
                            new AsyncTask<Void, Void, FirebaseUser>(){

                                @Override
                                protected FirebaseUser doInBackground(Void... params) {
                                  FirebaseUser  f = AppTools.getInfoUser(Utils.getCurrentUserID(getContext()));
                                    return f;
                                }

                                @Override
                                protected void onPostExecute(FirebaseUser firebaseUser) {
                                    super.onPostExecute(firebaseUser);
                                    mFirebaseUser = firebaseUser;
                                }
                            }.execute();

                            final Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(getContext()) + "/favorite");
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    favoriteFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final Firebase ff = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(getContext()) + "/");
                                            if (dataSnapshot.hasChild(id)) {
                                                favoriteFirebase.child(id).removeValue();
                                                ff.child("no_favorite").setValue(mFirebaseUser.getNo_favorite() - 1);
                                            } else {
                                                favoriteFirebase.child(id).setValue("true");
                                                ff.child("no_favorite").setValue(mFirebaseUser.getNo_favorite() + 1);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(FirebaseError firebaseError) {

                                        }
                                    });
                                    return null;
                                }
                            }.execute();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        if ((sound.getLink_on_Disk())!= null) {
                            mFilePath = sound.getLink_on_Disk();
                        } else {
                            getPath(audioId);
                            mFilePath = AppTools.getContentAudio(audioId, getActivity());
                        }

                        Intent intent = new Intent(getContext(), CaptureVideoActivity.class);
                        intent.putExtra("FilePath", mFilePath);
                        startActivity(intent);
                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }
        });
        mLvSound.setAdapter(mAdapter);
    }

    private void getPath(final String audioId){
        new AsyncTask<Void,String,String>(){
            @Override
            protected String doInBackground(Void... params) {
                String  path = AppTools.getContentAudio(audioId, getActivity());
                return path;
            }
            @Override
            protected void onPostExecute(String aVoid) {
                super.onPostExecute(aVoid);
                mFilePath = aVoid;
            }
        }.execute();
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
                mAdapter.getItems().get(mCurrentPos).setIsPlaying(false);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void createPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(getContext(), v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.show();

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
