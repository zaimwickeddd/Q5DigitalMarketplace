package com.example.q5digitalmarketplace;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private final List<Listing> listingList;
    private final OnListingActionListener listener;

    public interface OnListingActionListener {
        void onItemClick(Listing listing);
        void onEdit(Listing listing);
        void onDelete(Listing listing);
        void onMarkSold(Listing listing);
    }

    public ListingAdapter(List<Listing> listingList, OnListingActionListener listener) {
        this.listingList = listingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_explore_card, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing listing = listingList.get(position);
        holder.bind(listing, listener);
    }

    @Override
    public int getItemCount() {
        return listingList != null ? listingList.size() : 0;
    }

    public static class ListingViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgProduct;
        private final TextView tvCategoryTag;
        private final TextView tvTitle;
        private final TextView tvPrice;
        private final View btnAddToCart;

        public ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_product);
            tvCategoryTag = itemView.findViewById(R.id.tv_category_tag);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }

        public void bind(final Listing listing, final OnListingActionListener listener) {
            Context context = itemView.getContext();

            // 1. Set Title String safely
            if (listing.getTitle() != null) {
                tvTitle.setText(listing.getTitle());
            } else {
                tvTitle.setText(context.getString(R.string.no_title));
            }

            // 2. Format Price cleanly (Prevents "RM RM xx" duplications)
            String rawPrice = String.valueOf(listing.getPrice()).trim();
            if (rawPrice.toUpperCase().startsWith("RM")) {
                tvPrice.setText(rawPrice);
            } else {
                tvPrice.setText(context.getString(R.string.price_format, rawPrice));
            }

            // 3. Set Category Badge Layout state
            if (tvCategoryTag != null) {
                if (listing.getCategory() != null && !listing.getCategory().isEmpty()) {
                    tvCategoryTag.setText(listing.getCategory());
                    tvCategoryTag.setVisibility(View.VISIBLE);
                } else {
                    tvCategoryTag.setVisibility(View.GONE);
                }
            }

            // 4. FIXED: Using listing.getImagePath() to map perfectly with your Listing model class
            if (imgProduct != null) {
                String dbImageString = listing.getImagePath();

                if (dbImageString != null && !dbImageString.trim().isEmpty()) {
                    dbImageString = dbImageString.trim();

                    try {
                        if (dbImageString.startsWith("content://") || dbImageString.startsWith("file://") || dbImageString.startsWith("/")) {
                            // CASE A: Stored as a local device file path or URI string
                            imgProduct.setImageURI(Uri.parse(dbImageString));
                        } else {
                            // CASE B: Stored as a raw Base64 image data string
                            if (dbImageString.contains(",")) {
                                dbImageString = dbImageString.substring(dbImageString.indexOf(",") + 1);
                            }
                            byte[] decodedBytes = Base64.decode(dbImageString, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

                            if (decodedBitmap != null) {
                                imgProduct.setImageBitmap(decodedBitmap);
                            } else {
                                imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                            }
                        }
                    } catch (Exception e) {
                        imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                    }
                } else {
                    imgProduct.setImageResource(android.R.drawable.ic_menu_gallery);
                }
            }

            // 5. Tap Interaction Routing Systems
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(listing);
                }
            });

            if (btnAddToCart != null) {
                btnAddToCart.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(listing);
                    }
                });
            }
        }
    }
}