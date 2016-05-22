package datn.bkdn.com.saywithvideo.firebase;

public class FirebaseAudio {
    private String name;
    private String user_id;
    private String date_create;
    private String url;
    private String group_id;
    private int plays;

    public FirebaseAudio(String name, String user_id, String date_create, int plays, String url) {
        this.name = name;
        this.user_id = user_id;
        this.date_create = date_create;
        this.plays = plays;
        this.url = url;
    }

    public FirebaseAudio(String name, String user_id, String date_create, String url, String group_id, int plays) {
        this.name = name;
        this.user_id = user_id;
        this.date_create = date_create;
        this.url = url;
        this.group_id = group_id;
        this.plays = plays;
    }

    public FirebaseAudio() {
    }


    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDate_create() {
        return date_create;
    }

    public void setDate_create(String date_create) {
        this.date_create = date_create;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
