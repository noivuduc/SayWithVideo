package datn.bkdn.com.saywithvideo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.model.ImportSound;

public class ListImportSoundAdapter extends ArrayAdapter<ImportSound> {

    public interface OnItemClicked {
        void onClick(int pos, View v);
    }

    public OnItemClicked mItemClicked;

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

    public ListImportSoundAdapter(Context context, List<ImportSound> sounds) {
        super(context, -1, sounds);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_import_sound, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.tvAuthor = (TextView) convertView.findViewById(R.id.tvAuthor);
            viewHolder.imgPlay = (ImageView) convertView.findViewById(R.id.imgPlay);
            viewHolder.rlPlay = (RelativeLayout) convertView.findViewById(R.id.rlPlay);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.rlPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v);
                }
            }
        });

        ImportSound sound = getItem(position);
        viewHolder.imgPlay.setImageResource(sound.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvName.setText(sound.getName());
        viewHolder.tvAuthor.setText(sound.getAuthor());

        return convertView;
    }

    private class ViewHolder {
        private TextView tvName;
        private TextView tvAuthor;
        private ImageView imgPlay;
        private RelativeLayout rlPlay;
    }
}
