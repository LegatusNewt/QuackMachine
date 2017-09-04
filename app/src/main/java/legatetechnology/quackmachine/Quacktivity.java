package legatetechnology.quackmachine;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.szugyi.circlemenu.view.CircleLayout;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Quacktivity extends AppCompatActivity implements CircleLayout
        .OnItemSelectedListener, CircleLayout.OnItemClickListener, Animator.AnimatorListener,
        View.OnLongClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private int quack_factor = 0;
    private SeekBar seek_quackFactor;
    private TextView tv_quackFactor;
    private TextView tv_qTitle;
    private ImageButton ib_quacker;
    private ImageButton ib_birdSelect;
    private CircleLayout circle_selector;
    private View mainLayout;
    private boolean menuState = true;
    private static final int QUACK_CONSTANT = 5;
    private AnimatorSet animSetXYOpen;
    private AnimatorSet animSetXYClose;
    private AnimatorSet animSetTrans;
    private AnimatorSet animSetTrans_n;
    private ViewGroup.LayoutParams origParams;
    private ViewGroup.LayoutParams noParams;
    private String birdState = "duck";


    //Quack sources
    private int[] quackSource = {R.raw.quack_n_5, R.raw.quack_n_4, R.raw.quack_n_3, R.raw.quack_n_2
            , R.raw.quack_n_1, R.raw.quack_0, R.raw.quack_1, R.raw.quack_2, R.raw.quack_3, R.raw
            .quack_4, R.raw.quack_5};

    private int gooseSource = R.raw.goose;
    private int pigeonSource = R.raw.pigeon;

    //HashMap for associating quacksource with loaded quack file in the soundpool
    private HashMap<Integer, Integer> quacks;
    private HashMap<Integer, Integer> gooseCall;
    private HashMap<Integer, Integer> pigeonCoo;
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
        ib_birdSelect = (ImageButton) findViewById(R.id.ib_birdSelect);
        circle_selector = (CircleLayout) findViewById(R.id.rMenu_context);
        tv_qTitle = (TextView) findViewById(R.id.tv_title);
        mainLayout = findViewById(R.id.main);

        //Listeners
        ib_quacker.setOnClickListener(this);
        ib_quacker.setOnLongClickListener(this);
        ib_birdSelect.setOnClickListener(this);
        seek_quackFactor.setOnSeekBarChangeListener(this);
        mainLayout.setOnClickListener(this);
        circle_selector.setOnItemClickListener(this);
        circle_selector.setOnItemSelectedListener(this);


        //initialize HashMap
        quacks = new HashMap<Integer, Integer>();
        pigeonCoo = new HashMap<Integer, Integer>();
        gooseCall = new HashMap<Integer, Integer>();

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

        //Fill the hash map with the resource id as the key
        //and the loaded quack audio as the value
        for (int res : quackSource) {
            quacks.put(res, mySound.load(this, res, 1));
        }
        gooseCall.put(gooseSource,mySound.load(this,gooseSource,1));
        pigeonCoo.put(pigeonSource,mySound.load(this,pigeonSource,1));
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus){
        super.onWindowFocusChanged(hasFocus);
        initAnimations();
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

        if(menuState)
            animateClose();
        //On button click play audio based on seekbar progress
        else {
            switch (v.getId()) {
                case R.id.ib_quacker: {
                    if (menuState)
                        animateClose();
                    else
                        play_Quack();
                    break;
                }
                case R.id.rMenu_context: {
                    if (!menuState)
                        break;
                }
                case R.id.ib_birdSelect: {
                    if (!menuState)
                        animateOpen();
                    break;
                }
            }
        }
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

        int[] circle_Pos = new int[2];

        //Top left location of circle_selector
        circle_selector.getLocationOnScreen(circle_Pos);
        circle_Pos[0] += (circle_selector.getWidth()/2); //get center X
        circle_Pos[1] += (circle_selector.getHeight()/2); //get center Y
        System.out.println("circle_Pos : " + circle_Pos[0] + " , " + circle_Pos[1]);

        //Top left location of ib_birdSelect
        int[] ib_Pos = new int[2];
        ib_birdSelect.getLocationOnScreen(ib_Pos);
        ib_Pos[0]+=(ib_birdSelect.getWidth()/2); //get center X
        ib_Pos[1]+=(ib_birdSelect.getHeight()/2); //get center Y
        System.out.println("ib_Pos : " + ib_Pos[0] + " , " + ib_Pos[1]);


        Configuration thisConfig = getResources().getConfiguration();

        float[] offset = new float[2];

        offset[0] = ib_Pos[0] - circle_Pos[0];
        offset[1] = ib_Pos[1] - circle_Pos[1];
        System.out.println("Offset : " + offset[0] + " , " + offset[1]);

        ObjectAnimator transX;
        ObjectAnimator transY;
        ObjectAnimator transX_n;
        ObjectAnimator transY_n;

        transX = ObjectAnimator.ofFloat(circle_selector,"translationX",0f,offset[0]);
        transY = ObjectAnimator.ofFloat(circle_selector,"translationY",0f,offset[1]);
        transX_n = ObjectAnimator.ofFloat(circle_selector,"translationX",offset[0],
                0f);
        transY_n = ObjectAnimator.ofFloat(circle_selector,"translationY",offset[1],
                0f);

        animSetTrans = new AnimatorSet();
        animSetTrans.playTogether(transX,transY);
        animSetTrans.addListener(this);

        animSetTrans_n = new AnimatorSet();
        animSetTrans_n.playTogether(transX_n,transY_n);
        animSetTrans_n.addListener(this);
    }


    /**
     * Plays an animation to resize the Radial Selector
     */
    private void animateOpen() {
        menuState = true;
        animSetXYOpen.start();
        animSetTrans_n.start();
    }

    private void animateClose(){
        menuState = false;
        animSetXYClose.start();
        animSetTrans.start();
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
     * Set the state of the Quacker
     *      duck,goose,pigeon
     */
    private void setState(String bird)
    {
        switch(bird){
            case "duck":
                ib_quacker.setImageResource(R.drawable.duck);
                seek_quackFactor.setVisibility(View.VISIBLE);
                tv_qTitle.setVisibility(View.VISIBLE);
                tv_quackFactor.setVisibility(View.VISIBLE);
                ib_birdSelect.setImageResource(R.drawable.style_birdselect_duck);
                break;
            case "goose":
                ib_quacker.setImageResource(R.drawable.feature_goose);
                seek_quackFactor.setVisibility(View.INVISIBLE);
                tv_qTitle.setVisibility(View.INVISIBLE);
                tv_quackFactor.setVisibility(View.INVISIBLE);
                ib_birdSelect.setImageResource(R.drawable.style_birdselect_goose);
                break;
            case "pigeon":
                ib_quacker.setImageResource(R.drawable.feature_pigeon);
                seek_quackFactor.setVisibility(View.INVISIBLE);
                tv_qTitle.setVisibility(View.INVISIBLE);
                tv_quackFactor.setVisibility(View.INVISIBLE);
                ib_birdSelect.setImageResource(R.drawable.style_birdselect_pigeon);
                break;
        }
        birdState = bird;
        if(BuildConfig.DEBUG){
            Toast toast = Toast.makeText(this, birdState, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Plays quacks using the SoundPool that was created before
     */
    private void play_Quack() {
        switch(birdState){
            case "duck":{
                if (quack_factor < quackSource.length)
                    mySound.play(quacks.get(quackSource[quack_factor]), 1, 1, 1, 0, 1.0f);
                else
                    mySound.play(quacks.get(quackSource[0]), 1, 1, 1, 0, 1.0f);
                break;
            }
            case "goose":{
                mySound.play(gooseCall.get(gooseSource),1,1,1,0,1.0f);
                break;
            }
            case "pigeon":{
                mySound.play(pigeonCoo.get(pigeonSource),1,1,1,0,1.0f);
                break;
            }
        }
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
    public void onAnimationCancel(Animator animation) {

    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }


    @Override
    public void onAnimationStart(Animator animation) {
        if(menuState)
            toggleSize();
    }

    @Override
    public void onItemClick(View view) {
        switch(view.getId()){
            case R.id.menu_duck:
            {
                setState("duck");
                break;
            }
            case R.id.menu_goose:
            {
                setState("goose");
                break;
            }
            case R.id.menu_pigeon:
            {
                setState("pigeon");
                break;
            }
        }
    }

    @Override
    public void onItemSelected(View view) {
        switch(view.getId()){
            case R.id.menu_duck:
            {
                setState("duck");
                break;
            }
            case R.id.menu_goose:
            {
                setState("goose");
                break;
            }
            case R.id.menu_pigeon:
            {
                setState("pigeon");
                break;
            }
        }
    }
}


