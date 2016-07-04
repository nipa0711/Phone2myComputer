package net.nipa011.www.phone2mycomputer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    ArrayAdapter<String> adapter;
    String ip;
    Boolean IPvaild = false;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ArrayList<String> items = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, items);

        // ListView 가져오기
        listView = (ListView) findViewById(R.id.showFolderTree);

        // ListView에 각각의 아이템표시를 제어하는 Adapter를 설정
        listView.setAdapter(adapter);

        Button chkIP = (Button) findViewById(R.id.btnChkIP);
        chkIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText IPaddr = (EditText) findViewById(R.id.editIPaddr);
                ip = IPaddr.getText().toString();

                String validIp = "^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$"; // IP 유효성 검사

                if (!Pattern.matches(validIp, ip)) {
                    Toast.makeText(getApplicationContext(), "IP주소 " + ip + " 가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    IPvaild = false;
                    return;
                } else {
                    Toast.makeText(getApplicationContext(), "IP주소 " + ip + " 가 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    IPvaild = true;
                }
            }
        });

        Button addFolder = (Button) findViewById(R.id.btnFolderAdd);
        addFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FolderBrowser.class);
                startActivityForResult(intent, 1);
            }
        });

        Button delFolder = (Button) findViewById(R.id.btnFolderRemove);
        delFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                if (checkedItems.size() <= 0) {
                    Toast.makeText(getApplicationContext(), "선택된 것이 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                int count = adapter.getCount();

                for (int i = count - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        items.remove(i);
                    }
                }

                // 모든 선택 상태 초기화.
                listView.clearChoices();
                adapter.notifyDataSetChanged();
                onResume();
            }
        });

        Button StartEnd = (Button) findViewById(R.id.btnStartEnd);
        StartEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 폴더 목록을 ArrayList 에 추가
                ArrayList<String> folders = new ArrayList<String>();

                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                int count = adapter.getCount();

                for (int i = count - 1; i >= 0; i--) {
                    if (checkedItems.get(i)) {
                        folders.add(items.get(i));
                        Log.d("==============폴더 목록 : ", items.get(i));
                    }
                }

                for (int i = 0; i < folders.size(); i++) {
                    // ArrayList의 목록을 돌면서, 해당 폴더의 파일들을 구한다.
                    String[] files = getFileList(folders.get(i));

                    for (int j = 0; j < files.length; j++) {
                        File file = new File(folders.get(i) + "/" + files[j]);
                        if (!file.isDirectory()) {
                            Log.d("=================", folders.get(i) + "/" + files[j]);
                            new TCPclient().execute(ip, folders.get(i) + "/" + files[j]);
                        }
                    }
                }
            }
        });
    }


    // 특정 폴더의 파일 목록을 구해서 반환
    public String[] getFileList(String strPath) {
        // 폴더 경로를 지정해서 File 객체 생성
        File fileRoot = new File(strPath);
        // 해당 경로가 폴더가 아니라면 함수 탈출
        if (fileRoot.isDirectory() == false)
            return null;

        // 파일 목록을 구한다
        String[] fileList = fileRoot.list();
        return fileList;
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Button StartEnd = (Button) findViewById(R.id.btnStartEnd);
        if (IPvaild == true && listView.getCount() > 0) {
            StartEnd.setEnabled(true);
        } else {
            StartEnd.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) // 액티비티가 정상적으로 종료되었을 경우
        {
            if (requestCode == 1) // requestCode==1 로 호출한 경우에만 처리.
            {
                for (int i = 0; i < adapter.getCount(); i++) { // 중복 확인
                    if (adapter.getItem(i).equals(data.getStringExtra("addr"))) {
                        return;
                    }
                }

                adapter.add(data.getStringExtra("addr"));

                // listview 갱신
                adapter.notifyDataSetChanged();
            }
        }
    }
}
