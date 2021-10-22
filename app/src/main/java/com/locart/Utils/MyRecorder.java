package com.locart.Utils;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import com.locart.R;

import java.io.File;
import java.io.IOException;

public class MyRecorder {
    private MediaRecorder recorder;
    private String outputFile;

    public MyRecorder() {

        try {
            /* Create folder to store recordings */
            File myDirectory = new File(Environment.getExternalStorageDirectory() +
                    File.separator + "Locart" + File.separator +
                    File.separator + "Audio");
            if (!myDirectory.exists()) {
                myDirectory.mkdirs();
            }
            outputFile = myDirectory + "/record.3gp";

        } catch (Exception e) {
            e.printStackTrace();
        }

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setOutputFile(outputFile);
    }

    public void startRecording() {
        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (recorder != null) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
                e.printStackTrace();
            } finally {
                recorder.release();
                recorder = null;
            }
        }
    }

    public void playRecord() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(outputFile);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
