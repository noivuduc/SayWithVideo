package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.FavoriteActivity;
import datn.bkdn.com.saywithvideo.activity.MainActivity;
import datn.bkdn.com.saywithvideo.activity.RecordNewSoundActivity;
import datn.bkdn.com.saywithvideo.activity.SettingActivity;
import datn.bkdn.com.saywithvideo.activity.SoundActivity;
import datn.bkdn.com.saywithvideo.activity.SoundBoardActivity;
import datn.bkdn.com.saywithvideo.adapter.ListMyVideoAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Video;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class UserProfileFragment extends Fragment implements View.OnClickListener {

    private boolean mIsVolume;
    private ListView mLvMyVideo;
    private ImageView mImgVolume;
    private LinearLayout mLnSound;
    private LinearLayout mLnSoundboards;
    private LinearLayout mLnFavorites;
    private LinearLayout mLlCreateDub;
    private TextView mTvCreateDub;
    private TextView mTvUserName;
    private TextView mNumFavorite;
    private TextView mNumSound;
    private TextView mNumSoundBoard;
    private ImageView mImgBackgroundVideo;

    public static UserProfileFragment newInstance() {

        Bundle args = new Bundle();

        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.from(getContext()).inflate(R.layout.fragment_user_profile, container, false);

        mLnSound = (LinearLayout) v.findViewById(R.id.lnSounds);
        mLnSoundboards = (LinearLayout) v.findViewById(R.id.lnSoundboards);
        mLnFavorites = (LinearLayout) v.findViewById(R.id.lnFavorites);
        mLlCreateDub = (LinearLayout) v.findViewById(R.id.llCreateDub);
        mTvCreateDub = (TextView) v.findViewById(R.id.tvCreateDub);
        mTvUserName = (TextView) v.findViewById(R.id.tvNameUser);
        mNumFavorite = (TextView) v.findViewById(R.id.tvNumberSoundFavorite);
        mNumSound = (TextView) v.findViewById(R.id.tvNumberSound);
        mNumSoundBoard = (TextView) v.findViewById(R.id.tvNumberSoundBoards);
        mImgVolume = (ImageView) v.findViewById(R.id.imgVolume);
        mImgBackgroundVideo = (ImageView) v.findViewById(R.id.imgBackgroundVideo);
        mLvMyVideo = (ListView) v.findViewById(R.id.lvMyDubs);

        RealmResults<Video> videos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());
        if (videos.size() != 0) {
            mLlCreateDub.setVisibility(View.INVISIBLE);
        }
        ListMyVideoAdapter adapter = new ListMyVideoAdapter(getContext(), videos);
        mLvMyVideo.setAdapter(adapter);
        init();
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
    }

    @Override
    public void onResume() {
        super.onResume();
        mTvUserName.setText(Utils.getCurrentUserName(getContext()));
        int num = RealmUtils.getRealmUtils(getContext()).getFavoriteSound(getContext()).size();
        int numsound = RealmUtils.getRealmUtils(getContext()).getSoundOfUser(getContext(), Utils.getCurrentUserID(getContext())).size();
        mNumFavorite.setText("" + num);
        mNumSound.setText(numsound + "");
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
                mIsVolume = !mIsVolume;
                mImgVolume.setImageResource(mIsVolume ? R.mipmap.ic_action_volume_on : R.mipmap.ic_action_volume_muted);
                break;
        }
    }
}
