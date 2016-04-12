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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.database.RealmManager;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Sound;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.model.Audio;
import datn.bkdn.com.saywithvideo.utils.Utils;
import io.realm.Realm;


public class SoundAdapter extends FirebaseRecyclerAdapter<SoundAdapter.SoundHolder,Audio>{
    private Context mContext;
    public SoundAdapter(Query query, Class<Audio> itemClass, Context context) {
        super(query, itemClass);
        this.mContext = context;
    }

    public SoundAdapter(Query query, Class<Audio> itemClass, @Nullable ArrayList<Audio> items, @Nullable ArrayList<String> keys, Context mContext) {
        super(query, itemClass, items, keys);
        this.mContext = mContext;
    }

    @Override
    public SoundHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_sound,parent,false);
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
                    mItemClicked.onClick(model, v,position);
                   itemChanged(model,model,getKeys().get(position),position);
                }
            }
        });
        viewHolder.imgPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v,position);
                }
            }
        });
        viewHolder.rlOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClicked != null) {
                    mItemClicked.onClick(model, v,position);
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

        viewHolder.imgFavorite.setImageResource(model.isFavorite() ? R.mipmap.favorite_selected : R.mipmap.favorite_unselected);
        viewHolder.imgPlayPause.setImageResource(model.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play);
        viewHolder.tvSoundName.setText(model.getName());
        viewHolder.tvSoundAuthor.setText("upload by " + model.getAuthor());
    }

    @Override
    protected void itemAdded(final Audio item, final String key, int position) {
        String author = Utils.getUserName(item.getUser_id());
        item.setAuthor(author);
        item.setId(key);
        final Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + Utils.getCurrentUserID(mContext) + "/favorite/");
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                firebase.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(key)) {
                            item.setIsFavorite(true);
                            notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
                return null;
            }
        }.execute();
        Sound sound = convertAudio(item);
        if(!RealmUtils.getRealmUtils(mContext).checkExistSound(mContext,key))
        {
            new AsyncAddSound().execute(sound);
        }
        else
        {
            new AsyncTask<Void, Void,Void>(){

                @Override
                protected Void doInBackground(Void... params) {
                    RealmUtils.getRealmUtils(mContext).updateSound(mContext,key,item);
                    return null;
                }
            }.execute();
        }
    }

    @Override
    protected void itemChanged(Audio oldItem, final Audio newItem, final String key, int position) {
        new AsyncTask<Void, Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                RealmUtils.getRealmUtils(mContext).updateSound(mContext,key,newItem);
                return null;
            }
        }.execute();
    }

    @Override
    protected void itemRemoved(Audio item, final String key, int position) {
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... params) {
                Realm realm = RealmManager.getRealm(mContext);
                realm.where(Sound.class).equalTo("id",key).findAll().clear();
                return null;
            }
        }.execute();
    }

    @Override
    protected void itemMoved(Audio item, String key, int oldPosition, int newPosition) {

    }

    public interface OnItemClicked {
        void onClick(Audio audio, View v, int pos);
    }
    public OnItemClicked mItemClicked;

    public void setPlayButtonClicked(OnItemClicked playButtonClicked) {
        this.mItemClicked = playButtonClicked;
    }

  public static class SoundHolder extends RecyclerView.ViewHolder{
        View mView;
        private TextView tvSoundAuthor;
        private TextView tvSoundName;
        private ImageView imgPlayPause;
        private ImageView imgFavorite;
        private ImageView imgMenu;
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
            imgMenu = (ImageView) mView.findViewById(R.id.imgOption);
            linearLayout = (LinearLayout) mView.findViewById(R.id.llSoundInfor);
            rlOption = (RelativeLayout) mView.findViewById(R.id.rlOption);
            rlFavorite = (RelativeLayout) mView.findViewById(R.id.rlFavorite);
        }
    }
    class AsyncAddSound extends AsyncTask<Sound, Void, Void> {

        @Override
        protected Void doInBackground(Sound... sound) {
            RealmUtils.getRealmUtils(mContext).addNewSound(mContext, sound[0]);
            return null;
        }
    }

    private Sound convertAudio(Audio audio){
        return new Sound(audio.getId(),audio.getName(),audio.getAuthor(),
                audio.isFavorite(),audio.getPlays(),audio.getDate_create(),
                audio.getUser_id());
    }
}

