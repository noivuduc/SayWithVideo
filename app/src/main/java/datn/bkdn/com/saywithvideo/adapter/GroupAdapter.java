package datn.bkdn.com.saywithvideo.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.firebase.FirebaseGroup;

/**
 * Created by Admin on 5/22/2016.
 */
public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupHolder>{
    private ArrayList<FirebaseGroup> mFirebaseGroups;
    public interface GroupItemClicked{
        void onClick(int pos);
    }

    private GroupItemClicked groupItemClicked;

    public void setGroupItemClicked(GroupItemClicked groupItemClicked) {
        this.groupItemClicked = groupItemClicked;
    }

    public GroupAdapter(ArrayList<FirebaseGroup> mFirebaseGroups) {
        this.mFirebaseGroups = mFirebaseGroups;
    }

    @Override
    public GroupAdapter.GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_sound_group,parent,false);
        return new GroupHolder(v);
    }

    @Override
    public void onBindViewHolder(GroupAdapter.GroupHolder holder, final int position) {
        FirebaseGroup firebaseGroup = mFirebaseGroups.get(position);
        holder.tvGroupname.setText(firebaseGroup.getName());
        holder.tvGroupname.setTextColor(Color.BLUE);

        holder.rlGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groupItemClicked !=null ){
                    groupItemClicked.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFirebaseGroups.size();
    }

    class GroupHolder extends RecyclerView.ViewHolder{
        private TextView tvGroupname;
        private ImageView imgIcon;
        private RelativeLayout rlGroup;
        public GroupHolder(View itemView) {
            super(itemView);
            tvGroupname = (TextView) itemView.findViewById(R.id.tvGroupName);
            imgIcon = (ImageView) itemView.findViewById(R.id.imgGroup);
            rlGroup = (RelativeLayout) itemView.findViewById(R.id.rlGroup);
        }
    }
}
