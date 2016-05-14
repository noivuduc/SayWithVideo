package datn.bkdn.com.saywithvideo.database;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmManager {
    private static RealmConfiguration defaultConfig;

    public static Realm getRealm(Context context) {

        if (defaultConfig == null) {
            defaultConfig = getDefaultConfig(context);
        }
        return Realm.getInstance(defaultConfig);
    }

    public static RealmConfiguration getConfig(Context context) {
        return new RealmConfiguration.Builder(context)
                .schemaVersion(0)
                .migration(new Migration())
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    public static RealmConfiguration getDefaultConfig(Context context){
        RealmConfiguration config = new RealmConfiguration.Builder(context)
                .build();
        return config;
    }
}
