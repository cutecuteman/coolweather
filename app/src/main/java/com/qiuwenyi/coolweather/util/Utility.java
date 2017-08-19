package com.qiuwenyi.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.qiuwenyi.coolweather.db.City;
import com.qiuwenyi.coolweather.db.County;
import com.qiuwenyi.coolweather.db.Province;
import com.qiuwenyi.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by QiuWenYi on 2017/8/14.
 * 由于服务器返回的省市县数据都是JSON格式的，所以我们最好再提供一个工具类解析和处理这种数据
 *
 * 可以看到，我们提供了handleProvinceResponse（），handleCityResponse（）和handleCountyResponse（）这3个方法，
 * 分别用于解析和处理服务器返回的省级，市级和县级数据。处理的方式都是类似的，先使用JSONArray和JSONObject将数据解析出来
 * ，然后组装成实体类对象，在调用save（）方法将数据存储到数据库当中，由于这里的JSON数据结构比较简单，我们就不使用GSON来进行解析了。
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public static Boolean handleProvinceResponse(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i=0;i<allProvinces.length();i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);//先使用JSONArray和JSONObject将数据解析出来
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));//然后组装成实体类对象
                    province.save();//在调用save（）方法将数据存储到数据库当中
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId) {

        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i=0;i<allCities.length();i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i=0;i<allCounties.length();i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSOM数据解析成Weather实体类
     * 解析天气JSON数据的方法
     * 可以看到，handleWeatherResponse（）方法中先是通过JSONObject和JSONArray将天气数据中的主体内容解析出来，即如下内容：
     * {
     *     "status":"ok",
     *     "basic":{},
     *     "aqi":{},
     *     "now":{},
     *     "suggestion":{},
     *     "daily_forecast":{}
     * }
     * 然后由于我们之前已经按照上面的数据格式定义过相应的GSON实体类，因此只需要通过调用fromJson（）方法就能直接将JSON
     * 数据转换成Weather对象了。
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
