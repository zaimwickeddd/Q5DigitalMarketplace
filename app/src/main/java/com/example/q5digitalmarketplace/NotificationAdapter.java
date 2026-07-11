package com.example.q5digitalmarketplace;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Cursor cursor;

    public NotificationAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    public void updateCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        this.cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("Title"));
            String message = cursor.getString(cursor.getColumnIndexOrThrow("Message"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("Timestamp"));
            int isRead = cursor.getInt(cursor.getColumnIndexOrThrow("IsRead"));

            holder.tvTitle.setText(title);
            holder.tvMessage.setText(message);
            holder.tvTime.setText(time);
            holder.viewUnread.setVisibility(isRead == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return cursor != null ? cursor.getCount() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvMessage, tvTime;
        View viewUnread;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notif_title);
            tvMessage = itemView.findViewById(R.id.tv_notif_message);
            tvTime = itemView.findViewById(R.id.tv_notif_time);
            viewUnread = itemView.findViewById(R.id.view_unread_dot);
        }
    }
}
