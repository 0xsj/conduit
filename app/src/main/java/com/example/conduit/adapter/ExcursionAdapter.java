package com.example.conduit.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conduit.R;
import com.example.conduit.entities.Excursion;

import java.util.List;

public class ExcursionAdapter extends RecyclerView.Adapter<ExcursionAdapter.ExcursionViewHolder> {
    private List<Excursion> excursions;
    private final OnExcursionListener onExcursionListener;

    public ExcursionAdapter(List<Excursion> excursions, OnExcursionListener onExcursionListener) {
        this.excursions = excursions;
        this.onExcursionListener = onExcursionListener;
    }

    @NonNull
    @Override
    public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("ExcursionAdapter", "onCreateViewHolder: Creating new ViewHolder");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.excursion_item, parent, false);
        return new ExcursionViewHolder(view, onExcursionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcursionViewHolder holder, int position) {
        if (position >= 0 && position < excursions.size()) {
            Excursion excursion = excursions.get(position);
            Log.d("ExcursionAdapter", "onBindViewHolder: Binding data for position " + position + ", Title: " + excursion.getTitle());
            holder.bind(excursion, onExcursionListener);
        }
    }

    @Override
    public int getItemCount() {
        return excursions != null ? excursions.size() : 0;
    }

    static class ExcursionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDate;
        Button editButton, deleteButton, saveButton, alertButton;

        ExcursionViewHolder(View itemView, OnExcursionListener listener) {
            super(itemView);

            textViewTitle = itemView.findViewById(R.id.textViewExcursionTitle);
            textViewDate = itemView.findViewById(R.id.textViewExcursionDate);

            editButton = itemView.findViewById(R.id.buttonExcursionEdit);
            deleteButton = itemView.findViewById(R.id.buttonExcursionDelete);
            saveButton = itemView.findViewById(R.id.buttonExcursionSave);
            alertButton = itemView.findViewById(R.id.buttonSetExcursionAlert);


        }

        void bind(Excursion excursion, OnExcursionListener listener) {
            textViewDate.setText(excursion.getDate());
            textViewTitle.setText(excursion.getTitle());

            editButton.setOnClickListener(v -> {
                Log.d("ExcursionAdapter", "Edit Button clicked for excursion:" + excursion.getTitle());
                listener.onEditExcursionClicked(excursion);
            });
            deleteButton.setOnClickListener(v -> {
                Log.d("ExcursionAdapter", "Delete Button clicked for excursion:"+ excursion.getTitle());
                listener.onDeleteExcursionClicked(excursion);
            });
            saveButton.setOnClickListener(v -> {
                Log.d("ExcursionAdapter", "Save Button clicked for excursion:"+ excursion.getTitle());
                listener.onSaveExcursionClicked(excursion);
            });
            alertButton.setOnClickListener(v -> {
                Log.d("ExcursionAdapter", "Alert Button clicked for excursion:"+ excursion.getTitle());
                listener.onSetExcursionAlertClicked(excursion);
            });
        }
    }

    public void setExcursions(List<Excursion> excursions) {
        this.excursions = excursions;
        notifyDataSetChanged();
        Log.d("ExcursionAdapter", "Adapter updated with " + getItemCount() + " items");
    }

    public interface OnExcursionListener {
        void onEditExcursionClicked(Excursion excursion);
        void onDeleteExcursionClicked(Excursion excursion);
        void onSaveExcursionClicked(Excursion excursion);
        void onSetExcursionAlertClicked(Excursion excursion);
    }
}
