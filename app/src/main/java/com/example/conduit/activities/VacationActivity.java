package com.example.conduit.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conduit.entities.Vacation;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VacationActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Vacation currentVacation;

    @Override
    protected void onCreate(Bundle savedInstance) {}

    public void initViews() {}
    public void initEventHandlers() {}


    public void onAddExcursionsClicked() {}

    public void onEditClicked() {}

    public void onDeleteClicked() {}

    public void onShareClicked() {}

    public void onViewExcursionsClicked() {}

    protected void onDestroy() {}

    private void loadVacations() {}

    private void shareVacationDetails() {}
}
