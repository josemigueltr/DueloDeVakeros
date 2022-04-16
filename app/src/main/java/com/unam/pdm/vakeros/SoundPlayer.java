package com.unam.pdm.vakeros;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

public class SoundPlayer extends JobIntentService {
    //identificador de la accion que ejecutara el sonido de disparo
    public  static  final String ACTION_FIRE="com.unam.pdm.vakeros.action.FIRE";
    //Identificador del servicio que se ejecutara
    public  static final byte JOB_ID=0;


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        String action= intent.getAction();
        //Revidamos que la accion sea valida
        if (action != "")
            Log.d(SoundPlayer.class.getSimpleName(), "Accion no especificada: " +action);
        //Revisamos de que tipo es la accion recibida
        switch (action) {
            case ACTION_FIRE :
                MediaPlayer soundPlayer = MediaPlayer.create(this, R.raw.shot);
                soundPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }
                });
                soundPlayer.start();
                break;
            default:
                Log.d(SoundPlayer.class.getSimpleName(), "Accion no reconocida: " +action);
        }

    }



}
