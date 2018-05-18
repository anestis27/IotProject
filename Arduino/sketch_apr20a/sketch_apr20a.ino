#include <Wire.h>

#include <sl868a.h>
#include <Arduino.h>
#include "cc2541.h"
 #include <avr/dtostrf.h>

int led_status = HIGH;

// the setup function runs once when you press reset or power the board
void setup() {
    SerialUSB.begin(115200);
    smeGps.begin();
    smeBle.begin();
}


// the loop function runs over and over again forever
void loop() {
    static unsigned loop_cnt = 0;
    unsigned int altitude = 0;
    double latitude = 0;
    double longitude = 0;
    delay(5);

    if (smeGps.ready()) {
/*#ifdef ARDUINO_SAMD_SMARTEVERYTHING
        ledGreenLight(HIGH);
#endif*/
        if ((loop_cnt % 2000) == 0) {
            altitude   = smeGps.getAltitude();
            latitude   = smeGps.getLatitude();
            longitude  = smeGps.getLongitude();
            SerialUSB.println(" ");
            SerialUSB.print("Latitude    =  ");
            SerialUSB.println(latitude, 6);
            SerialUSB.print("Longitude   =  ");
            SerialUSB.println(longitude, 6);
            SerialUSB.print("Altitude    =  ");
            SerialUSB.println(altitude, DEC);

           String latitudeString = double2string(latitude,10000000);
            String longitudeString = double2string(longitude,10000000);
            String altitudeString = double2string(altitude,10000000);
            char latitudechar[latitudeString.length()+1];
            char longitudechar[longitudeString.length()+1];
            char altitudechar[altitudeString.length()+1];
            (latitudeString).toCharArray(latitudechar,sizeof(latitudechar)); 
            (longitudeString).toCharArray(longitudechar,longitudeString.length()); 
            (altitudeString).toCharArray(altitudechar,altitudeString.length()); 
            String allthedatastring = latitudeString + "=" +longitudeString;
            char allthedatachar[allthedatastring.length()];
            allthedatastring.toCharArray(allthedatachar,allthedatastring.length()); 
            smeBle.write(allthedatachar,sizeof(allthedatachar));
            //smeBle.write(longitudechar,sizeof(longitudechar));
            //smeBle.write(altitudechar,sizeof(altitudechar));
            //smeBle.write(allthedatachar,sizeof(allthedatachar));
            Serial.println(allthedatachar);
        }
    } else {
        if ((loop_cnt % 1000) == 0) {
           SerialUSB.println("Locking GPS position..."); 
           char error[]="e";
           smeBle.write(error,sizeof(error));
        }
    }

    loop_cnt++;
/*#ifdef ARDUINO_SAMD_SMARTEVERYTHING
    ledGreenLight(led_status);
#endif*/
}


String double2string( double val, unsigned int precision) {
 
  String str = String(int(val));  //prints the int part
  str += "."; // print the decimal point
  unsigned int frac;
  if (val >= 0)
    frac = (val - int(val)) * precision;
  else
    frac = (int(val) - val ) * precision;
  int frac1 = frac;
 
  while ( frac1 /= 10 )
    precision /= 10;
  precision /= 10;
 
  while (  precision /= 10)
    str += "0";
 
  str += String(frac, DEC) ;
 
  return str;
}


