package datn.bkdn.com.saywithvideo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.firebase.client.Query;

import datn.bkdn.com.saywithvideo.lib.FirebaseRecyclerViewAdapter;

/**
 * Created by Admin on 4/30/2016.
 */
public class NewAdapter extends FirebaseRecyclerViewAdapter{
    public NewAdapter(Class modelClass, int modelLayout, Class viewHolderClass, Query ref, int pageSize, boolean orderASC) {
        super(modelClass, modelLayout, viewHolderClass, ref, pageSize, orderASC);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return super.onCreateViewHolder(parent, viewType);
    }
}
