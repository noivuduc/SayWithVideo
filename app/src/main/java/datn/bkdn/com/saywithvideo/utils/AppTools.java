package datn.bkdn.com.saywithvideo.utils;

import android.app.Activity;
import android.os.Environment;
import android.support.design.widget.Snackbar;

import com.firebase.client.utilities.Base64;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import datn.bkdn.com.saywithvideo.database.RealmUtils;
import datn.bkdn.com.saywithvideo.model.ContentAudio;
import datn.bkdn.com.saywithvideo.model.FireBaseContent;
import datn.bkdn.com.saywithvideo.model.FirebaseConstant;

public class AppTools {
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hhmmss_ddMMyyyy");
        return format.format(date);
    }

    public static ContentAudio getContentAudio(String audioId, Activity context){
        ContentAudio contentAudio;
        contentAudio= RealmUtils.getRealmUtils(context).getContentAudio(context, audioId);
        if(contentAudio==null) {
            if (datn.bkdn.com.saywithvideo.network.Tools.isOnline(context)) {
                String link = FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_CONTENT_URL + "/" + audioId + ".json";
                String json = datn.bkdn.com.saywithvideo.network.Tools.getJson(link);
                FireBaseContent c = new Gson().fromJson(json, FireBaseContent.class);
                String content = c.getContent();
                String path_audio = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + audioId + ".m4a";
                ContentAudio audio = new ContentAudio();
                audio.setId(audioId);
                audio.setContent(path_audio);
                try {
                    Base64.decodeToFile(content, path_audio);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RealmUtils.getRealmUtils(context).addSoundContent(context, audio);

                contentAudio = RealmUtils.getRealmUtils(context).getContentAudio(context, audioId);
            }else
            {
                Snackbar.make(context.getCurrentFocus(), "Please make sure to have an internet connection.", Snackbar.LENGTH_LONG).show();
                return null;
            }
        }
        return contentAudio;
    }
}
