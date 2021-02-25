#include<iostream>
#include <winsock2.h>
#include <Ws2tcpip.h>


using namespace std;

#pragma comment(lib, "ws2_32.lib")


int main()
{
	//設定連接字串
	WSADATA wsaData;
	int iRet = 0;
	iRet = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iRet != 0)
	{
		cout << "WSAStartup(MAKEWORD(2, 2), &wsaData) execute failed!" << endl;
		return -1;
	}
	if (2 != LOBYTE(wsaData.wVersion) || 2 != HIBYTE(wsaData.wVersion))
	{
		WSACleanup();
		cout << "WSADATA version is not correct!" << endl;
		return -1;
	}

	//創建連接字串
	SOCKET clientSocket = socket(AF_INET, SOCK_STREAM, 0);
	if (clientSocket == INVALID_SOCKET)
	{
		cout << "clientSocket = socket(AF_INET, SOCK_STREAM, 0) execute failed!" << endl;
		return -1;
	}

	//初始化服務器端地址設定
	SOCKADDR_IN srvAddr;
	//srvAddr.sin_addr.S_un.S_addr = inet_addr("127.0.0.1");
	inet_pton(AF_INET, "127.0.0.1", &srvAddr.sin_addr);
	srvAddr.sin_family = AF_INET;
	srvAddr.sin_port = htons(6000);

	//連接服務器
	iRet = connect(clientSocket, (SOCKADDR*)&srvAddr, sizeof(SOCKADDR));
	if (0 != iRet)
	{
		cout << "connect(clientSocket, (SOCKADDR*)&srvAddr, sizeof(SOCKADDR)) execute failed!" << endl;
		return -1;
	}

	int x = 0;

	while (true)
	{
		x++;

		// test: 發送字串到服務器
		char sendBuf[100];
		sprintf_s(sendBuf, "Hello, this is client %s %d！", "bunny", x);
		printf(sendBuf);
		printf("\n");
		send(clientSocket, sendBuf, strlen(sendBuf) + 1, 0);

		//接收消息
		char recvBuf[100];
		int sig = recv(clientSocket, recvBuf, 100, 0);
		//沒有 client 連接的時候斷開
		if (sig <= 0)
		{
			printf("Error: Lost connection!\n");
			break;
		}
		printf("%s\n", recvBuf);

		//跳出
		if (x == 20)
		{
			break;
		}

		Sleep(3000);
	}

	//關閉連線
	closesocket(clientSocket);
	WSACleanup();

	system("pause");
	return 0;
}