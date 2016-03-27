package datn.bkdn.com.saywithvideo.database;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.model.AudioUser;
import datn.bkdn.com.saywithvideo.model.ContentAudio;
import datn.bkdn.com.saywithvideo.model.Sound;
import datn.bkdn.com.saywithvideo.model.User;
import datn.bkdn.com.saywithvideo.model.Video;
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

    public void addNewSound(Context context, Sound sound) {

        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(sound);
        realm.commitTransaction();
        //  realm.close();

//        Realm.setDefaultConfiguration(config1);

    }


    public void deleteSound(Context context, final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(AudioUser.class).equalTo("id", id).findAll().clear();
                realm.where(ContentAudio.class).equalTo("id", id).findAll().clear();
            }
        });
    }

    public void deleteSoundContent(Context context, final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(ContentAudio.class).equalTo("id", id).findAll().clear();
            }
        });

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

    public void updatePlays(Context context, final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                if (sound != null) {
                    sound.setPlays(sound.getPlays() + 1);
                }
                AudioUser audioUser = realm.where(AudioUser.class).equalTo("id", id).findFirst();
                if (audioUser != null) {
                    audioUser.setPlays(audioUser.getPlays() + 1);
                }

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

    public void updateSoundUserPlaying(Context context, final String id) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                AudioUser sound = realm.where(AudioUser.class).equalTo("id", id).findFirst();
                sound.setIsPlaying(!sound.isPlaying());
            }
        });
    }

    public void addAudioUser(Context context, AudioUser audio){
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(audio);
        realm.commitTransaction();
    }

    public void deleteAllAudioUser(Context context){
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.where(AudioUser.class).findAll().clear();
        realm.commitTransaction();
    }
    public RealmResults<AudioUser> getSoundOfUser(Context context, String id) {
        realm = RealmManager.getRealm(context);
        RealmResults<AudioUser> sounds = realm.where(AudioUser.class).findAll();
        return sounds;
    }

    public RealmResults<Sound> getAllSound(Context context) {
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).findAll();
        //realm.close();
        return sounds;
    }

    public void addSoundContent(Context context, ContentAudio audio){
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(audio);
        realm.commitTransaction();
    }

    public ContentAudio getContentAudio(Context context, String id){
        realm = RealmManager.getRealm(context);
        RealmResults<ContentAudio> audios = realm.where(ContentAudio.class).equalTo("id",id).findAll();
        if(audios.size()>0){
            Log.d("nnnnn","nnnn");
            return audios.get(0);
        }
        return null;
    }

    public void deleteAllSound(Context context){
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.where(Sound.class).findAll().clear();
        realm.commitTransaction();
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
