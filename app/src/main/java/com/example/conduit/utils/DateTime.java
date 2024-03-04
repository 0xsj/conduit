package com.example.conduit.utils;

import android.app.DatePickerDialog;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTime {
    public static void showDatePickerDialog(EditText editText, AppCompatActivity activity) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                activity,
                (view, year1, month1, dayOfMonth) -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", month1 + 1, dayOfMonth, year1);
                    editText.setText(selectedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }
}
