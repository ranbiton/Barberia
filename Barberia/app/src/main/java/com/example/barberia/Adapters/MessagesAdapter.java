package com.example.barberia.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.barberia.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.ViewHolder> {

    private List<String> messages;
    private List<String> messageIds;
    private Context context;
    private boolean isAdmin;  // Admin flag

    // Constructor now includes an isAdmin parameter
    public MessagesAdapter(Context context, List<String> messages, List<String> messageIds, boolean isAdmin) {
        this.context = context;
        this.messages = messages;
        this.messageIds = messageIds;
        this.isAdmin = isAdmin;  // Set the admin flag
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtMessage.setText(messages.get(position));
        if (isAdmin) {  // Only show the remove button if the user is an admin
            holder.btnRemoveMessage.setVisibility(View.VISIBLE);
            holder.btnRemoveMessage.setOnClickListener(v -> {
                FirebaseDatabase.getInstance().getReference("Messages")
                        .child(messageIds.get(position)).removeValue();
            });
        } else {
            holder.btnRemoveMessage.setVisibility(View.GONE);  // Hide the button if not admin
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageButton btnRemoveMessage;

        public ViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            btnRemoveMessage = itemView.findViewById(R.id.btnRemoveMessage);
        }
    }
}
