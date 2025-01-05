package com.example.trackspendingapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ShowActivity extends AppCompatActivity {
    private long recordId; // 用來儲存該條記錄的 id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.show);


        // 檢查布局文件是否正確
        if (findViewById(R.id.main) == null) {
            throw new IllegalStateException("Layout file 'show.xml' must contain a View with ID 'main'.");
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 獲取傳遞的數據
        Intent intent = getIntent();
        String incomeOrExpense = intent.getStringExtra("收入支出");
        String category = intent.getStringExtra("類別");
        int amount = intent.getIntExtra("金額", 0);
        String date = intent.getStringExtra("日期");
        String time = intent.getStringExtra("時間");
        String note = intent.getStringExtra("備註");

        // 獲取傳遞的 ID
        recordId = intent.getLongExtra("id", -1);

        // 檢查傳遞的數據是否完整
        if (recordId == -1) {
            throw new IllegalArgumentException("Invalid record ID passed to ShowActivity.");
        }

        // 顯示數據
        TextView incomeExpenseTextView = findViewById(R.id.textView10);
        TextView categoryTextView = findViewById(R.id.textView11);
        TextView amountTextView = findViewById(R.id.textView12);
        TextView dateTextView = findViewById(R.id.textView13);
        TextView timeTextView = findViewById(R.id.textView14);
        TextView noteTextView = findViewById(R.id.textView15);

        incomeExpenseTextView.setText(incomeOrExpense != null ? incomeOrExpense : "無數據");
        categoryTextView.setText(category != null ? category : "無數據");
        amountTextView.setText(amount != 0 ? String.valueOf(amount) : "無數據");
        dateTextView.setText(date != null ? date : "無數據");
        timeTextView.setText(time != null ? time : "無數據");
        noteTextView.setText(note != null ? note : "無數據");

        // 編輯按鈕
        Button editButton = findViewById(R.id.update);
        editButton.setOnClickListener(v -> {
            // 將數據傳遞到 AddActivity 并設置為編輯模式
            Intent editIntent = new Intent(ShowActivity.this, AddActivity.class);
            editIntent.putExtra("id", recordId);
            editIntent.putExtra("收入支出", incomeOrExpense);
            editIntent.putExtra("類別", category);
            editIntent.putExtra("金額", amount);
            editIntent.putExtra("日期", date);
            editIntent.putExtra("時間", time);
            editIntent.putExtra("備註", note);
            startActivityForResult(editIntent, MainActivity.REQUEST_ADD_ACTIVITY); // 检查编辑后的数据
        });

        // 刪除按鈕
        Button deleteButton = findViewById(R.id.delete);
        deleteButton.setOnClickListener(this::delete);
    }

    // 刪除按鈕的點擊事件
    public void delete(View view) {
        // 開啟數據庫
        SQLiteDatabase db = openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);

        try {
            // 執行刪除操作
            int rowsDeleted = db.delete(MainActivity.TB_NAME, "_id = ?", new String[]{String.valueOf(recordId)});
            if (rowsDeleted == 0) {
                throw new IllegalStateException("Record not found or already deleted.");
            }
            // 刪除成功後顯示 Toast
            Toast.makeText(this, "資料已成功刪除", Toast.LENGTH_SHORT).show();
            // 傳遞數據更新狀態
            Intent resultIntent = new Intent();
            resultIntent.putExtra("data_updated", true);
            setResult(RESULT_OK, resultIntent);
            finish(); // 返回 MainActivity

        } catch (Exception e) {
            e.printStackTrace();
            // 顯示錯誤訊息
            throw new IllegalStateException("Error occurred while deleting the record.", e);

        } finally {
            // 關閉數據庫
            db.close();
        }
    }

}
