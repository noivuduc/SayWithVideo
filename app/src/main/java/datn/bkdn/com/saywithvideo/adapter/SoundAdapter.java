package datn.bkdn.com.saywithvideo.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.lib.FirebaseRecyclerAdapter;
import datn.bkdn.com.saywithvideo.model.Audio;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Admin on 4/7/2016.
 */

public class SoundAdapter extends FirebaseRecyclerAdapter<SoundAdapter.SoundHolder, Audio> implements RealmChangeListener {
    private final Context mContext;
    private final RealmResults<Sound> mSounds;

    public SoundAdapter(Query query, @Nullable Query favorite, Class<Audio> itemClass, RealmResults<Sound> sounds, @Nullable ArrayList<Audio> items, @Nullable ArrayList<String> keys, Context mContext) {
        super(query,favorite, itemClass, items, keys);
        this.mSounds = sounds;
        this.mContext = mContext;
        mSounds.addChangeListener(this);

    }

    @Override
    public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_sound, parent, false);
        return new SoundHolder(view);
    }

    @Override
    public void onBindViewHolder(SoundHolder viewHolder, final int position) {
        final Audio model = getItem(position);
        model.setId(getKeys().get(position));
        viewHolder.rlFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, position);
                }
            }
        });
        viewHolder.imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, position);
                }
            }
        });
        viewHolder.rlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, position);
                }
            }
        });
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, position);
                }
            }
        });

        if (model.isLoadFavorite()) {
            viewHolder.progressBar.setVisibility(View.VISIBLE);
            viewHolder.imgFavorite.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.progressBar.setVisibility(View.INVISIBLE);
            viewHolder.imgFavorite.setVisibility(View.VISIBLE);
        }

        if (model.isLoadAudio()) {
            viewHolder.progressPlay.setVisibility(View.VISIBLE);
            viewHolder.imgPlayPause.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.progressPlay.setVisibility(View.INVISIBLE);
            viewHolder.imgPlayPause.setVisibility(View.VISIBLE);
        }
        viewHolder.imgFavorite.setImageResource(model.isFavorite() ? R.mipmap.favorite_selected : R.mipmap.favorite_unselected);
        viewHolder.imgPlayPause.setImageResource(model.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvSoundName.setText(model.getName());
        viewHolder.tvSoundAuthor.setText("upload by " + model.getAuthor());
    }

    /**
     * @param item     item exist
     * @param key      key of item
     * @param position
     */
    @Override
    protected void itemExist(final Audio item, final String key, final int position) {
        if(getUsernames() != null)
            if (getUsernames().containsKey(item.getUser_id())) {
                getItem(position).setAuthor(getUsernames().get(item.getUser_id()));
            }
        if (getmFavorites() != null) {
            if (getmFavorites().contains(key)) {
                getItem(position).setIsFavorite(true);
                notifyDataSetChanged();
            } else {
                getItem(position).setIsFavorite(false);
                notifyDataSetChanged();
            }
        }
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
    protected void itemAdded(final Audio item, final String key, final int position) {
        item.setId(key);
        if(getUsernames() != null)
        if (getUsernames().containsKey(item.getUser_id())) {
            item.setAuthor(getUsernames().get(item.getUser_id()));
        }

        if (getmFavorites() != null) {
            if (getmFavorites().contains(key)) {
                item.setIsFavorite(true);
                notifyDataSetChanged();
            } else {
                item.setIsFavorite(false);
            }
        }
        final Sound sound = convertAudio(item);
        if (!RealmUtils.getRealmUtils(mContext).checkExistSound(mContext, key)) {
            Log.d("SoundAdapter","run");
            new AsyncAddSound().execute(sound);
        }
    }


    @Override
    protected void itemChanged(final Audio oldItem, final Audio newItem, final String key, final int position) {
        if (datn.bkdn.com.saywithvideo.network.Tools.isOnline(mContext)) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    Realm realm = RealmManager.getRealm(mContext);
                    realm.beginTransaction();
                    Sound s = realm.where(Sound.class).equalTo("id", key).findFirst();
                    s.setPlays(newItem.getPlays());
                    realm.commitTransaction();
                    realm.close();
                    return null;
                }
            }.execute();
        }
    }

    @Override
    protected void itemRemoved(Audio item, final String key, int position) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Realm realm = RealmManager.getRealm(mContext);
                realm.beginTransaction();
                realm.where(Sound.class).equalTo("id", key).findAll().clear();
                realm.commitTransaction();
                return null;
            }
        }.execute();
    }

    @Override
    protected void itemMoved(Audio item, String key, int oldPosition, int newPosition) {

    }

    @Override
    public void onChange() {
        Log.d("SoundAdapter.onChange", "SoundAdapter.onChange");
//        getItems().clear();
//        getKeys().clear();
//        for (Sound s : mSounds) {
//            Audio audio = convertAudio(s);
//            getItems().add(audio);
//            getKeys().add(audio.getId());
//        }
//        notifyDataSetChanged();
    }

    private Audio convertAudio(Sound sound) {
        return new Audio(sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite());
    }

    public interface OnItemClicked {
        void onClick(Audio audio, View v, int pos);
    }

    private OnItemClicked mItemClicked;

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

    public static class SoundHolder extends RecyclerView.ViewHolder {
        View mView;
        private final TextView tvSoundAuthor;
        private final TextView tvSoundName;
        private final ImageView imgPlayPause;
        private final ImageView imgFavorite;
        private final ProgressBar progressBar;
        private final ProgressBar progressPlay;
        private final LinearLayout linearLayout;
        private final RelativeLayout rlOption;
        private final RelativeLayout rlFavorite;

        public SoundHolder(View itemView) {
            super(itemView);
            mView = itemView;
            tvSoundName = (TextView) mView.findViewById(R.id.tvSoundName);
            tvSoundAuthor = (TextView) mView.findViewById(R.id.tvSoundAuthor);
            imgPlayPause = (ImageView) mView.findViewById(R.id.imgPlay);
            imgFavorite = (ImageView) mView.findViewById(R.id.imgFavorite);
            progressBar = (ProgressBar) mView.findViewById(R.id.progressFavorite);
            progressPlay = (ProgressBar) mView.findViewById(R.id.progressPlay);
            linearLayout = (LinearLayout) mView.findViewById(R.id.llSoundInfor);
            rlOption = (RelativeLayout) mView.findViewById(R.id.rlOption);
            rlFavorite = (RelativeLayout) mView.findViewById(R.id.rlFavorite);
        }


    }

   private class AsyncAddSound extends AsyncTask<Sound, Void, Void> {

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

    private Sound convertAudio(Audio audio) {
        return new Sound(audio.getId(), audio.getName(), audio.getAuthor(),
                audio.isFavorite(), audio.getPlays(), audio.getDate_create(),
                audio.getUser_id());
    }
}

