package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Sound;
import io.realm.RealmResults;

public class SoundFragment extends Fragment {
    private int mCurrentPos = -1;
    private RealmResults<Sound> mSounds;
    private MediaPlayer mPlayer;
    private ListView mLvSound;
    private ListSoundAdapter mAdapter;

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
        mSounds = RealmUtils.getRealmUtils(getContext()).getAllSound(getContext());
        mAdapter = new ListSoundAdapter(getContext(), mSounds, false);
        mAdapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = mSounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
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
                            playMp3(sound.getLinkOnDisk());
                        }
                        RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), mSounds.get(pos).getId());
                        mAdapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
//                        if (!sound.isFavorite()) {
//                            RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(), sound.getId());
//                        }
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
