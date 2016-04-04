package datn.bkdn.com.saywithvideo.model;

/**
 * Created by Admin on 4/3/2016.
 */
public class Audio {
    private String id;
    private String name;
    private String author;
    private boolean isPlaying;
    private boolean isFavorite;
    private int plays;
    private String dateOfCreate;

    public Audio(String id, String name, String author, boolean isPlaying, boolean isFavorite, int plays, String dateOfCreate) {
        this.id = id;
        this.name = name;
        this.author = author;
        this.isPlaying = isPlaying;
        this.isFavorite = isFavorite;
        this.plays = plays;
        this.dateOfCreate = dateOfCreate;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getDateOfCreate() {
        return dateOfCreate;
    }

    public void setDateOfCreate(String dateOfCreate) {
        this.dateOfCreate = dateOfCreate;
    }
}
