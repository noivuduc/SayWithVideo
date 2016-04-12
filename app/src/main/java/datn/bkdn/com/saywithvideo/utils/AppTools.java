package datn.bkdn.com.saywithvideo.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.Snackbar;

import com.firebase.client.utilities.Base64;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.firebase.FireBaseContent;
import datn.bkdn.com.saywithvideo.firebase.FirebaseConstant;
import datn.bkdn.com.saywithvideo.firebase.FirebaseUser;

public class AppTools {
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hhmmss_ddMMyyyy");
        return format.format(date);
    }

    public static String getContentAudio(final String audioId, final Activity context) {
        if (datn.bkdn.com.saywithvideo.network.Tools.isOnline(context)) {
            String link = FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_CONTENT_URL + "/" + audioId + ".json";
            String json = datn.bkdn.com.saywithvideo.network.Tools.getJson(link);
            FireBaseContent c = new Gson().fromJson(json, FireBaseContent.class);
            String content = c.getContent();
            final String path_audio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + audioId + ".m4a";
            try {
                Base64.decodeToFile(content, path_audio);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    RealmUtils.getRealmUtils(context).setSoundPath(context, audioId, path_audio);
                    return null;
                }
            }.execute();
            return path_audio;
        } else {
            Snackbar.make(context.getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
            return null;
        }


    }

    public static FirebaseUser getInfoUser(String id) {
        String link = FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + id + ".json";
        String json = datn.bkdn.com.saywithvideo.network.Tools.getJson(link);
        FirebaseUser f = new Gson().fromJson(json, FirebaseUser.class);
        return f;
    }

}
