package com.example.conduit.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.conduit.R;
import com.example.conduit.broadcast.AlertInterceptor;
import com.example.conduit.database.AppDatabase;
import com.example.conduit.entities.Vacation;
import com.example.conduit.utils.DateTimeUtil;
import com.example.conduit.utils.StringFormatUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddVacationActivity extends AppCompatActivity {
    private EditText editTitle, editHotel, editStartDate, editEndDate;
    private Vacation currentVacation;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vacation_add);
        db = AppDatabase.getDatabase(getApplicationContext());
        initViews();
        initEventHandlers();
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);
        editHotel = findViewById(R.id.editHotel);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);

        editStartDate.setOnClickListener(v -> DateTimeUtil.showDatePickerDialog(editStartDate, this));
        editEndDate.setOnClickListener(v -> DateTimeUtil.showDatePickerDialog(editEndDate, this));

    }

    private void initEventHandlers() {
        Button saveVacationButton = findViewById(R.id.saveVacation);
        saveVacationButton.setOnClickListener(v -> onSaveVacation());

        Button viewVacationButton = findViewById(R.id.viewVacations);
        viewVacationButton.setOnClickListener(v -> {
//            List<Vacation> vacations = db.vacationDao().getAllVacations();
//            for (Vacation vacation : vacations) {
//                Log.d("ViewVacations", "Vacation: " + vacation.getTitle() + ", Hotel: " + vacation.getHotel() + ", Start Date: " + vacation.getStartDate() + ", End Date: " + vacation.getEndDate());
//            }
        });

        Button setAlertButton = findViewById(R.id.setAlert);
        setAlertButton.setOnClickListener(v -> {
            if (currentVacation != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    onSetAlarmForVacation(currentVacation, true);
                }
                onAddEventToCalendar(currentVacation);
                Toast.makeText(AddVacationActivity.this, "Alerts for vacation start and end have been set.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AddVacationActivity.this, "No vacation selected to set alert for.", Toast.LENGTH_SHORT).show();
            }
        });

//        if (getIntent().hasExtra("vacationId")) {
//            int vacationId = getIntent().getIntExtra("vacationId", -1);
//            if (vacationId != -1) {
//                onLoadVacationDetails(vacationId);
//            }
//        }
    }

    private void onAddEventToCalendar(Vacation vacation) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setData(CalendarContract.Events.CONTENT_URI);
        intent.putExtra(CalendarContract.Events.TITLE, vacation.getTitle() + "Vacation");
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Vacation Hotel: " + vacation.getHotel());

        long startMilliseconds = DateTimeUtil.convertDateToMilliseconds(vacation.getStartDate());
        long endMilliseconds = DateTimeUtil.convertDateToMilliseconds(vacation.getEndDate());

        if (startMilliseconds == 0 || endMilliseconds == 0) {
            Toast.makeText(this, "Invalid dates for calendar", Toast.LENGTH_SHORT).show();
            return;
        }

        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMilliseconds);
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMilliseconds);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No calendar app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSetAlarm(Vacation vacation, boolean isStart) {
        Log.d("SetAlarm", "Setting alarm for " + (isStart ? "start" : "end") + " time.");
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlertInterceptor.class);
        intent.putExtra("VACATION_TITLE", vacation.getTitle());
        intent.putExtra("STARTING", isStart);

        long timeInMillis = isStart ? DateTimeUtil.convertDateToMilliseconds(vacation.getStartDate()) : DateTimeUtil.convertDateToMilliseconds(vacation.getEndDate());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (vacation.getId() * 2) + (isStart ? 0 : 1), intent, PendingIntent.FLAG_IMMUTABLE);
        if (timeInMillis > System.currentTimeMillis()) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void onSetAlarmForVacation(Vacation vacation, boolean isStart) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
            Log.d("SetAlarm", "Permission to schedule exact alarms not granted. Requesting permission.");
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        } else {
            Log.d("SetAlarm", "Permission to schedule exact alarms granted or not needed.");
        }

        onSetAlarm(vacation, true);
        onSetAlarm(vacation, false);

    }

    private void onShowSaveSuccess() {
        runOnUiThread(() -> {
            Toast.makeText(AddVacationActivity.this, "Vacation saved Successfully", Toast.LENGTH_SHORT).show();
            StringFormatUtil.resetFields();
        });
    }

    private void onShowValidationError() {
        runOnUiThread(() -> Toast.makeText(AddVacationActivity.this, "Please fill all fields out correctly", Toast.LENGTH_SHORT).show());
    }

    private void onUpdateUIVacationDetails(Vacation vacation) {
        editTitle.setText(vacation.getTitle());
        editHotel.setText(vacation.getHotel());
        editStartDate.setText(vacation.getStartDate());
        editEndDate.setText(vacation.getEndDate());
        currentVacation = vacation;
    }

    private void onSaveVacation() {
        final String title = editTitle.getText().toString();
        final String hotel = editHotel.getText().toString();
        final String startDate = editStartDate.getText().toString();
        final String endDate = editEndDate.getText().toString();

        if (onValidateInput(title, hotel, startDate, endDate)) {
            executor.execute(() -> {
                if (currentVacation == null) {
                    currentVacation = new Vacation(0, title, hotel, startDate, endDate);
                    long result = db.vacationDao().insert(currentVacation);
                    if (result != -1) {
                        Log.d("Insertion", "Vacation inserted successfully with id: " + result);
                        onShowSaveSuccess();
                    } else {
                        Log.d("Insertion", "Failed to insert vacation");
                    }
                } else {
                    currentVacation.setTitle(title);
                    currentVacation.setHotel(hotel);
                    currentVacation.setStartDate(startDate);
                    currentVacation.setEndDate(endDate);
                    int result = db.vacationDao().update(currentVacation);
                    if (result > 0) {
                        Log.d("Update", "Vacation updated successfully");
                        onShowSaveSuccess();
                    } else {
                        Log.d("Update", "Failed to update vacation");
                    }
                }
            });
        } else {
            onShowValidationError();
        }
    }


    private void onLoadVacationDetails(int vacationId) {
        db.vacationDao().getVacationByIdAsync(vacationId).observe(this, vacation -> {
            if (vacation != null) {
                onUpdateUIVacationDetails(vacation);
            } else {
                Toast.makeText(AddVacationActivity.this, "Vacation not found.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean onValidateInput(String title, String hotel, String startDate, String endDate) {
        if (title.isEmpty() || hotel.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);

            if (start == null || end == null) {
                return false;
            }
            return !start.after(end);
        } catch (ParseException e) {
            return false;
        }
    }
}
