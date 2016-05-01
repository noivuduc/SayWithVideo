package datn.bkdn.com.saywithvideo.model;

/**
 * Created by Tien on 3/15/2016.
 */
public class ImportSound {
    private String name;
    private boolean isPlaying;
    private String path;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
