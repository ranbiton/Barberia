package com.example.barberia.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberia.Adapters.MessagesAdapter;
import com.example.barberia.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AdminMsgsFragment extends Fragment {
    private EditText messageInput;
    private RecyclerView recyclerViewMessages;
    private MessagesAdapter adapter;
    private ArrayList<String> messages = new ArrayList<>();
    private ArrayList<String> messageIds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_msgs, container, false);

        messageInput = view.findViewById(R.id.messageInput);
        Button submitMessageButton = view.findViewById(R.id.submitMessageButton);
        recyclerViewMessages = view.findViewById(R.id.recyclerViewMessages);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessagesAdapter(getContext(), messages, messageIds,true);
        recyclerViewMessages.setAdapter(adapter);

        submitMessageButton.setOnClickListener(v -> submitMessage());
        loadMessages();

        ImageView goBackIcon = view.findViewById(R.id.goBackIcon);
        goBackIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the AdminMenuFragment
                NavHostFragment.findNavController(AdminMsgsFragment.this)
                        .navigate(R.id.action_adminMsgsFragment_to_adminMenuFragment);
            }
        });

        return view;
    }

    private void submitMessage() {
        String msg = messageInput.getText().toString().trim();
        if (!msg.isEmpty()) {
            FirebaseDatabase.getInstance().getReference("Messages")
                    .push().setValue(msg).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            messageInput.setText("");
                            Toast.makeText(getContext(), "Message submitted successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to submit message", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadMessages() {
        FirebaseDatabase.getInstance().getReference("Messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        messages.clear();
                        messageIds.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            messages.add(snapshot.getValue(String.class));
                            messageIds.add(snapshot.getKey());
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
