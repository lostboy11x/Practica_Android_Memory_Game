package com.example.memorygame

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

/**
 * Servei per reproduir música de fons.
 *
 * Aquest servei utilitza MediaPlayer per reproduir la música de fons del joc.
 * Reprodueix una pista de música de forma contínua en un bucle.
 *
 * @constructor Crea una nova instància del servei de música.
 */
class MusicService : Service() {
    /** Reproductor de música per reproduir la pista de música. */
    private var mediaPlayer: MediaPlayer? = null

    /**
     * Mètode que es crida quan una activitat s'enllaça amb el servei.
     *
     * @param intent L'intenció associada a la sol·licitud de vinculació.
     * @return Un objecte IBinder o null si la vinculació no és permesa.
     */
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Mètode que s'inicia quan s'inicia el servei.
     *
     * Inicialitza el reproductor de música amb la pista de música Peppa Pig.
     * La música es reprodueix en un bucle infinit.
     *
     * @param intent L'intenció que es va passar a startService(Intent).
     * @param flags Indicadors addicionals sobre com s'hauria de començar el servei.
     * @param startId Un identificador únic per a la comanda de començar que es dona a aquesta instància del servei.
     * @return La forma en què s'hauria de comportar el sistema si el servei s'acaba de forma inesperada.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaPlayer = MediaPlayer.create(this, R.raw.music_peppa_pig)
        mediaPlayer?.isLooping = true
        mediaPlayer?.start()

        return START_STICKY
    }

    /**
     * Mètode que s'inicia quan el servei està a punt de destruir-se.
     *
     * Allibera els recursos utilitzats pel reproductor de música quan el servei s'atura o es destrueix.
     */
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
    }
}