package com.example.barberia.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.barberia.R;

public class AdminMenuFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_menu, container, false);

        Button btnAppointments = view.findViewById(R.id.btnAppointments);
        Button btnManageCalendar = view.findViewById(R.id.btnManageCalendar);
        Button btnMessageForCustomers = view.findViewById(R.id.btnMessageForCustomers);
        Button btnLogOut = view.findViewById(R.id.btnLogOut);

        btnAppointments.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminMenuFragment.this)
                        .navigate(R.id.action_adminMenuFragment_to_adminFragment));

        btnManageCalendar.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminMenuFragment.this)
                        .navigate(R.id.action_adminMenuFragment_to_adminManageFragment));

        btnMessageForCustomers.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminMenuFragment.this)
                        .navigate(R.id.action_adminMenuFragment_to_adminMsgsFragment));

        btnLogOut.setOnClickListener(v ->
                NavHostFragment.findNavController(AdminMenuFragment.this)
                        .navigate(R.id.action_adminMenuFragment_to_loginFragment));

        return view;
    }
}
