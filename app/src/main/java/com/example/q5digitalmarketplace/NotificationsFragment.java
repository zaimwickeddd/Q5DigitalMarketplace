package com.example.q5digitalmarketplace;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private Cursor currentCursor; // Tracks the cursor lifecycle to prevent memory resource leaks

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        dbHelper = new DatabaseHelper(getContext());
        rvNotifications = view.findViewById(R.id.rv_notifications);
        layoutEmpty = view.findViewById(R.id.layout_empty_notif);

        // 🛠️ FIXED: Changed type from ImageButton to View to prevent ClassCastException crash
        View btnBack = view.findViewById(R.id.btn_back_notif);

        rvNotifications.setLayoutManager(new LinearLayoutManager(getContext()));

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Running these procedures inside onViewCreated ensures layout bindings are fully ready
        loadNotifications();
        markAllAsRead();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Force evaluation updates if the viewport is re-entered from a backstack pop action
        loadNotifications();
    }

    private void markAllAsRead() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", null);
        if (email != null) {
            int userId = dbHelper.getStuIDByEmail(email);
            dbHelper.markAllNotificationsAsRead(userId);
        }
    }

    private void loadNotifications() {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", null);

        if (email != null) {
            int userId = dbHelper.getStuIDByEmail(email);

            // Close any existing open cursor structure before requesting a fresh data query instance
            if (currentCursor != null && !currentCursor.isClosed()) {
                currentCursor.close();
            }

            currentCursor = dbHelper.getNotifications(userId);

            if (currentCursor != null && currentCursor.getCount() > 0) {
                rvNotifications.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);

                if (adapter == null) {
                    adapter = new NotificationAdapter(currentCursor);
                    rvNotifications.setAdapter(adapter);
                } else {
                    adapter.updateCursor(currentCursor);
                }
            } else {
                rvNotifications.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Safely close database cursor handles to protect application memory structures
        if (currentCursor != null && !currentCursor.isClosed()) {
            currentCursor.close();
        }
    }
}