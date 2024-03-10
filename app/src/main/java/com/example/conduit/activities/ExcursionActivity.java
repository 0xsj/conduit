package com.example.conduit.activities;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Update;

import com.example.conduit.R;
import com.example.conduit.adapter.ExcursionAdapter;
import com.example.conduit.broadcast.AlertInterceptor;
import com.example.conduit.database.AppDatabase;
import com.example.conduit.entities.Excursion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ExcursionActivity extends AppCompatActivity implements ExcursionAdapter.OnExcursionListener {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ExcursionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excursion);

        initViews();
        initEventHandlers();

        loadExcursions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadExcursions();
        initViews();
    }

    private void initViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewExcursions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ExcursionAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

    }

    private void initEventHandlers() {
        Log.d("ExcursionActivity", "Init event handlers");
    }

    private void loadExcursions() {
        int vacationId = getIntent().getIntExtra("vacationId", -1);
        if (vacationId == -1) {
            Log.d("On Load excursions: ", "vacationId < 0");
            return;
        }

        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Excursion> excursions = db.excursionDao().getExcursionsForVacation(vacationId);

            excursions.sort(Comparator.comparing(Excursion::getDate));

            runOnUiThread(() -> {
                adapter.setExcursions(excursions);
            });
        });
    }

    private Calendar onParseExcursionDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        android.icu.util.Calendar calendar = android.icu.util.Calendar.getInstance();
        try {
            calendar.setTime(dateFormat.parse(dateString));
        } catch (ParseException e) {
            Toast.makeText(this, "Failed to parse date", Toast.LENGTH_SHORT).show();
        }
        return calendar;
    }



    @SuppressLint("ScheduleExactAlarm")
    @Override
    public void onSetExcursionAlertClicked(Excursion excursion) {
        Calendar calendar = onParseExcursionDate(excursion.getDate());
        Intent alertIntent = new Intent(this, AlertInterceptor.class);
        alertIntent.putExtra("excursion_title", excursion.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, excursion.getId(), alertIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            Toast.makeText(this, "Alert set for: " + excursion.getTitle(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error setting the alarm", Toast.LENGTH_SHORT).show();
        }
    }

    public void onSaveExcursionClicked(Excursion excursion) {
        Intent intent = new Intent(this, UpdateExcursionActivity.class);
        intent.putExtra("excursionId", excursion.getId());
        startActivity(intent);
    }

    public void onEditExcursionClicked(Excursion excursion) {
        Intent intent = new Intent(this, UpdateExcursionActivity.class);
        intent.putExtra("excursionId", excursion.getId());
        intent.putExtra("vacationId", excursion.getVacationId());
        startActivity(intent);
    }
    public void onDeleteExcursionClicked(Excursion excursion) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Excursion");
        builder.setMessage("Are you sure you want to delete this excursion?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            onDeleteExcursion(excursion);
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void onDeleteExcursion(Excursion excursion) {
        AppDatabase db = AppDatabase.getDatabase(this);
        executorService.execute(() -> {
            db.excursionDao().delete(excursion);
            runOnUiThread(() -> loadExcursions());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
