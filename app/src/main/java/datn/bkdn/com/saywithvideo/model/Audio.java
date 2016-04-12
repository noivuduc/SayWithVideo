package datn.bkdn.com.saywithvideo.model;

import org.parceler.Parcel;

/**
 * Created by Admin on 4/3/2016.
 */
@Parcel
public class Audio {
    private String date_create;
    private String name;
    private String author;
    private int plays;
    private String user_id;
    private String id;
    private String link_on_Disk;
    private boolean isPlaying;
    private boolean isFavorite;

    public Audio() {
    }

    public Audio(String date_create, String name, String author, int plays, String user_id, String id, String link_on_Disk, boolean isFavorite) {
        this.date_create = date_create;
        this.name = name;
        this.author = author;
        this.plays = plays;
        this.user_id = user_id;
        this.id = id;
        this.link_on_Disk = link_on_Disk;
        this.isFavorite = isFavorite;
    }

    public String getLink_on_Disk() {
        return link_on_Disk;
    }

    public void setLink_on_Disk(String link_on_Disk) {
        this.link_on_Disk = link_on_Disk;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public boolean isPlaying() {
        return isPlaying;
    }

    public void setIsPlaying(boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

}
