package datn.bkdn.com.saywithvideo.database;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class RealmUtils {
    private static RealmUtils realmUtils;
    Realm realm;

    private RealmUtils() {
    }

    public static RealmUtils getRealmUtils(Context context) {
        if (realmUtils == null) {
            realmUtils = new RealmUtils();
        }
        return realmUtils;
    }


    public void deleteSound(final Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Sound.class).equalTo("id", id).findAll().removeLast();
            }
        });
        realm.close();
    }


    public void updateFavorite(Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                sound.setIsFavorite(!sound.isFavorite());
            }
        });
        realm.close();
    }


    public void updatePlaying(Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                sound.setIsPlaying(!sound.isPlaying());
            }
        });
        realm.close();
    }

    public void deleteAllSound(Context context){
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.where(Sound.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
        realm.close();
    }

    public boolean addVideo(final Context context, final String name, final String path, final String userId) {
        realm = RealmManager.getRealm(context);
        String id = UUID.randomUUID().toString();
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        Video video = new Video(id, name, ft.format(date), path, userId);
        realm.beginTransaction();
        realm.copyToRealm(video);
        realm.commitTransaction();
        realm.close();
        return true;
    }

    public RealmResults<Video> getVideo(Context context, String userId) {
        realm = RealmManager.getRealm(context);
        return realm.where(Video.class).equalTo("userId", userId).findAll();

    }

    public void deleteVideo(final Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Video.class).equalTo("id", id).findAll().deleteLastFromRealm();
            }
        });
        realm.close();
    }

    public boolean checkExistSound(Context mContext, String key) {
        realm = RealmManager.getRealm(mContext);
        RealmResults<Sound> sounds = realm.where(Sound.class).equalTo("id", key).findAll();
        return sounds.size() > 0;
    }
}