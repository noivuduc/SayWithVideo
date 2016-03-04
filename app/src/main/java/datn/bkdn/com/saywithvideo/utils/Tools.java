package datn.bkdn.com.saywithvideo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Tien on 3/2/2016.
 */
public class Tools {
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("hhmmss_ddMMyyyy");
        return format.format(date);
    }
}
