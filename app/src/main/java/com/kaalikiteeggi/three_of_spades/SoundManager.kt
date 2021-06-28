package com.kaalikiteeggi.three_of_spades

import android.content.Context
import android.media.SoundPool

class SoundManager private constructor() {

     companion object {

        private var singleton: SoundManager? = null

        fun initialize(context: Context?) {
            instance!!.loadSound(context)
        }

        @get:Synchronized
        val instance: SoundManager?
            get() {
                if (singleton == null) {
                    singleton = SoundManager()
                }
                return singleton
            }
    }
    private var soundPool: SoundPool = SoundPool.Builder().setMaxStreams(18).build()
    private var updateSound = 0
    private var errorSOUND = 0
    private var successSOUND = 0
    private var cardPlayedSOUND = 0
    private var cardCollectSOUND = 0
    private var shuffleSOUND = 0
    private var chatSOUND = 0
    private var winSOUND = 0
    private var dholSound = 0
    private var lostSound = 0
    private var timerSound = 0
    private var zipSound = 0

    fun loadSound(context: Context?) {
        zipSound = soundPool.load(context, R.raw.zip, 1)
        updateSound = soundPool.load(context, R.raw.card_played, 1)
        errorSOUND = soundPool.load(context, R.raw.error, 1)
        successSOUND = soundPool.load(context, R.raw.success, 1)
        cardPlayedSOUND = soundPool.load(context, R.raw.card_played, 1)
        cardCollectSOUND = soundPool.load(context, R.raw.card_collect, 1)
        shuffleSOUND = soundPool.load(context, R.raw.card_shuffle, 1)
        chatSOUND = soundPool.load(context, R.raw.chat, 1)
        winSOUND = soundPool.load(context, R.raw.game_win, 1)
        dholSound = soundPool.load(context, R.raw.dhol, 1)
        lostSound = soundPool.load(context, R.raw.game_lose, 1)
        timerSound = soundPool.load(context, R.raw.timer_over, 1)
        // load other sound if you like
    }

    fun playUpdateSound() {
        soundPool.play(updateSound, 0.5f, 0.5f, 0, 0, 1.0f)
    }

    fun playErrorSound() {
        soundPool.play(errorSOUND, 0.1f, 0.1f, 0, 0, 1.0f)
    }

    fun playSuccessSound() {
        soundPool.play(successSOUND, 0.2f, 0.2f, 0, 0, 1.0f)
    }

    fun playCardPlayedSound() {
        soundPool.play(cardPlayedSOUND, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playCardCollectSound() {
        soundPool.play(cardCollectSOUND, 0.3f, 0.3f, 0, 0, 1.0f)
    }

    fun playShuffleSound() {
        soundPool.play(shuffleSOUND, 0.5f, 0.5f, 0, 0, 1.0f)
    }

    fun playWonSound() {
        soundPool.play(winSOUND, 0.3f, 0.3f, 0, 0, 1.0f)
    }

    fun playDholSound() {
        soundPool.play(dholSound, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playChatSound() {
        soundPool.play(chatSOUND, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playLostSound() {
        soundPool.play(lostSound, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playTimerSound() {
        soundPool.play(timerSound, 1.0f, 1.0f, 0, 0, 1.0f)
    }

    fun playZipSound() {
        soundPool.play(zipSound, 0.6f, 0.6f, 0, 0, 1.0f)
    }

}