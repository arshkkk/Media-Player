package com.example.mediaplayer_ai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ImageButton mic;
    private String keeper="";
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextView songName;
    private ImageButton playPause;
    private ImageButton next;
    private ImageButton previous;
    private MediaPlayer mediaPlayer;
    private ArrayList<File> songsFileList;
    private int currentPosition;
    private Button voiceCommand;
    private boolean flag=false;



    //Headset Listener
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();


    private final String GOOGLE_RECOGNITION_SERVICE_NAME = "com.google.android.googlequicksearchbox/com.google.android.voicesearch.serviceapi.GoogleRecognitionService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ClassForStoringMediaPlayerVariable.mainActivity = MainActivity.this;

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext(), ComponentName.unflattenFromString(GOOGLE_RECOGNITION_SERVICE_NAME));

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {

                ArrayList<String> listOfResults = results.getStringArrayList((SpeechRecognizer.RESULTS_RECOGNITION));

                if(listOfResults!=null)
                {
                    keeper = listOfResults.get(0);
                    if(keeper.equals("play the song"))
                        playPause();
                    else if(keeper.equals("pause the song"))
                        playPause();

                    else if(keeper.equals("play next song"))
                        playNext();
                    else if(keeper.equals("play previous song"))
                        playPrevious();
                    Toast.makeText(MainActivity.this, keeper, Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(MainActivity.this, "In onResults",Toast.LENGTH_LONG).show();

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        initEventListenerLayout();
        initViews();
        getIntentValuesFromListActivity();





    }



    private void initViews(){



        songName = findViewById(R.id.songName);
        voiceCommand = findViewById(R.id.button);
        voiceCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(flag==false){
                    flag=true;
                    voiceCommand.setText("Voice Commands-ON");
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
                else {
                    flag=false;
                    voiceCommand.setText("Voice Commands-OFF");
                    speechRecognizer.stopListening();
                }
            }
        });
        next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });
        previous = findViewById(R.id.previous);
        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });
        playPause = findViewById(R.id.playPause);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
    }

    private void playPrevious(){
        boolean flag = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        mediaPlayer.release();
        if(currentPosition==0)
            currentPosition = songsFileList.size()-1;
        else
        currentPosition = ((currentPosition-1)%songsFileList.size());
        Uri uri = Uri.parse(songsFileList.get((currentPosition)).toString());
        songName.setText(songsFileList.get(currentPosition).getName().toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

        if(flag)
        mediaPlayer.start();

    }
    private void playNext(){
        boolean flag = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        mediaPlayer.release();
        currentPosition = ((currentPosition+1)%songsFileList.size());
        Uri uri = Uri.parse(songsFileList.get((currentPosition)).toString());
        songName.setText(songsFileList.get(currentPosition).getName().toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

        if(flag)
        mediaPlayer.start();
    }

    public void playPause(){
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying()){

                mediaPlayer.pause();
                playPause.setImageResource(R.drawable.play);
                registerReceiver(myNoisyAudioStreamReceiver, intentFilter);

            }
            else{
                registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                mediaPlayer.start();
                playPause.setImageResource(R.drawable.pause);
            }
        }
    }

    private void getIntentValuesFromListActivity()
    {

        String songNameTemp = getIntent().getExtras().get("nameOfSong").toString();
        songName.setText(songNameTemp);
        currentPosition = Integer.parseInt(getIntent().getExtras().get("position").toString());
        songsFileList = (ArrayList)getIntent().getExtras().getParcelableArrayList("arrayFileListOfSongs");

        if(ClassForStoringMediaPlayerVariable.mediaPlayer!=null){
            ClassForStoringMediaPlayerVariable.mediaPlayer.pause();
            ClassForStoringMediaPlayerVariable.mediaPlayer.stop();
            ClassForStoringMediaPlayerVariable.mediaPlayer.release();
        }

        Uri uri = Uri.parse(songsFileList.get(currentPosition).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

        mediaPlayer.start();






    }

    private void initEventListenerLayout(){
        mic = findViewById(R.id.speechButton);
        mic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

               switch(event.getAction())
               {
                   case MotionEvent.ACTION_DOWN:
                       Toast.makeText(MainActivity.this,"On Touch Listener", Toast.LENGTH_LONG).show();
                       new Handler() {
                           @Override
                           public void handleMessage(Message msg) {
                               // process incoming messages here
                               // this will run in the thread, which instantiates it
                               speechRecognizer.startListening(speechRecognizerIntent);
                           }
                       };
                       break;

                   case MotionEvent.ACTION_UP:
                       speechRecognizer.stopListening();
                       keeper="";
                       break;
               }
                return false;
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        
    }

    @Override
    public void finish() {
        super.finish();
        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;


    }

    @Override
    protected void onStop() {
        super.onStop();
        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

        speechRecognizer.stopListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

    }


}


