package datn.bkdn.com.saywithvideo.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.Query;

import java.util.Comparator;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.model.Audio;

import static java.util.Collections.sort;

/**
 * Created by Admin on 4/10/2016.
 */
public class ListMySoundAdapter2 extends FirebaseRecyclerAdapter<ListMySoundAdapter2.AudioViewholder,Audio>{

    public interface OnItemClicked {
        void onClick(int pos, View v, Audio audio);
    }
    public OnItemClicked mItemClicked;

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }
    public ListMySoundAdapter2(Query query, Class<Audio> itemClass) {
        super(query, itemClass);
    }

    @Override
    public AudioViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("MySoundAdapter","onCreateViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_mysound,parent,false);
        return new AudioViewholder(view);
    }

    @Override
    public void onBindViewHolder(AudioViewholder viewHolder, final int position) {
        Log.d("MySoundAdapter","onBindViewHolder");
        final Audio sound = getItem(position);
        viewHolder.imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v, sound);
                }
            }
        });
        viewHolder.rlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v,sound);
                }
            }
        });
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v,sound);
                }
            }
        });
        viewHolder.imgPlayPause.setImageResource(sound.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvSoundName.setText(sound.getName());
        viewHolder.tvPlays.setText(sound.getPlays() + " plays");
        viewHolder.tvDateOfCreate.setText(sound.getDate_create());
    }

    @Override
    protected void itemAdded(Audio item, String key, int position) {
       item.setId(key);
    }

    @Override
    protected void itemChanged(Audio oldItem, Audio newItem, String key, int position) {

    }

    @Override
    protected void itemRemoved(Audio item, String key, int position) {

    }

    @Override
    protected void itemMoved(Audio item, String key, int oldPosition, int newPosition) {

    }

    public static class AudioViewholder extends RecyclerView.ViewHolder{
        private TextView tvSoundName;
        private TextView tvPlays;
        private TextView tvDateOfCreate;
        private ImageView imgPlayPause;
        private LinearLayout linearLayout;
        private RelativeLayout rlOption;
        public AudioViewholder(View itemView) {
            super(itemView);
            tvSoundName = (TextView) itemView.findViewById(R.id.tvSoundName);
            tvPlays = (TextView) itemView.findViewById(R.id.tvPlays);
            tvDateOfCreate = (TextView) itemView.findViewById(R.id.tvDate);
            imgPlayPause = (ImageView) itemView.findViewById(R.id.imgPlay);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.llSoundInfor);
            rlOption = (RelativeLayout) itemView.findViewById(R.id.rlOption);
        }
    }

    public void sortByName(){
        sort(getItems(), new Comparator<Audio>() {
            @Override
            public int compare(Audio lhs, Audio rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        notifyDataSetChanged();
    }
    public void sortByPlays(){
        sort(getItems(), new Comparator<Audio>() {
            @Override
            public int compare(Audio lhs, Audio rhs) {
                return lhs.getPlays()+"".compareTo(rhs.getPlays()+"");
            }
        });
        notifyDataSetChanged();
    }
    public void sortByDateUpload(){
        sort(getItems(), new Comparator<Audio>() {
            @Override
            public int compare(Audio lhs, Audio rhs) {
                return lhs.getDate_create().compareTo(rhs.getDate_create());
            }
        });
        notifyDataSetChanged();
    }
}
