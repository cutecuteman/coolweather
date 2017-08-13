package com.qiuwenyi.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by QiuWenYi on 2017/8/13.
 */

public class Province extends DataSupport {//另外，LitePal中的每一个实体类都是必须继承自DataSupport类的。
    private int id;//是每个实体类中都应该有的字段
    private String provinceName;//记录省的名字
    private int provinceCode;//记录省的代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
