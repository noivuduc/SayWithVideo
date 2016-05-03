package datn.bkdn.com.saywithvideo.database;

import io.realm.RealmObject;

public class Video extends RealmObject {
    private String id;
    private String name;
    private String time;
    private String path;
    private String userId;

    public Video(String id, String name, String time, String path, String userId) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.path = path;
        this.userId = userId;
    }

    public Video() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
