package com.example.yuichi_oba.ecclesia.view;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.example.yuichi_oba.ecclesia.activity.ReserveListActivity;
import com.example.yuichi_oba.ecclesia.dialog.CancelDialog;
import com.example.yuichi_oba.ecclesia.model.Reserve;
import com.example.yuichi_oba.ecclesia.tools.MyHelper;
import com.example.yuichi_oba.ecclesia.tools.Util;

import java.util.ArrayList;
import java.util.List;

import static com.example.yuichi_oba.ecclesia.tools.NameConst.CALL;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.MAX_WIDTH;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.NONE;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.ROOM_A;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.ROOM_B;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.ROOM_C;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.TOKUBETSU;
import static com.example.yuichi_oba.ecclesia.tools.NameConst.ZERO;

/**
 * Created by Yuichi-Oba on 2017/08/28.
 */

// TODO: 2017/11/07 タップした会議を正しく探索できていない？タップしたところと違う会議情報が表示されることがある
public class TimeTableView extends View implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

  //*** 早期退出」オプション選択時の ダイアログフラグメントクラス ***//
//    public static class EarlyOutDialog extends DialogFragment {
//        @Override
//        public Dialog onCreateDialog(Bundle savedInstanceState) {
//            return new AlertDialog.Builder(getActivity())
//                    .setTitle("早期退出")
//                    .setMessage("早期退出しますか？")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            Toast.makeText(getActivity(), "早期退出", Toast.LENGTH_SHORT).show();
//                        }
//                    })
//                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                        }
//                    })
//                    .create();
//        }
//
//        @Override
//        public void onPause() {
//            super.onPause();
//            dismiss();
//        }
//    }

//  public static class CancelDialog extends DialogFragment {
//    @Override
//    public Dialog onCreateDialog(final Bundle savedInstanceState) {
//      return new AlertDialog.Builder(getActivity())
//          .setTitle("予約のキャンセル")
//          .setMessage("本当にこの予約をキャンセルしますか？")
//          .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//              //*** Bundle で渡された引数を取得する ***//
//              String re_id = savedInstanceState.getString("re_id");
//              Log.d(CALL, "引数で渡された予約ID : " + re_id);
//
//              //*** 予約のキャンセル処理を行う ***//
//              SQLiteOpenHelper helper = new MyHelper(getContext());
//              SQLiteDatabase db = helper.getWritableDatabase();
//
//              //*** 予約レコードの削除を行うSQL実行 ***//
//              int result = db.delete("t_reserve", "re_id = ?", new String[]{re_id});
//              Log.d(CALL, "処理件数 : " + result);
//            }
//          })
//          .setNegativeButton("Cancel", null)
//          .show();
//    }
//  }


  public static final int Y_HEIGHT = 40;
  public static final int X_WIGDH = 216;

  public static final int RE_ID = 0;
  public static final int RE_OVERVIEW = 1;
  public static final int RE_START_DAY = 2;
  public static final int RE_END_DAY = 3;
  public static final int RE_START_TIME = 4;
  public static final int RE_END_TIME = 5;
  public static final int RE_SWITCH = 6;
  public static final int RE_ROOM_ID = 10;
  private static final String Q_MY_MEETING = "select * from v_reserve_member where mem_id = ? and re_startday = ? group by re_id";
  private static final String Q_OTHER_MEETING = "select * from v_reserve_member where mem_id <> ? and re_startday = ? group by re_id";

  //*** Field ***//
  private Paint p;
  private Paint p2;
  private Paint room;
  private Paint tokubetsu;
  private Paint roomA;
  private Paint roomB;
  private Paint roomC;
  private Paint p_txtTime;
  private Paint p_txtConference;
  private Paint p_myConference;           //*** 自分の会議用 ***//
  private Paint p_myConference_waku;
  private Paint p_otherConference;        //*** 他人の会議用 ***//
  private Paint p_detail;                 //*** RECT内部の、社内社外・会議目的描画 ***//
  private Paint p_extension;


  public static float x = 0;    // タップしたｘ座標
  public static float y = 0;    // タップしたｙ座標
  public Integer hour = 0;

  GestureDetector detector;

  private float[] timeFloats;
  public boolean thread_flg;
  private List<Reserve> reserveInfo;      //*** 自分の会議記録用リスト ***//
  private List<Reserve> reserveOther;     //*** 他人の会議記録用リスト ***//
  private List<Reserve> reserveExtension; //*** 「延長用」リスト ***//
  private boolean longPressLfg = false;

  //*** Constractor ***//
  public TimeTableView(Context context) {
    super(context);
    init();
  }

  public TimeTableView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public TimeTableView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }


  //*** 描画するメソッド ***//
  @Override
  protected void onDraw(Canvas c) {
    Log.d(CALL, "TimeTableView->onDraw()");

    //*** 特別ABC列の色の描画 ***//
    // l t r b

    c.drawRect(216, 3, 432, 2200, tokubetsu);
    c.drawRect(432, 3, 648, 2200, roomA);
    c.drawRect(648, 3, 864, 2200, roomB);
    c.drawRect(864, 3, 1078, 2200, roomC);


    timeFloats = new float[24];
    for (int i = 0, j = 100; i < timeFloats.length; i++) {
      timeFloats[i] = j;
      j += Y_HEIGHT * 2;
    }
    // 特別会議室 Ａ Ｂ Ｃ 列の描画
    float x = X_WIGDH; // 216
    float room_y = 100;
    c.drawRect(ZERO, 0, MAX_WIDTH, room_y, p);
    c.drawLine(ZERO, 0, ZERO, room_y, p);   // sx sy ex ey
    for (int i = 1; i <= 4; i++) {
      c.drawLine(i * x, 0, i * x, room_y, p);
    }


    // 会議室名の描画
    float y_conference = 70;
    c.drawText("特別", 316, y_conference, p_txtConference);
    c.drawText("A", 532, y_conference, p_txtConference);
    c.drawText("B", 748, y_conference, p_txtConference);
    c.drawText("C", 964, y_conference, p_txtConference);

    // 時間割の枠の描画
    onDrawTimeTable(c);
    // 時間の文字の描画 text x y paint
    for (int i = 0, j = 150; i <= 24; i++) {
      String time = String.format("%02d:00", i);
      c.drawText(time, 100, j, p_txtTime);
      j += Y_HEIGHT * 2;
    }

    // アプリを立ち上げた社員の予約情報の描画
    onDrawConference(c);


  }

  //*** 会議を角丸で描画するメソッド ***//
  private void onDrawConference(Canvas c) {
    int cnt = 0;
    //*** 他人の参加会議に対する処理 ***//
    for (Reserve r : reserveOther) {
      String sTime = r.getRe_startTime();
      String eTime = r.getRe_endTime();
      Log.d(CALL, String.format("%s startTime : %s endTime : %s", r.getId(), sTime, eTime));

      String room_id = r.getRe_room_id();
      String extensionTime = r.getRe_extensionEndTime();
      RectF rectF = retRectCooperation(sTime, eTime, room_id);
      // 予約会議のざ行情報を記録する
      // TODO: 2017/12/18 延長終了時刻があれば、そっちで記録するロジックの実装
      reserveOther.get(cnt).setCoop(new float[]{rectF.left, rectF.top, rectF.right, rectF.bottom});
      //*** その会議が延長されているなら、描画する（null対策) ***//
      if (r.getRe_extensionEndTime() != null) {
        RectF rectFEx = retRectCooperation(sTime, extensionTime, room_id);
        c.drawRoundRect(rectFEx, 30, 30, p_extension);
        c.drawRoundRect(rectFEx, 30, 30, p_myConference_waku);
        reserveOther.get(cnt).setCoop(new float[]{rectFEx.left, rectFEx.top, rectFEx.right, rectFEx.bottom});
      }


      c.drawRoundRect(rectF, 30, 30, p_otherConference);
      c.drawRoundRect(rectF, 30, 30, p_myConference_waku);
      //*** 矩形内部に社内社外・会議目的の文字を描画する ***//
      onDrawRectText(r, rectF, c);
      cnt++;
    }
    cnt = 0;
    //*** 自分の参加会議に対する処理 ***//
    for (Reserve r : reserveInfo) {
      String sTime = r.getRe_startTime();                         //*** 開始時刻の取得 ***//
      String eTime = r.getRe_endTime();                           //*** 終了時刻の取得 ***//
      String room_id = r.getRe_room_id();                         //*** 会議室ＩＤの取得 ***//
      String extensionTime = r.getRe_extensionEndTime();
      RectF rectF = retRectCooperation(sTime, eTime, room_id);    //***  ***//
      // 予約会議の座標情報を記録する
      // TODO: 2017/12/18 延長終了時刻があれば、そっちで記録するロジックの実装
      reserveInfo.get(cnt).setCoop(new float[]{rectF.left, rectF.top, rectF.right, rectF.bottom});
      //*** その会議が延長されているなら、描画する（null対策) ***//
      if (r.getRe_extensionEndTime() != null) {
        RectF rectFEx = retRectCooperation(sTime, extensionTime, room_id);
          c.drawRoundRect(rectFEx, 30, 30, p_extension);
          c.drawRoundRect(rectFEx, 30, 30, p_myConference_waku);
        reserveInfo.get(cnt).setCoop(new float[]{rectFEx.left, rectFEx.top, rectFEx.right, rectFEx.bottom});
      }

      // 予約会議の描画
      c.drawRoundRect(rectF, 30, 30, p_myConference);       //*** 会議の矩形を描画 ***//
      c.drawRoundRect(rectF, 30, 30, p_myConference_waku);  //*** 矩形の枠を描画 ***//
      //*** 矩形内部に社内社外・会議目的の文字を描画する ***//
      onDrawRectText(r, rectF, c);




//            //*** RECTの高さが、100dp以上ならば、描画を行う ***//
//            if (rectF.bottom - rectF.top >= 100) {
//                float margin = 20;
//                c.drawText(r.getRe_switch().contains("0") ? "[社内]" : "[社外]",
//                        rectF.centerX(), rectF.centerY() - margin, p_detail);                               //*** 社内社外区分の描画 ***//
//                c.drawText(r.getRe_purpose_name(), rectF.centerX(), rectF.centerY() + margin, p_detail);    //*** 会議目的名の描画 ***//
//            }
      cnt++;  //*** 次の会議を見るために、添え字をインクリする ***//
    }

  }

  //*** --- SELF MADE METHOD --- 矩形内部に社内社外・会議目的の文字を描画する ***//
  private void onDrawRectText(Reserve r, RectF rectF, Canvas c) {
    //*** RECTの高さが、100dp以上ならば、描画を行う ***//
    if (rectF.bottom - rectF.top >= 150) {
      float margin = 20;
      c.drawText(r.getRe_switch().contains("0") ? "[社内]" : "[社外]",
          rectF.centerX(), rectF.centerY() - margin, p_detail);                               //*** 社内社外区分の描画 ***//


      String str = r.getRe_purpose_name();
      if (str.length() > 4){
        str = str.substring(0, 4) + "...";
      }
      c.drawText(str, rectF.centerX(), rectF.centerY() + margin + 20, p_detail);    //*** 会議目的名の描画 ***//
    }
  }

  //*** --- SELF MADE METHOD --- 開始終了時刻・会議室を基に、描画すべき座標を返すメソッド ***//
  private RectF retRectCooperation(String sTime, String eTime, String room_id) {
    float sX = 0, eX = 0, sY = 0, eY = 0;
    float x = 216;
    float padding = 2;

    switch (room_id) {
      case TOKUBETSU:
        sX = x + padding;
        eX = 2 * x - padding;
        break;
      case ROOM_A:
        sX = 2 * x + padding;
        eX = 3 * x - padding;
        break;
      case ROOM_B:
        sX = 3 * x + padding;
        eX = 4 * x - padding;
        break;
      case ROOM_C:
        sX = 4 * x + padding;
        eX = 5 * x - padding;
        break;
    }
    // TODO: 2017/11/04 0時をまたぐ予約で落ちるバグ発覚
    int s = Integer.parseInt(sTime.split("：")[0]); // 08:00 -> 8 => 8 - 8 = 0
    sY = timeFloats[s] + padding;
    if (Integer.parseInt(sTime.split("：")[1]) >= 30) {
      sY += Y_HEIGHT + padding;
    }

    int e = Integer.parseInt(eTime.split("：")[0]); // 10:30 -> 10 - 8 = 2
    eY = timeFloats[e] - padding;
    if (Integer.parseInt(eTime.split("：")[1]) >= 30) { // 30 >= 30
      eY += Y_HEIGHT - padding;
    }

    return new RectF(sX, sY, eX, eY);
  }

  //*** 時間割の枠の描画 ***//
  private void onDrawTimeTable(Canvas canvas) {
    float x = 216;
    float y_timetable = 100;
    for (int i = 1; i <= 4; i++) {
      canvas.drawLine(i * x, y_timetable, i * x, 2200, p2);
    }
    // 中の線の線
    float y = Y_HEIGHT;
    for (int i = 1; i < 48; i++) {
      canvas.drawLine(x, y_timetable + i * y, MAX_WIDTH, y_timetable + i * y, p2);
      if (i % 2 == 0) {
        canvas.drawLine(ZERO, y_timetable + i * y, x, y_timetable + i * y, p);
      }
    }
    canvas.drawRect(ZERO, y_timetable, MAX_WIDTH, y_timetable + 48 * y, p);
  }

  //*** Paintクラスの初期化処理メソッド ***//
  private void init() {
    Log.d(CALL, "call TimeTableView->init()");


    reserveInfo = new ArrayList<>();      //*** 自分会議記録用リスト ***//
    reserveOther = new ArrayList<>();     //*** 他人会議記録用リスト ***//
    reserveExtension = new ArrayList<>(); //*** 延長用リスト        ***//

    detector = new GestureDetector(ReserveListActivity.getInstance(), this);

    // 枠線用
    p = new Paint();
    p.setColor(Color.DKGRAY);
    p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(10);

    p2 = new Paint();
    p2.setStrokeWidth(2.0f);

    // 特別会議室用
    tokubetsu = new Paint();
    tokubetsu.setColor(Color.parseColor("#ffb6c1"));
    tokubetsu.setStyle(Paint.Style.FILL);
    tokubetsu.setStrokeWidth(10);

    // 会議室Ａ用
    roomA = new Paint();
    roomA.setColor(Color.parseColor("#add8e6"));
    roomA.setStyle(Paint.Style.FILL);
    roomA.setStrokeWidth(10);

    // 会議室Ｂ用
    roomB = new Paint();
    roomB.setColor(Color.parseColor("#98fb98"));
    roomB.setStyle(Paint.Style.FILL);
    roomB.setStrokeWidth(10);

    // 会議室Ｃ用
    roomC = new Paint();
    roomC.setColor(Color.parseColor("#fffacd"));
    roomC.setStyle(Paint.Style.FILL);
    roomC.setStrokeWidth(10);

    // テキスト用
    p_txtTime = new Paint();
    p_txtTime.setTypeface(Typeface.MONOSPACE);
    p_txtTime.setTextSize(40);
    p_txtTime.setTextAlign(Paint.Align.CENTER);
    p_txtTime.setColor(Color.BLACK);

    p_txtConference = new Paint();
    p_txtConference.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
    p_txtConference.setTextSize(70);
    p_txtConference.setTextAlign(Paint.Align.CENTER);
    p_txtConference.setColor(Color.BLACK);

    //*** 自分の会議の描画用 ***//
    p_myConference = new Paint();
    p_myConference.setColor(Color.parseColor("#ff6347"));
    p_myConference.setStyle(Paint.Style.FILL);
    p_myConference.setStrokeWidth(10);

    p_myConference_waku = new Paint();
    p_myConference_waku.setColor(Color.parseColor("#a0222222")); //*** 透明度指定AARRGGBB ***//
    p_myConference_waku.setStyle(Paint.Style.STROKE);
    p_myConference_waku.setStrokeWidth(10);

    //*** 他人の会議の描画用 ***//
    p_otherConference = new Paint();
    p_otherConference.setColor(Color.parseColor("#f5f5f5"));
    p_otherConference.setStyle(Paint.Style.FILL);
    p_otherConference.setStrokeWidth(10);

    //*** RECT内部の社内社外・会議目的 描画用 ***//
    p_detail = new Paint();
    p_detail.setTextSize(40);
    p_detail.setTextAlign(Paint.Align.CENTER);
    p_detail.setTypeface(Typeface.DEFAULT_BOLD);
    p_detail.setColor(Color.BLACK);

    //*** 延長用 ***//
    p_extension = new Paint();
//    p_extension.setColor(Color.YELLOW);
    p_extension.setColor(Color.parseColor("#FFCA28"));
    p_extension.setStyle(Paint.Style.FILL);
    p_extension.setStrokeWidth(10);

  }

  //*** 画面タッチ時のイベント ***//
  @Override
  public boolean onTouchEvent(MotionEvent e) {

    Log.d("call", "call onTouchEvent()");
//      x = e.getX();
//      y = e.getY();

    switch (e.getAction()) {
      case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_DOWN:
        Log.d(CALL, "TimeTableView->onTouchEvent()");
        // タップした座標を取得する
        x = e.getX();
        y = e.getY();
        Log.d(CALL, e.getX() + " : " + e.getY());
        Log.d(CALL, String.valueOf(x) + " : " + String.valueOf(y));
        // TODO: 2017/12/08 タップしたｙ軸で、時間帯を特定・設定する ]
        hour = getYOfCoodinate(y);
        Log.d("call", hour.toString());
        break;
    }
    if (detector.onTouchEvent(e)) return true;

    return true;
  }

  //*** タップしたY座標の時間帯を返すメソッド ***//
  private Integer getYOfCoodinate(float y) {
    Integer hour = Math.round(y / 10) * 10;
    if (100 <= hour && hour < 180) {
      return 0;
    } else if (180 <= hour && hour <= 260) {
      return 1;
    } else if (260 <= hour && hour <= 340) {
      return 2;
    } else if (340 <= hour && hour <= 420) {
      return 3;
    }else if (420 <= hour && hour <= 500) {
      return 4;
    }else if (500 <= hour && hour <= 580) {
      return 5;
    }else if (580 <= hour && hour <= 640) {
      return 6;
    }else if (720 <= hour && hour <= 800) {
      return 7;
    }else if (800 <= hour && hour <= 880) {
      return 8;
    }else if (880 <= hour && hour <= 960) {
      return 9;
    }else if (960 <= hour && hour <= 1040) {
      return 10;
    }else if (1040 <= hour && hour <= 1120) {
      return 11;
    }else if (1120 <= hour && hour <= 1200) {
      return 12;
    }else if (1200 <= hour && hour <= 1280) {
      return 13;
    }else if (1280 <= hour && hour <= 1360) {
      return 14;
    }else if (1360 <= hour && hour <= 1440) {
      return 15;
    }else if (1440 <= hour && hour <= 1520) {
      return 16;
    }else if (1520 <= hour && hour <= 1600) {
      return 17;
    }else if (1600 <= hour && hour <= 1680) {
      return 18;
    }else if (1680 <= hour && hour <= 1760) {
      return 19;
    }else if (1760 <= hour && hour <= 1840) {
      return 20;
    }else if (1840 <= hour && hour <= 1920) {
      return 21;
    }else if (1920 <= hour && hour <= 2000) {
      return 22;
    }else if (2000 <= hour && hour <= 2080) {
      return 23;
    }else if (2080 <= hour && hour <= 2160) {
      return 24;
    }
    return 7;
  }

  //*** 再描画を行うメソッド ***//
  public void reView(String emp_id, String date) {
    Log.d(CALL, "TimeTableView->reView()");
    //*** 前の情報をいったんクリアする ***//
    reserveInfo.clear();
    reserveOther.clear();

    SQLiteOpenHelper helper = new MyHelper(getContext());
    SQLiteDatabase db = helper.getReadableDatabase();
    Cursor c = null;

//    //*** 自分、他人の会議を全件検索して延長会議を調べる ***//
//    Cursor c = db.rawQuery("select * from t_extension where re_id = ?", null);
//    while (c.moveToNext()) {
//      Reserve r = new Reserve();
//      r.setRe_id(c.getString(0));               //*** 延長テーブルにある予約ID ***//
//      r.setRe_extensionEndTime(c.getString(4)); //*** 延長の終了時刻 ***//
//      reserveExtension.add(r);
//    }


    //*** 自分の参加会議の検索 ***//
    c = db.rawQuery(Q_MY_MEETING, new String[]{emp_id, date});
    while (c.moveToNext()) {
      // 予約情報のインスタンス生成
      Reserve r = new Reserve();
      r.setRe_id(c.getString(RE_ID));         //*** 予約ID ***//
      r.setRe_name(c.getString(1));           //*** 概要 ***//
      r.setRe_startDay(c.getString(2));       //*** 開始日時 ***//
      r.setRe_endDay(c.getString(3));         //*** 終了日時 ***//
      r.setRe_startTime(c.getString(4));      //*** 開始時刻 ***//
      r.setRe_endTime(c.getString(5));        //*** 終了時刻 ***/
      r.setRe_switch(c.getString(6));         //*** 社内社外区分 ***//
      r.setRe_fixtures(c.getString(7));       //*** 備品 ***//
      r.setRe_remarks(c.getString(8));        //*** 備考 ***//
      r.setRe_room_id(c.getString(10));       //*** 会議室ID ***//
      r.setRe_purpose_name(c.getString(19));  //*** 会議目的名 ***//

      String extensionEndTime = Util.isExtensionConference(r.getRe_id());
      //*** 延長テーブルを検索してその会議が延長されているなら、値をセットする ***//
      if (extensionEndTime != null) {
        r.setRe_extensionEndTime(extensionEndTime);
      }

      reserveInfo.add(r);
      Log.d(CALL, "取得した自分の参加会議 " + c.getString(0) + " : " + c.getString(2) + " : " + c.getString(3));
    }
    c.close();

    //*** 他人の参加会議の検索 ***//
    c = db.rawQuery(Q_OTHER_MEETING, new String[]{emp_id, date});
    while (c.moveToNext()) {

      Reserve r = new Reserve();
      r.setRe_id(c.getString(RE_ID));
      r.setRe_name(c.getString(1));
      r.setRe_startDay(c.getString(2));
      r.setRe_endDay(c.getString(3));
      r.setRe_startTime(c.getString(4));
      r.setRe_endTime(c.getString(5));
      r.setRe_switch(c.getString(6));
      r.setRe_fixtures(c.getString(7));
      r.setRe_remarks(c.getString(8));
      r.setRe_room_id(c.getString(10));
      r.setRe_purpose_name(c.getString(19));  //*** 会議目的名 ***//

      String extensionEndTime = Util.isExtensionConference(r.getRe_id());
      //*** 延長テーブルを検索してその会議が延長されているなら、値をセットする ***//
      if (extensionEndTime != null) {
        r.setRe_extensionEndTime(extensionEndTime);
      }

      reserveOther.add(r);
      Log.d(CALL, "取得した他人の参加会議 " + c.getString(0) + " : " + c.getString(2) + " : " + c.getString(3));
    }

    //*** 延長テーブルの検索 ***//
    List<Reserve> listAll = new ArrayList<>();



    c.close();
    invalidate();
  }

  //*** タップした会議の予約ＩＤを返すメソッド ***//
  public String[] getSelectedReserve() {
    Log.d(CALL, "TimeTableView->getSelectedReserve()");
    //
    String roomId = "";                         //*** 押された会議室の区分 ***//
    Log.d(CALL, String.valueOf(thread_flg));
//        thread_flg = true;]
    if (reserveInfo.size() == 0 && reserveOther.size() == 0) {
      Log.d(CALL, "この日の会議は空！");
      return new String[]{NONE, roomId};
    }


    int cnt = 0;
    while (thread_flg) {
      float wX = 216;
      // タッチされたか
      if (isTouched()) {
        Log.d("call", "public String[] getSelectedReserve() の isTouched()");
        if (x > wX && x < 2 * wX) {
          Log.d(CALL, "tokubetu");
          roomId = TOKUBETSU;
        } else if (x > 2 * wX && x < 3 * wX) {
          Log.d(CALL, "roomA");
          roomId = ROOM_A;
        } else if (x > 3 * wX && x < 4 * wX) {
          Log.d(CALL, "roomB");
          roomId = ROOM_B;
        } else if (x > 4 * wX && x < 5 * wX) {
          Log.d(CALL, "roomC");
          roomId = ROOM_C;
        }
        // roomId と y座標を基に、どの会議がタップされたかを返す
//        int cnt = 0;
        for (Reserve r : reserveInfo) {
          if (r.getCoop() != null && r.getCoop()[1] < y && r.getCoop()[3] > y) {
            // 特定した
            if (roomId.equals(r.getRe_room_id())) {
              Log.d(CALL, "会議を特定した！  " + r.getRe_room_id());
              thread_flg = false;
              if (longPressLfg) {
                thread_flg = true;
                return new String[]{r.getRe_id(), roomId, "long"};
              }
//                            return r.getRe_id();        //*** 特定した会議室予約IDを返す ***//
              //*** 特定した会議室IDと、会議室IDを返す ***//
              return new String[]{r.getRe_id(), roomId, "itiran"};

            }
            cnt++;
          }
        }
        // TODO: 2017/10/13 要検証
        //*** 他人の会議がタップされたかを判定する ***//
        for (Reserve r : reserveOther) {
          if (r.getCoop() != null && r.getCoop()[1] < y && r.getCoop()[3] > y) {
            // 特定した
            if (roomId.equals(r.getRe_room_id())) {
              Log.d(CALL, "会議を特定した！  " + r.getRe_room_id());
              thread_flg = false;
              if (longPressLfg) {
                thread_flg = true;
                return new String[]{r.getRe_id(), roomId, "long"};
              }
//                            return r.getRe_id();        //*** 特定した会議室予約IDを返す ***//
              //*** 特定した会議室IDと、会議室IDを返す ***//
              return new String[]{r.getRe_id(), roomId, "itiran"};
            }
            cnt++;
          }
        }
        Log.d(CALL, "cnt :: " + String.valueOf(cnt));
        Log.d(CALL, roomId);
        if (cnt == 0 || cnt >= 100) {
          Log.d(CALL, "新規会議の登録ロジック開");
          return new String[]{NONE, roomId};    //*** 新規予約であることを返す ***//
        }
//        return new String[]{NONE, roomId};
//                x = 0;
//                y = 0;
      }
    }
    Log.d(CALL, "Re_id : " + roomId);
    return new String[]{NONE, roomId};    //*** 新規予約であることを返す ***//
  }

  //*** x y の値を基に、ユーザがタッチしたのか否かを返すメソッド ***//
  public boolean isTouched() {
//        Log.d(CALL, "call TimeTableView->isTouched()");
    if (x != 0 && y != 0) {
      return true;
    }
    return false;
  }

  @Override
  public boolean onDown(MotionEvent e) {
    Log.d(CALL, "onDown");


    return true;
  }

  @Override
  public void onShowPress(MotionEvent e) {
//    x = e.getX();
//    y = e.getY();
  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    Log.d(CALL, "onSingleTapUp!");
    x = e.getX();
    y = e.getY();

    Log.d("call", String.valueOf("thread_flg :: " + thread_flg));
    return true;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    return false;
  }

  @Override
  public void onLongPress(MotionEvent e) {
//    Toast.makeText(ReserveListActivity.getInstance(), "この予約をキャンセルしますか？", Toast.LENGTH_SHORT).show();
    Log.d(CALL, "LongTouch");

    // TODO: 2017/11/27 ロングタップした予約IDの特定
    longPressLfg = true;
    x = e.getX();
    y = e.getY();
    Log.d("call", String.valueOf(x));
    Log.d("call", String.valueOf(y));

    String re_id = getSelectedReserve()[0];
    Log.d("call", "tokutei kaigi : " + re_id);

    Bundle bundle = new Bundle();
    bundle.putString("re_id", re_id);
    bundle.putString("emp_id", ReserveListActivity.employee.getEmp_id());
//    Toast.makeText(ReserveListActivity.getInstance(), "この予約をキャンセルしますか？", Toast.LENGTH_SHORT).show();
    CancelDialog cancelDialog = new CancelDialog();
    cancelDialog.setArguments(bundle);
    cancelDialog.show(ReserveListActivity.getInstance().getFragmentManager(), "aaaaa");

    //*** タップした会議の予約IDを求めて代入する ***//
//    String[] info = getSelectedReserve();
//
//    //*** キャンセルダイアログの生成 ***//
//    CancelDialog cancelDialog = new CancelDialog();
//    Bundle bundle = new Bundle();
//    bundle.putString("info", info[0]);       //*** Bundle に予約IDを渡す ***//
//    cancelDialog.show(ReserveListActivity.getInstance().getFragmentManager(), "cancel");
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    Log.d(CALL, "onFling");
    return false;
  }

  @Override
  public boolean onSingleTapConfirmed(MotionEvent e) {
    Log.d(CALL, "onSingleTapConfirmed");
    return false;
  }

  @Override
  public boolean onDoubleTap(MotionEvent e) {
    Log.d(CALL, "onDoubleTap");
    return false;
  }

  @Override
  public boolean onDoubleTapEvent(MotionEvent e) {
    Log.d(CALL, "onDoubleTapEvent");
    return false;
  }
}
