package com.example.trackspendingapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.DatePickerDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.TimePickerDialog;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity {
    private long recordId = -1; // 用來存儲該條記錄的 id
    private Spinner incomeOrExpenseSpinner;
    private Spinner categorySpinner;
    private EditText amountEditText;
    private EditText dateEditText;
    private EditText timeEditText;
    private EditText noteEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 初始化控件
        incomeOrExpenseSpinner = findViewById(R.id.收入支出);
        categorySpinner = findViewById(R.id.類別);
        amountEditText = findViewById(R.id.number);
        dateEditText = findViewById(R.id.date);
        timeEditText = findViewById(R.id.time);
        noteEditText = findViewById(R.id.remark);

        // 檢查是否是編輯模式
        Intent intent = getIntent();
        recordId = intent.getLongExtra("id", -1);

        if (recordId != -1) { // 如果有傳遞 id，則是編輯模式
            // 設置資料到 Spinner 中
            String incomeOrExpense = intent.getStringExtra("收入支出");
            String category = intent.getStringExtra("類別");

            // 設定收入支出的選項
            ArrayAdapter<CharSequence> incomeExpenseAdapter = ArrayAdapter.createFromResource(
                    this, R.array.收入支出, android.R.layout.simple_spinner_item);
            incomeExpenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            incomeOrExpenseSpinner.setAdapter(incomeExpenseAdapter);

            // 設定收入支出選項的選中值
            if (incomeOrExpense != null) {
                int incomeExpensePosition = incomeExpenseAdapter.getPosition(incomeOrExpense);
                incomeOrExpenseSpinner.setSelection(incomeExpensePosition);
            }

            // 設定類別選項（根據收入支出動態切換）
            if ("收入".equals(incomeOrExpense)) {
                ArrayAdapter<CharSequence> incomeAdapter = ArrayAdapter.createFromResource(this,
                        R.array.收入類別, android.R.layout.simple_spinner_item);
                incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(incomeAdapter);

                if (category != null) {
                    int categoryPosition = incomeAdapter.getPosition(category);
                    categorySpinner.setSelection(categoryPosition);
                }
            } else if ("支出".equals(incomeOrExpense)) {
                ArrayAdapter<CharSequence> expenseAdapter = ArrayAdapter.createFromResource(this,
                        R.array.支出類別, android.R.layout.simple_spinner_item);
                expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                categorySpinner.setAdapter(expenseAdapter);

                if (category != null) {
                    int categoryPosition = expenseAdapter.getPosition(category);
                    categorySpinner.setSelection(categoryPosition);
                }
            }

            // 設定其他輸入欄位的值
            amountEditText.setText(String.valueOf(intent.getIntExtra("金額", 0)));
            dateEditText.setText(intent.getStringExtra("日期"));
            timeEditText.setText(intent.getStringExtra("時間"));
            noteEditText.setText(intent.getStringExtra("備註"));


        }

        // 保存按钮
        Button saveButton = findViewById(R.id.button);
        saveButton.setOnClickListener(v -> {
            // 取得修改後的資料
            String incomeOrExpense = incomeOrExpenseSpinner.getSelectedItem().toString();
            String category = categorySpinner.getSelectedItem().toString();
            int amount = Integer.parseInt(amountEditText.getText().toString());
            String date = dateEditText.getText().toString();
            String time = timeEditText.getText().toString();
            String note = noteEditText.getText().toString();

            // 判斷是新增還是更新資料
            SQLiteDatabase db = openOrCreateDatabase(MainActivity.DB_NAME, MODE_PRIVATE, null);
            ContentValues values = new ContentValues();
            values.put("收入支出", incomeOrExpense);
            values.put("類別", category);
            values.put("金額", amount);
            values.put("日期", date);
            values.put("時間", time);
            values.put("備註", note);

            if (recordId == -1) {
                // 新增資料
                db.insert(MainActivity.TB_NAME, null, values);
                Toast.makeText(this, "資料已成功新增", Toast.LENGTH_SHORT).show();
            } else {
                // 更新資料
                db.update(MainActivity.TB_NAME, values, "_id = ?", new String[]{String.valueOf(recordId)});
                Toast.makeText(this, "資料已成功編輯", Toast.LENGTH_SHORT).show();
            }
            db.close();

            // 設置返回數據標誌
            Intent resultIntent = new Intent();
            resultIntent.putExtra("data_updated", true); // 標記數據已更新
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        // 設定日期選擇器
        Calendar calendar = Calendar.getInstance();
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
        dateEditText.setText(todayDate);
        dateEditText.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            // 創建日期選擇器
            DatePickerDialog datePicker = new DatePickerDialog(
                    AddActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dateEditText.setText(selectedDate);
                    },
                    year, month, day
            );

            datePicker.show();
        });

        // 設定時間選擇器
        String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime());
        timeEditText.setText(currentTime);
        timeEditText.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AddActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                        timeEditText.setText(selectedTime);
                    },
                    hour, minute, true
            );

            timePickerDialog.show();
        });

        // 設定收入/支出的動態類別更新
        ArrayAdapter<CharSequence> incomeAdapter = ArrayAdapter.createFromResource(this,
                R.array.收入類別, android.R.layout.simple_spinner_item);
        incomeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<CharSequence> expenseAdapter = ArrayAdapter.createFromResource(this,
                R.array.支出類別, android.R.layout.simple_spinner_item);
        expenseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        incomeOrExpenseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedIncomeOrExpense = incomeOrExpenseSpinner.getSelectedItem().toString();
                TextView textView2 = findViewById(R.id.textView2);
                textView2.setText(selectedIncomeOrExpense); // 更新收入/支出 TextView

                if (position == 0) { // 收入
                    categorySpinner.setAdapter(incomeAdapter);
                } else { // 支出
                    categorySpinner.setAdapter(expenseAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 不需要處理
            }

        });
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                TextView textView3 = findViewById(R.id.textView3);
                textView3.setText(selectedCategory); // 更新類別 TextView
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 不需要處理
            }
        });


        // 金額更新
        EditText editTextNumber = findViewById(R.id.number);
        TextView textView = findViewById(R.id.textView);
        editTextNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                textView.setText(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

}