package net.sankichi.eq_and_reverb;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private Button playButton;
    private MediaPlayer mp;
    private Equalizer eq;
    private PresetReverb pr;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        playButton = (Button)findViewById(R.id.playButton);
        
        mp = new MediaPlayer();
        mp.setLooping(true);
        
        setSoundSpn();
        setEqualizer();
        setReverb();
        
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    public void play(View view) {
        if (mp.isPlaying()) {
            mp.pause();
            playButton.setText("Play");
        } else {
            mp.start();
            playButton.setText("Pause");
        }
    }
    
    private void setSoundSpn() {
        Spinner sound = (Spinner)findViewById(R.id.soundSpn);
        sound.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mp.isPlaying()) playButton.setText("Play");
                mp.reset();
                mp.setLooping(true);
                String path = "android.resource://" + getPackageName() + "/";
                try{
                    switch (position) {
                    case 0: // Bass1
                        mp.setDataSource(view.getContext(), Uri.parse(path + R.raw.bass1));
                        break;
                    case 1: // Drum1
                        mp.setDataSource(view.getContext(), Uri.parse(path + R.raw.drum1));
                        break;
                    case 2: // Guitar1
                        mp.setDataSource(view.getContext(), Uri.parse(path + R.raw.guitar1));
                        break;
                    case 3: // Music1
                        mp.setDataSource(view.getContext(), Uri.parse(path + R.raw.music1));
                        break;
                    }
                    mp.prepare();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (SecurityException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
    }
    
    private void setEqualizer() {
        eq = new Equalizer(0, mp.getAudioSessionId());
        eq.setEnabled(true);
        final short minEQLevel = eq.getBandLevelRange()[0];
        final short maxEQLevel = eq.getBandLevelRange()[1];
        
        TableLayout eqLayout = (TableLayout)findViewById(R.id.eq);
        final ArrayList<SeekBar> eqsbs = new ArrayList<SeekBar>();
        for (short i = 0; i < eq.getNumberOfBands(); i++) {
            final short band = i;
            TableRow row = new TableRow(this);
            
            TextView freq = new TextView(this);
            freq.setGravity(Gravity.CENTER);
            freq.setText((eq.getCenterFreq(band) / 1000) + "Hz");
            row.addView(freq);
            
            eqsbs.add(new SeekBar(this));
            eqsbs.get(i).setMax(maxEQLevel - minEQLevel);
            eqsbs.get(i).setProgress(eq.getBandLevel(band) - minEQLevel);
            eqsbs.get(i).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    eq.setBandLevel(band, (short) (progress + minEQLevel));
                }
                public void onStartTrackingTouch(SeekBar seekBar) { }
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            row.addView(eqsbs.get(i));
            
            eqLayout.addView(row);
        }
        
        ArrayList<String> presetList = new ArrayList<String>();
        for (short i = 0; i < eq.getNumberOfPresets(); i++) {
            presetList.add(eq.getPresetName(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, presetList);
        Spinner eqPresetSpn = (Spinner)findViewById(R.id.eqPresetSpn);
        eqPresetSpn.setAdapter(adapter);
        eqPresetSpn.setOnItemSelectedListener(new OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eq.usePreset((short)position);
                for (int i = 0; i < eq.getNumberOfBands(); i++) {
                    eqsbs.get(i).setProgress(eq.getBandLevel((short)i) - minEQLevel);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    
    private void setReverb() {
        pr = new PresetReverb(0, mp.getAudioSessionId());
        pr.setEnabled(true);
        Spinner reverbSpn = (Spinner)findViewById(R.id.reverbSpn);
        reverbSpn.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                case 0:
                    pr.setPreset(PresetReverb.PRESET_NONE);
                    break;
                case 1:
                    pr.setPreset(PresetReverb.PRESET_SMALLROOM);
                    break;
                case 2:
                    pr.setPreset(PresetReverb.PRESET_MEDIUMROOM);
                    break;
                case 3:
                    pr.setPreset(PresetReverb.PRESET_LARGEROOM);
                    break;
                case 4:
                    pr.setPreset(PresetReverb.PRESET_MEDIUMHALL);
                    break;
                case 5:
                    pr.setPreset(PresetReverb.PRESET_LARGEHALL);
                    break;
                case 6:
                    pr.setPreset(PresetReverb.PRESET_PLATE);
                    break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
    }

}