package com.qiuwenyi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by QiuWenYi on 2017/8/16.
 * "now":{
 *     "tmp":"20"
 *     "cond":{
 *         "txt":"阵雨"
 *     }
 * }
 */

public class Now {

    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }


}
