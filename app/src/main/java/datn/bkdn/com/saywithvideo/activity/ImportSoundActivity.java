package datn.bkdn.com.saywithvideo.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListImportSoundAdapter;
import datn.bkdn.com.saywithvideo.model.ImportSound;

public class ImportSoundActivity extends AppCompatActivity implements View.OnClickListener,
        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, ListImportSoundAdapter.OnItemClicked,
        MediaPlayer.OnCompletionListener {

    private RelativeLayout mRlBack;
    private SearchView mSearchView;
    private ListView mLvSound;
    private ListImportSoundAdapter mAdapter;
    final String MEDIA_PATH = Environment.getExternalStorageDirectory().getPath() + "/";
    private ArrayList<ImportSound> songsList;
    private String[] mPattern = new String[]{".mp3", ".aac", ".m4a", ".wav", "ogg"};
    private MediaPlayer mMediaPlayer;
    private int mPrePos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_sound);

        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mSearchView = (SearchView) findViewById(R.id.searchView);

        mLvSound = (ListView) findViewById(R.id.lvSound);
        mSearchView.setQueryHint("Search Snips");
        mSearchView.setFocusable(true);
        songsList = new ArrayList<>();
        mMediaPlayer = new MediaPlayer();

        new LoadAllSound().execute();
    }

    private void init() {
        mAdapter = new ListImportSoundAdapter(this, new ArrayList<>(songsList));
        mLvSound.setAdapter(mAdapter);
        mLvSound.setOnItemClickListener(this);
        mAdapter.setPlayButtonClicked(this);

        mRlBack.setOnClickListener(this);
        mSearchView.setOnQueryTextListener(this);
        mAdapter.setPlayButtonClicked(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                finish();
                overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.clear();
        mAdapter.addAll(filter(newText));
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("Tien", "Click");
        Intent intent = new Intent(ImportSoundActivity.this, EditAudioActivity.class);
        intent.putExtra("FileName", songsList.get(position).getPath());
        intent.putExtra("Type", "Import");
        startActivity(intent);
    }

    @Override
    public void onClick(int pos, View v) {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            songsList.get(mPrePos).setIsPlaying(false);
        }

        mPrePos = pos;
        songsList.get(pos).setIsPlaying(true);
        mAdapter.notifyDataSetChanged();

        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(songsList.get(pos).getPath());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            mMediaPlayer.setOnCompletionListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        songsList.get(mPrePos).setIsPlaying(false);
        mAdapter.notifyDataSetChanged();
    }

    private class LoadAllSound extends AsyncTask<Void, Void, Void> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(ImportSoundActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle("Loading...");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            getPlayList();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            init();
        }
    }

    private List<ImportSound> filter(String s) {
        List<ImportSound> importSounds = new ArrayList<>();
        for (ImportSound sound : songsList) {
            if (sound.getName().contains(s)) {
                importSounds.add(sound);
            }
        }
        return importSounds;
    }

    public void getPlayList() {
        System.out.println(MEDIA_PATH);
        if (MEDIA_PATH != null) {
            File home = new File(MEDIA_PATH);
            File[] listFiles = home.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    System.out.println(file.getAbsolutePath());
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }
                }
            }
        }
    }

    private void scanDirectory(File directory) {
        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null && listFiles.length > 0) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        scanDirectory(file);
                    } else {
                        addSongToList(file);
                    }

                }
            }
        }
    }

    private boolean isAudio(String name) {
        for (String s : mPattern) {
            if (name.endsWith(s)) return true;
        }
        return false;
    }


    private void addSongToList(File song) {
        if (isAudio(song.getName())) {
            ImportSound importSound = new ImportSound();
            importSound.setName(song.getName());
            importSound.setPath(song.getPath());
            importSound.setIsPlaying(false);
            songsList.add(importSound);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.finish_in, R.anim.finish_out);
    }
}
