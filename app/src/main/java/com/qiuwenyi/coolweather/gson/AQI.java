package com.qiuwenyi.coolweather.gson;

/**
 * Created by QiuWenYi on 2017/8/16.
 *
 * "aqi":{
 *     "city":{
 *         "aqi":"44",
 *         "pm25":"13"
 *     }
 * }
 */

public class AQI {
    public AQICity city;

    public class AQICity {
        public String aqi;
        public String pm25;
    }
}
