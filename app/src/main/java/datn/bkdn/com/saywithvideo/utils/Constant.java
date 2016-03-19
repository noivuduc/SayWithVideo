package datn.bkdn.com.saywithvideo.utils;

import android.os.Environment;

public class Constant {
    private static final String BASE = "/SayWithVideo/";
    public static final String AUDIO_DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String VIDEO_DIRECTORY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
    public static final String FIREBASE_ROOT="https://saywithvideo.firebaseio.com/";
}
