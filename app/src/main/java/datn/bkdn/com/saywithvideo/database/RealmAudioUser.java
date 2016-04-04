package datn.bkdn.com.saywithvideo.database;

import io.realm.RealmObject;

/**
 * Created by Admin on 3/23/2016.
 */
public class RealmAudioUser extends RealmObject {
    private String name;
    private int plays;
    private String userID;
    private String id;
    private String linkOnDisk;
    private String date_create;
    private boolean isPlaying;

    public RealmAudioUser(String name, int plays, String id, String date_create) {
        this.name = name;
        this.plays = plays;
        this.id = id;
        this.date_create = date_create;
    }

    public RealmAudioUser() {
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLinkOnDisk() {
        return linkOnDisk;
    }

    public void setLinkOnDisk(String linkOnDisk) {
        this.linkOnDisk = linkOnDisk;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate_create() {
        return date_create;
    }

    public void setDate_create(String date_create) {
        this.date_create = date_create;
    }
}
