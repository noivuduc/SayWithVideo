package datn.bkdn.com.saywithvideo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListMySoundAdapter;
import datn.bkdn.com.saywithvideo.model.Sound;

public class SoundActivity extends AppCompatActivity implements View.OnClickListener {
    private ListMySoundAdapter adapter;
    private RelativeLayout rlBack;
    private RelativeLayout rlSort;
    private EditText tvSearch;
    private ListView listView;
    private ImageView imgSort;
    private int currentPos=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound);
        init();
        final List<Sound> sounds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            sounds.add(new Sound(i, "Sound of Tien Oc Cho " + i, "Author Noi Oc Cho " + i,"21/2/2016"));
        }
        adapter = new ListMySoundAdapter(this,sounds);
        listView.setAdapter(adapter);

        adapter.setPlayButtonClicked(new ListMySoundAdapter.OnItemClicked() {

            @Override
            public void onClick(int pos, View v) {
                Sound sound = sounds.get(pos);
                switch (v.getId()){
                    case R.id.imgPlay:
                        if(currentPos != -1 && pos!=currentPos){
                            sounds.get(currentPos).setIsPlaying(false);
                        }
                        currentPos = pos;
                        sound.setIsPlaying(!sound.isPlaying());
                        adapter.notifyDataSetChanged();
                        break;
                    case R.id.llSoundInfor:
                        break;
                    case R.id.imgOption:
                        break;
                }
            }
        });
    }

    private void init(){
        listView = (ListView) findViewById(R.id.lvMySound);
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
        }
    }
}
