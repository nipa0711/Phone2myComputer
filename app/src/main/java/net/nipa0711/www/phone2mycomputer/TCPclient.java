package net.nipa0711.www.phone2mycomputer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    private static final int port = 8282;
    String[] sendList;
    String[] arrFolderName;
    int[] filesInFolder;

    private ProgressDialog mDlg;
    private Context mContext;

    public TCPclient(String[] sendList, String[] arrFolderName, int[] filesInFolder, Context context) {
        this.sendList = sendList;
        this.arrFolderName = arrFolderName;
        this.filesInFolder = filesInFolder;
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        mDlg = new ProgressDialog(mContext);
        mDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDlg.setMessage("전송 시작");
        mDlg.show();

        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        Socket sock;
        int listCount = 0;
        publishProgress("max", Integer.toString(sendList.length));

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
                    //작업 진행 마다 진행률을 갱신하기 위해 진행된 개수와 설명을 publishProgress() 로 넘겨줌.
                    publishProgress("progress", "" + listCount, fileName + " 전송 중...");
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

    //onProgressUpdate() 함수는 publishProgress() 함수로 넘겨준 데이터들을 받아옴
    @Override
    protected void onProgressUpdate(String... progress) {
        if (progress[0].equals("progress")) {
            mDlg.setProgress(Integer.parseInt(progress[1]));
            mDlg.setMessage(progress[2]);
        } else if (progress[0].equals("max")) {
            mDlg.setMax(Integer.parseInt(progress[1]));
        }
    }

    //onPostExecute() 함수는 doInBackground() 함수가 종료되면 실행됨
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mDlg.dismiss();

        // 안내창
        AlertDialog.Builder showAlert = new AlertDialog.Builder(mContext);
        showAlert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();     //닫기
            }
        });
        showAlert.setTitle("전송완료 안내");
        showAlert.setMessage(sendList.length + " 개의 파일 전송이 완료되었습니다.");
        showAlert.show();
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
