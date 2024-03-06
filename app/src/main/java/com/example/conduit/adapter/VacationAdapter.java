package com.example.conduit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.conduit.R;
import com.example.conduit.entities.Vacation;

import java.util.List;

public class VacationAdapter extends RecyclerView.Adapter<VacationAdapter.VacationViewHolder> {
    private List<Vacation> vacations;
    private final OnVacationListener onVacationListener;

    public VacationAdapter(List<Vacation> vacations, OnVacationListener onVacationListener) {
        this.vacations = vacations;
        this.onVacationListener = onVacationListener;
    }

    @NonNull
    @Override
    public VacationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vacation_item, parent, false);
        return new VacationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacationViewHolder holder, int position) {
        if (position >= 0 && position < vacations.size()) {
            holder.bind(vacations.get(position), onVacationListener);
        }
    }

    @Override
    public int getItemCount() {
        return vacations != null ? vacations.size() : 0;
    }

    static class VacationViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewHotel, textViewStartDate, textViewEndDate;
        Button editButton, deleteButton, shareButton, addButton, viewButton;

        VacationViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewVacationTitle);
            textViewHotel = itemView.findViewById(R.id.textViewHotel);
            textViewStartDate = itemView.findViewById(R.id.textViewStartDate);
            textViewEndDate = itemView.findViewById(R.id.textViewEndDate);

            viewButton = itemView.findViewById(R.id.buttonViewExcursions);
            addButton = itemView.findViewById(R.id.buttonAddExcursion);
            editButton = itemView.findViewById(R.id.buttonEdit);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
            shareButton = itemView.findViewById(R.id.buttonShare);
        }

        void bind(Vacation vacation, OnVacationListener listener) {
            textViewTitle.setText(vacation.getTitle());
            textViewHotel.setText(vacation.getHotel());
            textViewStartDate.setText(vacation.getStartDate());
            textViewEndDate.setText(vacation.getEndDate());

            viewButton.setOnClickListener(v -> listener.onViewExcursionsClicked(vacation));
            addButton.setOnClickListener(v -> listener.onAddExcursionsClicked(vacation));
            editButton.setOnClickListener(v -> listener.onEditClicked(vacation));
            deleteButton.setOnClickListener(v -> listener.onDeleteClicked(vacation));
            shareButton.setOnClickListener(v -> listener.onShareClicked(vacation));
        }
    }

    public void setVacations(List<Vacation> vacations) {
        this.vacations = vacations;
        notifyDataSetChanged();
    }

    public interface OnVacationListener {
        void onEditClicked(Vacation vacation);
        void onDeleteClicked(Vacation vacation);
        void onShareClicked(Vacation vacation);
        void onAddExcursionsClicked(Vacation vacation);
        void onViewExcursionsClicked(Vacation vacation);
    }
}
