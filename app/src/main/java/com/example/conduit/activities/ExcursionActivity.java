package com.example.conduit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conduit.R;
import com.example.conduit.adapter.ExcursionAdapter;
import com.example.conduit.entities.Excursion;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExcursionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
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
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewExcursions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initEventHandlers() {
        // Implement event handlers here
    }

    private void loadExcursions() {
        // Implementation
    }

    private void onDeleteExcursion(Excursion excursion) {
        // Implementation
    }

    private Calendar parseExcursionDate(String dateString) {
        // Implementation
        return null;
    }



    private void onEditExcursionClicked() {
    }

    private void onDeleteExcursionClicked() {
    }

    private void onDeleteExcursionCanceled() {
    }

    private void onDeleteExcursionConfirmed() {
    }

    private void onParseExcursionDate() {
    }

    private void onParseExcursionDateFailed() {
    }

    private void onDestroyExecutorService() {
    }

    private void onLoadExcursions() {
    }

    private void onSetExcursionAlertClicked() {
    }


    private void onSaveExcursionClicked() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
