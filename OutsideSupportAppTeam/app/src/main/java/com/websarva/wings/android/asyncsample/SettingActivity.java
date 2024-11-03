package com.websarva.wings.android.asyncsample;

/*
 * 参考にしたサイト、サンプルコード
 * 「SharedPreferences を使用してシンプルなデータを保存する」 https://developer.android.com/training/data-storage/shared-preferences?hl=ja データ保存で使用
 * 「時間選択ダイアログ (TimePickerDialog)」https://androidguide.nomaki.jp/html/dlg/timepick/timepickMain.html 時間のダイアログ作成として参考
 * 「AlarmManagerの複数登録」https://qiita.com/a_chocolate/items/d9cfb82bccde9210beeb アラームの設定
 * 「https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=ja」https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=ja toastの表示
 */
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SettingActivity extends AppCompatActivity {

    //時間だけグローバル変数にしておく
    private int selectedHour = 0;
    private int selectedMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        //部品の変数
        EditText departure_place_edittext = findViewById(R.id.editTextText2);
        EditText destination_place_edittext = findViewById(R.id.editTextText4);
        ImageButton redirect_language_setting =  findViewById(R.id.language_button);
        TextView meeting_place_str = findViewById(R.id.textView7);
        TextView meeting_time_str = findViewById(R.id.textView8);
        TextView ten_time_alarm_str = findViewById(R.id.textView10);
        TextView tts_str = findViewById(R.id.textView11);
        TextView only_ten_time_alarm_str = findViewById(R.id.textView13);
        Button alarm_time_set_button = findViewById(R.id.button);
        EditText meeting_place_edittext = findViewById(R.id.editTextText5);
        Switch alarm_setting_switch = findViewById(R.id.switch1);
        Switch alarm_enable_tentime_switch = findViewById(R.id.switch2);
        Switch alarm_enable_tts_switch = findViewById(R.id.switch3);
        Switch alarm_only_tentime_switch = findViewById(R.id.switch4);

        //設定ファイルを読み込む
        SharedPreferences prefs = getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
        String departure_place = prefs.getString("departure", "tokyo"); //出発地
        String destination_place = prefs.getString("destination", "tokyo"); //到着地
        String meeting_place = prefs.getString("meeting_place", "tokyo"); //待ち合わせ場所
        int meeting_hour = prefs.getInt("meeting_hour", 0); //待ち合わせ時
        int meeting_minute = prefs.getInt("meeting_minute", 0); //待ち合わせ分
        boolean alarm_setting_switch_value = prefs.getBoolean("isAlarm", false); //アラームが有効かどうか
        boolean alarm_enable_tts_switch_value = prefs.getBoolean("isTts", false); //ttsが有効かどうか
        boolean alarm_enable_tentime_switch_value = prefs.getBoolean("isPreAlarm", false); //10分前アラームが有効かどうか
        boolean alarm_enable_tentimeonly_switch_value = prefs.getBoolean("isPreAlarmOnly", false); //10分前アラームのみ有効かどうか
        //待ち合わせ時分を変数に入れておく
        selectedHour = meeting_hour;
        selectedMinute = meeting_minute;
        updateTimeOnButton(); //タイマーボタンのテキストを時刻に設定
        //それぞれのedittextに設定ファイルから読み込んだ情報を入れておく
        departure_place_edittext.setText(departure_place); //出発地の情報
        destination_place_edittext.setText(destination_place); //目的地の情報
        meeting_place_edittext.setText(meeting_place); //待ち合わせ場所の情報
        //switchの部品にも情報を入れておく
        alarm_setting_switch.setChecked(alarm_setting_switch_value); //アラーム有効スイッチ
        alarm_enable_tentime_switch.setChecked(alarm_enable_tentime_switch_value); //10分前アナウンス有効スイッチ
        alarm_enable_tts_switch.setChecked(alarm_enable_tts_switch_value); //tts有効スイッチ
        alarm_only_tentime_switch.setChecked(alarm_enable_tentimeonly_switch_value); //10分前アナウンスのみ有効スイッチ
        //言語設定ボタンが押されたら
        redirect_language_setting.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS); //設定アプリの言語設定へ移動する
            startActivity(intent);
        });

        //アラームが有効でなければ　
        if(alarm_setting_switch.isChecked() == false){
            //時間設定とかの部品を表示させない
            meeting_place_str.setVisibility(View.INVISIBLE);
            meeting_time_str.setVisibility(View.INVISIBLE);
            ten_time_alarm_str.setVisibility(View.INVISIBLE);
            tts_str.setVisibility(View.INVISIBLE);
            alarm_time_set_button.setVisibility(View.INVISIBLE);
            meeting_place_edittext.setVisibility(View.INVISIBLE);
            alarm_enable_tentime_switch.setVisibility(View.INVISIBLE);
            alarm_enable_tts_switch.setVisibility(View.INVISIBLE);
            only_ten_time_alarm_str.setVisibility(View.INVISIBLE);
            alarm_only_tentime_switch.setVisibility(View.INVISIBLE);
        }else{
            //10分前アラームが有効でなければ
            if(alarm_enable_tentime_switch.isChecked() == false){
                //10分前のみアラームを有効にするスイッチを表示させない
                only_ten_time_alarm_str.setVisibility(View.INVISIBLE);
                alarm_only_tentime_switch.setVisibility(View.INVISIBLE);
            }else{
                only_ten_time_alarm_str.setVisibility(View.VISIBLE);
                alarm_only_tentime_switch.setVisibility(View.VISIBLE);
            }
        }

        //アラーム有効スイッチのイベントリスナー
        alarm_setting_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Switch が ON になったときの処理
                    meeting_place_str.setVisibility(View.VISIBLE);
                    meeting_time_str.setVisibility(View.VISIBLE);
                    ten_time_alarm_str.setVisibility(View.VISIBLE);
                    tts_str.setVisibility(View.VISIBLE);
                    alarm_time_set_button.setVisibility(View.VISIBLE);
                    meeting_place_edittext.setVisibility(View.VISIBLE);
                    alarm_enable_tentime_switch.setVisibility(View.VISIBLE);
                    alarm_enable_tts_switch.setVisibility(View.VISIBLE);
                    if(alarm_enable_tentime_switch.isChecked() == false){
                        //10分前のみアラームを有効にするスイッチを表示させない
                        only_ten_time_alarm_str.setVisibility(View.INVISIBLE);
                        alarm_only_tentime_switch.setVisibility(View.INVISIBLE);
                    }else{
                        only_ten_time_alarm_str.setVisibility(View.VISIBLE);
                        alarm_only_tentime_switch.setVisibility(View.VISIBLE);
                    }
                } else {
                    //Switch が ON になったときの処理
                    meeting_place_str.setVisibility(View.INVISIBLE);
                    meeting_time_str.setVisibility(View.INVISIBLE);
                    ten_time_alarm_str.setVisibility(View.INVISIBLE);
                    tts_str.setVisibility(View.INVISIBLE);
                    alarm_time_set_button.setVisibility(View.INVISIBLE);
                    meeting_place_edittext.setVisibility(View.INVISIBLE);
                    alarm_enable_tentime_switch.setVisibility(View.INVISIBLE);
                    alarm_enable_tts_switch.setVisibility(View.INVISIBLE);
                    only_ten_time_alarm_str.setVisibility(View.INVISIBLE);
                    alarm_only_tentime_switch.setVisibility(View.INVISIBLE);
                }
            }
        });

        //10分前アラームスイッチのイベントリスナー
        alarm_enable_tentime_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    only_ten_time_alarm_str.setVisibility(View.VISIBLE);
                    alarm_only_tentime_switch.setVisibility(View.VISIBLE);
                } else {
                    only_ten_time_alarm_str.setVisibility(View.INVISIBLE);
                    alarm_only_tentime_switch.setVisibility(View.INVISIBLE);
                }
            }
        });

        //待ち合わせ時刻ボタンが押されたら
        alarm_time_set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {showTimePickerDialog();}//タイムピッカーのダイアログを表示する
        });

        Button back_button = findViewById(R.id.back_button);
        //戻るボタンが押されたら遷移を終了する
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //設定ボタンが押されたら
        Button finish_button = findViewById(R.id.finish_button);
        finish_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //設定ファイルを読み込む
                SharedPreferences saveprefs = getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
                SharedPreferences.Editor editor = saveprefs.edit();
                if(alarm_setting_switch.isChecked() == false){ //アラームが有効にならなければ
                    //出発地と到着地のデータは書き込んでおわる
                    editor.putString("departure", departure_place_edittext.getText().toString());
                    editor.putString("destination", destination_place_edittext.getText().toString());
                    editor.putBoolean("isAlarm", alarm_setting_switch.isChecked());
                    editor.apply();
                    resetAlarm();
                    finish();
                }else{
                    //設定項目すべてを保存しておく
                    editor.putString("departure", departure_place_edittext.getText().toString());
                    editor.putString("destination", destination_place_edittext.getText().toString());
                    editor.putString("meeting_place", meeting_place_edittext.getText().toString());
                    editor.putInt("meeting_hour", selectedHour);
                    editor.putInt("meeting_minute", selectedMinute);
                    editor.putBoolean("isAlarm", alarm_setting_switch.isChecked());
                    editor.putBoolean("isPreAlarm", alarm_enable_tentime_switch.isChecked());
                    editor.putBoolean("isTts", alarm_enable_tts_switch.isChecked());
                    editor.putBoolean("isPreAlarmOnly", alarm_only_tentime_switch.isChecked());
                    editor.apply();
                    //アラームをセットする
                    SetAlarm(selectedHour, selectedMinute, alarm_enable_tentime_switch.isChecked(), alarm_only_tentime_switch.isChecked());
                    finish();
                }
            }
        });
    }

    //ダイアログをセットする関数
    private void showTimePickerDialog() {
        // TimePickerDialogを作成する
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                new TimePickerDialog.OnTimeSetListener() {
                    @RequiresApi(api = Build.VERSION_CODES.S)
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        //時間が選択されたときの処理
                        selectedHour = hourOfDay;
                        selectedMinute = minute;
                        //選択された時間をボタンのテキストに表示する
                        updateTimeOnButton();
                    }
                },
                selectedHour,
                selectedMinute,
                true // 24時間表示を使用する場合はtrue
        );
        //TimePickerDialogを表示
        timePickerDialog.show();
    }

    private void updateTimeOnButton() {
        Button alarm_time_set_button = findViewById(R.id.button);
        //選択された時間をButtonに表示する処理
        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute); //XX:XX形式に変換
        alarm_time_set_button.setText(formattedTime);
        SharedPreferences saveprefs = getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
        SharedPreferences.Editor editor = saveprefs.edit();
        editor.putInt("meeting_hour", selectedHour);
        editor.putInt("meeting_minute", selectedMinute);
        editor.apply();
    }

    private void resetAlarm() {
        // キャンセルするPendingIntentを生成
        Intent intent1 = new Intent(this, PreAlarmReceiver.class);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(this, 0, intent1, PendingIntent.FLAG_IMMUTABLE);
        Intent intent2 = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 1, intent2, PendingIntent.FLAG_IMMUTABLE);
        // AlarmManagerを取得
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // 既存のアラームをキャンセル
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent1);
            alarmManager.cancel(pendingIntent2);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void SetAlarm(int hour, int minute, boolean isTentime, boolean isPreAlarmOnly) {
        //設定情報を読み込む
        SharedPreferences saveprefs = getSharedPreferences("emer_app_data_team", MODE_PRIVATE);
        // 設定ファイルの編集を有効にする
        SharedPreferences.Editor editor = saveprefs.edit();
        if (isTentime) {
            //アラーム時刻を計算
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, hour);
            alarmTime.set(Calendar.MINUTE, minute);
            alarmTime.set(Calendar.SECOND, 0);
            alarmTime.add(Calendar.MINUTE, -10); //10分前に一旦アラームを鳴らすので-10しておく
            // 現在の時刻を取得
            Calendar nowTime = Calendar.getInstance();
            int nowHour = nowTime.get(Calendar.HOUR_OF_DAY);
            int nowMinute = nowTime.get(Calendar.MINUTE);
            //アラームが次の日になるかどうかを判定
            if (nowHour > hour || (nowHour == hour && nowMinute > minute)) {
                alarmTime.add(Calendar.DAY_OF_MONTH, 1);  //次の日に設定
                editor.putBoolean("next_day_alarm", true);
                editor.apply();
            } else {
                editor.putBoolean("next_day_alarm", false);
                editor.apply();
            }
            //アラームマネージャーを取得
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //アラーム時刻をXX:XXで表示する変数
            String time_str = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(alarmTime.getTime());

            //10分前のアラーム時刻が現在時刻より前であれば
            if (nowHour > alarmTime.get(Calendar.HOUR_OF_DAY) || (nowHour == alarmTime.get(Calendar.HOUR_OF_DAY) && nowMinute > alarmTime.get(Calendar.MINUTE))) {
                if (isPreAlarmOnly){ //10分前のみの設定である場合
                    Toast.makeText(this, getResources().getString(R.string.setting_class_failure_pre_alarm_only), Toast.LENGTH_SHORT).show();
                    return;
                } else { //そうでなければ
                    alarmTime.add(Calendar.MINUTE, 10); //+10分
                    //待ち合わせ時刻のアラームはセットしておく
                    Intent intent2 = new Intent(this, AlarmReceiver.class);
                    PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 1, intent2, PendingIntent.FLAG_IMMUTABLE);
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent2);
                    Toast.makeText(this, getResources().getString(R.string.setting_class_failure_pre_alarm), Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            //10分前のアラーム通知処理
            Intent intent = new Intent(this, PreAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, getResources().getString(R.string.setting_class_set_alarm, time_str), Toast.LENGTH_SHORT).show();

            //10分前のみでなければ
            if (!isPreAlarmOnly) {
                //待ち合わせ時刻のアラームも設定
                alarmTime.add(Calendar.MINUTE, 10);
                Intent intent2 = new Intent(this, AlarmReceiver.class);
                PendingIntent pendingIntent2 = PendingIntent.getBroadcast(this, 1, intent2, PendingIntent.FLAG_IMMUTABLE);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent2);
            }
        } else {
            //アラーム時刻を計算
            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, hour);
            alarmTime.set(Calendar.MINUTE, minute);
            alarmTime.set(Calendar.SECOND, 0);

            //現在の時刻を取得
            Calendar nowTime = Calendar.getInstance();
            int nowHour = nowTime.get(Calendar.HOUR_OF_DAY);
            int nowMinute = nowTime.get(Calendar.MINUTE);
            //アラームが次の日であれば
            if (nowHour > hour || (nowHour == hour && nowMinute > minute)) {
                alarmTime.add(Calendar.DAY_OF_MONTH, 1);  //次の日に設定
                editor.putBoolean("next_day_alarm", true);
                editor.apply();
            } else {
                editor.putBoolean("next_day_alarm", false);
                editor.apply();
            }
            //アラームマネージャーを取得
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //アラーム時刻をXX:XXで表示する変数
            String time_str = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(alarmTime.getTime());
            //待ち合わせ時刻にアラームを設定する
            Intent intent = new Intent(this, AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
            //アラームが次の日になる場合は、
            if (nowHour > hour || (nowHour == hour && nowMinute > minute)) {
                Toast.makeText(this, getResources().getString(R.string.setting_class_set_next_alarm, time_str), Toast.LENGTH_SHORT).show(); //toastで次の日のXX:XXにセットと表示
            } else {
                Toast.makeText(this, getResources().getString(R.string.setting_class_set_alarm, time_str), Toast.LENGTH_SHORT).show();//toastでXX:XXにセットと表示
            }
        }
    }

}
