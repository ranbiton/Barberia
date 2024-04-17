package com.example.barberia.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.barberia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class UserFragment extends Fragment {

    private CalendarView calendarView;
    private Spinner timeSpinner;
    private Button bookButton, seeMyAppointmentsButton, logOutButton;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String selectedFormattedDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        calendarView = view.findViewById(R.id.calendarView);
        timeSpinner = view.findViewById(R.id.timeSpinner);
        bookButton = view.findViewById(R.id.appointment_bookButton);

        initializeSpinnerWithAllTimes();

        calendarView.setOnDateChangeListener((view12, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            selectedFormattedDate = sdf.format(selectedDate.getTime());
            updateSpinnerWithAvailableAndConstrainedTimes(selectedFormattedDate);
        });

        bookButton.setOnClickListener(v -> bookAppointment());

        ImageView goBackIcon = view.findViewById(R.id.goBackIcon);
        goBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the AdminMenuFragment
                NavHostFragment.findNavController(UserFragment.this)
                        .navigate(R.id.action_userFragment_to_userMenuFragment);
            }
        });


        return view;
    }


    private void initializeSpinnerWithAllTimes() {
        // Create an array of all available times
        String[] allTimesArray = getResources().getStringArray(R.array.times_array);

        // Create an adapter with all available times
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, allTimesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the spinner
        timeSpinner.setAdapter(adapter);
    }

    private void updateSpinnerWithAvailableAndConstrainedTimes(String selectedDate) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        List<String> allTimesList = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.times_array)));
        ArrayList<String> availableTimes = new ArrayList<>(allTimesList);

        // Fetch constraints for the selected date
        dbRef.child("Constraints").orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot constraintSnapshot : dataSnapshot.getChildren()) {
                    if (constraintSnapshot.child("allDay").getValue(Boolean.class) != null && constraintSnapshot.child("allDay").getValue(Boolean.class)) {
                        availableTimes.clear();
                        break;
                    } else {
                        String constrainedTime = constraintSnapshot.child("time").getValue(String.class);
                        availableTimes.remove(constrainedTime);
                    }
                }

                // Now fetch and remove booked appointments times
                dbRef.child("appointments").orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot appointmentSnapshot : dataSnapshot.getChildren()) {
                            String bookedTime = appointmentSnapshot.child("time").getValue(String.class);
                            availableTimes.remove(bookedTime);
                        }

                        // Update spinner with available times not constrained or booked
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, availableTimes);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        timeSpinner.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load constraints", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookAppointment() {
        if (selectedFormattedDate == null) {
            Toast.makeText(requireContext(), "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedTime = timeSpinner.getSelectedItem().toString();

        Log.d("UserFragment", "Attempting to book: Date - " + selectedFormattedDate + ", Time - " + selectedTime);

        if (isDatePassed(selectedFormattedDate)) {
            Toast.makeText(requireContext(), "Cannot book an appointment for a past date.", Toast.LENGTH_SHORT).show();
            return;
        }

        checkAppointmentAvailability(selectedFormattedDate, selectedTime);
    }

    private void checkAppointmentAvailability(final String date, final String time) {
        DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference().child("appointments");
        appointmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean isTimeSlotAvailable = true;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String bookedDate = snapshot.child("date").getValue(String.class);
                    String bookedTime = snapshot.child("time").getValue(String.class);
                    if (date.equals(bookedDate) && time.equals(bookedTime)) {
                        isTimeSlotAvailable = false;
                        break;
                    }
                }

                if (isTimeSlotAvailable) {
                    writeAppointmentToFirebase(date, time);
                } else {
                    Toast.makeText(requireContext(), "This time slot is already booked.", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeAppointmentToFirebase(String date, String time) {
        String userId = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String username = dataSnapshot.child("userName").getValue(String.class);
                DatabaseReference appointmentsRef = FirebaseDatabase.getInstance().getReference().child("appointments");
                String appointmentId = appointmentsRef.push().getKey();
                HashMap<String, Object> appointmentDetails = new HashMap<>();
                appointmentDetails.put("userId", userId);
                appointmentDetails.put("date", date);
                appointmentDetails.put("time", time);
                appointmentDetails.put("username", username);

                appointmentsRef.child(appointmentId).setValue(appointmentDetails)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Appointment booked successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to book appointment", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to fetch user information", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean isDatePassed(String appointmentDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            Calendar appointmentCal = Calendar.getInstance();
            appointmentCal.setTime(dateFormat.parse(appointmentDate));

            return appointmentCal.before(today);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }
}