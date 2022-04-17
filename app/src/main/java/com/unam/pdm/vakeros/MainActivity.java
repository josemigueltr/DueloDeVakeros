package com.unam.pdm.vakeros;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity
        implements SensorEventListener {

    // Imagen del arma
    private ImageView gunView;

    private Button startButton;

    // Objetos necesarios para el sensor
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private byte steps;
    private ExecutorService singleThreadProducer;
    private DrawTimer asyncCounter;
    public static final byte SECONDS_TO_COUNT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Asignación de referencias
        gunView = findViewById(R.id.gun_iv);

        // instancias para el sensor
        sensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE
        );
        if(sensorManager != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(
                    Sensor.TYPE_STEP_DETECTOR
            );
        }
        if(stepDetectorSensor == null) sensorManager = null;
    }

    /**
     * Hace que la aplicación se muestre en pantalla completa.
     * No muestra el UI del dispositivo para que sea más inmersivo.
     */
    @Override
    public void onWindowFocusChanged( boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }

    /**
     * Inicializa el tiroteo, checando si tiene permiso para usar
     * el podómetro
     */
    private void init() {
        startButton.setVisibility(View.VISIBLE);
        gunView.setVisibility(View.INVISIBLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if(!checkActivityRecognitionPermission()) {
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACTIVITY_RECOGNITION}, 0);
            }
        }
    }

    private boolean checkActivityRecognitionPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) ==
                PackageManager.PERMISSION_GRANTED;
    }



    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        steps++;
        if(steps >= 3) {
            sensorManager.unregisterListener(this);
            gunView.setVisibility(View.VISIBLE);
            steps = 0;
        }
    }

    private void checkStepSensor() {
        if(sensorManager == null) {
            startTimer();
            return;
        }
        sensorManager.registerListener(this, stepDetectorSensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void startTimer(){
        if(singleThreadProducer == null){
            singleThreadProducer = Executors.newSingleThreadExecutor();
        }
        asyncCounter = new DrawTimer(gunView, SECONDS_TO_COUNT);
        singleThreadProducer.execute(asyncCounter);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void finalCountdown(View startButton){
        startButton.setVisibility(View.INVISIBLE);
        checkStepSensor();
    }

    /**
     * Metodo que se encarga de ejecutar el sonido de disparo cuando un arma es presionada
     * @param gun bloque de la interfaz en la que se ha ejecutado la accion
     */
    public  void fire(View gun){
        JobIntentService.enqueueWork(this,SoundPlayer.class,0, new Intent(SoundPlayer.ACTION_FIRE));

    }

    protected void onResume() {
        super.onResume();
        init();
    }

}