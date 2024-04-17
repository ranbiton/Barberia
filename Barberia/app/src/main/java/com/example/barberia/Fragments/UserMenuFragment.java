package com.example.barberia.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.barberia.Adapters.MessagesAdapter;
import com.example.barberia.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserMenuFragment extends Fragment {

    private RecyclerView recyclerView;
    private MessagesAdapter adapter;
    private List<String> messages = new ArrayList<>();
    private List<String> messageIds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_menu, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Load messages before setting adapter
        loadMessages();

        Button btnBookNewAppointment = view.findViewById(R.id.btnBookNewAppointment);
        Button btnMyLastAppointments = view.findViewById(R.id.btnMyLastAppointments);
        Button btnLogOut = view.findViewById(R.id.btnLogOut);

        btnBookNewAppointment.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_userMenuFragment_to_userFragment));

        btnMyLastAppointments.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_userMenuFragment_to_userAppointmentsFragment));

        btnLogOut.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_userMenuFragment_to_loginFragment));

        return view;
    }

    private void loadMessages() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Messages");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messages.clear();
                messageIds.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String message = snapshot.getValue(String.class);
                    String messageId = snapshot.getKey();
                    messages.add(message);
                    messageIds.add(messageId);
                }
                // Initialize adapter here after data is loaded
                adapter = new MessagesAdapter(getContext(), messages, messageIds,false);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }
}
