package com.example.q5digitalmarketplace;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ViewHolder> {

    private final List<Listing> itemsList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Listing listing);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ListingAdapter(List<Listing> itemsList) {
        this.itemsList = itemsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_marketplace_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Listing listing = itemsList.get(position);
        holder.tvTitle.setText(listing.getTitle());
        holder.tvPrice.setText(listing.getCardPrice());
        holder.tvTag.setText(String.format("%s • %s", listing.getCategory(), listing.getCondition()));

        if (listing.getImagePath() != null && !listing.getImagePath().isEmpty()) {
            try {
                holder.imgPreview.setImageURI(Uri.parse(listing.getImagePath()));
            } catch (Exception e) {
                holder.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgPreview.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(listing);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPreview;
        TextView tvTitle, tvTag, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.img_item_preview);
            tvTitle = itemView.findViewById(R.id.tv_item_title);
            tvTag = itemView.findViewById(R.id.tv_item_tag);
            tvPrice = itemView.findViewById(R.id.tv_item_price);
        }
    }
}