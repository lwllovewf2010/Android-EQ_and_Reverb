package net.sankichi.eq_and_reverb;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private Button playBtn;
    private ArrayList<SeekBar> eqSbs;
    private Spinner eqPresetSpn;
    private Spinner prSpn;
    private MediaPlayer mp;
    private Equalizer eq;
    private PresetReverb pr;
    private Visualizer vs;
    private SharedPreferences pref;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        playBtn = (Button)findViewById(R.id.playBtn);
        eqSbs = new ArrayList<SeekBar>();
        eqPresetSpn = (Spinner)findViewById(R.id.eqPresetSpn);
        prSpn = (Spinner)findViewById(R.id.reverbSpn);
        
        mp = new MediaPlayer();
        mp.setLooping(true);
        
        setSoundSpn();
        setEqualizer();
        setReverb();
        setVisualizer();
        
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        
    }
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_save1 :
            return save(1);
        case R.id.menu_save2 :
            return save(2);
        case R.id.menu_save3 :
            return save(3);
        case R.id.menu_load1 :
            return load(1);
        case R.id.menu_load2 :
            return load(2);
        case R.id.menu_load3 :
            return load(3);
        }
        return false;
    }
    
    public void play(View view) {
        if (mp.isPlaying()) {
            mp.pause();
            playBtn.setText("Play");
        } else {
            mp.start();
            playBtn.setText("Pause");
        }
    }
    
    private void setSoundSpn() {
        Spinner sound = (Spinner)findViewById(R.id.soundSpn);
        sound.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mp.isPlaying()) playBtn.setText("Play");
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
        short bands = eq.getNumberOfBands();
        for (short i = 0; i < bands; i++) {
            final short band = i;
            TableRow row = new TableRow(this);
            
            TextView freq = new TextView(this);
            freq.setGravity(Gravity.CENTER);
            freq.setText((eq.getCenterFreq(band) / 1000) + "Hz");
            row.addView(freq);
            
            eqSbs.add(new SeekBar(this));
            eqSbs.get(i).setMax(maxEQLevel - minEQLevel);
            eqSbs.get(i).setProgress(eq.getBandLevel(band) - minEQLevel);
            eqSbs.get(i).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    eq.setBandLevel(band, (short) (progress + minEQLevel));
                }
                public void onStartTrackingTouch(SeekBar seekBar) { }
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            row.addView(eqSbs.get(i));
            
            eqLayout.addView(row);
        }
        
        ArrayList<String> presetList = new ArrayList<String>();
        for (short i = 0; i < eq.getNumberOfPresets(); i++) {
            presetList.add(eq.getPresetName(i));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, presetList);
        eqPresetSpn.setAdapter(adapter);
        eqPresetSpn.setOnItemSelectedListener(new OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eq.usePreset((short)position);
                for (int i = 0; i < eq.getNumberOfBands(); i++) {
                    eqSbs.get(i).setProgress(eq.getBandLevel((short)i) - minEQLevel);
                }
                Log.d("EQ", eq.getProperties().toString());
            }
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }
    
    private void setReverb() {
        pr = new PresetReverb(0, mp.getAudioSessionId());
        pr.setEnabled(true);
        prSpn.setOnItemSelectedListener(new OnItemSelectedListener() {
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
    
    private void setVisualizer() {
        final FftView fftView = new FftView(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        fftView.setLayoutParams(layoutParams);
        vs = new Visualizer(mp.getAudioSessionId());
        vs.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        vs.setDataCaptureListener(new OnDataCaptureListener() {
            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                fftView.update(fft);
            }
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) { }
        },
        Visualizer.getMaxCaptureRate(), false, true);
        vs.setEnabled(true);
        fftView.setSamplingRate(vs.getSamplingRate());
        LinearLayout main = (LinearLayout)findViewById(R.id.main);
        main.addView(fftView, 1);
    }
    
    private boolean save(int i) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("eq" + i, eq.getProperties().toString());
        editor.putString("pr" + i, pr.getProperties().toString());
        return editor.commit();
    }
    
    private boolean load(int i) {
        eq.setProperties(new Equalizer.Settings(pref.getString("eq" + i, "Equalizer;curPreset=0;numBands=5;band1Level=300;band2Level=0;band3Level=0;band4Level=0;band5Level=300")));
        short bands = eq.getNumberOfBands();
        short minEQLevel = eq.getBandLevelRange()[0];
        // eqPresetSpn.setSelection(eq.getCurrentPreset());
        for (int j = 0; j < bands; j++) {
             eqSbs.get(j).setProgress(eq.getBandLevel((short)j) - minEQLevel);
        }
        pr.setProperties(new PresetReverb.Settings(pref.getString("pr" + i, "PresetReverb;preset=0")));
        switch (pr.getPreset()) {
        case PresetReverb.PRESET_NONE :
            prSpn.setSelection(0);
            break;
        case PresetReverb.PRESET_SMALLROOM :
            prSpn.setSelection(1);
            break;
        case PresetReverb.PRESET_MEDIUMROOM :
            prSpn.setSelection(2);
            break;
        case PresetReverb.PRESET_LARGEROOM :
            prSpn.setSelection(3);
            break;
        case PresetReverb.PRESET_MEDIUMHALL :
            prSpn.setSelection(4);
            break;
        case PresetReverb.PRESET_LARGEHALL :
            prSpn.setSelection(5);
            break;
        case PresetReverb.PRESET_PLATE :
            prSpn.setSelection(6);
            break;
        }
        return true;
    }

}