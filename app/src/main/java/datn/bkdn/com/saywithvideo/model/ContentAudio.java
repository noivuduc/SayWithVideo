package datn.bkdn.com.saywithvideo.model;

import io.realm.RealmObject;

/**
 * Created by Admin on 3/21/2016.
 */
public class ContentAudio extends RealmObject {
    private String id;
    private String content;

    public ContentAudio() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
