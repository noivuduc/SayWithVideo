package datn.bkdn.com.saywithvideo.database;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.model.Migration;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.model.User;
import datn.bkdn.com.saywithvideo.model.Video;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
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

    public void addNewSound(Context context, Sound sound) {

        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(sound);
        realm.commitTransaction();
        //  realm.close();

//        Realm.setDefaultConfiguration(config1);

    }


    public void deleteSound(Context context, String id) {
        realm = RealmManager.getRealm(context);
        realm.where(Sound.class).equalTo("id", id).findAll().clear();
    }

    public void updateFavorite(Context context, final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                sound.setIsFavorite(!sound.isFavorite());
            }
        });
    }

    public void updatePlaying(Context context, final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                sound.setIsPlaying(!sound.isPlaying());
            }
        });
    }

    public RealmResults<Sound> getSoundOfUser(Context context, String id) {
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).equalTo("idUser", id).findAll();
        return sounds;
    }

    public RealmResults<Sound> getAllSound(Context context) {
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).findAll();
        //realm.close();
        return sounds;
    }

    public RealmResults<Sound> getFavoriteSound(Context context) {
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).equalTo("isFavorite", true).findAll();
        return sounds;
    }

    public User checkisValidAccount(Context context, String email, String pass) {
        realm = RealmManager.getRealm(context);
        RealmResults<User> users = realm.where(User.class).equalTo("email", email).equalTo("pass", pass).findAll();
        if (users.size() > 0) return users.get(0);
        else
            return null;
    }

    public boolean checkExistsEmail(Context context, String email) {
        realm = RealmManager.getRealm(context);
        RealmResults<User> users = realm.where(User.class).equalTo("email", email).findAll();
        if (users.size() > 0) return true;
        return false;
    }

    public RealmResults<User> getUserWithEmail(Context context, String email) {
        realm = RealmManager.getRealm(context);
        RealmResults<User> users3 = realm.where(User.class).findAll();
        Log.d("size", users3.size() + "");
        RealmResults<User> users = realm.where(User.class).equalTo("email", email).findAll();
        Log.d("size", users.size() + "");
        return users;
    }

    public boolean addUser(Context context, String name, String pass, String email) {
        try {
            realm = RealmManager.getRealm(context);
            User user = new User(pass, name, email);
            realm.beginTransaction();
            realm.copyToRealm(user);
            realm.commitTransaction();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addVideo(Context context, String name, String path) {
        realm = RealmManager.getRealm(context);
        String id = UUID.randomUUID().toString();
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        Video video = new Video(id, name, ft.format(date).toString(), path);
        realm.beginTransaction();
        realm.copyToRealm(video);
        realm.commitTransaction();
        return true;
    }

    public RealmResults<Video> getVideo(Context context) {
        realm = RealmManager.getRealm(context);
        return realm.where(Video.class).findAll();
    }
}
