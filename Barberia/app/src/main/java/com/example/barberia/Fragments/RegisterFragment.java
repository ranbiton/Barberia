package com.example.barberia.Fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barberia.Models.User;
import com.example.barberia.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

public class RegisterFragment extends Fragment {

    private TextInputEditText Email, Password, Username, Phone;
    private FirebaseAuth mAuth;

    public RegisterFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initializeUI(view);
        TextView textViewLogin = view.findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginFragment
                NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_registerFragment_to_loginFragment);
            }
        });
        return view;
    }

    private void initializeUI(View view) {
        Email = view.findViewById(R.id.email);
        Password = view.findViewById(R.id.password);
        Username = view.findViewById(R.id.username);
        Phone = view.findViewById(R.id.phone);
        view.findViewById(R.id.signUp).setOnClickListener(v -> registerNewUser());
    }

    private void registerNewUser() {
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String username = Username.getText().toString().trim();
        String phone = Phone.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || username.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out the entire form", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isEmailValid(email)) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isPhoneValid(phone)) {
            Toast.makeText(getContext(), "Please enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Passwords must contain at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        writeDataToDataBaseFirstTime(email, phone, username);
                        Toast.makeText(getContext(), "Registered successfully", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(getView()).navigate(R.id.action_registerFragment_to_loginFragment);
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPhoneValid(CharSequence phone) {
        // Basic validation to check if a phone number could be valid.
        // You might want to enhance this with more specific logic.
        return android.util.Patterns.PHONE.matcher(phone).matches() && phone.length() >= 10;
    }

    private void writeDataToDataBaseFirstTime(String email, String phone, String username) {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            String userUID = firebaseUser.getUid();
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users").child(userUID);
            // Now include isAdmin set to false
            User user = new User(email, phone, username);
            myRef.setValue(user);
        }
    }
}
