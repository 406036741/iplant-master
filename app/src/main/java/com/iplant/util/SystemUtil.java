package com.iplant.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.util.Log;

import com.iplant.R;

import java.io.File;
import java.io.IOException;

public class SystemUtil {
    Context mContext;
    boolean shouldPlayBeep = true;
    public SystemUtil(Context wContext)
    {
        mContext=wContext;
    }


    public boolean isFileExists(String filePath) {

        File folder1 = new File(filePath);
        return folder1.exists();


    }

    public boolean deleteFile(String filePath) {

        File folder1 = new File(filePath);
        return folder1.delete();


    }

    public  void  runVIBRATE()
    {
        try {
            //2.获得震动服务。
            Vibrator vibrator = (Vibrator) mContext.getSystemService(mContext.VIBRATOR_SERVICE);
            //3.启动震动。
            //第一个参数，指代一个震动的频率数组。每两个为一组，每组的第一个为等待时间，第二个为震动时间。
            //   比如  [2000,500,100,400],会先等待2000毫秒，震动500，再等待100，震动400
            //第二个参数，repest指代从 第几个索引（第一个数组参数） 的位置开始循环震动。
            //会一直保持循环，我们需要用 vibrator.cancel()主动终止
            vibrator.vibrate(new long[]{500, 500, 500, 500}, -1);
        } catch (Exception ex) {
            Log.e("PlayBeep", ex.toString());
        }
    }

    public void PlayBeep() {
        AudioManager audioService = (AudioManager) mContext.getSystemService(mContext.AUDIO_SERVICE);

        //判断是否为非静音模式
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            shouldPlayBeep = false;
        }

        MediaPlayer mediaPlayer = CreateMediaPlayer();

        if (shouldPlayBeep && mediaPlayer != null) {
            //1.开启蜂鸣器
            mediaPlayer.start();
        }

    }

       MediaPlayer CreateMediaPlayer() {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });

        //设定数据源，并准备播放
        AssetFileDescriptor file = mContext.getResources().openRawResourceFd(
                R.raw.pizzicato);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.prepare();
        } catch (IOException ioe) {

            mediaPlayer = null;
        }
        return mediaPlayer;
    }
}
