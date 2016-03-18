package com.stealthmate.home.pcvolcontrol;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    Updater updt;

    public static Button connect;
    public static SeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updt = new Updater();
        updt.execute();

        connect = (Button) findViewById(R.id.btnconn);

        seekbar = (SeekBar) findViewById(R.id.SETTING_BAR);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                updt.handleMessage(UpdateHandler.MSG_VOL, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        updt.terminate();
        updt.cancel(false);

    }

    public void disconnect(View v) {

        connect.setBackgroundColor(Color.GRAY);

        if(updt == null) return;

        updt.terminate();
        updt.cancel(false);
    }

    public void connect(View v) {

        if(updt!=null) {

            updt.terminate();
            updt.cancel(false);

        }
        updt = new Updater();
        updt.execute();
    }

    public void next(View v) {
        updt.handleMessage(UpdateHandler.MSG_NEXTTRACK, 0);
    }

    public void prev(View v) {
        updt.handleMessage(UpdateHandler.MSG_PREVTRACK, 0);
    }

    public void play(View v) {
        updt.handleMessage(UpdateHandler.MSG_PLAYPAUSE, 0);
    }

    public void stop(View v) {
        updt.handleMessage(UpdateHandler.MSG_STOP, 0);
    }
}
