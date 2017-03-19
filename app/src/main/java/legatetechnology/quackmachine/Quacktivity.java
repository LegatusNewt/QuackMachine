package legatetechnology.quackmachine;

import android.content.DialogInterface;
import android.media.Image;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.HashMap;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Quacktivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{
    private int quack_factor = 0;
    private SeekBar seek_quackFactor;
    private TextView tv_quackFactor;
    private ImageButton ib_quacker;
    private static final int QUACK_CONSTANT = 5;

    //Quack sources
    private int [] quackSource = { R.raw.quack_n_5,R.raw.quack_n_4,R.raw.quack_n_3,R.raw.quack_n_2
            ,R.raw.quack_n_1,R.raw.quack_0,R.raw.quack_1,R.raw.quack_2,R.raw.quack_3,R.raw.quack_4,R.raw.quack_5 };

    //HashMap for associating quacksource with loaded quack file in the soundpool
    private HashMap<Integer,Integer> quacks;
    private SoundPool mySound;

    public Quacktivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quacktivity);

        //UI elements
        seek_quackFactor = (SeekBar)findViewById(R.id.seekBar);
        tv_quackFactor = (TextView) findViewById(R.id.tv_quackFactor);
        ib_quacker = (ImageButton) findViewById(R.id.ib_quacker);

        //Listeners
        ib_quacker.setOnClickListener(this);
        seek_quackFactor.setOnSeekBarChangeListener(this);

        //initialize HashMap
        quacks = new HashMap<Integer,Integer>();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        quack_factor = seek_quackFactor.getProgress();

        mySound = new SoundPool.Builder()
                .setMaxStreams(10)
                .build();

        //Fill the hash map with the resource id as the key
            //and the loaded quack audio as the value
        for (int res : quackSource)
        {
            quacks.put(res,mySound.load(this,res,1));
        }
    }

    /**
     * Set the quack factor text
     * @param txt
     */
    protected void setQuackFactorText(String txt)
    {
        tv_quackFactor.setText(txt);
    }

    @Override
    public void onClick(View v) {
        //On button click play audio based on seekbar progress
        switch(v.getId()){
            case R.id.ib_quacker: {
                play_Quack();
                break;
            }
        }
    }

    /**
     * Plays quacks using the SoundPool that was created before
     */
    private void play_Quack()
    {
        if(quack_factor < quackSource.length)
            mySound.play(quacks.get(quackSource[quack_factor]),1,1,1,0,1.0f);
        else
            mySound.play(quacks.get(quackSource[0]),1,1,1,0,1.0f);
    }

    @Override
    /**
     * Progress bar changed change the global quack factor as well as the text
     */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        quack_factor = progress;
        setQuackFactorText(Integer.toString(quack_factor-QUACK_CONSTANT));

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}


