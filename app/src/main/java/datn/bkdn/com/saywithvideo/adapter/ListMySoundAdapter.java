package datn.bkdn.com.saywithvideo.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.Comparator;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.lib.FirebaseRecyclerAdapter;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;

import static java.util.Collections.sort;

/**
 * Created by Admin on 4/10/2016.
 */
public class ListMySoundAdapter extends FirebaseRecyclerAdapter<ListMySoundAdapter.AudioViewholder, Audio> {
    private Context mContext;

    public interface OnItemClicked {
        void onClick(int pos, View v, Audio audio);
    }

    public OnItemClicked mItemClicked;

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }



    public ListMySoundAdapter(Context context, Query query, Class<Audio> itemClass, @Nullable ArrayList<Audio> items, @Nullable ArrayList<String> keys) {
        super(query,null, itemClass, items, keys);
        this.mContext = context;
    }

    @Override
    public AudioViewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_mysound, parent, false);
        return new AudioViewholder(view);
    }

    @Override
    public void onBindViewHolder(AudioViewholder viewHolder, final int position) {
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
                    mItemClicked.onClick(position, v, sound);
                }
            }
        });
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(position, v, sound);
                }
            }
        });

        if (sound.isLoadAudio()) {
            viewHolder.progressPlay.setVisibility(View.VISIBLE);
            viewHolder.imgPlayPause.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.progressPlay.setVisibility(View.INVISIBLE);
            viewHolder.imgPlayPause.setVisibility(View.VISIBLE);
        }
        viewHolder.imgPlayPause.setImageResource(sound.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvSoundName.setText(sound.getName());
        viewHolder.tvPlays.setText(sound.getPlays() + " plays");
        viewHolder.tvDateOfCreate.setText(sound.getDate_create());
    }

    @Override
    protected void itemExist(final Audio item, final String key, final int position) {
        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                return Utils.getUserName(item.getUser_id());

            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                getItem(position).setAuthor(s);
            }
        }.execute();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Realm realm = RealmManager.getRealm(mContext);
                realm.beginTransaction();
                Sound s = realm.where(Sound.class).equalTo("id", key).findFirst();
                s.setIsFavorite(getItem(position).isFavorite());
                s.setAuthor(getItem(position).getAuthor());
                s.setPlays(item.getPlays());
                realm.commitTransaction();
                realm.close();
                return null;
            }
        }.execute();

    }

    @Override
    protected void itemAdded(Audio item, String key, int position) {
        String author = Utils.getCurrentUserName(mContext);
        item.setId(key);
        item.setAuthor(author);
        final Sound sound = convertAudio(item);
        if (!RealmUtils.getRealmUtils(mContext).checkExistSound(mContext, key)) {
            new AsyncAddSound().execute(sound);
        }
    }


    @Override
    protected void itemChanged(Audio oldItem, Audio newItem, String key, int position) {
        oldItem.setPlays(newItem.getPlays());
        getItems().set(position, oldItem);
        notifyDataSetChanged();
    }

    @Override
    protected void itemRemoved(final Audio item, final String key, int position) {
    }

    @Override
    protected void itemMoved(Audio item, String key, int oldPosition, int newPosition) {

    }

    public static class AudioViewholder extends RecyclerView.ViewHolder {
        private final TextView tvSoundName;
        private final TextView tvPlays;
        private final TextView tvDateOfCreate;
        private final ImageView imgPlayPause;
        private final LinearLayout linearLayout;
        private final ProgressBar progressPlay;
        private final RelativeLayout rlOption;

        public AudioViewholder(View itemView) {
            super(itemView);
            tvSoundName = (TextView) itemView.findViewById(R.id.tvSoundName);
            tvPlays = (TextView) itemView.findViewById(R.id.tvPlays);
            tvDateOfCreate = (TextView) itemView.findViewById(R.id.tvDate);
            imgPlayPause = (ImageView) itemView.findViewById(R.id.imgPlay);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.llSoundInfor);
            rlOption = (RelativeLayout) itemView.findViewById(R.id.rlOption);
            progressPlay = (ProgressBar) itemView.findViewById(R.id.progressPlay);
        }
    }

    class AsyncAddSound extends AsyncTask<Sound, Void, Void> {

        @Override
        protected Void doInBackground(Sound... sound) {
            Realm realm = RealmManager.getRealm(mContext);
            realm.beginTransaction();
            realm.copyToRealm(sound[0]);
            realm.commitTransaction();
            realm.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    public void sortByName() {
        sort(getItems(), new Comparator<Audio>() {
            @Override
            public int compare(Audio lhs, Audio rhs) {
                return lhs.getName().compareTo(rhs.getName());
            }
        });
        notifyDataSetChanged();
    }

    public void sortByPlays() {
        sort(getItems(), new Comparator<Audio>() {
            @Override
            public int compare(Audio lhs, Audio rhs) {
                return rhs.getPlays() + "".compareTo(lhs.getPlays() + "");
            }
        });
        notifyDataSetChanged();
    }

    public void sortByDateUpload() {
        sort(getItems(), new Comparator<Audio>() {
            @Override
            public int compare(Audio lhs, Audio rhs) {
                return lhs.getDate_create().compareTo(rhs.getDate_create());
            }
        });
        notifyDataSetChanged();
    }

    private Sound convertAudio(Audio audio) {
        return new Sound(audio.getId(), audio.getName(), audio.getAuthor(),
                audio.isFavorite(), audio.getPlays(), audio.getDate_create(),
                audio.getUser_id());
    }
}
