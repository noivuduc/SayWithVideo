package datn.bkdn.com.saywithvideo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Admin on 3/23/2016.
 */
public class FirebaseUser {
    private String name;
    private List<String> favorite;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFavorite() {
        if(favorite==null){
            favorite = new ArrayList<>();
        }
        return favorite;
    }

    public void setFavorite(List<String> favorite) {
        this.favorite = favorite;
    }
}
