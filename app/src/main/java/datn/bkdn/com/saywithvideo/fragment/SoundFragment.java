package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.utils.Mp3Tools;
import io.realm.RealmResults;

/**
 * Created by Admin on 2/18/2016.
 */
public class SoundFragment extends Fragment {
    private int currentPos = -1;
    private RealmResults<Sound> sounds;
    private MediaPlayer player;
    private ListView lvSound;
    private ListSoundAdapter adapter;
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
        lvSound = (ListView) v.findViewById(R.id.lvSound);
        return v;
    }

    private void init(){
        sounds = RealmUtils.getRealmUtils(getContext()).getAllSound(getContext());
        adapter = new ListSoundAdapter(getContext(),sounds ,false);
        adapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = sounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if (currentPos != -1 && pos != currentPos) {
                            Sound sound1 = sounds.get(currentPos);
                            if (sound1.isPlaying()) {
                                RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), sounds.get(currentPos).getId());
                                player.stop();
                            }
                        }
                        currentPos = pos;
                        if (sound.isPlaying()) {
                            player.stop();
                            player.reset();
                        } else {
                            playMp3(sound.getLinkOnDisk());
                        }
                        RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), sounds.get(pos).getId());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
//                        if (!sound.isFavorite()) {
//                            RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(), sound.getId());
//                        }
                        RealmUtils.getRealmUtils(getContext()).updateFavorite(getContext(), sound.getId());

                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        Intent intent= new Intent(getContext(), CaptureVideoActivity.class);
                        intent.putExtra("FilePath",sound.getLinkOnDisk());
                        startActivity(intent);
                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }
        });
        lvSound.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
    }

    public void playMp3(String path)  {
        if(player==null) {
            player = new MediaPlayer();
        }else
        {
            player.reset();
        }
        try {
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.d("stop", "stop");
                RealmUtils.getRealmUtils(getContext()).updatePlaying(getContext(), sounds.get(currentPos).getId());
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void createPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(getContext(), v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.show();
    }
}
