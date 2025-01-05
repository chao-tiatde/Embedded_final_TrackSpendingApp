package com.example.trackspendingapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    static final String DB_NAME = "MoneyDB";
    static final String TB_NAME = "MoneyList";
    static final String[] FROM = new String[]{"_id", "收入支出", "類別", "金額", "日期", "時間", "備註"};
    static final int REQUEST_ADD_ACTIVITY = 1; // 請求碼

    SQLiteDatabase db;
    Cursor cur;
    SimpleCursorAdapter adapter;
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化資料庫
        db = openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null);

        // 創建資料表（如果尚未存在）
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TB_NAME +
                " (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "收入支出 TEXT, 類別 TEXT, 金額 INTEGER, 日期 TEXT, 時間 TEXT, 備註 TEXT)";
        db.execSQL(createTableQuery);


        // 初始化 ListView
        lv = findViewById(R.id.lv);

        if (cur == null || cur.isClosed()) {
            // 重新初始化或加載數據
            loadData();
        }
        // 更新收入和支出
        updateIncomeAndExpense();

        // 設置 ListView 點擊事件
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Cursor clickedCursor = (Cursor) lv.getItemAtPosition(position);

            // 獲取 id
            long recordId = clickedCursor.getLong(clickedCursor.getColumnIndexOrThrow("_id"));

            // 使用 id 來處理選擇的資料
            String incomeOrExpense = clickedCursor.getString(clickedCursor.getColumnIndexOrThrow("收入支出"));
            String category = clickedCursor.getString(clickedCursor.getColumnIndexOrThrow("類別"));
            int amount = clickedCursor.getInt(clickedCursor.getColumnIndexOrThrow("金額"));
            String date = clickedCursor.getString(clickedCursor.getColumnIndexOrThrow("日期"));
            String time = clickedCursor.getString(clickedCursor.getColumnIndexOrThrow("時間"));
            String note = clickedCursor.getString(clickedCursor.getColumnIndexOrThrow("備註"));

            // 傳遞到 ShowActivity
            Intent intent = new Intent(MainActivity.this, ShowActivity.class);
            intent.putExtra("id", recordId);  // 傳遞 id
            intent.putExtra("收入支出", incomeOrExpense);
            intent.putExtra("類別", category);
            intent.putExtra("金額", amount);
            intent.putExtra("日期", date);
            intent.putExtra("時間", time);
            intent.putExtra("備註", note);
            startActivity(intent);
        });

    }

    private void loadData() {
        if (cur != null && !cur.isClosed()) {
            cur.close(); // 關閉之前的 Cursor
        }
        try {
            cur = db.rawQuery("SELECT _id, 收入支出, 類別, 金額, 日期, 時間, 備註 FROM " + TB_NAME, null);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // 綁定資料到適配器
        adapter = new SimpleCursorAdapter(
                this,
                R.layout.item,
                cur,
                new String[]{"收入支出", "類別", "金額", "日期"},
                new int[]{R.id.收入支出, R.id.類別, R.id.金額, R.id.日期},
                0
        );

        // 設定適配器給 ListView
        lv.setAdapter(adapter);
    }



    public void gotoAdd(View v) {
        // 跳轉到 AddActivity
        Intent intent = new Intent(this, AddActivity.class);
        startActivityForResult(intent, REQUEST_ADD_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ADD_ACTIVITY && resultCode == RESULT_OK) {
            // 檢查是否有新資料插入
            boolean dataUpdated = data.getBooleanExtra("data_updated", false);
            if (dataUpdated) {
                // 重新查詢資料並刷新 ListView
                cur = db.rawQuery("SELECT _id, 收入支出, 類別, 日期, 金額 FROM " + TB_NAME, null);
                adapter.changeCursor(cur); // 更新適配器的 Cursor

                // 更新收入和支出
                updateIncomeAndExpense();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cur != null) cur.close();
        if (db != null) db.close();
    }

    private void updateIncomeAndExpense() {
        // 定義 SQL 查詢
        String incomeQuery = "SELECT SUM(金額) AS total FROM " + TB_NAME + " WHERE 收入支出 = '收入'";
        String expenseQuery = "SELECT SUM(金額) AS total FROM " + TB_NAME + " WHERE 收入支出 = '支出'";

        // 預設總和為 0
        int totalIncome = 0;
        int totalExpense = 0;

        // 查詢收入總和
        Cursor incomeCursor = db.rawQuery(incomeQuery, null);
        if (incomeCursor.moveToFirst() && incomeCursor.getString(0) != null) {
            totalIncome = incomeCursor.getInt(0);
        }
        incomeCursor.close();

        // 查詢支出總和
        Cursor expenseCursor = db.rawQuery(expenseQuery, null);
        if (expenseCursor.moveToFirst() && expenseCursor.getString(0) != null) {
            totalExpense = expenseCursor.getInt(0);
        }
        expenseCursor.close();

        // 計算總額（收入減支出）
        int totalBalance = totalIncome - totalExpense;

        // 使用 String.format 進行數字格式化
        TextView incomeTextView = findViewById(R.id.income);
        TextView expenseTextView = findViewById(R.id.expense);
        TextView totalTextView = findViewById(R.id.total);
        incomeTextView.setText(String.format("收入：%d", totalIncome));
        expenseTextView.setText(String.format("支出：%d", totalExpense));
        totalTextView.setText(String.valueOf(totalBalance));
    }
}
