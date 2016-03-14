package datn.bkdn.com.saywithvideo.database;

import android.content.Context;

import datn.bkdn.com.saywithvideo.model.Migration;
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

    private static RealmConfiguration getConfig(Context context) {
        RealmConfiguration defaultConfig = new RealmConfiguration.Builder(context)
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        return defaultConfig;
    }
}
