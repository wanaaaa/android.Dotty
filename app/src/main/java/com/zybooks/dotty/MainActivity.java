package com.zybooks.dotty;

import static android.content.ContentValues.TAG;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private DotsGame mGame;
    private DotsGrid mDotsGrid;
    private TextView mMovesRemaining;
    private TextView mScore;
    ///////////////////////////
    private MediaPlayer mMediaPlayer;
    //////////////////////
    private SoundEffects mSoundEffects;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private TextView hScoreTView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ////////////
        sharedPreferences = getSharedPreferences("highestScore", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        hScoreTView = findViewById(R.id.hScroeID);


        mMovesRemaining = findViewById(R.id.movesRemaining);
        mScore = findViewById(R.id.score);
        mDotsGrid = findViewById(R.id.gameGrid);
        mDotsGrid.setGridListener(mGridListener);

        mGame = DotsGame.getInstance();
        startNewGame();
        ////////////////
        mSoundEffects =  SoundEffects.getInstance(getApplicationContext());//__B__
        //hScoreTView.setText("77777777777");
    }

    private final DotsGrid.DotsGridListener mGridListener = new DotsGrid.DotsGridListener() {

        @Override
        public void onDotSelected(Dot dot, DotsGrid.DotSelectionStatus selectionStatus) {
            // Play first tone when first dot is selected
            if (selectionStatus == DotsGrid.DotSelectionStatus.First) {
                mSoundEffects.resetTones();//__C__
            }

            // Select the dot and play the right tone
            DotsGame.DotStatus addStatus = mGame.processDot(dot);
            if (addStatus == DotsGame.DotStatus.Added) {
                mSoundEffects.playTone(true);//__D__
            }
            else if (addStatus == DotsGame.DotStatus.Removed) {
                mSoundEffects.playTone(false);//__E__
            }

            // Ignore selections when game is over
            if (mGame.isGameOver()) return;

            // Add/remove dot to/from selected dots
            //DotsGame.DotStatus addStatus = mGame.processDot(dot);

            // If done selecting dots then replace selected dots and display new moves and score
            if (selectionStatus == DotsGrid.DotSelectionStatus.Last) {
                if (mGame.getSelectedDots().size() > 1) {
                    mDotsGrid.animateDots();
                    // These methods must be called AFTER the animation completes
//                    mGame.finishMove();
//                    updateMovesAndScore();
                }
                else {
                    mGame.clearSelectedDots();
                }
            }

            // Display changes to the game
            mDotsGrid.invalidate();
        }

        @Override
        public void onAnimationFinished() {
            mGame.finishMove();
            mDotsGrid.invalidate();
            updateMovesAndScore();

            if (mGame.isGameOver()) {
                mSoundEffects.playGameOver();//__F__
            }
        }
    };

    public void newGameClick(View view) {
        //startNewGame();
        // Animate down off screen
        int screenHeight = this.getWindow().getDecorView().getHeight();
        ObjectAnimator moveBoardOff = ObjectAnimator.ofFloat(mDotsGrid,
                "translationY", screenHeight);
        moveBoardOff.setDuration(700);
        moveBoardOff.start();

        moveBoardOff.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                startNewGame();

                // Animate from above the screen down to default location
                ObjectAnimator moveBoardOn = ObjectAnimator.ofFloat(mDotsGrid,
                        "translationY", -screenHeight, 0);
                moveBoardOn.setDuration(700);
                moveBoardOn.start();
            }
        });
        int hScore = sharedPreferences.getInt("hScore", -1);
        if (mGame.getScore() > hScore) {
            Log.d(TAG, "onCreate: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+hScore + " -->>"+ mGame.getScore());
            editor.putInt("hScore", mGame.getScore());
            editor.apply();
        }
        String hScoreStr = "The highest Score is: " + mGame.getScore();
        hScoreTView.setText(hScoreStr);
        //Log.d(TAG, "onCreate: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+hScore + " -->>"+ mGame.getScore());
    }

    private void startNewGame() {
        mGame.newGame();
        mDotsGrid.invalidate();
        updateMovesAndScore();
    }

    private void updateMovesAndScore() {
        mMovesRemaining.setText(String.format(Locale.getDefault(), "%d", mGame.getMovesLeft()));
        mScore.setText(String.format(Locale.getDefault(), "%d", mGame.getScore()));
        ////////////////////////////

//        int hScore = sharedPreferences.getInt("hScore", -1);
//        if (mGame.getScore() > hScore) {
//            Log.d(TAG, "onCreate: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+hScore + " -->>"+ mGame.getScore());
//            editor.putInt("hScore", mGame.getScore());
//            editor.apply();
//        }
//        String hScoreStr = "The highest Score is: " + mGame.getScore();
//        hScoreTView.setText(hScoreStr);
//        //Log.d(TAG, "onCreate: ~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+hScore + " -->>"+ mGame.getScore());
    }

    public void mStartFun(View view) {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.energy);
            mMediaPlayer.start();
        } else {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}