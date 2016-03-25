package datn.bkdn.com.saywithvideo.utils;

import android.app.Activity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hhmmss_ddMMyyyy");
        return format.format(date);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
