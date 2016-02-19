package datn.bkdn.com.saywithvideo.model;

/**
 * Created by Admin on 2/18/2016.
 */
public class Sound {

    private int id;
    private String name;
    private String author;
    private boolean isPlaying;
    private boolean isFavorite;
    private int plays;
    private String dateOfCreate;
    public Sound(int id, String name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;

    }

    public Sound(int id, String name, String author, String dateOfCreate) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.dateOfCreate = dateOfCreate;
    }

    public Sound(int id, String name, String author, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.isFavorite = isFavorite;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
