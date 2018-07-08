#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <pthread.h>

#include <iostream>

using namespace std;
string receiveS(int sock)
{
    char received_char[1024];
    int recvOut=recv(sock,received_char,1024,0);
    if(recvOut==0){
        return "-1";
    }else if(recvOut==-1){
        perror("recv");
        return "-2";
    }
    return string(received_char);
}
bool sendS(int sock, string message){

    message += "\r\n";
    int sendOut = send(sock, message.c_str(), message.size(), 0);
    if(sendOut == -1){
        perror("send");
        return false;
    }else{
        int sentChars = sendOut;
        while(sentChars!=message.size()){
            sendOut = send(sock, message.substr(sentChars,message.size()-sentChars) .c_str(), message.size()-sentChars, 0);
            if(sendOut != -1){
                sentChars +=sendOut;
            }else{
                perror("send");
                return false;
                break;
            }
        }
    }
    return true;
}
bool setServerSocket(int &sock)
{
    bool success=true;
    struct sockaddr_in sin;
    unsigned short port = 1234;

    if ((sock = socket(PF_INET, SOCK_STREAM, 0)) == -1) {
        perror("socket");
        success=false;
    }
    sin.sin_family = AF_INET;
    sin.sin_port = htons(port);
    sin.sin_addr.s_addr = htonl(INADDR_ANY);

    int enable=1;
    //zgub komunikat "Address already in use"
    if (setsockopt(sock,SOL_SOCKET,SO_REUSEADDR,&enable,sizeof(int)) == -1) {
        perror("setsockopt(SO_REUSEADDR) failed");
        success=false;
    }
    if (bind(sock, (struct sockaddr*) &sin, sizeof(sin)) != 0) {
        perror("bind");
        success=false;
    }
    if (listen(sock, SOMAXCONN) != 0) {
        perror("listen");
        success=false;
    }
    return success;

}
void setTimeout(int &sock,timeval timeout){
    if (setsockopt (sock, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout,sizeof(timeout)) < 0)
        perror("setsockopt recv failed\n");
    if (setsockopt (sock, SOL_SOCKET, SO_SNDTIMEO, (char *)&timeout,sizeof(timeout)) < 0)
        perror("setsockopt send failed\n");
}
