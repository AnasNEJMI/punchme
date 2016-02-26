package android.feetme.fr.punchme.controllers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.feetme.fr.punchme.GloveFactory;
import android.feetme.fr.punchme.dao.Glove;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Anas on 26/02/2016.
 */
public class GloveController implements IGloveController{

    private static final String TAG = GloveController.class.getSimpleName();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String IGNORED_MSG = "#hello";
    private static final int WRITE_DELAY_MS= 500;

    private volatile int mState;

    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private final Listener  mListener;

    private final Glove mGlove;

    private Context mContext;

    public static IGloveController newInstance(Context context, //used in BLE build flavor
                                               Glove glove,
                                               Listener listener){
        return new GloveController(context, glove, listener);
    }

    private GloveController(Context context, Glove glove, Listener listener){
        mContext = context;
        mState = STATE_DISCONNECTED;
        mGlove = glove;
        mListener = listener;
    }

    /*
    * Type of messages for the handler.
    */
    private static final int MSG_DATA_FRAME = 0;
    private static final int MSG_STATE_CHANGE = 100;

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            if(what == MSG_DATA_FRAME) mListener.onDataFrame((byte[]) msg.obj);
            else if (what == MSG_STATE_CHANGE) mListener.onStateChanged(msg.arg1);
            else if (what == Event.SENSOR_NB.code()) mListener.onSensorNb(msg.arg1);
            else if (what == Event.START_SENDING.code()) mListener.onStartSending();
            else if (what == Event.STOP_SENDING.code()) mListener.onStopSending();
            else if (what == Event.MODE.code()) mListener.onBootMode();
            else if (what == Event.CALIBRATION.code()) mListener.onCalibration();
            else if (what == Event.PING.code()) mListener.onPong();
            else if (what == Event.SIDE.code()) mListener.onSide(msg.arg1);
            else Log.w(TAG, "Wrong glove message! " + msg.what);
        }
    };

    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state + " for glove " + mGlove.getSide());
        mState = state;
        mHandler.obtainMessage(MSG_STATE_CHANGE, state, -1).sendToTarget();
    }

    private void init() {
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
    }

    @Override
    public int getState() {
        return mState;
    }

    @Override
    public synchronized void connect() {
        String address = mGlove.getAddress();
        BluetoothDevice device = mAdapter.getRemoteDevice(address);
        Log.d(TAG, "connect to: " + device);

        init();
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    synchronized void connected(BluetoothSocket socket) {
        Log.d(TAG, "connected");
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, mHandler);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }


    @Override
    public void disconnect() {
        Log.d(TAG, "disconnect");
        init();
        setState(STATE_DISCONNECTED);
    }

    @Override
    public void requestSensorNb() {
        addEventToQueue(Event.SENSOR_NB);
    }

    @Override
    public void requestSide() {
        addEventToQueue(Event.SIDE);
    }

    @Override
    public void requestPing() {
        addEventToQueue(Event.PING);
    }

    @Override
    public void requestStartSending() {
        addEventToQueue(Event.START_SENDING);
    }

    @Override
    public void requestStopSending() {
        addEventToQueue(Event.STOP_SENDING);
    }

    @Override
    public void requestCalibration() {
        addEventToQueue(Event.CALIBRATION);
    }


    enum Event {
        PING (1, "ping", 9),
        SENSOR_NB (2, "sensors_number", 6),
        SIDE (4, "glove_side", 6),
        RESET_TIMESTAMP (5, "reset_timestamp", 5),
        START_SENDING (10, "start_sending", 10),
        STOP_SENDING (11, "stop_sending", 9),
        MODE (14, null, 5), //TODO edge case
        CALIBRATION (15, "sensor_calibration", 16);

        private final int code;
        private final String message;
        private final int length;//expected response length

        Event(int code, String message, int length) {
            this.code = code;
            this.message = message;
            this.length = length;
        }

        public final int code() { return code; }
        public final String message() { return message; }
        public final int length() { return length; }
    }

    /*
         * Thread that manages a Bluetooth connection to one device
         */
    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                //reflection method to connect
                //Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
                //tmp = (BluetoothSocket) m.invoke(device, 1);
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                disconnect();
            }
            mmSocket = tmp;
        }

        public void run() {
            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect(); //Blocking method
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                Log.e(TAG, connectException.toString());
                disconnect();
                return;
            }
            synchronized (this) {
                mConnectThread = null;
            }
            // Do work to manage the connection (in a separate thread)
            connected(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                if(mmSocket != null) {
                    Log.i(TAG, "close socket");
                    mmSocket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private void addEventToQueue(Event event) {
        if(mState == STATE_CONNECTED){
            if(mConnectedThread != null) {
                Log.d(TAG, String.format("--------- REQUEST %s ---------",
                        event.name()));
                mConnectedThread.mmWriteQueue.add(event);
            }
        }
    }

    /*
     * Thread that manages sending and receiving data during Bluetooth connection.
     */
    private class ConnectedThread extends Thread{

        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final Handler mmHandler;
        private int frameLength;
        private int frameCount = 0;
        private final int MAX_BUFFER_SIZE = 200;
        private boolean isSensorNbKnown = false;
        private byte[] buffer = new byte[MAX_BUFFER_SIZE];  // buffer store for the stream

        final Queue<Event> mmWriteQueue = new EventQueue();
        WriteHandler mmWriteHandler;

        public ConnectedThread(BluetoothSocket socket, Handler handler) {
            mmSocket = socket;
            mmHandler = handler;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Could not get streams from socket");
                disconnect();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            //initiate write handler to write remote functions
            HandlerThread handlerThread = new HandlerThread("SocketWriter");
            handlerThread.start();
            mmWriteHandler = new WriteHandler(handlerThread.getLooper(), mmOutStream, mmWriteQueue);

            //waiting solves many issues
            //Can't wait too long before writing or the glove will stop
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Log.w(TAG, Log.getStackTraceString(e));
            }

            //check sensor number
            Integer sensorNb = mGlove.getSensorNb();
            if (sensorNb == null) {
                sensorNb = discoverSensorNb();
                if (sensorNb > 0 && sensorNb < MAX_BUFFER_SIZE - 10) {
                    mmHandler.obtainMessage(Event.SENSOR_NB.code(), sensorNb, -1).sendToTarget();
                } else {
                    disconnect();
                    return;
                }
            }
            frameLength = sensorNb + GloveFactory.OVERHEAD;
            isSensorNbKnown = true;

            synchronized (this) {
                if (mmWriteHandler != null) mmWriteHandler.send();
                else return;
            }

            // Keep listening to the InputStream until an exception occurs
            while (isSensorNbKnown) {
                try {
                    processMessage();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    disconnect();
                    break;
                }
            }
        }

        /* Call this from the BTService to shutdown the connection */
        public void cancel() {
            if(mmWriteHandler != null) {
                mmWriteHandler.removeCallbacksAndMessages(null);
                mmWriteHandler.getLooper().quit();
                mmWriteHandler = null;
            }
            try {
                Log.i(TAG, "Closing socket");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close socket", e);
            }
        }

        private void processMessage() throws IOException {
            int code = mmInStream.read();
            if (code == MSG_DATA_FRAME) {
                frameCount++;
                if (frameCount == 200) {
                    frameCount = 0;
                    Log.d(TAG, "--------- 200 frames ---------");

                }
                if (readMessage(frameLength)) {
                    buffer[0] = (byte) code;
                    mmHandler.obtainMessage(MSG_DATA_FRAME,
                            Arrays.copyOfRange(buffer, 0, frameLength))
                            .sendToTarget();
                }
            } else {
                 if (code == Event.SENSOR_NB.code()) {
                    Log.d(TAG, String.format("---------INDEX SENSOR NUMBER %d ---------",
                                (buffer[5] & 0xFF)));
                    if (readMessage(Event.SENSOR_NB.length())) {
                        int sensorNb = (buffer[5] & 0xFF);
                        if (mmWriteQueue.peek() == Event.SENSOR_NB) mmWriteQueue.poll();
                        mmHandler.obtainMessage(code, sensorNb, -1)
                                .sendToTarget();
                    }
                } else if (code == Event.START_SENDING.code()) {
                    Log.d(TAG, "--------- INDEX START ---------");
                    if (readMessage(Event.START_SENDING.length())) {
                        Log.d(TAG, "--------- READ START ---------");
                        if (mmWriteQueue.peek() == Event.START_SENDING) mmWriteQueue.poll();
                        mmHandler.obtainMessage(code).sendToTarget();
                    }
                } else if (code == Event.STOP_SENDING.code()) {
                    Log.d(TAG, "--------- INDEX STOP ---------");
                    if (readMessage(Event.STOP_SENDING.length())) {
                        if (mmWriteQueue.peek() == Event.STOP_SENDING) mmWriteQueue.poll();
                        mmHandler.obtainMessage(code).sendToTarget();
                    }
                } else if (code == Event.CALIBRATION.code()) {
                    Log.d(TAG, "--------- INDEX CALIBRATION ---------");
                    if (readMessage(Event.CALIBRATION.length())) {
                        if (mmWriteQueue.peek() == Event.CALIBRATION) mmWriteQueue.poll();
                        mmHandler.obtainMessage(code).sendToTarget();
                    }
                } else if (code == Event.RESET_TIMESTAMP.code()) {
                    Log.d(TAG, "---------INDEX RESET TIMESTAMP ---------");
                    if (readMessage(Event.RESET_TIMESTAMP.length())) {
                        if (mmWriteQueue.peek() == Event.RESET_TIMESTAMP) mmWriteQueue.poll();
                        mmHandler.obtainMessage(code).sendToTarget();
                    }
                } else if (code == Event.SIDE.code()) {
                    Log.d(TAG, "---------INDEX SIDE ---------");
                    if (readMessage(Event.SIDE.length())) {
                        if (mmWriteQueue.peek() == Event.SIDE) mmWriteQueue.poll();
                        char sideChar = (char) (buffer[5] & 0xFF);
                        if (sideChar == 'L') {
                            mmHandler.obtainMessage(code, GloveFactory.SIDE_LEFT, -1)
                                    .sendToTarget();
                        } else if (sideChar == 'R') {
                            mmHandler.obtainMessage(code, GloveFactory.SIDE_RIGHT, -1)
                                    .sendToTarget();
                        }
                    }
                } else if (code == Event.PING.code()) {
                    Log.d(TAG, "---------INDEX PING ---------");
                    if (readMessage(Event.PING.length())) {
                        mmHandler.obtainMessage(code).sendToTarget();
                    }
                } else {
                    int availbale = mmInStream.available();
                    if (availbale > 0) mmInStream.skip(availbale);
                    Log.w(TAG, String.format(
                            "Message not identified, not processed! %d, skipped %d",
                            code, availbale));
                }
            }
        }

        /**
         * read the right number of bytes from the input stream,
         * return false if not enough bytes are read, true otherwise.
         */
        private boolean readMessage(int length) throws IOException{
            int readNb = mmInStream.read(buffer, 1, length - 1) + 1;
            if(readNb < length){
                logReadTooShort(length, readNb);
                return false;
            }
            return true;
        }

        private void logReadTooShort(int expectedLength, int actualLength){
            Log.w(TAG, String.format("Message too short! Not processed. %d < %d",
                    actualLength, expectedLength));
        }

        /**
         * Discover the number of sensors of the glove.
         *
         * @return -1 if discovery failed
         */
        private int discoverSensorNb(){
            byte[] buff = new byte[Event.SENSOR_NB.length()];
            int readNb;
            int discoveryCount = 0;

            while (true) {
                discoveryCount++;
                try {
                    mmOutStream.write("sensors_number".getBytes());
                    readNb = mmInStream.read(buff);

                    if (readNb == Event.SENSOR_NB.length() && buff[0] == Event.SENSOR_NB.code()) {
                        int sensorNb = (buff[5] & 0xFF);
                        Log.d(TAG, String.format("---------INDEX SENSOR NUMBER %d ---------", sensorNb));
                        return sensorNb;
                    } else if (buff[0] == Event.MODE.code()) {
                        String mode = new String(Arrays.copyOfRange(buff, 1, Event.MODE.length()));
                        if (mode.contains("boot")){
                            mmHandler.obtainMessage(Event.MODE.code()).sendToTarget();
                        }
                    } else {
                        Log.d(TAG, String.format("---INDEX %d LENGTH %d during sensor discovery---",
                                buff[0], readNb));
                    }

                    int available = mmInStream.available();
                    if (available > 0) {
                        mmInStream.skip(available);
                    }
                    if(discoveryCount > 1000){
                        Log.w(TAG, "Sensor number discovery failed 1000 times for glove "
                                + mGlove.getSide());
                        return -1;
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    return -1;
                }
            }
        }
    }

    /**
     * Only add(), peek() and poll() method should be used withoud considering modifying this class.
     */
    private static class EventQueue extends ConcurrentLinkedQueue<Event> {

        private int peekCount = 0;

        @Override
        public Event peek() {
            if(peekCount > 50) return this.poll();

            peekCount++;
            return super.peek();

        }

        @Override
        public Event poll() {
            peekCount = 0;
            return super.poll();
        }
    }

    /**
     * Handler to write remote functions to the connected glove.
     * It writes remote functions with a time interval between every call.
     */
    private class WriteHandler extends Handler{
        private final OutputStream mmOutStream;
        private final Queue<Event> mmWriteQueue;

        WriteHandler(Looper lopper, OutputStream os, Queue<Event> queue){
            super(lopper);
            mmOutStream = os;
            mmWriteQueue = queue;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if (!mmWriteQueue.isEmpty()) {
                    //process write queue action
                    String message = mmWriteQueue.peek().message();
                    Log.d(TAG, "Writing " + message);
                    mmOutStream.write(message.getBytes());
                } else {
                    //write some data to prevent Android from sending sniff mode
                    mmOutStream.write(IGNORED_MSG.getBytes());
                }
            }catch(IOException e){
                Log.e(TAG, "disconnected while writing", e);
                disconnect();
                return;
            }
            send();
        }

        public void send() {
            sendMessageDelayed(obtainMessage(), WRITE_DELAY_MS);
        }
    }
}
