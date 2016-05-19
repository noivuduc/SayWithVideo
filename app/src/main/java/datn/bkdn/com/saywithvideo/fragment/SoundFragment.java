package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

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
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.SoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.network.Tools;
import datn.bkdn.com.saywithvideo.utils.AppTools;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 *
 */
public class SoundFragment extends Fragment implements PopupMenu.OnMenuItemClickListener {
    private int mCurrentPos = -1;
    private MediaPlayer mPlayer;
    private RecyclerView mLvSound;
    private Firebase mFirebase;
    File file = null;
    private String mFilePath;
    private ArrayList<Audio> mAdapterItems;
    private ArrayList<String> mAdapterKeys;
    private SoundAdapter mAdapter;
    private SweetAlertDialog mProgressDialog;

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

    private Audio convertAudio(Sound sound) {
        return new Audio(sound.getLinkDown(), sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite());
    }

    private void initData() {
        if (mAdapterItems == null) {
            mAdapterItems = new ArrayList<>();
        }
        if (mAdapterKeys == null) {
            mAdapterKeys = new ArrayList<>();
        }
        new GetData().execute();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addToSoundBoard:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), getActivity());
                break;
            case R.id.shareIT:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), getActivity());
                break;
            case R.id.report:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), getActivity());
                break;
            case R.id.improve:
                //TODO: Do somethings
                AppTools.showSnackBar(getString(R.string.come_in_soon), getActivity());
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
        mLvSound.setHasFixedSize(true);
        mLvSound.setLayoutManager(new LinearLayoutManager(getContext()));
        Firebase.setAndroidContext(getContext());
        mFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_URL);
        final Firebase fFavorite = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(getContext()) + "/favorite/");
        boolean isOnline = Tools.isOnline(getActivity());
        initData();
        mAdapter = new SoundAdapter(mFirebase, fFavorite, isOnline, Audio.class, mAdapterItems, mAdapterKeys, getContext());
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
                                    AppTools.showSnackBar(getString(R.string.internet_connection), getActivity());
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
                                        AppTools.showSnackBar(getResources().getString(R.string.resource_not_found), getActivity());
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
                            final Firebase favoriteFirebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + "/" + Utils.getCurrentUserID(getContext()) + "/favorite");
                            new AsyncTask<Void, Void, Void>() {

                                @Override
                                protected Void doInBackground(Void... params) {
                                    RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(), audioId);
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
                            finishActivity();
                        } else {
                            if (!Tools.isOnline(getContext())) {
                                AppTools.showSnackBar(getResources().getString(R.string.internet_connection), getActivity());
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
                                    sound.setLink_on_Disk(mFilePath);
                                    new AsyncUpdatePath().execute(sound.getId(), sound.getLink_on_Disk());
                                    finishActivity();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    AppTools.showSnackBar(getResources().getString(R.string.resource_not_found), getActivity());
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
            mProgressDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
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
        PopupMenu menu = new PopupMenu(getContext(), v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();

    }

    class GetData extends AsyncTask<Void, Void, ArrayList<Audio>> {

        @Override
        protected ArrayList<Audio> doInBackground(Void... params) {
            Realm realm = RealmManager.getRealm(getContext());
            RealmResults<Sound> mSounds = realm.where(Sound.class).findAll();
            ArrayList<Audio> sounds = new ArrayList<>();
            for (Sound sound : mSounds) {
                Audio audio = convertAudio(sound);
                sounds.add(audio);
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
            Realm realm = RealmManager.getRealm(getContext());
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
