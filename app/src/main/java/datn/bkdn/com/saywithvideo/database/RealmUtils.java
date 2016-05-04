package datn.bkdn.com.saywithvideo.database;

import android.content.Context;

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
        s.setAuthor(sound.getAuthor());
        s.setLinkOnDisk(sound.getLink_on_Disk());
        s.setPlays(sound.getPlays());
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
        return realm.where(RealmAudioUser.class).findAllAsync();
    }

    public RealmResults<Sound> getAllSound(Context context) {
        realm = RealmManager.getRealm(context);
        return realm.where(Sound.class).findAllAsync();
        //realm.close();
    }

    public boolean checkExistSound(Context context, String id) {
        realm = RealmManager.getRealm(context);
        int s = realm.where(Sound.class).equalTo("id", id).findAll().size();
        return s > 0;
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
        return realm.where(ContentAudio.class).equalTo("id", id).findFirst();
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

    public boolean addVideo(final Context context, final String name, final String path, final String userId) {
        realm = RealmManager.getRealm(context);
        String id = UUID.randomUUID().toString();
        Date date = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy");
        Video video = new Video(id, name, ft.format(date), path, userId);
        realm.beginTransaction();
        realm.copyToRealm(video);
        realm.commitTransaction();

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
                realm.where(Video.class).equalTo("id", id).findAll().removeLast();
            }
        });
    }
}