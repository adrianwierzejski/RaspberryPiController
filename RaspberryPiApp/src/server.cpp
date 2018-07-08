#include "server.h"
#include "socket.h"
#include "config.h"


Server::Server(Config settings){
    timeout.tv_sec = 20;
    timeout.tv_usec = 0;
    this->interrupted = false;
    this->alive = true;
    this->settings=Config(settings.toString());
    settingsUpdated = false;
    dataUpdated = false;
    for(int i=0; i<mcp3008Size; i++)
        data[i]=0;
    client = new thread(&Server::run,this);
    client -> detach();
}
Server::~Server()
{
}
bool Server::isInterrupted(){
    mInterrupt.lock();
    bool result = interrupted;
    mInterrupt.unlock();
    return result;
}
void Server::interupt(){
    mInterrupt.lock();
    interrupted = true;
    mInterrupt.unlock();
}
bool Server::isAlive(){
    mInterrupt.lock();
    bool result = alive;
    mInterrupt.unlock();
    return result;
}
string Server::readData(){
    mData.lock();
    string result=to_string(data[0]);
    for(int i=1; i<mcp3008Size-1; i++)
        result += " " + to_string(data[i]);
    dataUpdated = false;
    mData.unlock();
    return result;
}
bool Server::isSettingsUpdated(){
    mSettings.lock();
    bool result = settingsUpdated;
    mSettings.unlock();
    return result;
}
bool Server::isDataUpdated(){
    mData.lock();
    bool result = dataUpdated;
    mData.unlock();
    return result;
}
void Server::saveData(int data[]){
    mData.lock();
    for(int i=0; i<mcp3008Size; i++)
        this->data[i] = data[i];
    dataUpdated = true;
    mData.unlock();
}
Config Server::readSettings(){
    mSettings.lock();
    settingsUpdated=false;
    Config result(settings.toString());
    mSettings.unlock();
    return result;
}
void Server::saveSettings(string settings){
    mSettings.lock();
    this->settings.save(settings);
    settingsUpdated=true;
    mSettings.unlock();
}
string Server::readSettingsString(){
    mSettings.lock();
    string result=settings.toString();
    mSettings.unlock();
    return result;
}
void Server::run(){
    printf("%sUruchamiam\n",ServerLog);
    if(!setServerSocket(sockServer)){
        interupt();
        printf("%sBlad funkcji setServerSocket\n",ServerLog);
    }
    while(!Server::isInterrupted()){
        printf("%sOczekiwanie na klienta\n",ServerLog);
        if ((sockClient = accept(sockServer, NULL, NULL)) == -1) {
            perror("accept");
            break;
        }
        setTimeout(sockClient,timeout);
        while(true){
            printf("%sCzekam na dane od glownego watku\n",ServerLog);
            for(float i=0; !Server::isDataUpdated() && i<timeout.tv_sec/2; i+=(float)sleepTime/secondToMicro){
            usleep(sleepTime);
            }

            string received = receiveS(sockClient);

            string response="";
            switch(atoi(received.substr(0,2).c_str())){
            case sGetData:
                response = sGetData +" "+readData();
                break;
            case sGetConfig:
                response = sGetConfig +" "+readSettingsString();
                break;
            case sSaveConfig:
                saveSettings(received.substr(2));
                printf("%sZapisuje odebraną konfiguracje :%s",ServerLog,received);
                response = sSaveConfig + " ";
                break;
            case sEndConnection:
                response = sEndConnection + " ";
                break;
            case sUnknownCommand:
                response = sUnknownCommand + " ";
                printf("%sNieznana komenda :%s",ServerLog,received);
                break;
            case 0:
                response = sUnknownCommand + " ";
                printf("%sBlad funkcji atoi. Otrzymana komenda :%s",ServerLog,received);
                break;
            case -1:
                printf("%sKlient zerwal polaczenie.",ServerLog);
                break;
            case -2:
                printf("%sBlad funkcji recv.",ServerLog);
                break;
            }
            if(response == "")
                break;
            if(!sendS(sockClient,response)){
                printf("%sBlad funkcji: send.",ServerLog);
                break;
            }
        }
        close(sockClient);
    }
    close(sockServer);
    printf("%sZamykanie procesu\n",ServerLog);
    delete client;
    client = NULL;
    mInterrupt.lock();
    alive = false;
    mInterrupt.unlock();
}

