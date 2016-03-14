package datn.bkdn.com.saywithvideo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hhmmss_ddMMyyyy");
        return format.format(date);
    }
}
