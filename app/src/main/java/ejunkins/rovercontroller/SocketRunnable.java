package ejunkins.rovercontroller;

import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class SocketRunnable implements Runnable {

    private StringBuffer mExit;
    private StringBuffer mThrottle;
    private StringBuffer mSteering;
    private StringBuffer mRoverFace;
    private StringBuffer mServo;
    private StringBuffer mConnectedStatus;
    private BluetoothSocket mBluetoothSocket;

    protected SocketRunnable(StringBuffer throttle, StringBuffer steering, StringBuffer roverFace, StringBuffer servo, BluetoothSocket socket, StringBuffer exit,StringBuffer connectedStatus){
        this.mThrottle = throttle;
        this.mSteering = steering;
        this.mRoverFace = roverFace;
        this.mBluetoothSocket = socket;
        this.mServo = servo;
        this.mExit = exit;
        this.mConnectedStatus = connectedStatus;
    }

    private InputStream tmpIn = null;
    private OutputStream tmpOut = null;


    @Override
    public void run(){
        long lastTimeSent = System.currentTimeMillis();
        long lastTimeReceived = System.currentTimeMillis();
        long now;
        while (mExit.toString().equals("False")){
            now = System.currentTimeMillis();
            if (now > lastTimeSent + 50){
                lastTimeSent = now;
                try {
                    tmpIn = mBluetoothSocket.getInputStream();
                }catch (IOException e){
                    Log.e("MY_APP_DEBUG_TAG","Error occured when created input stream",e);
                }
                try {
                    tmpOut = mBluetoothSocket.getOutputStream();
                } catch (IOException e){
                    Log.e("MY_APP_DEBUG_TAG","Error occured when creating output stream",e);
                }
                InputStream mmInStream = tmpIn;
                OutputStream mmOutStream = tmpOut;
                try {
                    int a = Integer.parseInt(mThrottle.toString())+100;
                    int c = Integer.parseInt(mSteering.toString())+100;
                    int e = Integer.parseInt(mRoverFace.toString());
                    ByteBuffer b = ByteBuffer.allocate(4);
                    ByteBuffer d = ByteBuffer.allocate(4);
                    ByteBuffer f = ByteBuffer.allocate(4);
                    byte[] array_1 = b.putInt(a).array();
                    byte[] array_2 = d.putInt(c).array();
                    byte[] array_3 = f.putInt(e).array();
                    byte[] chk_sum = new byte[4];

                    for(int i = 0; i<array_1.length; i++){
                        chk_sum[i] = (byte)(array_1[i] ^ array_2[i] );
                    }
                    ByteBuffer x = ByteBuffer.allocate(16).putInt(a).putInt(c).putInt(e).put(chk_sum);
                    byte[] result = x.array();
                    mmOutStream.write(result);

                    int counter = 0;
                    while (counter < 200 && mmInStream.available() == 0){
                        SystemClock.sleep(5);
                        counter += 1;
                    }
                    if (counter >= 100){
                        mExit.delete(0, mExit.length());
                        mExit.append("True");
                        Log.d("MY_EXITING_THREAD", mExit.toString());
                        break;
                    }
                    String input = String.valueOf(mmInStream.read());
                    mConnectedStatus.delete(0, mConnectedStatus.length());
                    mConnectedStatus.append(input);
                } catch (IOException e) {
                    Log.d("ioExcpetion",e.toString());
                    e.printStackTrace();
                }
            }
        }
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mConnectedStatus.delete(0, mConnectedStatus.length());
        mConnectedStatus.append("50");
    }
}


