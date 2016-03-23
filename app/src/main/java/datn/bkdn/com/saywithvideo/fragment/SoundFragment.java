package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
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
import com.firebase.client.utilities.Base64;
import com.google.gson.Gson;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.ContentAudio;
import datn.bkdn.com.saywithvideo.model.FireBaseContent;
import datn.bkdn.com.saywithvideo.model.FirebaseAudio;
import datn.bkdn.com.saywithvideo.model.FirebaseConstant;
import datn.bkdn.com.saywithvideo.model.FirebaseUser;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.Constant;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class SoundFragment extends Fragment {
    private int mCurrentPos = -1;
    private RealmResults<Sound> mSounds;
    private MediaPlayer mPlayer;
    private ListView mLvSound;
    private ListSoundAdapter mAdapter;
    private Firebase mFirebase;
    private Firebase mContent;
    private String mJsonContent;
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
        return v;
    }
    private void init() {

        Firebase.setAndroidContext(getContext());
        user = Utils.getFavoriteUser(getContext());
        mFirebase = new Firebase(FirebaseConstant.BASE_URL);
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
                    int plays = firebaseAudio.getPlays();
                    Firebase base = new Firebase(Constant.FIREBASE_ROOT + "users/" + user_id + "/name/");
                    base.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String userName = dataSnapshot.getValue().toString();
                            Sound sound = new Sound(audio_id, name, userName, dateCreate);
                            if(user.getFavorite()!=null) {
                                if (user.getFavorite().contains(audio_id))
                                    sound.setIsFavorite(true);
                            }
                            RealmUtils.getRealmUtils(getContext()).addNewSound(getContext(), sound);

                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {

                        }
                    });

                }
//                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        mSounds = RealmUtils.getRealmUtils(getContext()).getAllSound(getContext());
        mAdapter = new ListSoundAdapter(getContext(), mSounds, false);
        mAdapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = mSounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        final String audioId = sound.getId();
                        String path="";
                        ContentAudio contentAudio;
                        contentAudio= RealmUtils.getRealmUtils(getContext()).getContentAudio(getContext(), audioId);
                        if(contentAudio==null) {
                            String link = FirebaseConstant.BASE_URL+FirebaseConstant.AUDIO_CONTENT_URL+"/"+audioId+".json";
                            String json = Tools.getJson(link);
                            FireBaseContent c = new Gson().fromJson(json,FireBaseContent.class);
                                        String content = c.getContent();
                                        String path_audio =  Environment.getExternalStorageDirectory().getAbsolutePath() + "/"+audioId+".m4a";
                                        ContentAudio audio = new ContentAudio();
                                        audio.setId(audioId);
                                        audio.setContent(path_audio);
                            try {
                                Base64.decodeToFile(content, path_audio);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            RealmUtils.getRealmUtils(getContext()).addSoundContent(getContext(), audio);

                            contentAudio = RealmUtils.getRealmUtils(getContext()).getContentAudio(getContext(), audioId);
                        }
                        path = contentAudio.getContent();
                        if (mCurrentPos != -1 && pos != mCurrentPos) {
                            Sound sound1 = mSounds.get(mCurrentPos);
                            if (sound1.isPlaying()) {
                                RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), mSounds.get(mCurrentPos).getId());
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
                        RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), mSounds.get(pos).getId());
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                        Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL+FirebaseConstant.USER_URL+"/"+ Utils.getCurrentUserID(getContext())).child("favorite");
                        String id = sound.getId();

                        if(user.getFavorite().contains(id))
                        {
                            user.getFavorite().remove(id);
                        }else
                        {
                            user.getFavorite().add(id);
                        }
                        favoriteFirebase.setValue(user.getFavorite());
                        RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(), sound.getId());

                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        Intent intent = new Intent(getContext(), CaptureVideoActivity.class);
                        intent.putExtra("FilePath", sound.getLinkOnDisk());
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

    private FirebaseUser user;

    @Override
    public void onResume() {
        super.onResume();
        init();
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
                Log.d("stop", "stop");
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
}
