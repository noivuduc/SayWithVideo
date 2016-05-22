package datn.bkdn.com.saywithvideo.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.GroupActivity;
import datn.bkdn.com.saywithvideo.adapter.GroupAdapter;
import datn.bkdn.com.saywithvideo.database.Group;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseGroup;
import datn.bkdn.com.saywithvideo.network.Tools;
import io.realm.RealmResults;

/**
 * Created by Admin on 5/22/2016.
 */
public class GroupFragment extends Fragment {
    private ArrayList<FirebaseGroup> mFirebaseGroups;
    private GroupAdapter mAdapter;
    private RecyclerView mRecycle;
    private SweetAlertDialog mAlertDialog;

    public static GroupFragment newInstance() {

        Bundle args = new Bundle();

        GroupFragment fragment = new GroupFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sound_group, container, false);
        mFirebaseGroups = new ArrayList<>();
        mRecycle = (RecyclerView) v.findViewById(R.id.recycleGroup);
        mRecycle.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecycle.setHasFixedSize(true);
        boolean isOnline = Tools.isOnline(getContext());
        if (isOnline) {
            RealmUtils.getRealmUtils(getContext()).deleteGroup(getContext());
            getDataFromServer();
        } else {
            getDataFromRealm();
        }
        return v;
    }

    private void initAdapter(){
        mAdapter.setGroupItemClicked(new GroupAdapter.GroupItemClicked() {
            @Override
            public void onClick(int pos) {
                ArrayList<String> groups = new ArrayList<>();
                groups.add(mFirebaseGroups.get(pos).getId());
                groups.add(mFirebaseGroups.get(pos).getName());

                Intent intent = new Intent(getActivity(), GroupActivity.class);
                intent.putExtra("data",groups);
                getActivity().startActivity(intent);
            }
        });
    }

    private void getDataFromServer() {
        showDialog();
        Firebase firebase = new Firebase(FirebaseConstant.BASE_URL + FirebaseConstant.GROUP_URL);
        firebase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dimissDialog();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    FirebaseGroup firebaseGroup = new FirebaseGroup(data.child("name").getValue().toString(), data.getKey());
                    mFirebaseGroups.add(firebaseGroup);
                    Group g = new Group(firebaseGroup.getId(),firebaseGroup.getName());
                    new AsyncAddGroup().execute(g);
                }
                mAdapter = new GroupAdapter(mFirebaseGroups);
                mRecycle.setAdapter(mAdapter);
                initAdapter();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                dimissDialog();
            }
        });
    }


    private void getDataFromRealm() {
        new AsyncTask<Void, Void, ArrayList<FirebaseGroup>>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showDialog();
            }

            @Override
            protected ArrayList<FirebaseGroup> doInBackground(Void... params) {
                ArrayList<FirebaseGroup> mGroups = new ArrayList<FirebaseGroup>();
                RealmResults<Group> groups = RealmUtils.getRealmUtils(getContext()).getGroup(getContext());
                for (Group g : groups) {
                    FirebaseGroup group = new FirebaseGroup(g.getName(), g.getId());
                    mGroups.add(group);
                }
                return mGroups;
            }

            @Override
            protected void onPostExecute(ArrayList<FirebaseGroup> aVoid) {
                super.onPostExecute(aVoid);
                dimissDialog();
                mFirebaseGroups = aVoid;
                mAdapter = new GroupAdapter(mFirebaseGroups);
                mRecycle.setAdapter(mAdapter);
                initAdapter();
            }
        }.execute();
    }

    private void showDialog() {
        mAlertDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        mAlertDialog.setTitleText(getResources().getString(R.string.please_wait));
        mAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
    }

    private void dimissDialog() {
        mAlertDialog.dismiss();
    }

    class AsyncAddGroup extends AsyncTask<Group,Void,Void>{

        @Override
        protected Void doInBackground(Group... params) {
            RealmUtils.getRealmUtils(getContext()).addGroup(getContext(),params[0]);
            return null;
        }
    }
}
