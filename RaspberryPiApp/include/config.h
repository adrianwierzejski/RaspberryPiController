#ifndef CONFIG_H_INCLUDED
#define CONFIG_H_INCLUDED
#include <iostream>
using namespace std;

const int configSize=6;

class Config{
    public:
    Config();
    Config(string config);
    bool save(int value, int i);
    bool save(string config);
    string toString();
    int baleSize();
    int timeBeltMove();
    int timeBeltRotate();
    int timeNetTwine();
    int sensorProximity();
    int counterNetTwine();
    private:
    int tab[configSize];

};


#endif // CONFIG_H_INCLUDED
