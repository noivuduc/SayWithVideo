package datn.bkdn.com.saywithvideo.utils;

import android.app.Activity;

import com.firebase.client.utilities.Base64;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

                String link = FirebaseConstant.BASE_URL + FirebaseConstant.AUDIO_CONTENT_URL + "/" + audioId + ".json";
                String json = datn.bkdn.com.saywithvideo.network.Tools.getJson(link);
                FireBaseContent c = new Gson().fromJson(json, FireBaseContent.class);
                String content = c.getContent();
                String folderPath = Constant.DIRECTORY_PATH + Constant.AUDIO;
                Tools.createFolder(folderPath);
                final String path_audio = folderPath + "/" + audioId + ".m4a";
                try {
                    Base64.decodeToFile(content, path_audio);
                    return path_audio;
                } catch (IOException e) {
                    e.printStackTrace();

                }

        return null;
    }

    public static FirebaseUser getInfoUser(String id) {
        String link = FirebaseConstant.BASE_URL + FirebaseConstant.USER_URL + id + ".json";
        String json = datn.bkdn.com.saywithvideo.network.Tools.getJson(link);
        FirebaseUser f = new Gson().fromJson(json, FirebaseUser.class);
        return f;
    }

}
