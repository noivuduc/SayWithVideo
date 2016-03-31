package datn.bkdn.com.saywithvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.model.Sound;
import io.realm.RealmResults;

/**
 * Created by Admin on 3/28/2016.
 */
public class DemoAdapter extends BaseAdapter {
    public OnItemClicked mItemClicked;
    private List<Sound> mSounds = Collections.emptyList();
    private Context mContext;

    public DemoAdapter(Context context) {
        this.mContext = context;
    }

    public void updateList(RealmResults<Sound> sounds) {
        this.mSounds = sounds;
        notifyDataSetChanged();
    }

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

    @Override
    public int getCount() {
        return mSounds.size();
    }

    @Override
    public Object getItem(int position) {
        return mSounds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_sound, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvSoundName = (TextView) convertView.findViewById(R.id.tvSoundName);
            viewHolder.tvSoundAuthor = (TextView) convertView.findViewById(R.id.tvSoundAuthor);
            viewHolder.imgPlayPause = (ImageView) convertView.findViewById(R.id.imgPlay);
            viewHolder.imgFavorite = (ImageView) convertView.findViewById(R.id.imgFavorite);
            viewHolder.imgMenu = (ImageView) convertView.findViewById(R.id.imgOption);
            viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.llSoundInfor);
            viewHolder.rlOption = (RelativeLayout) convertView.findViewById(R.id.rlOption);
            viewHolder.rlFavorite = (RelativeLayout) convertView.findViewById(R.id.rlFavorite);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        viewHolder.rlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        viewHolder.rlFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });
        Sound sound = (Sound) getItem(position);
        viewHolder.imgFavorite.setImageResource(sound.isFavorite() ? R.mipmap.favorite_selected : R.mipmap.favorite_unselected);
        viewHolder.imgPlayPause.setImageResource(sound.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvSoundName.setText(sound.getName());
        viewHolder.tvSoundAuthor.setText(sound.getAuthor());

        return convertView;
    }

    public interface OnItemClicked {
        void onClick(int pos, View v);
    }

    private class ViewHolder {
        private TextView tvSoundAuthor;
        private TextView tvSoundName;
        private ImageView imgPlayPause;
        private ImageView imgFavorite;
        private ImageView imgMenu;
        private LinearLayout linearLayout;
        private RelativeLayout rlOption;
        private RelativeLayout rlFavorite;
    }
}
