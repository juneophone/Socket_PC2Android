
#include<iostream>
#include<WinSock2.h>
#include <Ws2tcpip.h>

#define CONNECT_NUM_MAX 10

#pragma comment(lib, "ws2_32.lib")


using namespace std;


int main()
{
	//載入連接字串
	WSADATA wsaData;
	int iRet = 0;
	iRet = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iRet != 0)
	{
		cout << "WSAStartup(MAKEWORD(2, 2), &wsaData) execute failed!" << endl;;
		return -1;
	}
	if (2 != LOBYTE(wsaData.wVersion) || 2 != HIBYTE(wsaData.wVersion))
	{
		WSACleanup();
		cout << "WSADATA version is not correct!" << endl;
		return -1;
	}

	//建立連接字串
	SOCKET serverSocket = socket(AF_INET, SOCK_STREAM, 0);
	if (serverSocket == INVALID_SOCKET)
	{
		cout << "serverSocket = socket(AF_INET, SOCK_STREAM, 0) execute failed!" << endl;
		return -1;
	}

	//初始化服務器地址設定
	SOCKADDR_IN addrSrv;
	addrSrv.sin_addr.S_un.S_addr = htonl(INADDR_ANY);
	addrSrv.sin_family = AF_INET;
	addrSrv.sin_port = htons(6000);

	//綁定
	iRet = bind(serverSocket, (SOCKADDR*)&addrSrv, sizeof(SOCKADDR));
	if (iRet == SOCKET_ERROR)
	{
		cout << "bind(serverSocket, (SOCKADDR*)&addrSrv, sizeof(SOCKADDR)) execute failed!" << endl;
		return -1;
	}


	//監聽
	iRet = listen(serverSocket, CONNECT_NUM_MAX);
	if (iRet == SOCKET_ERROR)
	{
		cout << "listen(serverSocket, 10) execute failed!" << endl;
		return -1;
	}
	else
	{
		cout << "Server is listening on " << addrSrv.sin_addr.S_un.S_addr << ":" << addrSrv.sin_port << "." << endl;
	}

	//等待連接_接收/發送 訊息
	SOCKADDR_IN clientAddr;
	int len = sizeof(SOCKADDR);
	while (1)
	{
		SOCKET connSocket = accept(serverSocket, (SOCKADDR*)&clientAddr, &len);
		if (connSocket == INVALID_SOCKET)
		{
			cout << "accept(serverSocket, (SOCKADDR*)&clientAddr, &len) execute failed!" << endl;
			return -1;
		}
		else
		{
			cout << "Connection established! Waiting for messages." << endl;
		}

		int x = 0;

		while (true)
		{
			//接收訊息
			char recvBuf[1024];
			int sig = recv(connSocket, recvBuf, 1024, 0);
			//沒有連接的時候斷線
			if (sig <= 0)
			{
				printf("Error: Lost connection!\n");
				break;
			}
			printf("%s\n", recvBuf);

			//發送訊息
			char sendBuf[100];
			//sprintf_s(sendBuf, "Welcome %s", inet_ntoa(clientAddr.sin_addr));
			char str[INET_ADDRSTRLEN];
			sprintf_s(sendBuf, "Welcome! client from %s %d!", inet_ntop(AF_INET, &clientAddr.sin_addr, str, sizeof(str)), x);
			send(connSocket, sendBuf, strlen(sendBuf) + 1, 0);
			x++;
		}

		//關閉連接
		closesocket(connSocket);
	}

	system("pause");
	return 0;
}
