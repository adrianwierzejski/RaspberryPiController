#ifndef SERVER_H
#define SERVER_H

#include <thread>
#include <mutex>
#include <stdlib.h>

#include "config.h"
#include "socket.h"
using namespace std;
const int mcp3008Size=8;
const int sleepTime=500000; // Czas uspania procesu w MicroSekundach
const int secondToMicro=1000000;
const char ServerLog[]="Proces Klienta :";


class Server{

    private:
        timeval timeout;
        bool interrupted;
        bool alive;
        mutex mInterrupt;
        thread *client;
        int sockServer;
        int sockClient;
        int data[mcp3008Size];
        Config settings;
        bool settingsUpdated;
        bool dataUpdated;
        mutex mData;
        mutex mSettings;
        void run();
        string readSettingsString();
        public:
        static const int sGetData = 1;
        static const int sGetConfig = 2;
        static const int sSaveConfig = 3;
        static const int sEndConnection = 4;
        static const int sClosedConnection = 5;
        static const int sUnknownCommand = 6;
        Server(Config config);
        virtual ~Server();
        bool isInterrupted();
        void interupt();
        bool isAlive();
        string readData();
        bool isSettingsUpdated();
        bool isDataUpdated();
        void saveData(int data[]);
        Config readSettings();
        void saveSettings(string settings);



};
#endif // SERVER_H
