package com.example.barberia.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.barberia.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.navigation.Navigation;

public class LoginFragment extends Fragment {

    private TextInputEditText Email, Password;
    private FirebaseAuth mAuth;
    private boolean isLoginInProgress = false;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initializeUI(view);
        return view;
    }

    private void initializeUI(View view) {
        Email = view.findViewById(R.id.email);
        Password = view.findViewById(R.id.password);
        view.findViewById(R.id.signIn).setOnClickListener(v -> loginUser());
        view.findViewById(R.id.signUp).setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_registerFragment));
        view.findViewById(R.id.forgotPassword).setOnClickListener(v -> resetUserPassword());
    }

    private void loginUser() {
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out the entire form", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isLoginInProgress) {
            isLoginInProgress = true;
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                isLoginInProgress = false;
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        navigateBasedOnUserRole(user);
                    }
                } else {
                    Toast.makeText(getContext(), "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "Please wait, login in progress...", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateBasedOnUserRole(FirebaseUser user) {
        View view = getView(); // Get the current view
        if (view == null) {
            return; // In case getView() returns null, which is unlikely in this context but a good practice to check
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    boolean admin = dataSnapshot.child("admin").getValue(Boolean.class);
                    if (admin) {
                        Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_adminMenuFragment);
                    } else {
                        Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_userMenuFragment);
                    }
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void resetUserPassword() {
        String email = Email.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Reset instructions sent to your email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to send reset instructions", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
