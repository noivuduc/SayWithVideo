package datn.bkdn.com.saywithvideo.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.Query;
import com.jpardogo.android.googleprogressbar.library.FoldingCirclesDrawable;

import java.util.ArrayList;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.lib.FirebaseRecyclerAdapter;
import datn.bkdn.com.saywithvideo.model.Audio;
import io.realm.Realm;
import io.realm.RealmChangeListener;

public class ListFavoriteAdapter extends FirebaseRecyclerAdapter<ListFavoriteAdapter.SoundHolder, Audio> implements RealmChangeListener {
    public final Context mContext;
    private OnItemClicked mItemClicked;

    public ListFavoriteAdapter(Query query, @Nullable Query favorite, boolean isOnline, Class<Audio> itemClass, @Nullable ArrayList<Audio> items, @Nullable ArrayList<String> keys, Context mContext) {
        super(mContext, query, isOnline, favorite, itemClass, items, keys);
        this.mContext = mContext;

    }

    @Override
    public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_sound, parent, false);
        return new SoundHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(SoundHolder viewHolder, int position) {
        final Audio model = getItem(position);
        final int pos = viewHolder.getAdapterPosition();
        model.setId(getKeys().get(position));
        viewHolder.rlFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, pos);
                }
            }
        });
        viewHolder.imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, pos);
                }
            }
        });
        viewHolder.rlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, pos);
                }
            }
        });
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v, pos);
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
        String str = mContext.getResources().getString(R.string.upload_by) + "<b>"+model.getAuthor()+"</b>";
        viewHolder.tvSoundAuthor.setText(Html.fromHtml(str));
    }

    /**
     * @param item     item exist
     * @param key      key of item
     * @param position position of item
     */
    @Override
    protected void itemExist(final Audio item, final String key, final int position) {
        if (getUsernames() != null)
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
        if (getUsernames() != null)
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
            new AsyncAddSound().execute(sound);
        }
    }

    @Override
    protected void itemChanged(final Audio oldItem, final Audio newItem, final String key, final int position) {


    }

    @Override
    protected void itemRemoved(Audio item, final String key, int position) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                RealmUtils.getRealmUtils(mContext).updateFavorite(mContext, key);
                return null;
            }
        }.execute();
    }

    @Override
    protected void itemMoved(Audio item, String key, int oldPosition, int newPosition) {

    }

    @Override
    protected void itemFavoriteRemoved(String key) {

    }

    @Override
    public void onChange(Object element) {

    }

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

    private Sound convertAudio(Audio audio) {
        return new Sound(audio.getId(), audio.getName(), audio.getAuthor(),
                audio.isFavorite(), audio.getPlays(), audio.getDate_create(),
                audio.getUser_id(),audio.getUrl(),audio.getGroup_id());
    }

    public interface OnItemClicked {
        void onClick(Audio audio, View v, int pos);
    }

    public static class SoundHolder extends RecyclerView.ViewHolder {
        private final TextView tvSoundAuthor;
        private final TextView tvSoundName;
        private final ImageView imgPlayPause;
        private final ImageView imgFavorite;
        private final ProgressBar progressBar;
        private final ProgressBar progressPlay;
        private final LinearLayout linearLayout;
        private final RelativeLayout rlOption;
        private final RelativeLayout rlFavorite;
        View mView;

        public SoundHolder(View itemView, Context context) {
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
            progressPlay.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(context).build());
            progressBar.setIndeterminateDrawable(new FoldingCirclesDrawable.Builder(context).build());
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
}

