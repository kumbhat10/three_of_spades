package com.kaalikiteeggi.three_of_spades;

import android.content.Context;
import android.media.SoundPool;

public class SoundManager {

    private static SoundManager singleton;
    SoundPool soundPool;

    int UPDATE_SOUND;
    int ERROR_SOUND;
    int SUCCESS_SOUND;
    int CARD_PLAYED_SOUND;
    int CARD_COLLECT_SOUND;
    int SHUFFLE_SOUND;
    int CHAT_SOUND;
    int WON_SOUND;
    int DHOL_SOUND;
    int LOST_SOUND;
    int TIMER_SOUND;
    int ZIP_SOUND;

    public SoundManager(){
        soundPool = (new SoundPool.Builder()).setMaxStreams(18).build();
    }


    public void loadSound(Context context){
        ZIP_SOUND = soundPool.load(context, R.raw.zip, 1);
        UPDATE_SOUND = soundPool.load(context, R.raw.card_played, 1);
        ERROR_SOUND = soundPool.load(context, R.raw.error, 1);
        SUCCESS_SOUND = soundPool.load(context, R.raw.success, 1);
        CARD_PLAYED_SOUND = soundPool.load(context, R.raw.card_played, 1);
        CARD_COLLECT_SOUND = soundPool.load(context, R.raw.card_collect, 1);
        SHUFFLE_SOUND = soundPool.load(context, R.raw.card_shuffle, 1);
        CHAT_SOUND = soundPool.load(context, R.raw.chat, 1);
        WON_SOUND = soundPool.load(context, R.raw.game_win, 1);
        DHOL_SOUND = soundPool.load(context, R.raw.dhol, 1);
        LOST_SOUND = soundPool.load(context, R.raw.game_lose, 1);
        TIMER_SOUND = soundPool.load(context, R.raw.timer_over, 1);
        // load other sound if you like
    }

    public void playUpdateSound(){
        soundPool.play(UPDATE_SOUND, 0.5F, 0.5F, 0, 0, 1.0F);
    }
    public void playErrorSound(){
        soundPool.play(ERROR_SOUND, 0.1F, 0.1F, 0, 0, 1.0F);
    }
    public void playSuccessSound(){ soundPool.play(SUCCESS_SOUND, 0.2F, 0.2F, 0, 0, 1.0F); }
    public void playCardPlayedSound(){
        soundPool.play(CARD_PLAYED_SOUND, 1.0F, 1.0F, 0, 0, 1.0F);
    }
    public void playCardCollectSound(){
        soundPool.play(CARD_COLLECT_SOUND, 0.3F, 0.3F, 0, 0, 1.0F);
    }
    public void playShuffleSound(){
        soundPool.play(SHUFFLE_SOUND, 0.5F, 0.5F, 0, 0, 1.0F);
    }
    public void playWonSound(){
        soundPool.play(WON_SOUND, 0.3F, 0.3F, 0, 0, 1.0F);
    }
    public void playDholSound(){ soundPool.play(DHOL_SOUND, 1.0F, 1.0F, 0, 0, 1.0F); }
    public void playChatSound(){
        soundPool.play(CHAT_SOUND, 1.0F, 1.0F, 0, 0, 1.0F);
    }
    public void playLostSound(){
        soundPool.play(LOST_SOUND, 1.0F, 1.0F, 0, 0, 1.0F);
    }
    public void playTimerSound(){ soundPool.play(TIMER_SOUND, 1.0F, 1.0F, 0, 0, 1.0F);}
    public void playZipSound(){
        soundPool.play(ZIP_SOUND, 0.6F, 0.6F, 0, 0, 1.0F);
    }

    public static void initialize(Context context){
        SoundManager soundManager = getInstance();
        soundManager.loadSound(context);
    }
    public static synchronized SoundManager getInstance(){
        if(singleton == null){
            singleton = new SoundManager();
        }
        return singleton;
    }

}