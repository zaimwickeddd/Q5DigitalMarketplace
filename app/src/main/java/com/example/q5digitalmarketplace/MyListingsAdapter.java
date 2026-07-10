package com.example.q5digitalmarketplace;

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

    // MODIFIED: Removed 'final' so this reference can be dynamically updated by searches
    private List<Listing> itemsList;
    private final ListingAdapter.OnListingActionListener listener;

    public MyListingsAdapter(List<Listing> itemsList, ListingAdapter.OnListingActionListener listener) {
        this.itemsList = itemsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_listing_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = itemsList.get(position);
        holder.tvTitle.setText(listing.getTitle());
        holder.tvPrice.setText(listing.getPrice());

        // Handle image loading
        if (listing.getImagePath() != null && !listing.getImagePath().isEmpty()) {
            try {
                holder.imgPreview.setImageURI(Uri.parse(listing.getImagePath()));
            } catch (Exception e) {
                holder.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        // Professional Sold UI Logic Application
        if ("Sold".equalsIgnoreCase(listing.getStatus())) {
            // 1. Reveal the dark overlay badge on top of the thumbnail
            holder.tvSoldBadge.setVisibility(View.VISIBLE);

            // 2. Hide the mark-sold checkmark icon completely to unclutter the UI
            holder.btnMarkSold.setVisibility(View.GONE);

            // 3. Mutate colors to gray out the item text fields
            holder.tvTitle.setTextColor(Color.parseColor("#94A3B8"));
            holder.tvPrice.setTextColor(Color.parseColor("#94A3B8"));
        } else {
            // RESET layout state back to active defaults for regular listings
            holder.tvSoldBadge.setVisibility(View.GONE);
            holder.btnMarkSold.setVisibility(View.VISIBLE);

            holder.tvTitle.setTextColor(Color.parseColor("#1E293B")); // Dark Slate
            holder.tvPrice.setTextColor(Color.parseColor("#2563EB")); // Vibrant Blue
        }

        // Setup Button Listeners
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(listing));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(listing));
        holder.btnMarkSold.setOnClickListener(v -> listener.onMarkSold(listing));
    }

    @Override
    public int getItemCount() { return itemsList.size(); }

    // ADDED: Updates the internal data set when filtering via the search bar
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