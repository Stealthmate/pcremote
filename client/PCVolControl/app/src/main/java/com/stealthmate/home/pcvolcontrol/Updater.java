package com.stealthmate.home.pcvolcontrol;

import android.graphics.Color;
import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.xml.transform.sax.SAXSource;

/**
 * Created by Stealthmate on 2016/03/06.
 */
public class Updater extends AsyncTask<Void, Integer, Void> {

    private static final byte CONTROL_CODE_KILL = 0x00;
    private static final byte CONTROL_CODE_HEARTBEAT = 0x01;
    private static final byte CONTROL_CODE_CLOSE_CONNECTION = 0x0F;

    private static final byte RETURN_CODE_HEARTBEAT       = 0x00;
    private static final byte RETURN_CODE_INVALID_COMMAND = 0x01;
    private static final byte RETURN_CODE_VALUE_ERROR     = 0x02;
    private static final byte RETURN_CODE_CLOSE_CONN      = 0x0F;

    public static final int MSG_CONNECT    = 0x00;
    public static final int MSG_DISCONNECT = 0x01;
    public static final int MSG_KILL       = 0x02;

    private static final String ADDRESS = "192.168.100.5";
    private static final int PORT = 25565;

    private Socket socket;
    private UpdateHandler mHandler;
    private boolean conn_safe;

    private volatile boolean should_run;

    public static final byte [] out(byte code) {
        byte [] b = new byte[2];
        b[0] = code;
        return b;
    }


    public Updater() {
        super();
        should_run = true;
        mHandler = null;
        conn_safe = false;
    }

    public void handleMessage(int msgtype, int val) {
        try {
            switch (msgtype) {
                case MSG_DISCONNECT: {
                    terminate();
                    return;
                }
                case MSG_KILL: {
                    socket.getOutputStream().write(out(CONTROL_CODE_KILL));
                    socket.getOutputStream().flush();
                    terminate();
                    return;
                }
                default:
                    if(mHandler != null && conn_safe) mHandler.handleMessage(msgtype, val);
                    else System.err.println("No handler or dead connection!");
            }
        } catch (IOException e) {
            System.out.println("Something fucked up here");
            e.printStackTrace();
        }
    }


    private boolean connectionAlive() {

        conn_safe = false;

        if(socket == null) return conn_safe;
        if(!socket.isConnected()) return conn_safe;

        try {

            InputStream reader = socket.getInputStream();
            OutputStream writer = socket.getOutputStream();

            writer.write(out(CONTROL_CODE_HEARTBEAT));
            writer.flush();

            byte [] b = new byte[1];
            reader.read(b, 0, 1);
            if(b[0] != RETURN_CODE_HEARTBEAT) {
                conn_safe = false;
                return conn_safe;
            }

        } catch (SocketTimeoutException e) {
            return conn_safe;
        } catch (IOException e) {
            conn_safe = false;
            return conn_safe;
        }

        conn_safe = true;
        return conn_safe;
    }

    private void openConnection() {
        System.out.println("Connecting");
        try {
            socket = new Socket();
            socket.setSoTimeout(3000);
            socket.connect(new InetSocketAddress(ADDRESS, PORT), 1000);
            System.out.println("Connection successful!");
            byte [] b = new byte[1];
            System.out.println(socket.getInputStream().read(b));
            publishProgress((int) b[0]);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Socket fucked up");
        }

    }

    @Override
    public Void doInBackground(Void... params) {

        System.out.println("Thread started");

        try {

            while(!connectionAlive() && should_run) {
                publishProgress(-1);
                openConnection();
            }

            publishProgress(101);


            System.out.println("Connection established");

            mHandler = new UpdateHandler(socket.getInputStream(), socket.getOutputStream());
            long pingtime = 0;
            long pingthreshold = 1000;
            long lastping = 0;
            pingtime = System.currentTimeMillis();
            while(should_run && conn_safe) {
                pingtime = System.currentTimeMillis();
                if(pingtime - lastping > pingthreshold) {
                    connectionAlive();
                    lastping = System.currentTimeMillis();
                }
            }
            System.out.println("Closing off connection");

            socket.getOutputStream().write(out(CONTROL_CODE_HEARTBEAT));
            socket.getOutputStream().flush();

            try {
                byte [] b = new byte[1];
                System.out.println(socket.getInputStream().available());
                int i = socket.getInputStream().read(b, 0, 1);
                System.out.println(b[0] + " " + i);
                System.out.println("Closed successfully");

            } catch (SocketTimeoutException e) {
                System.out.println("Problem");
            }
            socket.close();

        } catch (IOException e) {
            System.err.println("Something fucked up");
            return null;
        }

        System.out.println("Thread over");

        return null;
    }

    public void terminate() {
        System.out.println("Terminating...");
        should_run = false;
    }

    @Override
    public void onProgressUpdate(Integer... status) {
        System.out.println(status[0]);
        if(status[0] > 100) MainActivity.connect.setBackgroundColor(Color.GREEN);
        else if(status[0] < 0) MainActivity.connect.setBackgroundColor(Color.RED);
        else {
            MainActivity.seekbar.setProgress(status[0]);
        }
    }

    @Override
    public void onPostExecute(Void v) {
        MainActivity.connect.setBackgroundColor(Color.RED);
    }



}
