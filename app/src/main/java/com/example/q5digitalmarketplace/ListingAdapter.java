package com.example.q5digitalmarketplace;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private List<Listing> listingsList;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private OnListingActionListener actionListener;

    public ListingAdapter(List<Listing> listingsList, Context context, int currentUserId, OnListingActionListener actionListener) {
        this.listingsList = listingsList;
        this.dbHelper = new DatabaseHelper(context);
        this.currentUserId = currentUserId;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marketplace_card, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing currentItem = listingsList.get(position);
        Context context = holder.itemView.getContext();

        // Bind data fields securely
        if (holder.tvTitle != null && currentItem.getTitle() != null) {
            holder.tvTitle.setText(currentItem.getTitle());
        }
        if (holder.tvPrice != null && currentItem.getPrice() != null) {
            holder.tvPrice.setText("RM " + currentItem.getPrice());
        }

        // Tap listener for full details viewing
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(currentItem);
            }
        });

        // Sync visual star indicator safely via updated SQLite validation mapping
        boolean isFavorited = dbHelper.isWishlisted(currentUserId, currentItem.getId());

        if (isFavorited) {
            holder.ivFavStar.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.ivFavStar.setImageResource(android.R.drawable.btn_star_big_off);
        }

        // Handle wishlist toggle click interactions
        holder.ivFavStar.setOnClickListener(v -> {
            if (dbHelper.isWishlisted(currentUserId, currentItem.getId())) {
                dbHelper.removeFromWishlist(currentUserId, currentItem.getId());
                holder.ivFavStar.setImageResource(android.R.drawable.btn_star_big_off);
                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.insertWishlist(currentUserId, currentItem.getId());
                holder.ivFavStar.setImageResource(android.R.drawable.btn_star_big_on);
                Toast.makeText(context, "Added to Favorites!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listingsList.size();
    }

    public static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFavStar;
        TextView tvTitle, tvPrice;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            // REFINED: Safely bound against explicit target layout fields
            ivFavStar = itemView.findViewById(R.id.iv_fav_star);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }

    public interface OnListingActionListener {
        void onItemClick(Listing listing);
        void onEdit(Listing listing);
        void onDelete(Listing listing);
        void onMarkSold(Listing listing);
    }
}