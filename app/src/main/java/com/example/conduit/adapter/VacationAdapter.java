package com.example.conduit.adapter;

import com.example.conduit.entities.Vacation;

import java.util.List;

public class VacationAdapter {




    public void setVacations(List<Vacation> vacations) {
//        this.vacations = vacations;
//        notifyDataSetChanged();
    }
    public interface OnVacationListener {
        void onEditClicked(Vacation vacation);
        void onDeleteClicked(Vacation vacation);
        void onShareClicked(Vacation vacation);
        void onAddExcursionsClicked(Vacation vacation);
        void onViewExcursionsClicked(Vacation vacation);
    }
}

