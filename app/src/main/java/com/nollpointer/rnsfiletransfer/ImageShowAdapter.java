package com.nollpointer.rnsfiletransfer;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

public class ImageShowAdapter extends RecyclerView.Adapter<ImageShowAdapter.ViewHolder> {

    private List<Bitmap> images;

    public ImageShowAdapter(List<Bitmap> images) {
        this.images = images;
    }

    public List<Bitmap> getImages() {
        return images;
    }

    public void setImages(List<Bitmap> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        private CardView mCardView;
        ViewHolder(CardView c){
            super(c);
            mCardView = c;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        CardView card = (CardView) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_card,viewGroup,false);
        return new ViewHolder(card);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        CardView card = viewHolder.mCardView;
        ImageView imageView = card.findViewById(R.id.image_card_image);
        imageView.setImageBitmap(images.get(i));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }
}
