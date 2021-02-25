package com.example.client_android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "SocketClient";
    private EditText serverIPText;
    private TextView msgContent;
    private EditText sendText;
    private Button connectBtn;
    private Button sendMsgBtn;
    private Button clearBtn, closeBtn;
    private Socket clientSocket;
    private String serverIP = "";
    private BufferedWriter bw;            //取得網路輸出串流
    private BufferedReader br;            //取得網路輸入串流
    private String tmp;                    //做為接收時的緩存
    private Thread thread;                //執行緒

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serverIPText = (EditText) findViewById(R.id.serverIPText);
        msgContent = (TextView) findViewById(R.id.msgcontent);
        sendText = (EditText) findViewById(R.id.sendText);
        connectBtn = (Button) findViewById(R.id.connectBtn);
        sendMsgBtn = (Button) findViewById(R.id.sendBtn);
        clearBtn = (Button) findViewById(R.id.clearBtn);
        closeBtn = (Button) findViewById(R.id.closeBtn);
        connectBtn.setOnClickListener(connectEvent);
        sendMsgBtn.setOnClickListener(sendMsgEvent);
        clearBtn.setOnClickListener(ClearEvent);
        closeBtn.setOnClickListener(CloseEvent);
    }

    private View.OnClickListener CloseEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    };

    private View.OnClickListener ClearEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            msgContent.setText("");
        }
    };

    // 連線伺服器
    private View.OnClickListener connectEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 檢查IP
            serverIP = serverIPText.getText().toString();
            Log.i(TAG, "IP: " + serverIP);
            String regRule = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

            if (serverIP.equals("")) {
                Log.e(TAG, "IP empty");
                return;
            }
            if (!Pattern.matches(regRule, serverIP)) {
                Log.e(TAG, "not authorized IP");
                return;
            }
            Log.i(TAG, "Connect to Server");
            connectBtn.setEnabled(false);
            serverIPText.setEnabled(false);
            thread = new Thread(Connection);                //賦予執行緒工作
            thread.start();                    //讓執行緒開始執行
        }
    };

    // 發送訊息
    private View.OnClickListener sendMsgEvent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendToServer();
            //sendPointToServer(1,500,800,10);
        }
    };

    private void sendPointToServer(int id, int x, int y, int w){
        String sBuf =null;
        sBuf = Integer.toString(id)+","+Integer.toString(x)+","+Integer.toString(y)+","+Integer.toString(w)+",";
        try {
            //寫入後送出
            bw.write(sBuf + "\n");
            bw.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void sendImageToServer(){

    }

    private void sendToServer(){
        Log.i(TAG, "Send Msg to Server");
        // 檢查訊息
        String message = sendText.getText().toString();
        if (message.equals("")) {
            return;
        }
        try {
            //寫入後送出
            bw.write(message + "\n");
            bw.flush();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //連結socket伺服器做傳送與接收
    private Runnable Connection = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                // IP為Server端
                int serverPort = 6000;
                clientSocket = new Socket(serverIP, serverPort);
                // 取得網路輸入串流
                br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                //取得網路輸出串流
                bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                Log.i(TAG," BufferedReader ");
                // 當連線後
                while (clientSocket.isConnected()) {
                    //read socket receive data
                    tmp = br.readLine();    //宣告一個緩衝,從br串流讀取值

                    // 如果不是空訊息
                    if (tmp != null) {
                        // 空字串訊息不理會
                        if (tmp.equals(""))
                            continue;
                        Log.i(TAG,"tmp ="+ tmp);
                        //將取到的String抓取{}範圍資料
//                        tmp=tmp.substring(tmp.indexOf("{"), tmp.lastIndexOf("}") + 1);
//                        json_read=new JSONObject(tmp);
                        //decode string Start
                        // 從java伺服器取得值後做拆解,可使用switch做不同動作的處理
                        String[] AfterSplit = tmp.split(",");
                        for (int i = 0; i < AfterSplit.length; i++){
//                            System.out.println(AfterSplit[i]);
                            Log.d(TAG,"AfterSplit["+i+"]"+ AfterSplit[i]);
                        }

                        //decode string end
                        Log.i(TAG, "receive from server: " + tmp);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            public void run() {
                                msgContent.append(tmp + "\n");
                            }
                        });

                    }
                }

                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        connectBtn.setEnabled(true);
                        serverIPText.setEnabled(true);
                    }
                });
            } catch (Exception e) {
                //當斷線時會跳到catch,可以在這裡寫上斷開連線後的處理
                e.printStackTrace();
                Log.w("text", "Socket連線=" + e.toString());
//                finish();    //當斷線時自動關閉房間
                MainActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        connectBtn.setEnabled(true);
                        serverIPText.setEnabled(true);
                    }
                });
            }
        }
    };

    @Override
    protected void onDestroy() {            //當銷毀該app時
        super.onDestroy();
        try {
//            json_write=new JSONObject();
//            json_write.put("action","離線");    //傳送離線動作給伺服器
//            Log.i("text","onDestroy()="+json_write+"\n");
            //寫入後送出
            bw.write("end \n");
            bw.flush();
            //關閉輸出入串流後,關閉Socket
            //最近在小作品有發現close()這3個時,導致while (clientSocket.isConnected())這個迴圈內的區域錯誤
            //會跳出java.net.SocketException:Socket is closed錯誤,讓catch內的處理再重複執行,如有同樣問題的可以將下面這3行註解掉
            bw.close();
            br.close();
            clientSocket.close();
//            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.e("text", "onDestroy()=" + e.toString());
        }
    }
}