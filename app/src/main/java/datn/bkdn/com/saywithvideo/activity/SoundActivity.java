package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListMySoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener {
    private ListMySoundAdapter adapter;
    private RelativeLayout rlBack;
    private RelativeLayout rlSort;
    private TextView tvAddSound;
    private EditText tvSearch;
    private MediaPlayer player;
    private ListView listView;
    private ImageView imgSort;
    private RealmResults<Sound> sounds;
    private int currentPos=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        init();
        String id = Utils.getCurrentUserID(this);
        sounds = RealmUtils.getRealmUtils(this).getSoundOfUser(this,id);
        adapter = new ListMySoundAdapter(this,sounds);
        listView.setAdapter(adapter);

        adapter.setPlayButtonClicked(new ListMySoundAdapter.OnItemClicked() {

            @Override
            public void onClick(int pos, View v) {
                Sound sound = sounds.get(pos);
                switch (v.getId()){
                    case R.id.imgPlay:
                        if (currentPos != -1 && pos != currentPos) {
                            Sound sound1 = sounds.get(currentPos);
                            if(sound1.isPlaying()) {
                                RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, sounds.get(currentPos).getId());
                                player.stop();
                            }
                        }
                        currentPos = pos;
                        if(sound.isPlaying()){
                            player.stop();
                            player.reset();
                        }
                        else
                        {
                            playMp3(sound.getLinkOnDisk());
                        }
                        RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, sounds.get(pos).getId());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        Intent intent= new Intent(SoundActivity.this, CaptureVideoActivity.class);
                        intent.putExtra("FilePath",sound.getLinkOnDisk());
                        intent.putExtra("FileName",sound.getName());
                        startActivity(intent);
                        break;
                    case R.id.imgOption:
                        break;
                }
            }
        });
    }
    public void playMp3(String path)  {
        player = new MediaPlayer();
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
                RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, sounds.get(currentPos).getId());
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void init(){
        listView = (ListView) findViewById(R.id.lvMySound);
        rlBack = (RelativeLayout) findViewById(R.id.rlBack);
        rlSort = (RelativeLayout) findViewById(R.id.rlSort);
        imgSort = (ImageView) findViewById(R.id.imgSort);
        tvSearch = (EditText) findViewById(R.id.edtSearch);
        tvAddSound = (TextView) findViewById(R.id.tvAddsound);
        setEvent();
    }

    private void setEvent(){
        rlBack.setOnClickListener(this);
        rlSort.setOnClickListener(this);
        tvAddSound.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
    }

    private void createSortMenu(View v){
        PopupMenu menu = new PopupMenu(this,v);
        menu.getMenuInflater().inflate(R.menu.sort_menu, menu.getMenu());
        menu.show();
    }

    private void finishActivity(){
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.rlBack:
                finishActivity();
                break;
            case R.id.rlSort:
                createSortMenu(imgSort);
                break;
            case R.id.edtSearch:
                tvSearch.setFocusable(true);
                tvSearch.setFocusableInTouchMode(true);
                break;
            case R.id.tvAddsound:
                startActivity(new Intent(SoundActivity.this,AddSoundActivity.class));
                break;
        }
    }
}
