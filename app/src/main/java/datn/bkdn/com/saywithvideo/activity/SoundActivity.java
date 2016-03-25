package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.IOException;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListMySoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.AudioUser;
import datn.bkdn.com.saywithvideo.model.FirebaseAudio;
import datn.bkdn.com.saywithvideo.model.FirebaseConstant;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.RealmResults;
import io.realm.Sort;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
    private ListMySoundAdapter adapter;
    private RelativeLayout rlBack;
    private RelativeLayout rlSort;
    private TextView tvAddSound;
    private EditText tvSearch;
    private MediaPlayer player;
    private ListView listView;
    private ImageView imgSort;
    private RealmResults<AudioUser> sounds;
    private int currentPos = -1;
    private Firebase mFirebase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        init();
        Query q =  mFirebase.child(FirebaseConstant.AUDIO_URL).orderByChild("user_id").equalTo(Utils.getCurrentUserID(this));
        q.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RealmUtils.getRealmUtils(SoundActivity.this).deleteAllAudioUser(SoundActivity.this);
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    FirebaseAudio audio = data.getValue(FirebaseAudio.class);
                    String id = dataSnapshot.getKey();
                    String name = audio.getName();
                    String dateCreate = audio.getDate_create();
                    int plays = audio.getPlays();
                    AudioUser audioUser = new AudioUser(name,plays,id,dateCreate);
                    RealmUtils.getRealmUtils(SoundActivity.this).addAudioUser(SoundActivity.this,audioUser);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        String id = Utils.getCurrentUserID(this);
        sounds = RealmUtils.getRealmUtils(this).getSoundOfUser(this, id);
        adapter = new ListMySoundAdapter(this, sounds);
        listView.setAdapter(adapter);

        adapter.setPlayButtonClicked(new ListMySoundAdapter.OnItemClicked() {

            @Override
            public void onClick(int pos, View v) {
                AudioUser sound = sounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if (currentPos != -1 && pos != currentPos) {
                            AudioUser sound1 = sounds.get(currentPos);
                            if (sound1.isPlaying()) {
                                RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, sounds.get(currentPos).getId());
                                player.stop();
                                player.reset();
                            }
                        }
                        currentPos = pos;
                        if (sound.isPlaying()) {
                            player.stop();
                            player.reset();
                        } else {
                            playMp3(sound.getLinkOnDisk());
                        }
                        RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, sounds.get(pos).getId());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        Intent intent = new Intent(SoundActivity.this, CaptureVideoActivity.class);
                        intent.putExtra("FilePath", sound.getLinkOnDisk());
                        intent.putExtra("FileName", sound.getName());
                        startActivity(intent);
                        break;
                    case R.id.rlOption:
                        createSoundMenu(v,pos);
                        break;
                }
            }
        });
    }

    public void playMp3(String path) {
        if(player==null) {
            player = new MediaPlayer();
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
                RealmUtils.getRealmUtils(SoundActivity.this).updatePlaying(SoundActivity.this, sounds.get(currentPos).getId());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void init() {
        mFirebase = new Firebase(FirebaseConstant.BASE_URL);
        listView = (ListView) findViewById(R.id.lvMySound);
        rlBack = (RelativeLayout) findViewById(R.id.rlBack);
        rlSort = (RelativeLayout) findViewById(R.id.rlSort);
        imgSort = (ImageView) findViewById(R.id.imgSort);
        tvSearch = (EditText) findViewById(R.id.edtSearch);
        tvAddSound = (TextView) findViewById(R.id.tvAddsound);
        setEvent();
    }

    private void setEvent() {
        rlBack.setOnClickListener(this);
        rlSort.setOnClickListener(this);
        tvAddSound.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
    }

    private void createSortMenu(View v) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.sort_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(this);
        menu.show();
    }

    private void createSoundMenu(View v, final int pos) {
        PopupMenu menu = new PopupMenu(this, v);
        menu.getMenuInflater().inflate(R.menu.sound_menu, menu.getMenu());
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.delete:
                        AudioUser sound = sounds.get(pos);
                        File file = new File(sound.getLinkOnDisk());
                       // file.delete();
                        RealmUtils.getRealmUtils(SoundActivity.this).deleteSound(SoundActivity.this,sound.getId());
                        adapter.notifyDataSetChanged();
                        break;

                }
                return false;
            }
        });
        menu.show();
    }

    private void finishActivity() {
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishActivity();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
                startActivity(new Intent(SoundActivity.this, AddSoundActivity.class));
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.byName:
                sounds.sort("name", Sort.ASCENDING);
                adapter.notifyDataSetChanged();
                break;
            case R.id.byDate:
                sounds.sort("dateOfCreate", Sort.ASCENDING);
                adapter.notifyDataSetChanged();
                break;
            case R.id.byPlays:
                sounds.sort("plays", Sort.ASCENDING);
                adapter.notifyDataSetChanged();
                break;

        }
        return true;
    }
}
