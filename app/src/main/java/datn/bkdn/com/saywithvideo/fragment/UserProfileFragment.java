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

/**
 * Created by Admin on 2/18/2016.
 */
public class UserProfileFragment extends Fragment implements View.OnClickListener {

    private boolean isVolume;
    private ListView lvMyVideo;
    private ImageView imgVolume;
    private LinearLayout lnSound;
    private LinearLayout lnSoundboards;
    private LinearLayout lnFavorites;
    private LinearLayout llCreateDub;
    private TextView tvCreateDub;
    private TextView tvUserName;
    private TextView numFavorite;
    private TextView numSound;
    private TextView numSoundBoard;
    private ImageView imgBackgroundVideo;
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

        lnSound = (LinearLayout) v.findViewById(R.id.lnSounds);
        lnSoundboards = (LinearLayout) v.findViewById(R.id.lnSoundboards);
        lnFavorites = (LinearLayout) v.findViewById(R.id.lnFavorites);
        llCreateDub = (LinearLayout) v.findViewById(R.id.llCreateDub);
        tvCreateDub = (TextView) v.findViewById(R.id.tvCreateDub);
        tvUserName = (TextView) v.findViewById(R.id.tvNameUser);
        numFavorite = (TextView) v.findViewById(R.id.tvNumberSoundFavorite);
        numSound = (TextView) v.findViewById(R.id.tvNumberSound);
        numSoundBoard = (TextView) v.findViewById(R.id.tvNumberSoundBoards);
        imgVolume = (ImageView) v.findViewById(R.id.imgVolume);
        imgBackgroundVideo = (ImageView) v.findViewById(R.id.imgBackgroundVideo);
        lvMyVideo = (ListView) v.findViewById(R.id.lvMyDubs);

        RealmResults<Video> videos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());
        if(videos.size()!=0){
            llCreateDub.setVisibility(View.INVISIBLE);
        }
        ListMyVideoAdapter adapter = new ListMyVideoAdapter(getContext(),videos);
        lvMyVideo.setAdapter(adapter);
        init();
        return v;
    }

    private void init(){
        lnSound.setOnClickListener(this);
        lnSoundboards.setOnClickListener(this);
        lnFavorites.setOnClickListener(this);
        tvUserName.setOnClickListener(this);
        tvCreateDub.setOnClickListener(this);
        imgVolume.setOnClickListener(this);
        llCreateDub.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        tvUserName.setText(Utils.getCurrentUserName(getContext()));
        int num = RealmUtils.getRealmUtils(getContext()).getFavoriteSound(getContext()).size();
        int numsound = RealmUtils.getRealmUtils(getContext()).getSoundOfUser(getContext(),Utils.getCurrentUserID(getContext())).size();
        numFavorite.setText(""+num);
        numSound.setText(numsound + "");
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
                isVolume = !isVolume;
                imgVolume.setImageResource(isVolume ? R.mipmap.ic_action_volume_on : R.mipmap.ic_action_volume_muted);
                break;
        }
    }
}
