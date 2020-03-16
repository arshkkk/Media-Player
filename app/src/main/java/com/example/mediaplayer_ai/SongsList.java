package com.example.mediaplayer_ai;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class SongsList extends AppCompatActivity {

    private ListView songsListView;
    private ArrayList<File> songsFileList;
    private String[] songsNameList;
    private ImageButton next = findViewById(R.id.nextOfSongList);
    private ImageButton prev = findViewById(R.id.previousOfSongList);
    private ImageButton playPause  = findViewById(R.id.playPauseOfSongList);
    private MediaPlayer mediaPlayer;
    private int positionToBeStored=0;

    //Headset Listener
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();



    //Control Button
    private LinearLayout controlButtons = findViewById(R.id.controlButtons);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);

        controlButtons.setOnClickListener(new View.OnClickListener() {//Set On Control Buttons Click Listener
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SongsList.this, MainActivity.class);
                intent.putExtra("nameOfSong",songsNameList[positionToBeStored]);
                intent.putExtra("position",String.valueOf(positionToBeStored));
                intent.putExtra("arrayFileListOfSongs",songsFileList);
                startActivity(intent);

            }
        });


        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPause();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNext();
            }
        });

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPrevious();
            }
        });

        songsListView = findViewById(R.id.songsListView);
        songsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if(ClassForStoringMediaPlayerVariable.mediaPlayer!=null){
                    ClassForStoringMediaPlayerVariable.mediaPlayer.pause();
                    ClassForStoringMediaPlayerVariable.mediaPlayer.stop();
                    ClassForStoringMediaPlayerVariable.mediaPlayer.release();
                }

                Uri uri = Uri.parse(songsFileList.get(position).toString());
                positionToBeStored = position;
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

                mediaPlayer.start();



            }
        });
        songsListView.setFooterDividersEnabled(true);

        songsFileList = new ArrayList<>();
        getPermissions();
        getStoragePermissions();



    }


//    private void  getAudioRecordPermission(){
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
//        {
//            if(!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED)){
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+getPackageName()));
//                startActivity(intent);
//                finish();
//            }
//        }
//    }

    private void getPermissions(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.RECORD_AUDIO)
                .withListener(new PermissionListener() {
                    @Override public void onPermissionGranted(PermissionGrantedResponse response) {

                    }
                    @Override public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            response.getRequestedPermission();
                        }
                    }
                    @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

    }


    private void getStoragePermissions(){

            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override public void onPermissionGranted(PermissionGrantedResponse response) {
                            displayAudioSongsNameList();
                        }
                        @Override public void onPermissionDenied(PermissionDeniedResponse response) {if(response.isPermanentlyDenied()){
                            response.getRequestedPermission();
                        }}
                        @Override public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }
                    }).check();

    }

    private void displayAudioSongsNameList(){
        songsFileList = readOnlyAudioSongs(Environment.getExternalStorageDirectory());
        String[] tempListOfSongsName = new String[songsFileList.size()];

        for(int i=0;i<songsFileList.size();i++){
            tempListOfSongsName[i] = songsFileList.get(i).getName();
        }

        songsNameList = tempListOfSongsName;

        ArrayAdapter<String> arrayAdapterForListView = new ArrayAdapter<>(SongsList.this, android.R.layout.simple_list_item_activated_1, tempListOfSongsName);
        songsListView.setAdapter(arrayAdapterForListView);
        songsListView.setCacheColorHint(Color.BLACK);
        songsListView.setSmoothScrollbarEnabled(true);

    }
    private ArrayList<File> readOnlyAudioSongs(File file)
    {
        ArrayList<File> fileArrayList  = new ArrayList<>();
        File[] allListFiles = file.listFiles();

        for(File individualFile: allListFiles){
            if(individualFile.isDirectory()&&!individualFile.isHidden()){
                fileArrayList.addAll(readOnlyAudioSongs(individualFile));
            }
            else if(individualFile.getName().endsWith(".mp3")){
                fileArrayList.add(individualFile);
            }
        }

        return fileArrayList;
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ClassForStoringMediaPlayerVariable.mediaPlayer!=null){
            ClassForStoringMediaPlayerVariable.mediaPlayer.stop();
            ClassForStoringMediaPlayerVariable.mediaPlayer.release();

        }
    }

    private void playPrevious(){
        boolean flag = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        mediaPlayer.release();
        if(positionToBeStored==0)
            positionToBeStored = songsFileList.size()-1;
        else
            positionToBeStored = ((positionToBeStored-1)%songsFileList.size());
        Uri uri = Uri.parse(songsFileList.get((positionToBeStored)).toString());
//        songName.setText(songsFileList.get(positionToBeStored).getName().toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        ClassForStoringMediaPlayerVariable.mediaPlayer = mediaPlayer;

        if(flag)
            mediaPlayer.start();

    }
    private void playNext(){
        boolean flag = mediaPlayer.isPlaying();
        mediaPlayer.stop();
        mediaPlayer.release();
        positionToBeStored = ((positionToBeStored+1)%songsFileList.size());
        Uri uri = Uri.parse(songsFileList.get((positionToBeStored)).toString());
//        songName.setText(songsFileList.get(positionToBeStored).getName().toString());
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

}
