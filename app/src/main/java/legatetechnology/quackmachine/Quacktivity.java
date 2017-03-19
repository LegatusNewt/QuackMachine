package legatetechnology.quackmachine;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.szugyi.circlemenu.view.CircleLayout;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Quacktivity extends AppCompatActivity implements Animator.AnimatorListener, View.OnLongClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private int quack_factor = 0;
    private SeekBar seek_quackFactor;
    private TextView tv_quackFactor;
    private ImageButton ib_quacker;
    private CircleLayout circle_selector;
    private View mainLayout;
    private boolean menuState = true;
    private static final int QUACK_CONSTANT = 5;
    private AnimatorSet animSetXYOpen;
    private AnimatorSet animSetXYClose;
    private ViewGroup.LayoutParams origParams;
    private ViewGroup.LayoutParams noParams;

    //Quack sources
    private int[] quackSource = {R.raw.quack_n_5, R.raw.quack_n_4, R.raw.quack_n_3, R.raw.quack_n_2
            , R.raw.quack_n_1, R.raw.quack_0, R.raw.quack_1, R.raw.quack_2, R.raw.quack_3, R.raw.quack_4, R.raw.quack_5};

    //HashMap for associating quacksource with loaded quack file in the soundpool
    private HashMap<Integer, Integer> quacks;
    private SoundPool mySound;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public Quacktivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC); //volume buttons affect media volume now
        setContentView(R.layout.activity_quacktivity);

        //UI elements
        seek_quackFactor = (SeekBar) findViewById(R.id.seekBar);
        tv_quackFactor = (TextView) findViewById(R.id.tv_quackFactor);
        ib_quacker = (ImageButton) findViewById(R.id.ib_quacker);
        circle_selector = (CircleLayout) findViewById(R.id.rMenu_context);
        mainLayout = findViewById(R.id.main);

        //Listeners
        ib_quacker.setOnClickListener(this);
        ib_quacker.setOnLongClickListener(this);
        seek_quackFactor.setOnSeekBarChangeListener(this);
        mainLayout.setOnClickListener(this);

        //initialize HashMap
        quacks = new HashMap<Integer, Integer>();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        quack_factor = seek_quackFactor.getProgress();

        mySound = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();

        //init params for resizing
        origParams = circle_selector.getLayoutParams();
        noParams = new RelativeLayout.LayoutParams(0,0);

        initAnimations();
        //Fill the hash map with the resource id as the key
        //and the loaded quack audio as the value
        for (int res : quackSource) {
            quacks.put(res, mySound.load(this, res, 1));
        }
    }

    /**
     * Set the quack factor text
     *
     * @param txt
     */
    protected void setQuackFactorText(String txt) {
        tv_quackFactor.setText(txt);
    }

    @Override
    public void onClick(View v) {
        //On button click play audio based on seekbar progress
        switch (v.getId()) {
            case R.id.ib_quacker: {
                if(menuState)
                    animateClose();
                else
                    play_Quack();
                break;
            }
            case R.id.rMenu_context:{
                if(!menuState)
                    break;
            }
        }

        if(menuState)
            animateClose();
    }

    @Override
    public boolean onLongClick(View v) {
        if(!menuState)
            animateOpen();
        return true;
    }

    /**
     * initialize animation set for circle menu
     */

    private void initAnimations(){
        ObjectAnimator animX = ObjectAnimator.ofFloat(circle_selector, "scaleX", 0f,1f);
        ObjectAnimator animY = ObjectAnimator.ofFloat(circle_selector, "scaleY", 0f,1f);
        animSetXYOpen = new AnimatorSet();
        animSetXYOpen.playTogether(animX, animY);
        animSetXYOpen.addListener(this);

        ObjectAnimator animX_n = ObjectAnimator.ofFloat(circle_selector, "scaleX", 1f,0f);
        ObjectAnimator animY_n = ObjectAnimator.ofFloat(circle_selector, "scaleY", 1f,0f);
        animSetXYClose = new AnimatorSet();
        animSetXYClose.playTogether(animX_n,animY_n);
        animSetXYClose.addListener(this);

    }


    /**
     * Plays an animation to resize the Radial Selector
     */
    private void animateOpen() {
        menuState = true;
        animSetXYOpen.start();
    }

    private void animateClose(){
        menuState = false;
        animSetXYClose.start();
    }

    /**
     * Toggles the layout size based on state, this affects the clickable area
     */
    private void toggleSize( ){
        if(menuState)
        {
            circle_selector.setLayoutParams(origParams);
            circle_selector.requestLayout();
        }
        else
        {
            circle_selector.setLayoutParams(noParams);
            circle_selector.requestLayout();
        }
    }


    /**
     * Plays quacks using the SoundPool that was created before
     */
    private void play_Quack() {
        if (quack_factor < quackSource.length)
            mySound.play(quacks.get(quackSource[quack_factor]), 1, 1, 1, 0, 1.0f);
        else
            mySound.play(quacks.get(quackSource[0]), 1, 1, 1, 0, 1.0f);
    }

    @Override
    /**
     * Progress bar changed change the global quack factor as well as the text
     */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        quack_factor = progress;
        setQuackFactorText(Integer.toString(quack_factor - QUACK_CONSTANT));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Quacktivity Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://legatetechnology.quackmachine/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Quacktivity Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://legatetechnology.quackmachine/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    /**
     * On Animation end toggle the size of the context menu so that it doesn't interfere with
     * onclick actions
     * @param animation
     */
    @Override
    public void onAnimationEnd(Animator animation)
    {
        if(!menuState)
            toggleSize();
    }


    @Override
    public void onAnimationStart(Animator animation) {
        if(menuState)
            toggleSize();
    }

    @Override
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }
}


