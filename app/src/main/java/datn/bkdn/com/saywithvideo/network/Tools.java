package datn.bkdn.com.saywithvideo.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Admin on 3/21/2016.
 */
public class Tools {
    public static String getJson(final String link) {
        final StringBuilder json = new StringBuilder();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader bf = null;
                HttpURLConnection conn = null;
                try {
                    conn = (HttpURLConnection) new URL(link).openConnection();
                    int responseCode = conn.getResponseCode();

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        System.out.println("NO INTERNET");
                        return;
                    }
                    bf = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while((line = bf.readLine()) != null) {
                        json.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(conn != null) {
                            conn.disconnect();
                        }
                        if(bf != null) {
                            bf.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}
