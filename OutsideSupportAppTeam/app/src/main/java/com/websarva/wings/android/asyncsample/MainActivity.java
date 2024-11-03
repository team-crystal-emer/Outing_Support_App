package com.websarva.wings.android.asyncsample;

/*
 * [このアプリケーションのベース]
 * AsyncSample
 * 大橋(個人)のお出かけ支援アプリ
 *
 * [参考にしたサイト、サンプルコード]
 * 大橋(個人)のアプリのソースコード 一部処理が同じコードを流用
 * ImplicitIntentSample
 * 「SharedPreferences を使用してシンプルなデータを保存する」 https://developer.android.com/training/data-storage/shared-preferences?hl=ja データ保存で使用
 * 「時間選択ダイアログ (TimePickerDialog)」https://androidguide.nomaki.jp/html/dlg/timepick/timepickMain.html 時間のダイアログ作成として参考
 * 「AlarmManagerの複数登録」https://qiita.com/a_chocolate/items/d9cfb82bccde9210beeb アラームの設定
 * 「toastの表示」https://developer.android.com/guide/topics/ui/notifiers/toasts?hl=ja toastの表示
 * 「Androidアプリ開発入門 (4) - TextToSpeech 」 https://note.com/npaka/n/n374eb05fc4b1 ttsについて
 * 「ビュークラスを作成する」 https://developer.android.com/training/custom-views/create-view?hl=ja Viewの作成
 * 「onDraw で自力で描画するカスタムビューを作る」 https://maku77.github.io/android/ui/create-custom-view.html Viewの作成
 * 「Javaを使ってアナログ時計を作ろうと思います（ソース編③『役割ごとにメソッドに分けた+文字盤追加』）」https://yatcho.hatenadiary.org/entry/20090421/1240308092　時計作成において、Mathなどの計算式を参考
 * 「スパン」 https://developer.android.com/guide/topics/text/spans?hl=ja SpannableString使用の際に参考
 */

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

	//activity_mainのレイアウトを表示する
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//ボタンにイベントリスナーを追加
		setupButton(R.id.btnTourismSearch, TourismSearchActivity.class); //観光地検索ボタン
		setupButton(R.id.btnWeather, WeatherViewActivity.class); //天気情報ボタン
		setupButton(R.id.btnMeeting, MeetingActivity.class); //待ち合わせ機能ボタン
		setupButton(R.id.btnSettings, SettingActivity.class); //アプリ設定ボタン
	}

	//イベントリスナーの関数
	private void setupButton(int btnId, Class Activity) {
		Button button = findViewById(btnId);
		button.setOnClickListener(v -> {
			Intent intent = new Intent(MainActivity.this, Activity);
			startActivity(intent);
		});
	}
}
