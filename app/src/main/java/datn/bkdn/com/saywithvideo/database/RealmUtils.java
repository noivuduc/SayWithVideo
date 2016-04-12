package datn.bkdn.com.saywithvideo.database;

import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import datn.bkdn.com.saywithvideo.model.Audio;
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

    public void addNewFavorite(Context context, FavoriteAudio sound) {

        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        realm.copyToRealm(sound);
        realm.commitTransaction();
        //  realm.close();

//        Realm.setDefaultConfiguration(config1);

    }

    public void deleteSound(final Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(RealmAudioUser.class).equalTo("id", id).findAll().removeLast();
            }
        });
    }

    public void deleteSoundContent(Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(ContentAudio.class).equalTo("id", id).findAll().clear();
            }
        });

    }

    public void setSoundPath(Context context, String id, String path) {
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
        sound.setLinkOnDisk(path);
        realm.commitTransaction();
    }

    public void deleteFavoriteAudio(Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(FavoriteAudio.class).equalTo("id", id).findAll().clear();
            }
        });

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
    }

    public void updatePlays(Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Sound sound = realm.where(Sound.class).equalTo("id", id).findFirst();
                if (sound != null) {
                    sound.setPlays(sound.getPlays() + 1);
                }
                RealmAudioUser audioUser = realm.where(RealmAudioUser.class).equalTo("id", id).findFirst();
                if (audioUser != null) {
                    audioUser.setPlays(audioUser.getPlays() + 1);
                }

            }
        });
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
    }

    public void updateSoundUserPlaying(Context context, final String id) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                RealmAudioUser sound = realm.where(RealmAudioUser.class).equalTo("id", id).findFirst();
                sound.setIsPlaying(!sound.isPlaying());
            }
        });
    }

    public void updateSound(Context context, String id, Audio sound) {
        realm = RealmManager.getRealm(context);
        realm.beginTransaction();
        Sound s = realm.where(Sound.class).equalTo("id", id).findFirst();
        s.setIsFavorite(sound.isFavorite());
//        s.setAuthor(sound.getAuthor());
        s.setPlays(sound.getPlays());
        Log.d("updateSound",s.getAuthor()+"|||" +sound.getAuthor());
        realm.commitTransaction();
    }

    public void addAudioUser(Context context, final RealmAudioUser audio) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(audio);
            }
        });
    }

    public void deleteAllAudioUser(Context context) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(RealmAudioUser.class).findAll().clear();
            }
        });

    }

    public RealmResults<RealmAudioUser> getSoundOfUser(Context context, String id) {
        realm = RealmManager.getRealm(context);
        RealmResults<RealmAudioUser> sounds = realm.where(RealmAudioUser.class).findAllAsync();
        return sounds;
    }

    public RealmResults<Sound> getAllSound(Context context) {
        realm = RealmManager.getRealm(context);
        RealmResults<Sound> sounds = realm.where(Sound.class).findAllAsync();
        //realm.close();
        return sounds;
    }

    public boolean checkExistSound(Context context, String id) {
        realm = RealmManager.getRealm(context);
        int s = realm.where(Sound.class).equalTo("id", id).findAll().size();
        return s > 0 ? true : false;
    }

    public void addSoundContent(Context context, final ContentAudio audio) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealm(audio);
            }
        });
    }

    public ContentAudio getContentAudio(Context context, String id) {
        realm = RealmManager.getRealm(context);
        ContentAudio audios = realm.where(ContentAudio.class).equalTo("id", id).findFirst();
        return audios;
    }

    public void deleteAllSound(Context context) {
        realm = RealmManager.getRealm(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(Sound.class).findAll().clear();
            }
        });
    }

    public RealmResults<FavoriteAudio> getFavoriteSound(Context context) {
        realm = RealmManager.getRealm(context);
        RealmResults<FavoriteAudio> sounds = realm.where(FavoriteAudio.class).equalTo("isFavorite", true).findAllAsync();
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
        RealmResults<User> users = realm.where(User.class).equalTo("email", email).findAll();
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
        return realm.where(Video.class).findAllAsync();
    }

    public Video getVideoProfile(Context context) {
        RealmResults<Video> videos = realm.where(Video.class).equalTo("isProfile", true).findAll();
        return videos.size() > 0 ? videos.get(0) : null;
    }
}