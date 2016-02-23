package datn.bkdn.com.saywithvideo.activity;

/**
 * Created by Admin on 2/21/2016.
 */

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import datn.bkdn.com.saywithvideo.R;

public class test extends Activity {
    public AudioRecord audioRecord;
    ByteBuffer buffer1;
    private  boolean mStartRecording = true;
    public int mSamplesRead; //how many samples read
    public int buffersizebytes;
    public int buflen;
    TextView textView;
    StringBuilder stringBuilder = new StringBuilder();
    StringBuilder stringBuilder2 = new StringBuilder();
    private RelativeLayout buttonRecord;
    public int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    public int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    public static short[] buffer; //+-32767
    public static final int SAMPPERSEC = 8000; //samp per sec 8000, 11025, 22050 44100 or 48000

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_record_new_sound);
        buffersizebytes = AudioRecord.getMinBufferSize(SAMPPERSEC, channelConfiguration, audioEncoding); //4096 on ion
        buffer = new short[buffersizebytes];
        buflen = buffersizebytes / 2;
        audioRecord = new AudioRecord(android.media.MediaRecorder.AudioSource.MIC, SAMPPERSEC,
                channelConfiguration, audioEncoding, buffersizebytes); //constructor
        buttonRecord = (RelativeLayout) findViewById(R.id.rlStartRecord);
        textView = (TextView) findViewById(R.id.tvTime);
        buttonRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mStartRecording) {
                   trigger();
                    buttonRecord.setBackgroundResource(R.drawable.selector_button_record_a_sound_pressed);
                    mStartRecording=!mStartRecording;
                }else
                {
                    audioRecord.stop();
                    Log.d("Buffer ", stringBuilder.toString());
                    byte[] a = buffer1.array();
                    for(int i =0;i<a.length;i++){
                        stringBuilder2.append(a[i]+"");
                    }
                    Log.d("ByteBuffer  ", stringBuilder2.toString());
//                    int intSize = android.media.AudioTrack.getMinBufferSize(4410, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                            AudioFormat.ENCODING_PCM_8BIT);
//                    AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, 4410, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                            AudioFormat.ENCODING_PCM_8BIT, intSize, AudioTrack.MODE_STREAM);
//                    if (at!=null) {
//                        at.play();
//// Write the byte array to the track
//                        at.write(a, 0, a.length);
//                        at.stop();
//                        at.release();
//                    }
//                    else
//                        Log.d("TCAudio", "audio track is not initialised ");
                    playMp3(a);

                }
//                    playMp3(a);
                    //onPlay(true);
            }
        });

    }//oncreate
    private void playMp3(byte[] mp3SoundByteArray) {
        try {
            // create temp file that will hold byte array
            File tempMp3 = File.createTempFile("kurchina", "3gp", getCacheDir());
            tempMp3.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(tempMp3);
            fos.write(mp3SoundByteArray);
            fos.close();

            // Tried reusing instance of media player
            // but that resulted in system crashes...
            MediaPlayer mediaPlayer = new MediaPlayer();

            // Tried passing path directly, but kept getting
            // "Prepare failed.: status=0x1"
            // so using file descriptor instead
            FileInputStream fis = new FileInputStream(tempMp3);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(fis.getFD());

            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {
            String s = ex.toString();
            ex.printStackTrace();
        }
    }
    //--------------------------------------
    public void trigger() {
        acquire();
        dump();
    }

    public void acquire() {
        try {
            audioRecord.startRecording();
            mSamplesRead = audioRecord.read(buffer, 0, buffersizebytes);
           // audioRecord.stop();
        } catch (Throwable t) {
// Log.e("AudioRecord", "Recording Failed");
        }
    }

    public void dump() {
        for (int i = 0; i < 256; i++) {
            stringBuilder.append(" " + buffer[i]);
            buffer1 = ByteBuffer.allocate(256 * 2);
            buffer1.putShort(buffer[i]);
        }

    }

    //-------lifecycle callbacks-------------------
    @Override
    public void onResume() {
        super.onResume();
        trigger();
    }//onresume

    @Override
    public void onPause() {
        super.onPause();
        audioRecord.stop();
    }//onpause

    @Override
    public void onStop() {
        super.onStop();
        audioRecord.release();
    }//onpause

    @Override
    public boolean onTouchEvent(MotionEvent motionevent) {
        if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {
            trigger(); //acquire buffer full of samples
        }
        return true;
    }
}
