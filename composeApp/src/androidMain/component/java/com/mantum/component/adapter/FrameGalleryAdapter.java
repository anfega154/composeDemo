package com.mantum.component.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mantum.component.OnSelected;
import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewGalleryAdapter;
import com.mantum.component.helper.DimensionHelper;

import java.io.File;

public class FrameGalleryAdapter extends GalleryAdapter<FrameGalleryAdapter.GalleryItem> {

    private Context context;

    OnSelected<FrameGalleryAdapter.GalleryItem> onSelected;

    OnSelected<FrameGalleryAdapter.GalleryItem> onLikePressed;

    OnSelected<FrameGalleryAdapter.GalleryItem> onDislikePressed;

    public FrameGalleryAdapter(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public OnSelected getOnSelected() {
        return onSelected;
    }

    public void setOnSelected(OnSelected<FrameGalleryAdapter.GalleryItem> onSelected) {
        this.onSelected = onSelected;
    }

    public OnSelected<GalleryItem> getOnLikePressed() {
        return onLikePressed;
    }

    public void setOnLikePressed(OnSelected<GalleryItem> onLikePressed) {
        this.onLikePressed = onLikePressed;
    }

    public OnSelected<GalleryItem> getOnDislikePressed() {
        return onDislikePressed;
    }

    public void setOnDislikePressed(OnSelected<GalleryItem> onDislikePressed) {
        this.onDislikePressed = onDislikePressed;
    }

    @NonNull
    @Override
    public GalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.frame_gallery_layout, parent, false);
        return new FrameViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final GalleryAdapter.ViewHolder holder, int position) {
        GalleryItem item = getItemPosition(position);

        ((FrameViewHolder) holder).textView.setText(item.getTitle());

        DimensionHelper dimensionHelper = new DimensionHelper((Activity) context);

        ((FrameViewHolder) holder).imageView.getLayoutParams().width = (int) ((dimensionHelper.getWidthScreenPx() / 2) - (dimensionHelper.getDimension(36)));
        ((FrameViewHolder) holder).imageView.getLayoutParams().height = ((FrameViewHolder) holder).imageView.getLayoutParams().width;
        ((FrameViewHolder) holder).imageView.requestLayout();

        Glide.with(context)
                .load(item.getImage())
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(((FrameViewHolder) holder).imageView);

        if (item.getLeftImage() != null) {

            ((FrameViewHolder) holder).leftImageView.setColorFilter(context.getResources().getColor(item.isLike() ? R.color.light_blue : R.color.gray));

            ((FrameViewHolder) holder).leftImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getLeftImage())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((FrameViewHolder) holder).leftImageView);

            ((FrameViewHolder) holder).leftImageView.setOnClickListener((view)->{
                if (!item.like) item.like = true;
                ((FrameViewHolder) holder).leftImageView.setColorFilter(context.getResources().getColor(item.isLike() ? R.color.light_blue : R.color.gray));
                if (item.dislike) {
                    item.dislike = !item.like;
                    ((FrameViewHolder) holder).rightImageView.setColorFilter(context.getResources().getColor(!item.isDislike() ? R.color.gray : R.color.light_blue));
                }
                onLikePressed.onClick(item,position);
            });
        }

        if (item.getRightImage() != null) {

            ((FrameViewHolder) holder).rightImageView.setColorFilter(context.getResources().getColor(item.isDislike() ? R.color.light_blue : R.color.gray));

            ((FrameViewHolder) holder).rightImageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(item.getRightImage())
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(((FrameViewHolder) holder).rightImageView);

            ((FrameViewHolder) holder).rightImageView.setOnClickListener((view)->{
                if (!item.dislike) item.dislike = true;
                ((FrameViewHolder) holder).rightImageView.setColorFilter(context.getResources().getColor(item.isDislike() ? R.color.light_blue : R.color.gray));
                if (item.like) {
                    item.like = !item.dislike;
                    ((FrameViewHolder) holder).leftImageView.setColorFilter(context.getResources().getColor(!item.isLike() ? R.color.gray : R.color.light_blue));
                }
                onDislikePressed.onClick(item,position);
            });

        }

        if (onSelected != null)
            ((FrameViewHolder) holder).imageView.setOnClickListener((view)->{
                onSelected.onClick(item, position);
            });

    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;

        public ImageView leftImage;

        public ImageView rightImage;

        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            leftImage = (ImageView) itemView.findViewById(R.id.image_left);
            rightImage = (ImageView) itemView.findViewById(R.id.image_right);
            text = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public static class GalleryItem implements ViewGalleryAdapter<GalleryItem> {

        Integer id;

        String title;

        Object image;

        Object leftImage;

        Object rightImage;

        boolean like;

        boolean dislike;

        public <T extends Object> GalleryItem(int id, String title, T image, T leftImage, T rightImage, boolean like, boolean dislike){
            this.id = id;
            this.title = title;
            this.image = image;
            this.leftImage = leftImage;
            this.rightImage = rightImage;
            this.like = like;
            this.dislike = dislike;
        }

        @NonNull
        public Integer getId() {
            return id;
        }

        @NonNull
        public String getTitle() {
            return title;
        }

        @NonNull
        public Object getImage() {
            return image;
        }

        @Nullable
        public Object getLeftImage() {
            return leftImage;
        }

        @Nullable
        public Object getRightImage() {
            return rightImage;
        }

        public boolean isLike() {
            return like;
        }

        public boolean isDislike() {
            return dislike;
        }

        public void setLike(boolean like) {
            this.like = like;
        }

        public void setDislike(boolean dislike) {
            this.dislike = dislike;
        }

        @Override
        public boolean compareTo(GalleryItem value) {
            return false;
        }

        @Nullable
        @Override
        public File getFile() {
            return null;
        }

        @Nullable
        @Override
        public Uri getUri() {
            return null;
        }

        @NonNull
        @Override
        public GalleryItem getValue() {
            return this;
        }
    }

    public static class FrameViewHolder extends GalleryAdapter.ViewHolder {

        final ImageView imageView;

        final TextView textView;

        final ImageView rightImageView;

        final ImageView leftImageView;

        final CardView cardView;

        public FrameViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.title);
            rightImageView = itemView.findViewById(R.id.image_right);
            leftImageView = itemView.findViewById(R.id.image_left);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

}