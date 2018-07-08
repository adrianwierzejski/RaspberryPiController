#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <fstream>

#include <wiringPi.h>
#include <mcp3004.h>

using namespace std;
const int dataSize=9;
//const int mcp3008Size=8;
const int BASE=100;
const int SPI_CHAN=0;
const int CEWKA_1=37;
const int CEWKA_2=35;
const int CEWKA_3=33;
const int CEWKA_4=31;
const int CEWKA_5=29;
const int CEWKA_6=27;
//const int sleepTime=500000; // Czas uspania procesu w MicroSekundach
//const int secondToMicro=1000000;
const string filePath="/mnt/mmcblk0p3/PrasaSerwer.config";
const string defaultConfig="600 15 60 20 300 10";
const char MainLog[]="Watek glowny :";

#include "config.h"
#include "server.h"

void startPI(){
    wiringPiSetupPhys();
    mcp3004Setup(BASE, SPI_CHAN);
    pinMode(CEWKA_1,OUTPUT);
    pinMode(CEWKA_2,OUTPUT);
    pinMode(CEWKA_3,OUTPUT);
    pinMode(CEWKA_4,OUTPUT);
    digitalWrite(CEWKA_1,HIGH);
    digitalWrite(CEWKA_2,HIGH);
    digitalWrite(CEWKA_3,HIGH);
    digitalWrite(CEWKA_4,HIGH);

}
void updateData(int data[]){
    for(int i=0; i<mcp3008Size; i++)
        data[i]=analogRead (BASE+i);
}
void print(int tab[]){
    for(int i=0; i<dataSize; i++){
        cout<<tab[i]<<" ";
    }cout<<endl;}
void wrap(Config settings, int data[], Server &client)
{
    time_t startTime;
    updateData(data);
    int counter=0;
    if(data[5]<settings.baleSize())
        return;
    //Rozpoczecie przesuwania tasmy
    printf("%sZaczynam przesuwac tasme\n",MainLog);
    time(&startTime);
    digitalWrite(CEWKA_1,LOW);
    usleep(sleepTime);
    updateData(data);
    client.saveData(data);
    //zakonczenie przesuwania tasmy
    while(difftime(time(NULL),startTime)<settings.timeBeltMove() && data[0]<settings.sensorProximity())
    {
        printf("%sPrzesuwam %f\n",MainLog,difftime(time(NULL),startTime));
        usleep(sleepTime);
        updateData(data);
        client.saveData(data);
    }
    digitalWrite(CEWKA_1,HIGH);
    printf("%sKoncze\n",MainLog);
    //rozpoczecie podawania sznurka/siatki obrot tasmy
    time(&startTime);
    digitalWrite(CEWKA_3,LOW);
    digitalWrite(CEWKA_4,LOW);
    printf("%sPuszczam siatke i tasme w obroty\n",MainLog);
    int net=data[2],twine=data[3];
    while(difftime(time(NULL),startTime)<settings.timeNetTwine() && counter< settings.counterNetTwine()){
        usleep(sleepTime);
        updateData(data);
        client.saveData(data);
        if(abs(data[2]-net)>settings.sensorProximity() || abs(data[3]-twine)>settings.sensorProximity()){
            net = data[2];
            twine = data[3];
            counter++;
            printf("%sLicze\n",MainLog);
        }
    }
    digitalWrite(CEWKA_4,HIGH);
    printf("%sWylaczam siatke\n",MainLog);
    while(difftime(time(NULL),startTime)<settings.timeBeltRotate()){
        printf("%sCzekam na tasme %f\n",MainLog,difftime(time(NULL),startTime));
        usleep(sleepTime);
        updateData(data);
        client.saveData(data);
    }
    digitalWrite(CEWKA_3,HIGH);
    printf("%sWylaczam obracanie tasmy\n",MainLog);

    //Rozpoczecie skladania tasmy
    time(&startTime);
    digitalWrite(CEWKA_2,LOW);
    printf("%sSkladam tasme\n",MainLog);
    //zakonczenie skladania tasmy
    while(difftime(time(NULL),startTime)<settings.timeBeltMove() && data[1]<settings.sensorProximity()){
        usleep(sleepTime);
        updateData(data);
        client.saveData(data);
    }
    digitalWrite(CEWKA_2,HIGH);
    printf("%sKoncze skladac\n",MainLog);
    while(tab[4]>settings.sensorProximity()){
        printf("%sCzekam na otwarcie klapy\n",MainLog);
        usleep(sleepTime);
        updateData(data);
        client.saveData(data);
    }
    updateData(data);
    data[dataSize-1]++;
    client.saveData(data);
    printf("%sZakonczono owijanie\n",MainLog);
}
        /**
        tDane
        0 - Taśma tył
        1 - Taśma przód
        2 - Siatka
        3 - Sznurek
        4 - Klapa
        5 - Środek
        6 - Lewa
        7 - Prawa
        */
int main()
{
    Config settings;
    ifstream fileReadSettings(filePath);
    string file="";
    getline(fileReadSettings,file);
    fileReadSettings.close();
    printf("%sOdczytana konfiguracja :%s\n",MainLog,file.c_str());
    if(!settings.save(file)){
        settings.save(defaultConfig);
        printf("%sWczytano domyslna konfiguracje.\n",MainLog);
    }else{
        printf("%sWczytano konfiguracje z pliku.\n",MainLog);
    }
    startPI();
    int dataM[dataSize];
    for(int i=0; i<dataSize; i++)
        dataM[i]=0;
    Server client(settings);
    while(true){
        if(!client.isAlive())
            printf("%sProblem z serwerwem.\n",MainLog);
        updateData(dataM);
        client.saveData(dataM);
        print(dataM);
        if(client.isSettingsUpdated()){
            printf("%sAktualizuje ustawienia.\n",MainLog);
            settings = client.readSettings();
            ofstream fileSaveSettings(filePath);
            fileSaveSettings<<settings.toString();
            fileSaveSettings.close();
        }
        wrap(settings,dataM,client);
        usleep(sleepTime*10);
    }
    client.interupt();
    return 0;
}
