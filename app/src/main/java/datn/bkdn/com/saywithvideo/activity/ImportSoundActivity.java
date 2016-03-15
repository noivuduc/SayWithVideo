package datn.bkdn.com.saywithvideo.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import datn.bkdn.com.saywithvideo.R;
import datn.bkdn.com.saywithvideo.adapter.ListImportSoundAdapter;
import datn.bkdn.com.saywithvideo.model.ImportSound;

public class ImportSoundActivity extends AppCompatActivity implements View.OnClickListener,
        SearchView.OnQueryTextListener, AdapterView.OnItemClickListener, ListImportSoundAdapter.OnItemClicked {

    private RelativeLayout mRlBack;
    private SearchView mSearchView;
    private ListView mLvSound;
    private List<ImportSound> mSounds;
    private ListImportSoundAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_sound);

        mRlBack = (RelativeLayout) findViewById(R.id.rlBack);
        mSearchView = (SearchView) findViewById(R.id.searchView);
        mLvSound = (ListView) findViewById(R.id.lvSound);
        mSearchView.setQueryHint("Search Snips");
        mSearchView.setFocusable(true);
        mSounds = new ArrayList<>();

        new LoadAllSound().execute();
    }

    private void init() {
        mAdapter = new ListImportSoundAdapter(this, mSounds);
        mLvSound.setAdapter(mAdapter);
        mLvSound.setOnItemClickListener(this);

        mRlBack.setOnClickListener(this);
        mSearchView.setOnQueryTextListener(this);
        mAdapter.setPlayButtonClicked(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlBack:
                finish();
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ImportSoundActivity.this, EditAudioActivity.class);
        intent.putExtra("FileName", mSounds.get(position).getPath());
        startActivity(intent);
    }

    @Override
    public void onClick(int pos, View v) {

    }

    private class LoadAllSound extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getAllSound();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            init();
        }
    }

    private void getAllSound() {
        final Cursor mCursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ARTIST}, null, null,
                "LOWER(" + MediaStore.Audio.Media.TITLE + ") ASC");

        if (mCursor.moveToFirst()) {
            do {
                ImportSound sound = new ImportSound();
                sound.setName(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                sound.setPath(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                sound.setAuthor(mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
                mSounds.add(sound);
            } while (mCursor.moveToNext());
        }
        mCursor.close();
    }
}
