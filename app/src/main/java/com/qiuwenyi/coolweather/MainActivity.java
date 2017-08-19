package com.qiuwenyi.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
/**
 * 可以看到这里在onCreate（）方法的开始先从SharePreferences文件中读取缓存数据，
 * 如果不为null就说明之前已经请求过数据了，那么就没必要让用户再次选择城市，
 * 而是直接跳转到WeatherActivity即可。
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("weather", null) != null) {
            Intent intent = new Intent(this, WeatherActivity.class);
            startActivity(intent);
            finish();

        }
    }
}
