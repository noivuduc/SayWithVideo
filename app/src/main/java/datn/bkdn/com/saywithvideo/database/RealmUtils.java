package datn.bkdn.com.saywithvideo.database;

import android.content.Context;
import android.util.Log;

import java.util.UUID;

import datn.bkdn.com.saywithvideo.model.Migration;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.model.User;
import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmResults;

/**
 * Created by Admin on 2/21/2016.
 */
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

    public void addNewSound(Context context,Sound sound) {

        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(sound);
        realm.commitTransaction();
      //  realm.close();

//        Realm.setDefaultConfiguration(config1);

    }

    public void addNewUser(Context context, String name, String pass, String email) {
        User user = new User(pass, name, email);
        String uuid = UUID.randomUUID().toString();
        user.setId(uuid);
        Realm realm = Realm.getInstance(context);

        realm.beginTransaction();
        realm.copyToRealm(user);
        realm.commitTransaction();
//        RealmResults<User> users = realm.allObjects(User.class);
    }

    public void deleteSound(Context context,String id) {
        realm = RealmManager.getRealm(context);
        realm.where(Sound.class).equalTo("id",id).findAll().clear();
    }

    public void updateFavorite(Context context, final String id){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                sound.setIsFavorite(!sound.isFavorite());
            }
        });
    }

    public void updatePlaying(Context context, final String id){
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id",id).findFirst();
                sound.setIsPlaying(!sound.isPlaying());
            }
        });
    }

    public RealmResults<Sound> getAllSound(Context context) {
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).findAll();
        //realm.close();
        return sounds;
    }

    public RealmResults<Sound> getFavoriteSound(Context context){
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).equalTo("isFavorite", true).findAll();
        return sounds;
    }

    public User checkisValidAccount(Context context, String email, String pass){
        realm = RealmManager.getRealm(context);
        RealmResults<User> users = realm.where(User.class).equalTo("email",email).equalTo("pass",pass).findAll();
        if(users.size()>0) return users.get(0);
        else
            return null;
    }
}
