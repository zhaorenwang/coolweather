package com.example.r.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * litepal会自动创建表和数据库，直接利用他的借口查询数据
 * Created by Administrator on 2017/12/14 0014.
 */

public class City extends DataSupport {
    private int id;
    private String cityName;
    private int cityCode;
    private int provinceId;

    public int getId() {
        return id;
    }

    public String getCityName() {
        return cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public void setProvinceId(int province) {
        this.provinceId = province;
    }
}
