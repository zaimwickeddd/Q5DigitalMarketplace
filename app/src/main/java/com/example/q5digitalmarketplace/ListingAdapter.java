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
    private int layoutResId;

    public ListingAdapter(List<Listing> listingsList, Context context, int currentUserId, OnListingActionListener actionListener) {
        this(listingsList, context, currentUserId, actionListener, R.layout.item_product);
    }

    public ListingAdapter(List<Listing> listingsList, Context context, int currentUserId, OnListingActionListener actionListener, int layoutResId) {
        this.listingsList = listingsList;
        this.dbHelper = new DatabaseHelper(context);
        this.currentUserId = currentUserId;
        this.actionListener = actionListener;
        this.layoutResId = layoutResId;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutResId, parent, false);
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

        // 2. Dynamic Category Theme Tint Logic
        applyCategoryTheme(holder, currentItem.getCategory(), context);

        // 3. Dynamic Image Rendering Layer
        if (holder.imgProduct != null && currentItem.getImagePath() != null) {
            String imageName = currentItem.getImagePath();
            int imageResId = context.getResources().getIdentifier(
                    imageName, "drawable", context.getPackageName());

            if (imageResId != 0) {
                holder.imgProduct.setImageResource(imageResId);
                holder.imgProduct.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                // Determine default icon based on category
                holder.imgProduct.setImageResource(getDefaultCategoryIcon(currentItem.getCategory()));
                holder.imgProduct.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
        }

        // 4. Wishlist / Favorite Toggle Logic
        updateHeartIcon(holder, currentItem.getId());

        holder.btnFavorite.setOnClickListener(v -> {
            boolean isCurrentlyWishlisted = dbHelper.isWishlisted(currentUserId, currentItem.getId());
            if (isCurrentlyWishlisted) {
                dbHelper.removeFromWishlist(currentUserId, currentItem.getId());
                Toast.makeText(context, "Removed from Favorites", Toast.LENGTH_SHORT).show();
            } else {
                dbHelper.insertWishlist(currentUserId, currentItem.getId());
                Toast.makeText(context, "Added to Favorites!", Toast.LENGTH_SHORT).show();
            }
            updateHeartIcon(holder, currentItem.getId());
        });

        // 5. Card Container Item Click Navigation Link Router
        holder.itemView.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemClick(currentItem);
            }
        });
    }

    private void updateHeartIcon(ListingViewHolder holder, int itemId) {
        boolean isWishlisted = dbHelper.isWishlisted(currentUserId, itemId);
        if (holder.btnFavorite instanceof android.widget.ImageButton) {
            android.widget.ImageButton btn = (android.widget.ImageButton) holder.btnFavorite;
            btn.setImageResource(isWishlisted ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
            btn.setColorFilter(holder.itemView.getContext().getColor(isWishlisted ? R.color.colorSecondary : R.color.textColorSecondary));
        }
    }

    private void applyCategoryTheme(ListingViewHolder holder, String category, Context context) {
        int bgColor = context.getColor(R.color.colorSurface);
        int textColor = context.getColor(R.color.colorPrimary);

        if ("Electronics".equalsIgnoreCase(category)) {
            bgColor = context.getColor(R.color.tint_blue_bg);
            textColor = context.getColor(R.color.tint_blue_text);
        } else if ("Books".equalsIgnoreCase(category)) {
            bgColor = context.getColor(R.color.tint_purple_bg);
            textColor = context.getColor(R.color.tint_purple_text);
        } else if ("Clothes".equalsIgnoreCase(category) || "Bags".equalsIgnoreCase(category)) {
            bgColor = context.getColor(R.color.tint_amber_bg);
            textColor = context.getColor(R.color.tint_amber_text);
        }

        if (holder.imgContainer != null) {
            holder.imgContainer.setCardBackgroundColor(bgColor);
        }
        if (holder.tvCategoryTag != null) {
            holder.tvCategoryTag.setTextColor(textColor);
        }
        if (holder.imgProduct != null) {
            holder.imgProduct.clearColorFilter();
        }
    }

    private int getDefaultCategoryIcon(String category) {
        if ("Electronics".equalsIgnoreCase(category)) return R.drawable.ic_laptop_outline;
        if ("Books".equalsIgnoreCase(category)) return R.drawable.ic_book_outline;
        if ("Clothes".equalsIgnoreCase(category)) return R.drawable.ic_shirt_outline;
        return R.drawable.ic_person_outline;
    }

    @Override
    public int getItemCount() {
        return listingsList.size();
    }

    // ViewHolder class mapped precisely to your active item_product XML tags
    public static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvTitle, tvPrice, tvCategoryTag;
        com.google.android.material.card.MaterialCardView imgContainer;
        View btnFavorite;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvCategoryTag = itemView.findViewById(R.id.tv_category_tag);
            imgContainer = itemView.findViewById(R.id.img_container);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
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