package com.qiuwenyi.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by QiuWenYi on 2017/8/13.
 */

public class Country extends DataSupport {
    private int id;
    private String CountryName;//countryName记录县的名字
    private String weatherId;//weatherId记录县所对应的天气id
    private int cityId;//cityId记录当前县所属市的id值

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return CountryName;
    }

    public void setCountryName(String countryName) {
        CountryName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
