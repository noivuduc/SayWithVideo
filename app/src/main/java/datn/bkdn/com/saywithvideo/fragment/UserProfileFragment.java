package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.FavoriteActivity;
import datn.bkdn.com.saywithvideo.activity.MainActivity;
import datn.bkdn.com.saywithvideo.activity.SettingActivity;
import datn.bkdn.com.saywithvideo.activity.ShareActivity;
import datn.bkdn.com.saywithvideo.activity.ShowVideoActivity;
import datn.bkdn.com.saywithvideo.activity.SoundActivity;
import datn.bkdn.com.saywithvideo.activity.SoundBoardActivity;
import datn.bkdn.com.saywithvideo.adapter.ListMyVideoAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Video;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class UserProfileFragment extends Fragment implements View.OnClickListener, ListMyVideoAdapter.OnItemClicked,
        TextureView.SurfaceTextureListener, ListMyVideoAdapter.OnMenuItemClicked {

    private boolean mIsVolume;
    private ImageView mImgVolume;
    private LinearLayout mLnSound;
    private LinearLayout mLnSoundboards;
    private LinearLayout mLnFavorites;
    private LinearLayout mLlCreateDub;
    private TextView mTvCreateDub;
    private TextView mTvUserName;
    private TextView mNumFavorite;
    private TextView mNumSound;
    private RealmResults<Video> mVideos;
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private String mVideoPath;
    private Video mDefaultVideo;

    public static UserProfileFragment newInstance() {

        Bundle args = new Bundle();

        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mLnSound = (LinearLayout) v.findViewById(R.id.lnSounds);
        mLnSoundboards = (LinearLayout) v.findViewById(R.id.lnSoundboards);
        mLnFavorites = (LinearLayout) v.findViewById(R.id.lnFavorites);
        mLlCreateDub = (LinearLayout) v.findViewById(R.id.llCreateDub);
        mTvCreateDub = (TextView) v.findViewById(R.id.tvCreateDub);
        mTvUserName = (TextView) v.findViewById(R.id.tvNameUser);
        mNumFavorite = (TextView) v.findViewById(R.id.tvNumberSoundFavorite);
        mNumSound = (TextView) v.findViewById(R.id.tvNumberSound);
        mTextureView = (TextureView) v.findViewById(R.id.video);
        mImgVolume = (ImageView) v.findViewById(R.id.imgVolume);
        ImageView mImgBackgroundVideo = (ImageView) v.findViewById(R.id.imgBackgroundVideo);
        ListView mLvMyVideo = (ListView) v.findViewById(R.id.lvMyDubs);
        mMediaPlayer = new MediaPlayer();

        mVideos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());
        Log.d("size", "" + mVideos.size());
        if (mVideos.size() != 0) {
            mLlCreateDub.setVisibility(View.INVISIBLE);
        }
        ListMyVideoAdapter mAdapter = new ListMyVideoAdapter(getContext(), mVideos);
        mAdapter.setPlayButtonClicked(this);
        mAdapter.setMenuItemClicked(this);
        mLvMyVideo.setAdapter(mAdapter);
        init();

        mDefaultVideo = RealmUtils.getRealmUtils(getContext()).getVideoProfile(getContext());
        if (mDefaultVideo != null) {
            mVideoPath = mDefaultVideo.getPath();
            mImgBackgroundVideo.setVisibility(View.GONE);
        } else {
            mVideoPath = "";
            mImgVolume.setVisibility(View.GONE);
        }

        mIsVolume = true;

        return v;
    }

    private void init() {
        mLnSound.setOnClickListener(this);
        mLnSoundboards.setOnClickListener(this);
        mLnFavorites.setOnClickListener(this);
        mTvUserName.setOnClickListener(this);
        mTvCreateDub.setOnClickListener(this);
        mImgVolume.setOnClickListener(this);
        mLlCreateDub.setOnClickListener(this);
        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        String id = Utils.getCurrentUserID(getContext());
        Firebase baseUser = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + id + "/");
        baseUser.child("no_favorite").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String content = dataSnapshot.getValue() == null ? "0" : dataSnapshot.getValue().toString();
                mNumFavorite.setText(content);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        baseUser.child("no_sound").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String content = dataSnapshot.getValue() == null ? "0" : dataSnapshot.getValue().toString();
                mNumSound.setText(content);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        baseUser.child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mTvUserName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lnSounds:
                startActivity(new Intent(getContext(), SoundActivity.class));
                break;
            case R.id.lnSoundboards:
                startActivity(new Intent(getContext(), SoundBoardActivity.class));
                break;
            case R.id.lnFavorites:
                startActivity(new Intent(getContext(), FavoriteActivity.class));
                break;
            case R.id.tvNameUser:
                startActivity(new Intent(getContext(), SettingActivity.class));
                break;
            case R.id.tvCreateDub:
                ((MainActivity) getContext()).showSounds();
                break;
            case R.id.imgVolume:
                if (mIsVolume) {
                    mMediaPlayer.setVolume(0.0f, 0.0f);
                    mImgVolume.setImageResource(R.mipmap.ic_action_volume_muted);
                } else {
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                    mImgVolume.setImageResource(R.mipmap.ic_action_volume_on);
                }
                mIsVolume = !mIsVolume;
                break;
        }
    }

    @Override
    public void onClick(int pos, View v) {
        Video video = mVideos.get(pos);
        switch (v.getId()) {
            case R.id.llinfo:
                Intent intent = new Intent(getActivity(), ShowVideoActivity.class);
                intent.putExtra("VideoPath", video.getPath());
                startActivity(intent);
                break;
            case R.id.imgshare:
                Intent i = new Intent(getActivity(), ShareActivity.class);
                i.putExtra("filePath", video.getPath());
                startActivity(i);
                break;
            case R.id.imgoption:
                Log.d("Position", pos + "");
                break;
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        Log.d("Tien", "Available");

        if (mVideoPath.equals("")) return;

        Surface surface = new Surface(surfaceTexture);
        try {
            mMediaPlayer.setDataSource(mVideoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setSurface(surface);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.prepareAsync();

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onItemClick(final int pos, MenuItem menuItem) {
        final String newid = mVideos.get(pos).getId();
        switch (menuItem.getItemId()) {
            case R.id.setProfile:
                if (mDefaultVideo != null) {
                    String id = mDefaultVideo.getId();
                    if (!id.equals(newid)) {
                        Log.d("tien", "co");
                        RealmUtils.getRealmUtils(getContext()).setVideoProfile(getContext(), id);

                    }
                }
                Log.d("tien", "co 2");
                RealmUtils.getRealmUtils(getContext()).setVideoProfile(getContext(), newid);
                mMediaPlayer.reset();
                try {
                    mMediaPlayer.setDataSource(mVideos.get(pos).getPath());
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.delete:
                String path = mVideos.get(pos).getPath();
                RealmUtils.getRealmUtils(getContext()).deleteVideo(getContext(), newid);
                File file = new File(path);
                file.delete();
                mVideos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());
                break;
        }
    }
}
