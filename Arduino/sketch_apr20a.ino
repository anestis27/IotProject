#include <Wire.h>
#include "cc2541.h"
#include <sl868a.h>
#include <Arduino.h>

#define SIGFOX_FRAME_LENGTH 12
int led_status = HIGH;
int flag = 0;
uint8_t datasend[6];
uint8_t size;

struct data {
  float lon;
  float lan;
  float alt;
};


// the setup function runs once when you press reset or power the board
void setup() {
    smeBle.begin(); 
    SigFox.begin(19200);
    SerialUSB.begin(115200);
    SerialUSB.println("aaa");
}



// the loop function runs over and over again forever
void loop() {
    static unsigned loop_cnt = 0;
    unsigned int altitude = 0;
    unsigned char lockedSatellites = 0;
    double latitude = 0;
    double longitude = 0;
    // UTC, speed and course here below
    unsigned int utc_hour =0;
    unsigned int utc_min = 0;
    unsigned int utc_sec = 0;
    unsigned char utc_secDecimals = 0;
    unsigned char utc_year =0;
    unsigned char utc_month =0;
    unsigned char utc_dayOfMonth =0;
    double speed_knots =0;
    double course = 0;
    sl868aCachedDataT data;
    delay(5);

    if (smeGps.ready()) {
#ifdef ARDUINO_SAMD_SMARTEVERYTHING
        ledGreenLight(HIGH);
#endif

        altitude   = smeGps.getAltitude();
        latitude   = smeGps.getLatitude();
        longitude  = smeGps.getLongitude();
        lockedSatellites = smeGps.getLockedSatellites();
        // additional new getters
        utc_hour = smeGps.getUtcHour();
        utc_min = smeGps.getUtcMinute();
        utc_sec = smeGps.getUtcSecond();
        //utc_secDecimals = smeGps.getUtcSecondDecimals();
        utc_year = smeGps.getUtcYear();
        utc_month = smeGps.getUtcMonth();
        utc_dayOfMonth = smeGps.getUtcDayOfMonth();
        //speed_knots = smeGps.getSpeedKnots();
        //course = smeGps.getCourse();
        // get raw cached data as a sl868aCachedDataT struct
        data = smeGps.getData();

        if ((loop_cnt % 200) == 0) {
            SerialUSB.println(" ");
            SerialUSB.print("Latitude    =  ");
            SerialUSB.println(latitude, 6);
            SerialUSB.print("Longitude   =  ");
            SerialUSB.println(longitude, 6);
            SerialUSB.print("Altitude    =  ");
            SerialUSB.println(altitude, DEC);
            SerialUSB.print("Satellites  =  ");
            SerialUSB.println(lockedSatellites, DEC);
                       SerialUSB.print("Hour  =  ");
            SerialUSB.println(data.utc_hour, DEC);
            SerialUSB.print("Minute  =  ");
            SerialUSB.println(data.utc_min, DEC);
            SerialUSB.print("Second  =  ");
            SerialUSB.print(data.utc_sec, DEC);
            SerialUSB.print(".");
            SerialUSB.println(data.utc_sec_decimals, DEC);
            //print date & time
            SerialUSB.print("Date  =  ");
            SerialUSB.print(data.utc_year, DEC);
            SerialUSB.print("/");
            SerialUSB.print(data.utc_month, DEC);
            SerialUSB.print("/");
            SerialUSB.print(data.utc_dayOfMonth, DEC);
            SerialUSB.println();
            
            SerialUSB.print("Speek (knots) = ");
            SerialUSB.println(speed_knots, DEC);
            SerialUSB.print("Course = ");
            SerialUSB.println(course, DEC);


            if(flag==0){
              data f;
              f.lan = latitude;
              f.lon =longitude
              f.alt = altitude;
              bool answer = sendSigfox(&f, sizeof(data));
              flag=1;
            }
        }
    } else {
        if ((loop_cnt % 200) == 0) {
            SerialUSB.println("Locking GPS position...");
             smeBle.writeChar(0xab);
            if (led_status == LOW) {
                led_status = HIGH;
            } else {
                led_status = LOW;
            }
        }
    }

    loop_cnt++;
#ifdef ARDUINO_SAMD_SMARTEVERYTHING
    ledGreenLight(led_status);
#endif
}


String getSigfoxFrame(const void* data, uint8_t len){
  String frame = "";
  uint8_t* bytes = (uint8_t*)data;
  
  if (len < SIGFOX_FRAME_LENGTH){
    //fill with zeros
    uint8_t i = SIGFOX_FRAME_LENGTH;
    while (i-- > len){
      frame += "00";
    }
  }

  //0-1 == 255 --> (0-1) > len
  for(uint8_t i = len-1; i < len; --i) {
    if (bytes[i] < 16) {frame+="0";}
    frame += String(bytes[i], HEX);
  }
  
  return frame;
}

bool sendSigfox(const void* data, uint8_t len){
  String frame = getSigfoxFrame(data, len);
  String status = "";
  char output;
  if (DEBUG){
    SerialUSB.print("AT$SF=");
    SerialUSB.println(frame);
  }
  SigFox.print("AT$SF=");
  SigFox.print(frame);
  SigFox.print("\r");
  while (!SigFox.available());
  
  while(SigFox.available()){
    output = (char)SigFox.read();
    status += output;
    delay(10);
  }
  if (DEBUG){
    SerialUSB.print("Status \t");
    SerialUSB.println(status);
  }
  if (status == "OK\r"){
    //Success :)
    return true;
  }
  else{
    return false;
  }
}
