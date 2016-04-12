package datn.bkdn.com.saywithvideo.database;

import io.realm.RealmObject;

public class Video extends RealmObject {
    private String id;
    private String name;
    private String time;
    private String path;
    private boolean isProfile;

    public Video(String id, String name, String time, String path) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.path = path;
    }

    public Video() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isProfile() {
        return isProfile;
    }

    public void setIsProfile(boolean isProfile) {
        this.isProfile = isProfile;
    }
}
