package datn.bkdn.com.saywithvideo.database;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Admin on 2/18/2016.
 */
public class Sound extends RealmObject {

    private String id;
    private String name;
    private String author;
    private boolean isPlaying;
    private boolean isFavorite;
    private int plays;
    private String linkDown;
    private String linkOnDisk;
    private String dateOfCreate;
    private String idUser;
    private RealmList<User> listFavorite;

    public Sound() {
    }

    public Sound(String id, String name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;

    }

    public Sound(String id, String name, String author, String dateOfCreate) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.dateOfCreate = dateOfCreate;
    }

    public Sound(String id, String name, String author, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.isFavorite = isFavorite;
    }

    public Sound(String id, String name, String author, String linkDown, String linkOnDisk, String dateOfCreate) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.linkDown = linkDown;
        this.linkOnDisk = linkOnDisk;
        this.dateOfCreate = dateOfCreate;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public RealmList<User> getListFavorite() {
        return listFavorite;
    }

    public void setListFavorite(RealmList<User> listFavorite) {
        this.listFavorite = listFavorite;
    }

    public String getLinkDown() {
        return linkDown;
    }

    public void setLinkDown(String linkDown) {
        this.linkDown = linkDown;
    }

    public String getLinkOnDisk() {
        return linkOnDisk;
    }

    public void setLinkOnDisk(String linkOnDisk) {
        this.linkOnDisk = linkOnDisk;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public int getPlays() {
        return plays;
    }

    public void setPlays(int plays) {
        this.plays = plays;
    }

    public String getDateOfCreate() {
        return dateOfCreate;
    }

    public void setDateOfCreate(String dateOfCreate) {
        this.dateOfCreate = dateOfCreate;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
