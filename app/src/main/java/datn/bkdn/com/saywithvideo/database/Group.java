package datn.bkdn.com.saywithvideo.database;

import io.realm.RealmObject;

<<<<<<< Updated upstream
/**
 * Created by Admin on 5/22/2016.
 */
public class Group extends RealmObject{
=======
public class Group extends RealmObject {
>>>>>>> Stashed changes
    private String id;
    private String name;

    public Group() {
    }

    public Group(String id, String name) {
        this.id = id;
        this.name = name;
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
