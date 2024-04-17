package com.example.barberia.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.barberia.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdminManageFragment extends Fragment {

    private CalendarView calendarView;
    private Switch switchAllDay;
    private Spinner timeSpinner;
    private Button submitConstraintButton;
    private boolean isAllDay = false;
    private String selectedDate;
    private String selectedTime;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_manage, container, false);

        calendarView = view.findViewById(R.id.adminManageCalendarView);
        switchAllDay = view.findViewById(R.id.switchAllDay);
        timeSpinner = view.findViewById(R.id.timeSpinner);
        submitConstraintButton = view.findViewById(R.id.submitConstraintButton);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.times_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(adapter);

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selectedCalendarDate = Calendar.getInstance();
            selectedCalendarDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedDate = sdf.format(selectedCalendarDate.getTime());
        });

        switchAllDay.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAllDay = isChecked;
            timeSpinner.setEnabled(!isChecked);
        });

        ImageView goBackIcon = view.findViewById(R.id.goBackIcon);
        goBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the AdminMenuFragment
                NavHostFragment.findNavController(AdminManageFragment.this)
                        .navigate(R.id.action_adminManageFragment_to_adminMenuFragment);
            }
        });

        submitConstraintButton.setOnClickListener(v -> submitConstraint());

        return view;
    }

    private void submitConstraint() {
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isAllDay) {
            selectedTime = timeSpinner.getSelectedItem().toString();
        }

        // Logic to add the constraint to the database
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Constraints");
        String key = dbRef.push().getKey();

        Map<String, Object> constraint = new HashMap<>();
        constraint.put("date", selectedDate);
        constraint.put("allDay", isAllDay);
        if (!isAllDay) {
            constraint.put("time", selectedTime);
        }

        dbRef.child(key).setValue(constraint)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Constraint added successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add constraint", Toast.LENGTH_SHORT).show());
    }
}
