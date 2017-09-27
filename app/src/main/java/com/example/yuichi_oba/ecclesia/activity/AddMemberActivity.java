package com.example.yuichi_oba.ecclesia.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.yuichi_oba.ecclesia.R;
import com.example.yuichi_oba.ecclesia.model.Employee;
import com.example.yuichi_oba.ecclesia.tools.DB;
import com.example.yuichi_oba.ecclesia.tools.MyInterface;
import com.example.yuichi_oba.ecclesia.tools.NameConst.*;
import com.example.yuichi_oba.ecclesia.tools.Util;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import static com.example.yuichi_oba.ecclesia.activity.ReserveActivity.member;

public class AddMemberActivity extends AppCompatActivity
        implements MyInterface {

    public static final String SELECT_ADD_HISTORY = "";
    //*** NameConst には、移動しないこと！ ***//
    public static final int OUTEMP_ID = 10;
    public static final int OUTEMP_NAME = 11;
    public static final int OUTEMP_TEL = 12;
    public static final int OUTEMP_MAILADDR = 13;
    public static final int OUTEMP_COM_NAME = 18;
    public static final int OUTEMP_DEP_NAME = 14;
    public static final int OUTEMP_POS_NAME = 15;
    public static final int EMP_ID = 11;
    public static final int EMP_NAME = 12;
    public static final int EMP_TEL = 13;
    public static final int EMP_MAILADDR = 14;
    public static final int EMP_DEP_NAME = 15;
    public static final int EMP_POS_NAME = 16;

    //*** ここまで ***//

    //    EditText ed_depart;
    //    EditText ed_position;
    EditText ed_name;
    Button bt_cancel;
    Button bt_regist;
    EditText ed_company;
    EditText ed_email;
    EditText ed_tel;
    RadioGroup rbn_group;
    Spinner sp_history;
    Spinner sp_position;
    Spinner sp_depart;
    // 会議に参加したことのあるメンバー情報を格納するメンバークラスのリスト
    List<Employee> members = new ArrayList<>();
    private String emp_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.easyLog("AddMemberActivity->onCreate() 参加者の追加を行う画面");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        Intent in = getIntent();
        emp_id = in.getStringExtra("emp_id");
        Log.d("call", "emp_id " + emp_id);
        /***
         * 各種Widgetの初期化処理
         */
        init();
        setWidgetListener();

    }
    //*** 新規登録ラジオボタンを再度選択したとき、再度編集可能にするメソッド ***//
    private void setAgainEditable() {
        // 全Edittextに対して、再編集可能にする
        ed_company.setFocusable(true);
        ed_company.setEnabled(true);
        ed_company.setFocusableInTouchMode(true);
        ed_name.setFocusable(true);
        ed_name.setEnabled(true);
        ed_name.setFocusableInTouchMode(true);
//        ed_depart.setFocusable(true);
//        ed_depart.setEnabled(true);
//        ed_depart.setFocusableInTouchMode(true);
//        ed_position.setFocusable(true);
//        ed_position.setEnabled(true);
//        ed_position.setFocusableInTouchMode(true);
        ed_tel.setFocusable(true);
        ed_tel.setEnabled(true);
        ed_tel.setFocusableInTouchMode(true);
        ed_email.setFocusable(true);
        ed_email.setEnabled(true);
        ed_email.setFocusableInTouchMode(true);
    }
    //*** 各ウィジェットの初期化処理メソッド ***//
    public void init() {
        bt_cancel = (Button) findViewById(R.id.bt_add_cancel);          //  キャンセルボタン
        bt_regist = (Button) findViewById(R.id.bt_add_regist);          //  登録（追加？）ボタン
        ed_company = (EditText) findViewById(R.id.ed_company);          //  会社入力項目
        ed_name = (EditText) findViewById(R.id.ed_add_name);            //  氏名入力項目
        ed_email = (EditText) findViewById(R.id.ed_add_mailaddr);       //  Email入力項目
        ed_tel = (EditText) findViewById(R.id.ed_add_tel);              //  電話入力項目
        rbn_group = (RadioGroup) findViewById(R.id.rbngroup_addmember); //  ラジオボタングループ
        sp_history = (Spinner) findViewById(R.id.sp_add_history);       //  会社履歴スピナー
        sp_position = (Spinner) findViewById(R.id.sp_add_position);     //  役職スピナー
        sp_depart = (Spinner) findViewById(R.id.sp_add_depart);         //  部署スピナー
         //*** 履歴スピナーの各種設定 ***//
        setSpinnerHistory();
         //*** 部署スピナーの各種設定 ***//
        setSpinnerDepart();
        //*** 役職スピナーの各種設定 ***//
        setSpinnerPosition();
    }
    //*** 各ウィジェットのリスナー登録メソッド ***//
    @Override
    public void setWidgetListener() {
        //*** 履歴スピナーのリスナー ***//
        sp_history.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            //*** 履歴スピナー選択時の処理 ***//
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Spinner spinner = (Spinner) adapterView;
                String name = spinner.getSelectedItem().toString().split(" : ")[1];
                Log.d("call", name);

                //*** 選択した人間の情報を、各ウィジェットにマッピングする ***//
                for (Employee e : members) {
                    if (name.equals(e.getName())) {
                        // 履歴から選択された人間の情報を下の項目群にマッピングする
                        ed_company.setText(e.getCom_name());
                        ed_name.setText(e.getName());
                        ed_email.setText(e.getMailaddr());
                        ed_tel.setText(e.getTel());
                        sp_position.setSelection(Util.setSelection(sp_position, e.getPos_name()));
                        sp_depart.setSelection(Util.setSelection(sp_depart, e.getDep_name()));
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        // TODO: 2017/09/19  登録ボタン押下で、参加者リストを追加するロジックの実装

        //*** 登録ボタン押下時の処理 ***//
        bt_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //*** 各ウィジェットの情報を基に、参加者のインスタンスを生成 ***//
                Log.d("call", "add regist");
                Employee e = new Employee();
                e.setName(ed_name.getText().toString());
                e.setMailaddr(ed_email.getText().toString());
                e.setCom_name(ed_company.getText().toString());
                e.setDep_name(sp_depart.getSelectedItem().toString());
                e.setPos_name(sp_position.getSelectedItem().toString());

                int checkedRadioId = rbn_group.getCheckedRadioButtonId();
                if (checkedRadioId == R.id.rbt_new_regist) {        //*** 新規登録 ***//
                    // DO: 2017/09/27 新規登録なら、社員IDのマックス＋１を参加者インスタンスに設定する
                    SQLiteOpenHelper helper = new DB(getApplicationContext());
                    SQLiteDatabase db = helper.getReadableDatabase();

                    //*** 社員IDのマックス＋１を検索するSQL ***//
                    Cursor c = db.rawQuery("select max(emp_id) + 1 from t_emp", null);
                    String maxId = "";
                    while (c.moveToNext()) {
                        maxId = c.getString(0);
                    }
                    c.close();
                    //***  ***//

                    // 社員の社員IDに、マックス＋１を設定する
                    e.setId(maxId);
                    Log.d("call", String.format("%04d", maxId));
                    // TODO: 2017/09/27  社外者・社員ファイルに新規登録をかける ***//
                    ContentValues val = new ContentValues();
                    val.put("emp_id", e.getId());
                    val.put("emp_name", e.getId());
                    val.put("emp_tel", e.getId());
                    val.put("emp_mailaddr", e.getId());
                    val.put("dep_id", "0001"); //*** 暫定 ***//
                    val.put("pos_id", "0001"); //*** 暫定 ***//

                    long rs = db.insert("t_emp", null, val);        //*** INSERT SQL 実行 ***//
                    if (rs == -1) {
                        //*** INSERT 失敗 ***//
                        Log.d("call", "insert 失敗");
                    } else {
                        //*** INSERT 成功 ***//
                        Log.d("call", "insert 成功");
                    }
                    // TODO: 2017/09/27 社員VERのインサート処理
                    // TODO: 2017/09/27 社外者VERのインサート処理
                } else {                                            //*** 履歴検索 ***//
                    //*** 社員リストの中から、検索して社員IDを検索する ***//
                    for (Employee emp : members) {
                        if (emp.getName().contains(e.getName())) {
                            e.setId(emp.getId());
                        }
                    }
                }
                // TODO: 2017/09/22 役職の優先度をどうするのか
                //*** ReserveActivityの参加者リスト(member)にaddする ***//
//                member.add(e);    //==> startActivityForResult()で対応したので、いらない
                //*** 選んだ（もしくは入力した）参加者を追加する ***//
                Intent intent = new Intent();
                intent.putExtra("member", e);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        //*** キャンセルボタン押下時の処理 ***//
        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("call", "AddMemberActivity->finish()");
                finish();
            }
        });
        //*** ラジオボタングループのリスナー ***//
        rbn_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                switch (radioButton.getId()) {
                    // 履歴検索
                    case R.id.rbt_history:

                        break;
                    // 新規登録
                    case R.id.rbt_new_regist:
                        break;
                }
            }
        });
    }
    //*** 部署スピナーの項目をDB検索して設定するメソッド ***//
    private void setSpinnerDepart() {
        // ＤＢ検索
        SQLiteOpenHelper helper = new DB(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        // 結果をリストにつなぐ
        Cursor c = db.rawQuery("select * from m_depart", new String[]{});
        List<String> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(c.getString(1));
        }
        c.close();
        //  スピナーに設定する
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        sp_depart.setAdapter(adapter);
    }
    //*** 役職スピナーの項目をDB検索して設定するメソッド ***//
    private void setSpinnerPosition() {
        SQLiteOpenHelper helper = new DB(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("select * from m_position", null);

        List<String> list = new ArrayList<>();
        while (c.moveToNext()) {
            list.add(c.getString(1));
        }
        c.close();
        // スピナーに設定する
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        sp_position.setAdapter(adapter);
    }
    //*** 履歴スピナーの項目を動的設定するメソッド ***//
    private void setSpinnerHistory() {
        // DB 検索
        SQLiteOpenHelper helper = new DB(getApplicationContext());
        SQLiteDatabase db = helper.getReadableDatabase();
//        Cursor cursor = db.rawQuery("select * from v_member", new String[]{});
        // 自分が参加した会議に参加したことのある人間を検索(社内)
        Cursor c = db.rawQuery("select *, count(*) as cnt from v_reserve_member where re_id in (select re_id from t_member where mem_id = ?) " +
                " group by mem_id order by cnt desc limit 10", new String[]{emp_id});
        // メンバークラスのインスタンス生成
        List<String> list = new ArrayList<>();
        while (c.moveToNext()) {
            //*** 社員？（人間）クラスのインスタンスを生成 ***//
            Employee e = new Employee();
            e.setId(c.getString(11));           // ID
            e.setName(c.getString(12));         // 氏名
            e.setTel(c.getString(13));          // 電話番号
            e.setMailaddr(c.getString(14));     // メールアドレス
            e.setCom_name("社内");
            e.setDep_name(c.getString(15));     // 部署名
            e.setPos_name(c.getString(16));     // 役職名
            e.setPos_priority(c.getString(17)); // 役職の優先度

            members.add(e);
            list.add(e.getCom_name() + " : " + e.getName());
        }
        c.close();
        // 自分が参加した会議に参加したことのある人間を検索(社外)
        c = db.rawQuery("select * from v_reserve_out_member where re_id in (select re_id from t_member where mem_id = ?)", new String[]{emp_id});
        while (c.moveToNext()) {
            //*** 社員(社外者）クラスのインスタンスを生成 ***//
            Employee e = new Employee();
            e.setId(c.getString(10));           // ID
            e.setName(c.getString(11));         // 氏名
            e.setTel(c.getString(12));          // 電話番号
            e.setMailaddr(c.getString(13));     // メールアドレス
            e.setDep_name(c.getString(14));     // 部署名
            e.setPos_name(c.getString(15));     // 役職名
            e.setPos_priority(c.getString(16)); // 役職の優先度
            e.setCom_name(c.getString(18));     // 会社名

            members.add(e);
            list.add(e.getCom_name() + " : " + e.getName());
        }
        // スピナーに設定する
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, list);
        sp_history.setAdapter(adapter);
    }


}
