package datn.bkdn.com.saywithvideo.database;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {

    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            schema.create("Sound")
                    .addField("id", String.class)
                    .addField("name", String.class)
                    .addField("author", String.class)
                    .addField("dateupload", String.class)
                    .addField("datecreated", String.class)
                    .addField("isfavorite", String.class);
            schema.create("User")
                    .addField("id", String.class)
                    .addField("name", String.class)
                    .addField("pass", String.class)
                    .addField("email", String.class);
            oldVersion++;
        }
    }
}
