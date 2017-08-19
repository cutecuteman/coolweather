package com.qiuwenyi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 *
 * {
 *     "HeWeather":{
 *         {
 *             "status":"ok",
 *             "basic":{},
 *             "aqi":{},
 *             "now":{},
 *             "suggestion",{},
 *             "daily_forecast":{}
 *         }
 *     }
 * }
 *
 * Created by QiuWenYi on 2017/8/16.
 * 把basic，aqi，now，suggestion，和daily_forecast对应的实体类全部都创建好了，接下来还需要再创建一个总的实体类
 * 来引用刚才创建的各个实体类，在gson包下新建一个weather类
 * 在Weather 类中，我们对Basic，AQI，Now，Suggestion，Forecast类进行了引用， 其中，由于daily_forecast中包含的是一个数组，
 * 因此这里使用了List集合来引用Forecast类，另外，返回的天气数据中包含一项status数据，成功返回OK，失败则返回具体原因，那么这里
 * 也需要添加一个对应的status字段
 */

public class Weather {

    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;


}
