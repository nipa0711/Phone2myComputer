package net.nipa011.www.phone2mycomputer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by nipa0711 on 2016-07-01.
 */
public class TCPclient extends AsyncTask<String, String, String> {

    private final int port = 8282;

    @Override
    protected String doInBackground(String... params) {

        Socket sock;
        try {
            sock = new Socket(params[0], 8282);
            Log.d("=================", "Connecting...");

            // sendfile
            File myFile = new File(params[1]);
            byte[] fileData = new byte[(int) myFile.length()];

            FileInputStream fis = new FileInputStream(myFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(fileData, 0, fileData.length);

            String fileName = myFile.getName();

            int fileNameSize = fileName.length();
            byte[] fileNameLen = intToByteArray(fileNameSize);

            int bodySize = fileData.length;
            byte[] bodyLen = intToByteArray(bodySize);

            byte[] fileNameByte = fileName.getBytes("UTF-8");

            byte[] clientData = new byte[4 + 4 + fileNameByte.length + fileData.length];

            //System.arraycopy (원본, 원본 시작위치, 복사본, 복사본 시작위치, 복사본에 얼마만큼 원본의 자료를 쓸까)
            System.arraycopy(fileNameLen, 0, clientData, 0, fileNameLen.length);
            Log.d("===파일 이름 길이 : ", "" + fileNameSize);

            System.arraycopy(bodyLen, 0, clientData, 4, bodyLen.length);
            Log.d("===파일 용량 : ", "" + bodySize);

            System.arraycopy(fileNameByte, 0, clientData, 8, fileNameByte.length);
            Log.d("===파일 이름 : ", "" + fileName);
            Log.d("===파일 이름 바이트 길이 : ", "" + fileNameByte.length);

            System.arraycopy(fileData, 0, clientData, 8 + fileNameByte.length, fileData.length);

            OutputStream os = sock.getOutputStream();
            Log.d("=================", "Sending...");
            Log.d(params[1] + " clientData.length : ", "" + clientData.length);

            os.write(clientData);
            os.flush();

            sock.close();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public byte[] intToByteArray(int value) {
        // 0123 big endian
        // 3210 little endian
        byte[] byteArray = new byte[4];
        byteArray[3] = (byte) (value >> 24);
        byteArray[2] = (byte) (value >> 16);
        byteArray[1] = (byte) (value >> 8);
        byteArray[0] = (byte) (value);
        return byteArray;
    }
}
