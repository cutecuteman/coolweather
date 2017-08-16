package com.qiuwenyi.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by QiuWenYi on 2017/8/13.
 */

public class County extends DataSupport {
    private int id;
    private String CountyName;//countyName记录县的名字
    private String weatherId;//weatherId记录县所对应的天气id
    private int cityId;//cityId记录当前县所属市的id值

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return CountyName;
    }

    public void setCountyName(String countyName) {
        CountyName = countyName;
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
