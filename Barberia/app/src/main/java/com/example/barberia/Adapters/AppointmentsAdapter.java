package com.example.barberia.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberia.Models.Appointment;
import com.example.barberia.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AppointmentsAdapter extends RecyclerView.Adapter<AppointmentsAdapter.ViewHolder> {

    private final Context context;
    private final ArrayList<Appointment> appointments;

    public AppointmentsAdapter(Context context, ArrayList<Appointment> appointments) {
        this.context = context;
        this.appointments = appointments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.appointment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointments.get(position);
        holder.textViewUsername.setText(appointment.getUsername());
        holder.textViewDate.setText(appointment.getDate());
        holder.textViewTime.setText(appointment.getTime());

        holder.buttonRemoveAppointment.setOnClickListener(v -> {
            // Correctly retrieve the current position
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                removeAppointment(appointments.get(currentPosition).getAppointmentId(), currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointments.size();
    }

    // Inside AppointmentsAdapter
    private void promptEmailIntent(String emailAddress) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { emailAddress });
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Appointment Cancellation");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Your appointment has been cancelled.");

        try {
            context.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeAppointment(String appointmentId, int position) {
        if (appointmentId == null) {
            Toast.makeText(context, "Error: Appointment ID is null", Toast.LENGTH_SHORT).show();
            return; // Do not proceed if the appointmentId is null
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("appointments").child(appointmentId);
        dbRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appointments.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Appointment removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to remove appointment", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUsername, textViewDate, textViewTime;
        ImageButton buttonRemoveAppointment;

        public ViewHolder(View itemView) {
            super(itemView);
            textViewUsername = itemView.findViewById(R.id.textViewTreatment);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewTime = itemView.findViewById(R.id.textViewTimeSlot);
            buttonRemoveAppointment = itemView.findViewById(R.id.removeAppointmentButton);
        }
    }
}
