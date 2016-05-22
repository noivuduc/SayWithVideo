package datn.bkdn.com.saywithvideo.firebase;

/**
 * Created by Admin on 5/22/2016.
 */
public class FirebaseGroup {
    private String id;
    private String name;

    public FirebaseGroup(String name) {
        this.name = name;
    }

    public FirebaseGroup(String name, String id) {
        this.name = name;
        this.id = id;
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
}
