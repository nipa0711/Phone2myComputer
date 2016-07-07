package net.nipa0711.www.phone2mycomputer;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by nipa0711 on 2016-07-01.
 */
public class TCPclient extends AsyncTask<String, String, String> {

    private final int port = 8282;
    String[] sendList;
    String[] arrFolderName;
    int[] filesInFolder;

    public TCPclient(String[] sendList, String[] arrFolderName, int[] filesInFolder) {
        this.sendList = sendList;
        this.arrFolderName = arrFolderName;
        this.filesInFolder = filesInFolder;
    }

    @Override
    protected String doInBackground(String... params) {

        Socket sock;
        int listCount = 0;

        for (int i = 0; i < arrFolderName.length; i++) {

            String folderName = arrFolderName[i];
            int filesCount = filesInFolder[i];

            for (int j = 0; j < filesCount; j++) {
                try {
                    sock = new Socket(params[0], port);
                    Log.d("=================", "Connecting...");

                    // sendfile
                    File myFile = new File(sendList[listCount]);
                    byte[] fileData = new byte[(int) myFile.length()];

                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    bis.read(fileData, 0, fileData.length);

                    String fileName = myFile.getName();

                    int fileNameSize = fileName.length();
                    byte[] fileNameLen = intToByteArray(fileNameSize);

                    int folderNameSize = folderName.length();
                    byte[] folderNameLen = intToByteArray(folderNameSize);

                    int bodySize = fileData.length;
                    byte[] bodyLen = intToByteArray(bodySize);

                    byte[] fileNameByte = fileName.getBytes("UTF-8");
                    byte[] folderNameByte = folderName.getBytes("UTF-8");

                    byte[] clientData = new byte[4 + 4 + 4 + fileNameByte.length + folderNameByte.length + fileData.length];

                    //System.arraycopy (원본, 원본 시작위치, 복사본, 복사본 시작위치, 복사본에 얼마만큼 원본의 자료를 쓸까)
                    System.arraycopy(fileNameLen, 0, clientData, 0, fileNameLen.length);
                    Log.d("===파일 이름 길이 : ", "" + fileNameSize);

                    System.arraycopy(bodyLen, 0, clientData, 4, bodyLen.length);
                    Log.d("===파일 용량 : ", "" + bodySize);

                    System.arraycopy(folderNameLen, 0, clientData, 8, folderNameLen.length);
                    Log.d("===폴더 이름 길이 : ", "" + folderNameSize);

                    System.arraycopy(fileNameByte, 0, clientData, 12, fileNameByte.length);
                    Log.d("===파일 이름 : ", "" + fileName);

                    System.arraycopy(folderNameByte, 0, clientData, 12 + fileNameByte.length, folderNameByte.length);
                    Log.d("===폴더 이름 : ", "" + folderName);

                    System.arraycopy(fileData, 0, clientData, 12 + fileNameByte.length + folderNameByte.length, fileData.length);

                    OutputStream os = sock.getOutputStream();

                    Log.d(sendList[listCount] + " 총 전송 크기 : ", "" + clientData.length);

                    listCount++;
                    os.write(clientData);
                    os.flush();
                    sock.close();
                    Log.d("=================", "전송완료");

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    Log.d("=================", "전송오류");
                    continue;
                }
            }
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
