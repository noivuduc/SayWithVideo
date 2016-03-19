package datn.bkdn.com.saywithvideo.model;

public class FirebaseAudioContent {
    private String audio_id;
    private String content;

    public FirebaseAudioContent() {
    }

    public FirebaseAudioContent(String audio_id, String content) {
        this.audio_id = audio_id;
        this.content = content;
    }

    public String getAudio_id() {
        return audio_id;
    }

    public void setAudio_id(String audio_id) {
        this.audio_id = audio_id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
