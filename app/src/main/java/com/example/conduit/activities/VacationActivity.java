package com.example.conduit.activities;

import android.os.Bundle;

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

public class VacationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Vacation currentVacation;

    private VacationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.vacation);
        initViews();

    }

    public void initViews() {
        recyclerView = findViewById(R.id.recyclerViewVacations);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new VacationAdapter(new ArrayList<>(), this)
//        recyclerView.setAdapter(adapter);
    }
    public void initEventHandlers() {
//        adapter.set
    }



    public void onAddExcursionsClicked(Vacation vacation) {}

    public void onEditClicked(Vacation vacation) {}

    public void onDeleteClicked(Vacation vacation) {}

    public void onShareClicked(Vacation vacation) {}

    public void onViewExcursionsClicked(Vacation vacation) {

    }

    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void loadVacations() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
            List<Vacation> vacations = db.vacationDao().getAllVacations();
            runOnUiThread(() -> {
                adapter.setVacations(vacations);
            });
        });
    }

    private void shareVacationDetails(Vacation vacation) {}
}
