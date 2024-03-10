package com.example.conduit.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.conduit.R;
import com.example.conduit.database.AppDatabase;
import com.example.conduit.entities.Excursion;
import com.example.conduit.entities.Vacation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class UpdateExcursionActivity extends AppCompatActivity {
    private static final String DATE_FORMAT = "MM/dd/yyyy";
    private static final String ERROR_INVALID_DATE_FORMAT = "Invalid date format.";
    private static final String ERROR_DATE_NOT_IN_RANGE = "Date is not within the vacation range.";
    private static final String ERROR_SAVING_EXCURSION = "Error saving excursion: ";

    private EditText editExcursionTitle, editExcrusionDate;
    private Button saveExcursion;
    private Excursion currentExcursion;
    private int vacationId;
    private AppDatabase db;

    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.excursion_update);

        executor = Executors.newSingleThreadExecutor();

        db = AppDatabase.getDatabase(getApplicationContext());
        vacationId = getIntent().getIntExtra("vacationId", -1);
        if (vacationId == -1) {
            Toast.makeText(this, "Vacation ID is missing", Toast.LENGTH_SHORT).show();
        }

        int excursionId = getIntent().getIntExtra("excursionId", -1);
        if (excursionId != -1) {
            observeExcursionDetails(excursionId);
        }

        initViews();
        initEventHandlers();
    }

    private void initViews() {
        editExcursionTitle = findViewById(R.id.editTextExcursionTitle);
        editExcrusionDate = findViewById(R.id.editTextExcursionDate);
        saveExcursion = findViewById(R.id.buttonSaveExcursion);

        editExcrusionDate.setOnClickListener(v -> showDatePickerDialog(editExcrusionDate));
    }

    private void initEventHandlers() {
        saveExcursion.setOnClickListener(v -> saveOrUpdateExcursion());
    }

    private void observeExcursionDetails(int excursionId) {
        db.excursionDao().getExcursionById(excursionId).observe(this, excursion -> {
            if (excursion != null) {
                editExcursionTitle.setText(excursion.getTitle());
                editExcrusionDate.setText(excursion.getDate());
                currentExcursion = excursion;
            }
        });
    }

    private void saveOrUpdateExcursion() {
        String title = editExcursionTitle.getText().toString();
        String date = editExcrusionDate.getText().toString();
        Log.d("AddEditExcursionActivity", "Attempting to save or update excursion");

        if (!isValidDate(date)) {
            showErrorMessage(ERROR_INVALID_DATE_FORMAT);
            return;
        }

        db.vacationDao().getVacationByIdAsync(vacationId).observe(this, vacation -> {
            if (vacation == null) {
                Toast.makeText(this, "Vacation not found.", Toast.LENGTH_SHORT).show();
                Log.e("AddEditExcursionActivity", "Vacation not found for ID: " + vacationId);
                return;
            }

            if (!isDateWithinRange(date, vacation)) {
                Toast.makeText(this, "Date is not within the vacation range.", Toast.LENGTH_SHORT).show();
                Log.e("AddEditExcursionActivity", "Date is not within the vacation range.");
                return;
            }

            persistExcursion(title, date);
        });
    }

    private void persistExcursion(String title, String date) {
        executor.execute(() -> {
            Excursion excursionToSave = new Excursion(0, title, date, vacationId); // Assuming 0 for id since it's auto-generated
            try {
                if (currentExcursion == null) {
                    db.excursionDao().insert(excursionToSave);
                } else {
                    db.excursionDao().update(excursionToSave);
                }

                runOnUiThread(this::finish);
            } catch (Exception e) {
                showErrorMessage(ERROR_SAVING_EXCURSION + e.getMessage());
            }
        });
    }

    private boolean isValidDate(String dateString) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            dateFormat.parse(dateString);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isDateWithinRange(String dateString, Vacation vacation) {
        try {
            android.icu.text.SimpleDateFormat dateFormat = new android.icu.text.SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date excursionDate = dateFormat.parse(dateString);
            Date vacationStart = dateFormat.parse(vacation.getStartDate());
            Date vacationEnd = dateFormat.parse(vacation.getEndDate());

            return excursionDate != null && !excursionDate.before(vacationStart) && !excursionDate.after(vacationEnd);
        } catch (ParseException e) {
            return false;
        }
    }

    private void showDatePickerDialog(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                UpdateExcursionActivity.this,
                (view, year1, month1, dayOfMonth) -> {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", month1 + 1, dayOfMonth, year1);
                    editText.setText(selectedDate);
                },
                year,
                month,
                day
        );

        datePickerDialog.show();
    }

    private void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
