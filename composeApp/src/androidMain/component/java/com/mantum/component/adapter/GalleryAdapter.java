package com.mantum.component.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mantum.component.Mantum;
import com.mantum.demo.R;
import com.mantum.component.OnSelected;
import com.mantum.component.adapter.handler.ViewGalleryAdapter;

public class GalleryAdapter<T extends ViewGalleryAdapter<T>> extends Mantum.Adapter<T, GalleryAdapter.ViewHolder> {

    private OnSelected<T> onSelectedPhoto;

    public GalleryAdapter(@NonNull Context context) {
        super(context);
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_layout, parent, false);
        return new GalleryAdapter.ViewHolder(view);
    }

    public void showMessageEmpty(@NonNull View view) {
        RelativeLayout empty = view.findViewById(R.id.empty);
        empty.setVisibility(isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapter.ViewHolder holder, int position) {
        final T value = getItemPosition(position);
        if (value == null) {
            return;
        }

        if (value.getFile() != null) {
            String path = value.getFile().getPath();
            String extension = path.substring(path.lastIndexOf(".") + 1);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

            if (mimeType != null && mimeType.startsWith("image/")) {
                Glide.with(context)
                        .load(value.getFile())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .fitCenter()
                        .crossFade()
                        .placeholder(R.drawable.loading_img)
                        .error(R.drawable.ic_broken_image)
                        .into(holder.imageView);
            } else {
                holder.textView.setText(value.getFile().getName());
                if (value.getFile().exists()) {
                    holder.imageView.setImageResource(R.drawable.ic_files);
                } else {
                    holder.imageView.setImageResource(R.drawable.ic_hide_image);
                }
            }
        }

        if (value.getUri() != null) {
            Glide.with(context)
                    .load(value.getUri())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .fitCenter()
                    .crossFade()
                    .error(R.drawable.ic_broken_image)
                    .placeholder(R.drawable.loading_img)
                    .into(holder.imageView);
        }

        if (onSelectedPhoto != null) {
            holder.imageView.setOnClickListener(v ->
                    onSelectedPhoto.onClick(value.getValue(), holder.getAdapterPosition()));
        }

        if (onSelectedPhoto != null) {
            holder.imageView.setOnLongClickListener(v ->
                    onSelectedPhoto.onLongClick(value.getValue(), holder.getAdapterPosition()));
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Glide.clear(holder.imageView);
        holder.imageView.setImageDrawable(null);

        super.onViewRecycled(holder);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnAction(@NonNull OnSelected<T> onSelectedPhoto) {
        this.onSelectedPhoto = onSelectedPhoto;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView imageView;
        final TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.text);
        }
    }
}