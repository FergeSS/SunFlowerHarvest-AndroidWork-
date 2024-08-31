package com.foyslasim.milotremec;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.foyslasim.milotremec.databinding.ActivityGameBinding;

import java.util.Random;

public class Game extends AppCompatActivity {
    private ActivityGameBinding binding;
    public static boolean active = false;
    private int score = 0;
    private int currentScore = 0;
    private int beforeBonus = 0;
    private boolean isBonus = true;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        windowSettings();
        audioSettings();
        active = true;
    }

    public void home(View v) {
        action();
        finish();
    }

    public void audioSettings() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    public void windowSettings() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        score = getScore();
        binding.score.setText(score + "");

        binding.level.setText("Level: " +
                ((score / 500 == 0 ? 0 : 1) +
                        (score / 1500 == 0 ? 0 : 1) +
                        (score / 3500 == 0 ? 0 : 1) +
                        (score / 6500 == 0 ? 0 : 1) + 1));

        currentScore = score -
                ((score / 500 == 0 ? 0 : 500) +
                        (score / 1500 == 0 ? 0 : 1000) +
                        (score / 3500 == 0 ? 0 : 2000) +
                        (score / 6500 == 0 ? 0 : 3000));
        binding.currentScore.setText(currentScore + "");

        beforeBonus = score / 6500 != 0 ? -currentScore + 3000 :
                score / 3500 != 0 ? -currentScore + 1000 :
                        score / 1500 != 0 ? -currentScore + 400 :
                                score / 500 != 0 ? -currentScore + 300 : -currentScore + 100;

        if (beforeBonus <= 0) {
            beforeBonus = 0;
            isBonus = false;
        }
        binding.beforeBonus.setText("Before bonus: " + beforeBonus);

        binding.pupel.setOnClickListener(v -> mainClick());
    }

    private void createAndAnimateSeed() {
        ImageView seed = new ImageView(this);
        Random rand = new Random();
        int[] seeds = {R.drawable.seed1, R.drawable.seed2};
        seed.setImageResource(seeds[rand.nextInt(2)]);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                dpToPx(52),
                dpToPx(29)
        );
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        seed.setLayoutParams(params);

        binding.seedCont.addView(seed);

        TranslateAnimation animation = new TranslateAnimation(
                0, (float) (Math.random() * 1000 - 500),
                0, (float) (Math.random() * 1000 - 500)
        );
        animation.setDuration(200);
        animation.setFillAfter(true);

        seed.startAnimation(animation);
        Handler hand = new Handler(Looper.getMainLooper());
        hand.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.seedCont.removeAllViews();
            }
        }, 200);

    }
    public void mainClick() {
        action();
        createAndAnimateSeed();
        ++score;
        if (score == 16500) {
            score = 0;
            setScore(score);
            winDialog();
        }

        beforeBonus = score / 6500 != 0 ? -currentScore + 3000 :
                score / 3500 != 0 ? -currentScore + 1000 :
                        score / 1500 != 0 ? -currentScore + 400 :
                                score / 500 != 0 ? -currentScore + 300 : -currentScore + 100;
        beforeBonus--;

        if (beforeBonus == 0 && isBonus) {
            score += currentScore + 1;
        }

        binding.score.setText(score + "");

        binding.level.setText("Level: " +
                ((score / 500 == 0 ? 0 : 1) +
                        (score / 1500 == 0 ? 0 : 1) +
                        (score / 3500 == 0 ? 0 : 1) +
                        (score / 6500 == 0 ? 0 : 1) + 1));

        currentScore = score -
                ((score / 500 == 0 ? 0 : 500) +
                        (score / 1500 == 0 ? 0 : 1000) +
                        (score / 3500 == 0 ? 0 : 2000) +
                        (score / 6500 == 0 ? 0 : 3000));
        binding.currentScore.setText(currentScore + "");


        if (beforeBonus <= 0) {
            beforeBonus = 0;
            isBonus = false;
        } else {
            isBonus = true;
        }
        binding.beforeBonus.setText("Before bonus: " + beforeBonus);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        active = false;
        setScore(score);
    }

    public void action() {
        if (getSharedPreferences("settings", MODE_PRIVATE).getBoolean("vibro_enabled", true)) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(100);
            }
        }

        if (getSharedPreferences("settings", MODE_PRIVATE).getBoolean("sound_enabled", true)) {
            MediaPlayer mediaPlayer = MediaPlayer.create(Game.this, R.raw.click);
            mediaPlayer.setVolume(0.2f, 0.2f);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
        }
    }

    private void winDialog(){
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
        wlp.dimAmount = 0.7f;
        dialog.getWindow().setAttributes(wlp);
        dialog.setContentView(R.layout.dialog);

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                binding.score.setText(score + "");

                binding.level.setText("Level: " +
                        ((score / 500 == 0 ? 0 : 1) +
                                (score / 1500 == 0 ? 0 : 1) +
                                (score / 3500 == 0 ? 0 : 1) +
                                (score / 6500 == 0 ? 0 : 1) + 1));

                currentScore = score -
                        ((score / 500 == 0 ? 0 : 500) +
                                (score / 1500 == 0 ? 0 : 1000) +
                                (score / 3500 == 0 ? 0 : 2000) +
                                (score / 6500 == 0 ? 0 : 3000));
                binding.currentScore.setText(currentScore + "");

                beforeBonus = score / 3000 != 0 ? -currentScore + 3000 :
                        score / 2000 != 0 ? -currentScore + 1000 :
                                score / 1000 != 0 ? -currentScore + 400 :
                                        score / 500 != 0 ? -currentScore + 300 : -currentScore + 100;

                if (beforeBonus <= 0) {
                    beforeBonus = 0;
                    isBonus = false;
                }
                binding.beforeBonus.setText("Before bonus: " + beforeBonus);
            }
        });
        dialog.show();
    }

    public void playAgain(View v) {
        if (dialog != null) {
            dialog.dismiss();
        }

    }

    private int getScore() {
        SharedPreferences sh = getSharedPreferences("score", MODE_PRIVATE);
        return sh.getInt("score", 0);
    }

    private void setScore(int score) {
        SharedPreferences.Editor ed = getSharedPreferences("score", MODE_PRIVATE).edit();
        ed.putInt("score", score);
        ed.apply();

    }

    int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}