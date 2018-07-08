#include "config.h"

Config::Config(){
    for(int i=0; i<configSize; i++)
        tab[i]=0;
}
Config::Config(string config){
    for(int i=0; i<configSize; i++)
        tab[i]=0;
    save(config);
}
bool Config::save(int value, int i){
    if(i<configSize){
        tab[i]=value;
        return true;
    }
    else
        return false;
}
bool Config::save(string config){
    bool success = true;
    int result[configSize];
    printf(config.c_str());
    int i_old= 0, j= 0, i= config.find(" ");
    while(i!=string::npos && j<configSize){
        string text = config.substr(i_old,i-i_old);
        int tmp = atoi(text.c_str());
        if(tmp >0){
            result[j] = tmp;
            j++;
        }else {
            success = false;
            break;
        }
        i_old = i+1;
        i = config.find(" ",i_old);
    }

    string text = config.substr(i_old,config.size()-i_old);
    int tmp = atoi(text.c_str());
    if(tmp>0 && j<configSize && success){
        result[j] = tmp;
    }else success = false;
    for(int i=0; i<configSize && success; i++)
        tab[i]=result[i];
    return success;
}
string Config::toString(){
    string result=to_string(tab[0]);
    for(int i=1; i<configSize; i++)
        result+= " "+to_string(tab[i]);
    return result;
}
int Config::baleSize(){
    return tab[0];
}
int Config::timeBeltMove(){
    return tab[1];
}
int Config::timeBeltRotate(){
    return tab[2];
}
int Config::timeNetTwine(){
    return tab[3];
}
int Config::sensorProximity(){
    return tab[4];
}
int Config::counterNetTwine(){
    return tab[5];
}
