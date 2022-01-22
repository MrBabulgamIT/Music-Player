package com.example.com.music;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.karumi.dexter.Dexter;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {
    private Button buttonplay,buttonnext,buttonpriview,buttonfastnext,buttonfastpriview;
    private TextView textViewsongname,textViewstart,textViewstop;
    private SeekBar seekBarmusic;
    private ImageView imageView;
    private BarVisualizer visualizer;
    String songsname;
    public  static final String EXTRA_NAME="song_name";
    static MediaPlayer mediaPlayer;
    int positioni;
    ArrayList<File> mysong;
    private  Thread updateseeckbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {

        if (visualizer != null)
        {

            visualizer.release();
        }
        super.onDestroy();
    }


    @Nullable


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonpriview=findViewById(R.id.priviewButton_id);
        buttonnext=findViewById(R.id.nextButton_id);
        buttonfastnext=findViewById(R.id.nextfastButton_id);
        buttonfastpriview=findViewById(R.id.priviewfastButton_id);
        buttonplay=findViewById(R.id.playButton_id);
        textViewsongname=findViewById(R.id.textsongname_player_id);
        textViewstart=findViewById(R.id.textview_start_plyer_id);
        textViewstop=findViewById(R.id.textview_stop_plyer_id);
        visualizer=findViewById(R.id.blastvisualizer);
        seekBarmusic=findViewById(R.id.seeckbart_player_id);
        imageView=findViewById(R.id.imageviewsong_player_id);

        if (mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();

        mysong=(ArrayList) bundle.getStringArrayList("songs");
        String songName=intent.getStringExtra("songname");
        positioni=bundle.getInt("position",0);
        textViewsongname.setSelected(true);
        Uri uri=Uri.parse(mysong.get(positioni).toString());
        songsname=mysong.get(positioni).getName();
        textViewsongname.setText(songsname);

        mediaPlayer=mediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();


        updateseeckbar= new Thread()
        {
            @Override
            public void run() {

                int totalduration=mediaPlayer.getDuration();
                int currentposition =0;
                while (currentposition<totalduration)
                {

                    try {
                        sleep(500);
                        currentposition=mediaPlayer.getCurrentPosition();
                        seekBarmusic.setProgress(currentposition);
                    }
                    catch (InterruptedException|IllegalAccessError e)
                    {
                        e.printStackTrace();
                    }


                }


            }
        };



        seekBarmusic.setMax(mediaPlayer.getDuration());
        updateseeckbar.start();
        seekBarmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        seekBarmusic.getThumb().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);





        seekBarmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        //visualizer

        int audioSessionId= mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1)
        {
            visualizer.setAudioSessionId(audioSessionId);
        }


        /*time handle to seekbar*/

        String endtime=createTIme(mediaPlayer.getDuration());
        textViewstop.setText(endtime);

        final Handler handler=new Handler();
        final int delay=1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime=createTIme(mediaPlayer.getCurrentPosition());
                textViewstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        buttonplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying())
                {
                    buttonplay.setBackgroundResource(R.drawable.ic_play_circle);
                    mediaPlayer.pause();

                }else

                {
                    buttonplay.setBackgroundResource(R.drawable.ic_pause_circle);
                    mediaPlayer.start();
                }

            }
        });



        buttonpriview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                positioni=((positioni-1)<0)?(mysong.size()-1):(positioni-1);

                Uri ur= Uri.parse(mysong.get(positioni).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),ur);
                songsname=mysong.get(positioni).getName();
                textViewsongname.setText(songsname);
                mediaPlayer.start();
                buttonplay.setBackgroundResource(R.drawable.ic_pause_circle);
                startAnimation(imageView);

                int audioSessionId= mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1)
                {
                    visualizer.setAudioSessionId(audioSessionId);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                buttonnext.performClick();
            }
        });

        buttonnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mediaPlayer.stop();
                mediaPlayer.release();
                positioni=((positioni+1)%mysong.size());

                Uri ur= Uri.parse(mysong.get(positioni).toString());
                mediaPlayer=MediaPlayer.create(getApplicationContext(),ur);
                songsname=mysong.get(positioni).getName();
                textViewsongname.setText(songsname);
                mediaPlayer.start();
                buttonplay.setBackgroundResource(R.drawable.ic_pause_circle);
                startAnimation(imageView);


                int audioSessionId= mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1)
                {
                    visualizer.setAudioSessionId(audioSessionId);
                }

            }
        });

        buttonfastnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+1000);
                }
            }
        });

        buttonfastpriview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-1000);
                }
            }
        });

    }


    public String createTIme(int duration)
    {
        String time="";
        int min=duration/1000/60;
        int sec=duration/1000%60;
        time+=min+":";

        if (sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;

    }

    public void startAnimation(View view)
    {
        ObjectAnimator animator= ObjectAnimator.ofFloat(imageView,"rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet=new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();

    }

}
