package datn.bkdn.com.saywithvideo.database;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmManager {
    private static RealmConfiguration defaultConfig;

    public static Realm getRealm(Context context) {

        if (defaultConfig == null) {
            defaultConfig = getConfig(context);
            Realm.migrateRealm(defaultConfig,new Migration());
        }
        return Realm.getInstance(defaultConfig);
    }

    public static RealmConfiguration getConfig(Context context) {
        RealmConfiguration defaultConfig = new RealmConfiguration.Builder(context)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        return defaultConfig;
    }
}
