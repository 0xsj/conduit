package com.example.conduit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conduit.R;
import com.example.conduit.adapter.VacationAdapter;
import com.example.conduit.database.AppDatabase;
import com.example.conduit.entities.Vacation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationActivity extends AppCompatActivity implements VacationAdapter.OnVacationListener {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private VacationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        adapter = new VacationAdapter(new ArrayList<>(), this);
        setContentView(R.layout.vacation);
        initViews();
        loadVacations();
    }

    public void initViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewVacations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (adapter == null) {
            adapter = new VacationAdapter(new ArrayList<>(), this);
        }
        recyclerView.setAdapter(adapter);
    }

    public void onAddExcursionsClicked(Vacation vacation) {
        Intent intent = new Intent(VacationActivity.this, UpdateExcursionActivity.class);
        intent.putExtra("vacationId", vacation.getId());
        startActivity(intent);
    }

    public void onEditClicked(Vacation vacation) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Edit Vacation")
                .setMessage("Confirm edit")
                .setPositiveButton("Yes", (dialog, which) -> onEditVacation(vacation))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .create();
    }

    private void onEditVacation(Vacation vacation) {
        Intent intent = new Intent(VacationActivity.this, AddVacationActivity.class);
        intent.putExtra("vacationId", vacation.getId());
        startActivity(intent);
    }

    public void onDeleteClicked(Vacation vacation) {
        executorService.execute(() -> {
            AppDatabase appDatabase = AppDatabase.getDatabase(VacationActivity.this);
            appDatabase.vacationDao().delete(vacation);
            runOnUiThread(this::loadVacations);
        });
    }
    public void onShareClicked(Vacation vacation) {
        shareVacationDetails(vacation);
    }
    public void onViewExcursionsClicked(Vacation vacation) {
        Intent intent = new Intent(VacationActivity.this, ExcursionActivity.class);
        intent.putExtra("vacationId", vacation.getId());
        startActivity(intent);
    }
    private void loadVacations() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Vacation> vacations = db.vacationDao().getAllVacations();
            runOnUiThread(() -> {
                adapter.setVacations(vacations);
                Log.d("VacationActivity", "Loaded " + vacations.size() + " vacations");
            });
        });
    }

    private void shareVacationDetails(Vacation vacation) {
        String shareText = "Vacation Details:\n" +
                "Title: " + vacation.getTitle() + "\n" +
                "Hotel: " + vacation.getHotel() + "\n" +
                "Start Date: " + vacation.getStartDate() + "\n" +
                "End Date: " + vacation.getEndDate();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");
    }

    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
