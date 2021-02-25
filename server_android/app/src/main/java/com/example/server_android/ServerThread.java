package com.example.server_android;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ServerThread extends Thread {

    private final static String TAG = "ServerThread";

    private Context ctx = null;
    private static ServerSocket serverSocket = null;
    private Thread clientCloseThread = null;
    private Runnable clientCloseRunnable = null;
    //    private static ArrayList<Socket> socketlist = new ArrayList<Socket>();
    private static ArrayList<SocketInfo> socketlist = new ArrayList<SocketInfo>();


    public static class SocketInfo {
        public String socketID;
        public Socket socket;
    }

    public ServerThread(Context context, ServerSocket socket)
    {
        super();
        // Save the activity
        ctx = context;
        serverSocket = socket;

        clientCloseRunnable = new Runnable(){    //讓執行緒每兩秒判斷一次SocketList內是否有客戶端強制斷線
            @Override
            public void run() {                                //在此抓取的是關閉wifi等斷線動作
                // TODO Auto-generated method stub
                try {
                    while(true){
                        Thread.sleep(2000);                    //每兩秒執行一輪
                        for(SocketInfo client:socketlist){

                            if(isServerClose(client.socket))        //當該客戶端網路斷線時,從SocketList剔除
                                socketlist.remove(client);
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
    }

    @Override
    public void run()
    {
        try
        {
            // 判斷socketlist內有沒有客戶端網路斷線
            clientCloseThread = new Thread(clientCloseRunnable);
            clientCloseThread.start();

            // 當Server運作中時
            Log.i(TAG, "Waiting for client connection…");
            while (!serverSocket.isClosed()) {
                // 呼叫等待接受客戶端連接
                waitNewSocket(serverSocket);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Boolean isServerClose(Socket socket){    //判斷連線是否中斷
        try{
            socket.sendUrgentData(0);        //發送一個字節的緊急數據,默認情況下是沒有開啟緊急數據處理,不影響正常連線
            return false;                    //如正常則回傳false
        }catch(Exception e){
            return true;                      //如連線中斷則回傳true
        }
    }

    // 等待接受客戶端連接
    public static void waitNewSocket(ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();
            // 呼叫創造新的使用者
            createNewThread(socket);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 創造新的使用者
    public static void createNewThread(final Socket socket) {
        // 以新的執行緒來執行
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "add new client");
                    // 增加新的使用者
                    SocketInfo clientInfo = new SocketInfo();
                    clientInfo.socketID = genID(10);
                    clientInfo.socket = socket;
                    Log.d(TAG,"Client ID: "+clientInfo.socketID);

                    socketlist.add(clientInfo);
                    // 取得網路輸入串流
                    BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //取得網路輸出串流
                    BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));

//                    InputStream input = socket.getInputStream();
//                    BufferedInputStream bis = new BufferedInputStream(input);
//                    byte[] b = new byte[1024];
//                    int len = -1;
//                    while ((len = bis.read(b)) != -1) {
//                        Log.e(TAG,new String(b, 0, len, "UTF-8"));
//                    }
//                    socket.shutdownInput();

                    String tmp;
                    // 當Socket已連接時連續執行
                    while (socket.isConnected()) {
                        tmp = br.readLine();        //宣告一個緩衝,從br串流讀取值
                        // 如果不是空訊息
                        if(tmp!=null){


                            //從客戶端取得值後做拆解,可使用switch做不同動作的處理與回應
                            Log.i(TAG, "receive from " + socket.getInetAddress().getHostAddress() + ": " + tmp);

                            // 收到訊息後送給其他人
//                            bw.write("Receive :"+tmp + "\n");
//                            bw.flush();
                            sendMessageToOther(clientInfo.socketID, tmp);
                        }else{    //在此抓取的是使用使用強制關閉app的客戶端(會不斷傳null給server)
                            //當socket強制關閉app時移除客戶端
                            socketlist.remove(socket);
                            break;    //跳出迴圈結束該執行緒
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        // 啟動執行緒
        t.start();
    }

    // 關閉連接
    public static void stopSocketSever() {
        try {
            serverSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private static void sendMessageToOther(String from, BufferedReader message)
//    {
//        for(SocketInfo client:socketlist){
//
//            if(client.socketID.equals(from))
//            {
//                continue;
//            }
//            //取得網路輸出串流
//            try {
//                BufferedWriter bw = new BufferedWriter( new OutputStreamWriter(client.socket.getOutputStream()));
////                DataOutputStream bw = new DataOutputStream(new BufferedOutputStream(client.socket.getOutputStream()));
//
//                bw.write(message);
//                bw.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    private static void sendMessageToOther(String from, String message) {
        for (SocketInfo client : socketlist) {
            //將發送端的IP排除不要 receive
            if (client.socketID.equals(from)) {
                continue;
            }
            //取得網路輸出串流
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(client.socket.getOutputStream()));
//                Log.e(TAG,"Receive Start");
                bw.write(message + "\n");
                bw.flush();
//                Log.e(TAG,"Receive End");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 產生ID
    private static String genID(int len){
        String charArr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String result = "";

        for(int i=0;i<len;i++)
        {
            result += charArr.charAt((int)Math.floor(Math.random() * charArr.length()));
        }

        return result;
    }
}