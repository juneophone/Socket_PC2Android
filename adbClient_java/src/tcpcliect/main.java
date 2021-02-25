package tcpcliect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class main {

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			Runtime.getRuntime().exec("adb forward tcp:6000 tcp:8000");
		
			// 可以再执行adb forward --list解析一下结果判断是否转发成功
			Process process;
		
			process = Runtime.getRuntime().exec("adb forward --list");
		
			DataInputStream dis = new DataInputStream(process.getInputStream());
			byte[] buf = new byte[8];
			int len = -1;
			StringBuilder sb = new StringBuilder();
		
			while ((len = dis.read(buf)) != -1) {
			    String str = new String(buf, 0, len);
			    sb.append(str);
			}
		
		String adbList = sb.toString().toString();
		System.out.println("adb forward list=" + adbList);
		String[] forwardArr = adbList.split("\n");
		for(String forward: forwardArr) {
			System.out.println("forward=" + forward);
//		    if(forward.contains(localPort) && forward.contains(serverPort)) {
//		        mForwardSuccess = true;
//		    }
		}
		String msg = "Hello, this is a message sent by TCP/IP Client.";
		//if(!mForwardSuccess) return;
		//转发成功则建立socket
		Socket mSocket;
		
			mSocket = new Socket("127.0.0.1", 6000);
		
		
		byte[] buffer = new byte[256];
		DataOutputStream dos = new DataOutputStream(mSocket.getOutputStream());
		// 发送数据
		System.out.println("Client:" + msg);
		dos.write(msg.getBytes("UTF-8"));
		dos.flush();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
