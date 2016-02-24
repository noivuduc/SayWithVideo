package datn.bkdn.com.saywithvideo.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Admin on 2/21/2016.
 */
public class User extends RealmObject{
    @PrimaryKey
    private String id;
    private String email;
    private String name;
    private String pass;

    public User() {
    }

    public User(String pass, String name, String email) {
        this.pass = pass;
        this.name = name;
        this.email = email;
        this.id = UUID.randomUUID().toString();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

}
