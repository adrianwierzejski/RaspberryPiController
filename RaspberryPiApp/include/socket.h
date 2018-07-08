#ifndef SOCKET_H_
#define SOCKET_H_

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <iostream>

using namespace std;

string receiveS(int sock);
bool sendS(int sock, string message);
bool setServerSocket(int &sock);
void setTimeout(int &sock,timeval timeout);

#endif // SOCKET_H_
