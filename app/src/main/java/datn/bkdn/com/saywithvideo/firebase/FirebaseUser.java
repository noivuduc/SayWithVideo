package datn.bkdn.com.saywithvideo.firebase;

/**
 * Created by Admin on 3/23/2016.
 */
public class FirebaseUser {
    private String name;
    private int no_favorite;
    private int no_sound;
//    @Ignore
//    private String[] favorite;


    public int getNo_favorite() {
        return no_favorite;
    }

    public void setNo_favorite(int no_favorite) {
        this.no_favorite = no_favorite;
    }

    public int getNo_sound() {
        return no_sound;
    }

    public void setNo_sound(int no_sound) {
        this.no_sound = no_sound;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public List<String> getFavorite() {
//        if(favorite==null){
//            favorite = new ArrayList<>();
//        }
//        return favorite;
//    }
//
//    public void setFavorite(List<String> favorite) {
//        this.favorite = favorite;
//    }
}
