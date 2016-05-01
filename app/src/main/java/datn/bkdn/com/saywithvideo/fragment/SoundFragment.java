package datn.bkdn.com.saywithvideo.fragment;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.HashMap;
import java.util.Map;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.activity.FavoriteActivity;
import datn.bkdn.com.saywithvideo.adapter.SoundAdapter;
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
import io.realm.RealmChangeListener;
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
    private ProgressDialog mProgressDialog;

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
        init();
        return v;
    }


    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(mReceiver,new IntentFilter(FavoriteActivity.BROADCAST_FAVORITE));
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
        mSounds.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d("onChange", "onChange");
            }
        });
        for (Sound s : mSounds) {
            Audio audio = convertAudio(s);
            mAdapterItems.add(audio);
            mAdapterKeys.add(audio.getId());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void init() {
        mLvSound.setHasFixedSize(true);
        mLvSound.setLayoutManager(new LinearLayoutManager(getContext()));
        initData();
        Firebase.setAndroidContext(getContext());
        mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
        final Firebase fFavorite = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(getContext()) + "/favorite/");
        mAdapter = new SoundAdapter(mFirebase,fFavorite, Audio.class, mSounds, mAdapterItems, mAdapterKeys, getContext());
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
                                if (!Tools.isOnline(getContext())) {
                                    Snackbar.make(getActivity().getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
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
                                        String path = AppTools.getContentAudio(audioId, getActivity());
                                        return path;
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

                        //  mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                        try {
                            final String id = sound.getId();
                            sound.setIsFavorite(!sound.isFavorite());
                            final Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(getContext()) + "/favorite");
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                }

                                @Override
                                protected Void doInBackground(Void... params) {
                                    RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(),audioId);
                                    favoriteFirebase.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)) {
                                                favoriteFirebase.child(id).removeValue();
                                            } else {
                                                Map<String,String> values = new HashMap<String, String>();
                                                values.put("name",sound.getName());
                                                values.put("date_create",sound.getDate_create());
                                                values.put("user_id",sound.getUser_id());
                                                values.put("plays",sound.getPlays()+"");
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
                            finishActivity();
                        } else {
                            if (!Tools.isOnline(getContext())) {
                                Snackbar.make(getActivity().getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                                break;
                            }
                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    if(mProgressDialog==null){
                                        mProgressDialog = new ProgressDialog(getContext());
                                    }
                                    mProgressDialog.show();
                                }

                                @Override
                                protected String doInBackground(Void... params) {
                                    String f = AppTools.getContentAudio(audioId, getActivity());
                                    return f;
                                }

                                @Override
                                protected void onPostExecute(String aVoid) {
                                    super.onPostExecute(aVoid);
                                    mProgressDialog.dismiss();
                                    mFilePath = aVoid;
                                    sound.setLink_on_Disk(mFilePath);
                                    new AsyncUpdatePath().execute(sound.getId(),sound.getLink_on_Disk());
                                    finishActivity();
                                }
                            }.execute();
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

    public class AsyncUpdatePath extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            Realm realm = RealmManager.getRealm(getContext());
            realm.beginTransaction();
            Sound sound = realm.where(Sound.class).equalTo("id", params[0]).findFirst();
            sound.setLinkOnDisk(params[1]);
            realm.commitTransaction();
            realm.close();
            return null;
        }
    }

    private void finishActivity() {
        Intent intent = new Intent(getContext(), CaptureVideoActivity.class);
        intent.putExtra("FilePath", mFilePath);
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

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("aaass","aaaa");
            if(intent.getAction()== FavoriteActivity.BROADCAST_FAVORITE){
                int pos = intent.getIntExtra("POS",-1);
                if(pos != -1) {
                    mAdapter.getItem(pos).setFavorite(false);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    };

}
