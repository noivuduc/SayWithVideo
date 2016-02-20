package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.CaptureVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.model.Sound;

/**
 * Created by Admin on 2/18/2016.
 */
public class SoundFragment extends Fragment {
    private int currentPos = -1;

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

        ListView lvSound = (ListView) v.findViewById(R.id.lvSound);

        final List<Sound> sounds = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            sounds.add(new Sound(i, "Sound Noi Oc Cho " + i, "Author Noi Oc Cho " + i));
        }
        final ListSoundAdapter adapter = new ListSoundAdapter(getContext(), sounds);
        adapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = sounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if (currentPos != -1 && pos != currentPos) {
                            sounds.get(currentPos).setIsPlaying(false);
                        }
                        currentPos = pos;
                        sound.setIsPlaying(!sound.isPlaying());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                        sound.setIsFavorite(!sound.isFavorite());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        startActivity(new Intent(getContext(), CaptureVideoActivity.class));
                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }
        });
        lvSound.setAdapter(adapter);

        return v;
    }

    private void createPopupMenu(View v) {
        PopupMenu menu = new PopupMenu(getContext(), v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.show();
    }
}
