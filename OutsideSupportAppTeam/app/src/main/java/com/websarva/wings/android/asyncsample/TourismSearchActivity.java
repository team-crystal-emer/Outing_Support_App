package com.websarva.wings.android.asyncsample;

/*
 * 参考にしたサイト、サンプルコード
 * ImplicitIntentSample
 * 「スパン」 https://developer.android.com/guide/topics/text/spans?hl=ja SpannableString使用の際に参考
 */

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.TimeZone;

public class TourismSearchActivity extends AppCompatActivity {

    //部品グローバル変数
    TextView search_result_str;
    View border_line2, border_line3;
    TextView result_temp_str, weather_info_str, map_info_str, result_map_str;
    ImageView result_temp_image;
    ImageButton map_button;
    Button back_button;
    //文字列変数
    String result_place, resultWord;
    Switch inside_switch;

    //画面作成
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tourism_search);

        //使用する部品のidを取得
        search_result_str = findViewById(R.id.search_result);
        border_line2 = findViewById(R.id.line2);
        border_line3 = findViewById(R.id.line3);
        result_temp_str = findViewById(R.id.result_temp);
        result_temp_image = findViewById(R.id.result_temp_image);
        weather_info_str = findViewById(R.id.weather_info_str);
        map_info_str = findViewById(R.id.map_info_str);
        map_button = findViewById(R.id.map_button);
        back_button = findViewById(R.id.back_button);
        result_map_str = findViewById(R.id.result_map);
        inside_switch = findViewById(R.id.inside);

        //最初は検索以外画面を消したいのでinvisibleにする(setVisibility関数でまとめて処理)
        setVisibility(View.INVISIBLE, search_result_str, border_line2, border_line3, result_temp_str, result_temp_image, weather_info_str, map_info_str, map_button, result_map_str, inside_switch);

        //ボタン変数
        ImageButton search_Button = findViewById(R.id.searchbutton);
        EditText search_edit_text = findViewById(R.id.searchedittext);

        //検索ボタンが押されたときのイベントリスナーを登録
        search_Button.setOnClickListener(v -> {
            result_place = search_edit_text.getText().toString();
            search_result();
        });

        //地図ボタンが押されたときの動作を登録
        map_button.setOnClickListener(v -> {
            try {//室内ボタンをonにしたら
                if(inside_switch.isChecked() == true){
                    resultWord = URLEncoder.encode(result_place, "UTF-8") + " 室内";
                }else{
                    resultWord = URLEncoder.encode(result_place, "UTF-8");
                }
                //Google Mapへ遷移
                Intent mapintent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + resultWord));
                startActivity(mapintent);
            } catch (UnsupportedEncodingException ex) {
                Log.e("MainActivity", "検索キーワード変換失敗", ex);
            }
        });

        //戻るボタンが押されたときの動作を登録
        back_button.setOnClickListener(v -> {finish();});
    }

    //検索結果画面表示関数
    private void search_result() {
        //部品を表示する
        setVisibility(View.VISIBLE, search_result_str, border_line2, border_line3, result_temp_str, result_temp_image, weather_info_str, map_info_str, map_button, result_map_str, inside_switch);
        //「〇〇の検索結果」と表示する
        search_result_str.setText(result_place + getResources().getString(R.string.tourism_class_result_str));

        //weatherAPIから天気データを取得
        String destination_area_weather_result = weatherAPIHelper.receiveWeatherInfo(result_place);
        if(destination_area_weather_result.isEmpty()){ //天気データが空だったら(取得できない場合は空の状態で返ってくるため)
            //天候情報なしと表示＆？画像
            result_temp_str.setText(result_place + getResources().getString(R.string.tourism_class_no_weatherinfo));
            result_temp_image.setImageResource(R.drawable.question);
        }else{
            //一旦天候情報の文字列を作成
            String weatherInfo = getResources().getString(R.string.tourism_class_temp) + weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "temp") + "\n" + getResources().getString(R.string.tourism_class_humidity) + weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "humidity") + "\n" + getResources().getString(R.string.tourism_class_wind) + weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "wind");
            // SpannableString を作成 (特定の文字列の色を変えることができる)
            SpannableString spannableString = new SpannableString(weatherInfo);
            // 三項演算子を使い、気温が15℃以上なら赤色、未満なら青色にする
            int tempColor = Double.parseDouble(weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "temp").replace("℃", "")) >= 15.0 ? Color.RED : Color.BLUE;
            //setSpan(ForegroundColorSpan("色"), スタート位置(int), 終了位置(int), Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            spannableString.setSpan(new ForegroundColorSpan(tempColor), weatherInfo.indexOf(weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "temp")), weatherInfo.indexOf(getResources().getString(R.string.tourism_class_humidity)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 湿度の色変更
            int humidityValue = Integer.parseInt(weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "humidity").replace("%", ""));
            int humidityColor = getHumidityColor(humidityValue); //値で色を変更する関数
            spannableString.setSpan(new ForegroundColorSpan(humidityColor), weatherInfo.indexOf(weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "humidity")), weatherInfo.indexOf(getResources().getString(R.string.tourism_class_wind)), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 風速の色変更
            double windSpeed = Double.parseDouble(weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "wind").replace("m/s", ""));
            int windColor = getWindSpeedColor(windSpeed); //値で色を変更する関数
            spannableString.setSpan(new ForegroundColorSpan(windColor), weatherInfo.indexOf(weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "wind")), weatherInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // TextViewに設定
            result_temp_str.setText(spannableString);

            //天気イメージ設定
            String destination_area_weather = weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "weather");
            int destination_imageResourceId = weatherAPIHelper.getWeatherImage(destination_area_weather);
            if(destination_imageResourceId == R.drawable.clearsky){ //天候イメージが晴れの場合
                //入力した地域のタイムゾーンの値を取得する
                String timezone = weatherAPIHelper.getWeatherAPIData(destination_area_weather_result, "timezone");
                //カレンダー変数を作ってタイムゾーンから地域の時刻(時)を取得
                Calendar timezoneCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                timezoneCalendar.add(Calendar.SECOND, Integer.parseInt(timezone));
                int timeOfDay = timezoneCalendar.get(Calendar.HOUR_OF_DAY);
                //18時から翌6時までの場合
                if(timeOfDay >= 18 || timeOfDay < 6) result_temp_image.setImageResource(R.drawable.moon); //画像を月に変更する
                else result_temp_image.setImageResource(R.drawable.clearsky);
            }else{
                result_temp_image.setImageResource(destination_imageResourceId);
            }
        }
    }

    //forループでviews配列が終わるまで繰り返し(ForEach文)
    private static void setVisibility(int visibility, View... views) {
        for (View view : views) {
            view.setVisibility(visibility);
        }
    }

    //風速の色取得
    private int getWindSpeedColor(double windSpeed) {
        if (windSpeed >= 0 && windSpeed <= 4) return Color.CYAN; // 空色
        else if (windSpeed <= 10) return Color.parseColor("#009AFF"); // 濃い水色
        else if (windSpeed <= 15) return Color.parseColor("#34CE00"); // 黄緑
        else if (windSpeed <= 20) return Color.parseColor("#FF9800"); // 薄いオレンジ
        else if (windSpeed <= 25) return Color.parseColor("#FF6500"); // レンジ
        else return Color.RED; //赤
    }

    //湿度の色取得
    private int getHumidityColor(int humidityValue) {
        if (humidityValue <= 40) return Color.BLUE; //青
        else if (humidityValue <= 59) return Color.parseColor("#00FF00"); // 薄緑
        else return Color.RED; //赤
    }
}
