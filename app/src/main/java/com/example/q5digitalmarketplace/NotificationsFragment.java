package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationsFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private DatabaseHelper dbHelper;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        dbHelper = new DatabaseHelper(getContext());
        rvNotifications = view.findViewById(R.id.rv_notifications);
        layoutEmpty = view.findViewById(R.id.layout_empty_notif);
        ImageButton btnBack = view.findViewById(R.id.btn_back_notif);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        loadNotifications();
        markAllAsRead();

        return view;
    }

    private void markAllAsRead() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", null);
        if (email != null) {
            int userId = dbHelper.getStuIDByEmail(email);
            dbHelper.markAllNotificationsAsRead(userId);
        }
    }

    private void loadNotifications() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", null);
        
        if (email != null) {
            int userId = dbHelper.getStuIDByEmail(email);
            Cursor cursor = dbHelper.getNotifications(userId);

            if (cursor != null && cursor.getCount() > 0) {
                rvNotifications.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
                
                if (adapter == null) {
                    adapter = new NotificationAdapter(cursor);
                    rvNotifications.setAdapter(adapter);
                } else {
                    adapter.updateCursor(cursor);
                }
            } else {
                rvNotifications.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        }
    }
}
