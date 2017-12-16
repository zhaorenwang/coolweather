package com.example.r.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2017/12/15 0015.
 */

public class Now {
    @SerializedName("tmp")//没有隐射的先建立映射关系再声明
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {

        @SerializedName("txt")
        public String info;
    }
}
