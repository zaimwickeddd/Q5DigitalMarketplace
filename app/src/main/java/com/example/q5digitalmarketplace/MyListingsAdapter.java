package com.example.q5digitalmarketplace;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MyListingsAdapter extends RecyclerView.Adapter<MyListingsAdapter.ViewHolder> {

    private List<Listing> itemsList;
    private final ListingAdapter.OnListingActionListener listener;

    public MyListingsAdapter(List<Listing> itemsList, ListingAdapter.OnListingActionListener listener) {
        this.itemsList = itemsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 🛠️ FIXED: Inflates the correct XML layout file
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_listing, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = itemsList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvTitle.setText(listing.getTitle());

        // Ensure price formatting matches the currency display pattern
        String rawPrice = listing.getPrice();
        if (rawPrice != null && !rawPrice.trim().toUpperCase().startsWith("RM")) {
            holder.tvPrice.setText("RM " + rawPrice.trim());
        } else {
            holder.tvPrice.setText(rawPrice);
        }

        // 🛠️ PROBLEM 2 FIXED: Smart Image Resource Parser applied to thumbnails
        String imgPath = listing.getImagePath();
        if (imgPath != null && !imgPath.trim().isEmpty()) {
            String cleanImg = imgPath.trim();
            try {
                int resId = context.getResources().getIdentifier(cleanImg, "drawable", context.getPackageName());
                if (resId != 0) {
                    holder.imgPreview.setImageResource(resId);
                } else {
                    holder.imgPreview.setImageURI(Uri.parse(cleanImg));
                }
            } catch (Exception e) {
                holder.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // 🛠️ PROBLEM 1 & 3 FIXED: Apply sold layout configurations and block modifications
        if ("Sold".equalsIgnoreCase(listing.getStatus())) {
            holder.tvSoldBadge.setVisibility(View.VISIBLE);

            // Hide BOTH operational status update buttons when item is checked out
            holder.btnMarkSold.setVisibility(View.GONE);
            holder.btnEdit.setVisibility(View.GONE);

            // High contrast muted gray text for sold entries against dark surfaces
            holder.tvTitle.setTextColor(Color.parseColor("#6B7280"));
            holder.tvPrice.setTextColor(Color.parseColor("#6B7280"));
        } else {
            holder.tvSoldBadge.setVisibility(View.GONE);

            // Re-reveal management option controls for active product entries
            holder.btnMarkSold.setVisibility(View.VISIBLE);
            holder.btnEdit.setVisibility(View.VISIBLE);

            // Radiant high contrast titles and prices matching primary dashboard themes
            holder.tvTitle.setTextColor(Color.parseColor("#F9FAFB"));
            holder.tvPrice.setTextColor(Color.parseColor("#06B6D4"));
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(listing));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(listing));
        holder.btnMarkSold.setOnClickListener(v -> listener.onMarkSold(listing));
    }

    @Override
    public int getItemCount() { return itemsList.size(); }

    public void updateList(List<Listing> newList) {
        this.itemsList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvSoldBadge;
        ImageView imgPreview;
        ImageButton btnEdit, btnMarkSold, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_card_title);
            tvPrice = itemView.findViewById(R.id.tv_card_price);
            imgPreview = itemView.findViewById(R.id.img_card_thumb);
            tvSoldBadge = itemView.findViewById(R.id.tv_card_sold_badge);

            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnMarkSold = itemView.findViewById(R.id.btn_mark_sold);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}