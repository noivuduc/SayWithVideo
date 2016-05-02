package datn.bkdn.com.saywithvideo.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.activity.MainActivity;
import datn.bkdn.com.saywithvideo.activity.ShareActivity;
import datn.bkdn.com.saywithvideo.activity.ShowVideoActivity;
import datn.bkdn.com.saywithvideo.adapter.ListMyVideoAdapter;
import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.database.Video;
import io.realm.RealmResults;

public class UserProfileFragment extends Fragment implements View.OnClickListener, ListMyVideoAdapter.OnItemClicked,
        ListMyVideoAdapter.OnMenuItemClicked {

    private LinearLayout mLlCreateDub;
    private TextView mTvCreateDub;
    private RealmResults<Video> mVideos;
    private BroadcastReceiver mBroadcastReceiver;

    public static UserProfileFragment newInstance() {

        Bundle args = new Bundle();

        UserProfileFragment fragment = new UserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_profile, container, false);

        mLlCreateDub = (LinearLayout) v.findViewById(R.id.llCreateDub);
        mTvCreateDub = (TextView) v.findViewById(R.id.tvCreateDub);
        ListView mLvMyVideo = (ListView) v.findViewById(R.id.lvMyDubs);

        mVideos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());

        if (mVideos.size() != 0) {
            mLlCreateDub.setVisibility(View.INVISIBLE);
        }
        final ListMyVideoAdapter mAdapter = new ListMyVideoAdapter(getContext(), mVideos);
        mAdapter.setPlayButtonClicked(this);
        mAdapter.setMenuItemClicked(this);
        mLvMyVideo.setAdapter(mAdapter);
        init();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mVideos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());
                mAdapter.notifyDataSetChanged();
                if (mVideos != null && mVideos.size() > 0) {
                    mLlCreateDub.setVisibility(View.GONE);
                }
            }
        };
        getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter("AddVideo"));

        return v;
    }

    private void init() {
        mTvCreateDub.setOnClickListener(this);
        mLlCreateDub.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvCreateDub:
                ((MainActivity) getContext()).showSounds();
                break;
        }
    }

    @Override
    public void onClick(int pos, View v) {
        Video video = mVideos.get(pos);
        switch (v.getId()) {
            case R.id.llinfo:
                Intent intent = new Intent(getActivity(), ShowVideoActivity.class);
                intent.putExtra("VideoPath", video.getPath());
                startActivity(intent);
                break;
            case R.id.imgshare:
                Intent i = new Intent(getActivity(), ShareActivity.class);
                i.putExtra("filePath", video.getPath());
                startActivity(i);
                break;
            case R.id.imgoption:
                Log.d("Position", pos + "");
                break;
        }
    }


    @Override
    public void onItemClick(final int pos, MenuItem menuItem) {
        final String newid = mVideos.get(pos).getId();
        switch (menuItem.getItemId()) {
            case R.id.delete:
                String path = mVideos.get(pos).getPath();
                RealmUtils.getRealmUtils(getContext()).deleteVideo(getContext(), newid);
                File file = new File(path);
                file.deleteOnExit();
                mVideos = RealmUtils.getRealmUtils(getContext()).getVideo(getContext());
                if (mVideos == null || mVideos.size() == 0) {
                    mLlCreateDub.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }
}
