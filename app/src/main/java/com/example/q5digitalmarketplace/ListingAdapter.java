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
    private OnListingActionListener actionListener; // Tracks item click events

    // Unified Constructor setup containing your full interface routing maps
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

        // 1. Bind Text Descriptive Fields safely
        if (holder.tvTitle != null && currentItem.getTitle() != null) {
            holder.tvTitle.setText(currentItem.getTitle());
        }
        if (holder.tvPrice != null && currentItem.getPrice() != null) {
            holder.tvPrice.setText("RM " + currentItem.getPrice());
        }
        if (holder.tvCategoryTag != null && currentItem.getCategory() != null) {
            holder.tvCategoryTag.setText(currentItem.getCategory());
        }

        // 2. Dynamic Image Rendering Layer (Loads lenovo_laptop, mandarin_books, etc.)
        if (holder.imgProduct != null && currentItem.getImagePath() != null) {
            String imageName = currentItem.getImagePath();
            int imageResId = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName());

            if (imageResId != 0) {
                holder.imgProduct.setImageResource(imageResId);
            } else {
                // Fallback placeholder icon asset if specific resource name string isn't found
                holder.imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        // 3. Card Container Item Click Navigation Link Router
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(currentItem);
            }
        });

        // 4. Interactive Wishlist Star Persistence Logic
        if (holder.ivFavStar != null) {
            boolean isFavorited = dbHelper.isWishlisted(currentUserId, currentItem.getId());

            // Synchronize starting visual state with SQLite database records
            if (isFavorited) {
                holder.ivFavStar.setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                holder.ivFavStar.setImageResource(android.R.drawable.btn_star_big_off);
            }

            // Favorites quick toggle tap click click handler
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
    }

    @Override
    public int getItemCount() {
        return listingsList.size();
    }

    // ViewHolder class mapped precisely to your active item_marketplace_card XML tags
    public static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFavStar, imgProduct;
        TextView tvTitle, tvPrice, tvCategoryTag;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFavStar = itemView.findViewById(R.id.iv_fav_star);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCategoryTag = itemView.findViewById(R.id.tv_category_tag);
        }
    }

    // Unified interface declarations supporting structural project requirements
    public interface OnListingActionListener {
        void onItemClick(Listing listing);
        void onEdit(Listing listing);
        void onDelete(Listing listing);
        void onMarkSold(Listing listing);
    }
}