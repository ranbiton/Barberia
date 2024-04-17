package com.example.barberia.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.barberia.Adapters.AppointmentsAdapter;
import com.example.barberia.Models.Appointment;
import com.example.barberia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class UserAppointmentsFragment extends Fragment {

    private RecyclerView recyclerView;
    private AppointmentsAdapter adapter;
    private ArrayList<Appointment> appointmentsList = new ArrayList<>();



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_appointments, container, false);

        recyclerView = view.findViewById(R.id.appointmentsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AppointmentsAdapter(getContext(), appointmentsList);
        recyclerView.setAdapter(adapter);
        ImageView goBackIcon = view.findViewById(R.id.goBackIcon);

        goBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Assuming you are using the Navigation component
                // If not, you may need to adjust this code to use your current navigation method
                NavHostFragment.findNavController(UserAppointmentsFragment.this)
                        .navigate(R.id.action_userAppointmentsFragment_to_userMenuFragment);
            }
        });

        loadUserAppointments();

        return view;
    }

    private void loadUserAppointments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("appointments");
        ref.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                appointmentsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Appointment appointment = snapshot.getValue(Appointment.class);
                    if (appointment != null) {
                        String key = snapshot.getKey(); // Retrieve the appointment ID (key) from Firebase
                        appointment.setAppointmentId(key); // Set the appointment ID
                        appointmentsList.add(appointment);
                    }
                }
                adapter.notifyDataSetChanged();
            }


        @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
