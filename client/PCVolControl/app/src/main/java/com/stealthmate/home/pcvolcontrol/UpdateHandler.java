package com.stealthmate.home.pcvolcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Stealthmate on 2016/03/06.
 */
public class UpdateHandler {


    private static final byte ACTION_CODE_VOL        = 0x11;
    private static final byte ACTION_CODE_NEXT_TRACK = 0x12;
    private static final byte ACTION_CODE_PREV_TRACK = 0x13;
    private static final byte ACTION_CODE_PLAY_PAUSE = 0x14;
    private static final byte ACTION_CODE_STOP       = 0x15;

    public static final int MSG_VOL        = 0x03;
    public static final int MSG_NEXTTRACK  = 0x04;
    public static final int MSG_PREVTRACK  = 0x05;
    public static final int MSG_PLAYPAUSE  = 0x06;
    public static final int MSG_STOP       = 0x07;

    private InputStream is;
    private OutputStream os;

    public  UpdateHandler(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    private void sendVol(int val) throws IOException {
        os.write(ACTION_CODE_VOL);
        os.write(val & 0xFF);
        os.flush();
    }

    private void sendNextTrack() throws IOException {
        os.write(ACTION_CODE_NEXT_TRACK);
        os.write(0xFF);
        os.flush();
    }

    private void sendPrevTrack() throws IOException {
        os.write(ACTION_CODE_PREV_TRACK);
        os.write(0xFF);
        os.flush();
    }

    private void sendStop() throws IOException {
        os.write(ACTION_CODE_STOP);
        os.write(0xFF);
        os.flush();
    }

    private void sendPlayPause() throws IOException {
        os.write(ACTION_CODE_PLAY_PAUSE);
        os.write(0xFF);
        os.flush();
    }

    public void handleMessage(int msgtype, int val) throws IOException {
        switch (msgtype) {
            case MSG_VOL: {
                sendVol(val);
            }
            break;
            case MSG_NEXTTRACK: {
                sendNextTrack();
            }
            break;
            case MSG_PREVTRACK: {
                sendPrevTrack();
            }
            break;
            case MSG_PLAYPAUSE: {
                sendPlayPause();
            } break;
            case MSG_STOP: {
                sendStop();
            } break;
        }
    }

}
