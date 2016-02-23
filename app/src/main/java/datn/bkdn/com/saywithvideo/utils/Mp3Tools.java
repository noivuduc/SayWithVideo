package datn.bkdn.com.saywithvideo.utils;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Admin on 2/22/2016.
 */
public class Mp3Tools {
    public void playMp3(String path){
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(path);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
