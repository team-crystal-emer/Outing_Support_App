package com.websarva.wings.android.asyncsample;

/*
 * 参考にしたサイト、サンプルコード
 * ImplicitIntentSample
 */
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MeetingActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        //updateUIという関数を作って処理する(設定画面から戻る場合でもちゃんと設定を反映させるため)
        updateUI();
        Button back_button = findViewById(R.id.back_button);
        //戻るボタンが押されたらfinishする
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        Button setting_button = findViewById(R.id.setting_button);
        //設定ボタンが押されたら設定画面に遷移する
        setting_button.setOnClickListener(v -> {
            Intent intent = new Intent(MeetingActivity.this, SettingActivity.class);
            startActivity(intent);
        });
    }

    //再開した時、UIを再生成する
    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    //待ち合わせ情報表示関数
    private void updateUI() {
        //設定ファイルを読み込む
        SharedPreferences prefs = getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
        String meeting_place = prefs.getString("meeting_place", "");
        int meeting_hour = prefs.getInt("meeting_hour", 0);
        int meeting_minute = prefs.getInt("meeting_minute", 0);
        boolean isAlarm = prefs.getBoolean("isAlarm", false);
        boolean isPreAlarm = prefs.getBoolean("isPreAlarm", false);
        boolean isNextDayAlarm = prefs.getBoolean("next_day_alarm", false);
        //テキストviewの変数
        TextView meeting_time_str = findViewById(R.id.meeting_time);
        TextView meeting_place_str = findViewById(R.id.meeting_place);
        TextView pre_alarm_str = findViewById(R.id.textView15);
        TextView next_day_str = findViewById(R.id.textView14);
        AnalogClock analogclock = findViewById(R.id.analogclock);
        if(meeting_place.isEmpty()){ //待ち合わせ場所もなければ未設定と表示する
            meeting_place_str.setText(getResources().getString(R.string.meeting_class_no_set));
            meeting_place_str.setTextColor(Color.RED);
        }else{
            meeting_place_str.setText(meeting_place);
        }
        //アラームの設定がなければ
        if(!isAlarm){
            //
            meeting_time_str.setText(getResources().getString(R.string.meeting_class_no_set)); //待ち合わせ場所は未設定と表示
            meeting_time_str.setTextColor(Color.RED);
            analogclock.setVisibility(View.INVISIBLE); //アラームのviewも表示しない
            pre_alarm_str.setVisibility(View.INVISIBLE); //10分前アラームが有効であるテキスト
            next_day_str.setVisibility(View.INVISIBLE);

        }else{ //アラームの設定があれば
            //String.formatを使ってXX:XXの文字列にする
            meeting_time_str.setText(String.format("%02d:%02d", meeting_hour, meeting_minute));
            meeting_time_str.setTextColor(Color.BLACK);
            analogclock.setVisibility(View.VISIBLE);
            analogclock.setTime(meeting_hour, meeting_minute);
            meeting_place_str.setText(meeting_place);
            meeting_place_str.setTextColor(Color.BLACK);
            if(isPreAlarm == true){
                pre_alarm_str.setVisibility(View.VISIBLE); //10分前アラームが有効であるテキストを表示する
            }else{
                pre_alarm_str.setVisibility(View.INVISIBLE);
            }
            if(isNextDayAlarm == true){
                next_day_str.setVisibility(View.VISIBLE); //次の日にアラームが鳴るというテキストを表示する
                String time = String.format("%02d:%02d", meeting_hour, meeting_minute);
                next_day_str.setText(getResources().getString(R.string.meeting_next_day_alart, time));
            }else{
                next_day_str.setVisibility(View.INVISIBLE);
            }
        }

        ImageButton map_button = findViewById(R.id.map_button);
        //mapボタンが押されたら...
        map_button.setOnClickListener(v -> {
            try {
                String searchWord = URLEncoder.encode(meeting_place, "UTF-8");
                String uriStr = "geo:0,0?q=" + searchWord;
                Uri uri = Uri.parse(uriStr);
                Intent mapintent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(mapintent);
            } catch (UnsupportedEncodingException ex) {
                Log.e("MainActivity", "検索キーワード変換失敗", ex);
            }
        });
    }
}
