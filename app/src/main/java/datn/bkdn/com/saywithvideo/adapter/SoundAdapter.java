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

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;

import java.util.ArrayList;
import java.util.HashMap;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.lib.FirebaseRecyclerAdapter;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by Admin on 4/7/2016.
 */

public class SoundAdapter extends FirebaseRecyclerAdapter<SoundAdapter.SoundHolder, Audio> implements RealmChangeListener {
    private ArrayList<String> mFavorites;
    private Context mContext;
    private RealmResults<Sound> mSounds;
    private HashMap<String, String> mUsername = new HashMap<>();

    private ChildEventListener mListenner = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (mFavorites == null) {
                mFavorites = new ArrayList<>();
            }
            String key = dataSnapshot.getKey();
            if (!mFavorites.contains(key)) {
                mFavorites.add(key);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mFavorites.remove(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    };

    public SoundAdapter(Query query, @Nullable Query favorite, Class<Audio> itemClass, RealmResults<Sound> sounds, @Nullable ArrayList<Audio> items, @Nullable ArrayList<String> keys, Context mContext) {
        super(query, itemClass, items, keys);
        this.mSounds = sounds;
        this.mContext = mContext;
        mSounds.addChangeListener(this);
        if (favorite != null) {
            favorite.addChildEventListener(mListenner);
        }
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
        if (mUsername.containsKey(item.getUser_id())) {
            getItem(position).setAuthor(mUsername.get(item.getUser_id()));
            notifyDataSetChanged();
        } else {

            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {
                    String author = Utils.getUserName(item.getUser_id());
                    return author;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    getItem(position).setAuthor(s);
                    notifyDataSetChanged();
                    mUsername.put(item.getUser_id(), s);
                }
            }.execute();
        }

        if (mFavorites != null) {
            if (mFavorites.contains(key)) {
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
                s.setAuthor("");
                s.setPlays(item.getPlays());
                realm.commitTransaction();
                realm.close();
                return null;
            }
        }.execute();
    }

    @Override
    protected void itemAdded(final Audio item, final String key, final int position) {
        if (mUsername.containsKey(item.getUser_id())) {
            item.setAuthor(mUsername.get(item.getUser_id()));
        } else

            new AsyncTask<Void, Void, String>() {

                @Override
                protected String doInBackground(Void... params) {
                    String author = Utils.getUserName(item.getUser_id());
                    return author;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    item.setAuthor(s);
                    mUsername.put(item.getUser_id(), s);
                }
            }.execute();
        item.setId(key);
        final Sound sound = convertAudio(item);
        if (mFavorites != null) {
            if (mFavorites.contains(key)) {
                item.setIsFavorite(true);
                notifyItemChanged(position);
            } else {
                item.setIsFavorite(false);
            }
        }
        if (!RealmUtils.getRealmUtils(mContext).checkExistSound(mContext, key)) {
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
        Audio audio = new Audio(sound.getDateOfCreate(), sound.getName(), sound.getAuthor(),
                sound.getPlays(), sound.getIdUser(), sound.getId(), sound.getLinkOnDisk(), sound.isFavorite());
        return audio;
    }

    public interface OnItemClicked {
        void onClick(Audio audio, View v, int pos);
    }

    public OnItemClicked mItemClicked;

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

    public static class SoundHolder extends RecyclerView.ViewHolder {
        View mView;
        private TextView tvSoundAuthor;
        private TextView tvSoundName;
        private ImageView imgPlayPause;
        private ImageView imgFavorite;
        private ProgressBar progressBar;
        private ProgressBar progressPlay;
        private LinearLayout linearLayout;
        private RelativeLayout rlOption;
        private RelativeLayout rlFavorite;

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

    private Sound convertAudio(Audio audio) {
        return new Sound(audio.getId(), audio.getName(), audio.getAuthor(),
                audio.isFavorite(), audio.getPlays(), audio.getDate_create(),
                audio.getUser_id());
    }
}

