package com.example.barberia.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberia.Models.Appointment; // Ensure this import matches your Appointment model location
import com.example.barberia.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.example.barberia.Adapters.AppointmentsAdapter; // Ensure this import matches your AppointmentsAdapter location

public class AdminFragment extends Fragment {

    private CalendarView adminCalendarView;
    private RecyclerView appointmentsRecyclerView;
    private ArrayList<Appointment> appointmentsList; // Uncommented and ready for use
    private AppointmentsAdapter adapter; // Uncommented and ready for use

    private TextView noAppointmentsText; // Declaration for the no appointments TextView

    private Button manageAppointmentsButton; // Declare the manage button



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin, container, false);

        adminCalendarView = view.findViewById(R.id.adminCalendarView);
        appointmentsRecyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        noAppointmentsText = view.findViewById(R.id.noAppointmentsText); // Initialize the no appointments TextView
        appointmentsList = new ArrayList<>();
        adapter = new AppointmentsAdapter(getContext(), appointmentsList); // Ensure your adapter constructor matches this signature
        appointmentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        appointmentsRecyclerView.setAdapter(adapter);

        adminCalendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = sdf.format(selectedDate.getTime());
            fetchAppointmentsForDate(formattedDate);
        });

        ImageView goBackIcon = view.findViewById(R.id.goBackIcon);

        // Set an OnClickListener to navigate back to the AdminMenuFragment when clicked
        goBackIcon.setOnClickListener(v -> {
            // Using NavController to navigate
            NavHostFragment.findNavController(AdminFragment.this)
                    .navigate(R.id.action_adminFragment_to_adminMenuFragment);
        });


        return view;
    }

    private void fetchAppointmentsForDate(String date) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("appointments");
        Query query = dbRef.orderByChild("date").equalTo(date);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appointmentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        String key = snapshot.getKey(); // Get the appointment ID from Firebase
                        appointment.setAppointmentId(key); // Set the appointment ID
                        appointmentsList.add(appointment);
                    }
                }
                adapter.notifyDataSetChanged();
                noAppointmentsText.setVisibility(appointmentsList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

}