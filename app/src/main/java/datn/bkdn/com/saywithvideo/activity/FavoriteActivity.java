package datn.bkdn.com.saywithvideo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListMySoundAdapter;
import datn.bkdn.com.saywithvideo.adapter.ListSoundAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.Sound;
import io.realm.RealmResults;

public class FavoriteActivity extends AppCompatActivity implements View.OnClickListener{
    private ListSoundAdapter adapter;
    private RelativeLayout rlBack;
    private RelativeLayout rlSort;
    private EditText tvSearch;
    private ListView lvSound;
    private ImageView imgSort;
    private int currentPos=-1;
    private RealmResults<Sound> sounds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        init();
        sounds = RealmUtils.getRealmUtils(this).getFavoriteSound(this);
        adapter = new ListSoundAdapter(this,sounds ,false);
        adapter.setPlayButtonClicked(new ListSoundAdapter.OnItemClicked() {
            @Override
            public void onClick(int pos, View v) {
                Sound sound = sounds.get(pos);
                switch (v.getId()) {
                    case R.id.imgPlay:
                        if(currentPos != -1 && pos!=currentPos){
                            sounds.get(currentPos).setIsPlaying(false);
                        }
                        currentPos = pos;
                        sound.setIsPlaying(!sound.isPlaying());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.rlFavorite:
                            RealmUtils.getRealmUtils(FavoriteActivity.this).updateFavorite(FavoriteActivity.this,sound.getId());
                            sounds = RealmUtils.getRealmUtils(FavoriteActivity.this).getFavoriteSound(FavoriteActivity.this);
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        //:TODO
                        break;
                    case R.id.rlOption:
                        createPopupMenu(v);
                        break;
                }
            }
        });
        lvSound.setAdapter(adapter);

    }
    private void init(){
        lvSound = (ListView) findViewById(R.id.lvSoundFavorite);
        rlBack = (RelativeLayout) findViewById(R.id.rlBack);
        rlSort = (RelativeLayout) findViewById(R.id.rlSort);
        imgSort = (ImageView) findViewById(R.id.imgSort);
        tvSearch = (EditText) findViewById(R.id.edtSearch);
        setEvent();
    }

    private void setEvent(){
        rlBack.setOnClickListener(this);
        rlSort.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
    }

    private void createPopupMenu(View v){
        PopupMenu menu = new PopupMenu(this,v);
        menu.getMenuInflater().inflate(R.menu.popup_menu, menu.getMenu());
        menu.show();
    }

    private void createSortMenu(View v){
        PopupMenu menu = new PopupMenu(this,v);
        menu.getMenuInflater().inflate(R.menu.sort_favorite_menu, menu.getMenu());
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
        }
    }
}
