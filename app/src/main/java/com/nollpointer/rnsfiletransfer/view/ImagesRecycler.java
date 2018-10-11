package com.nollpointer.rnsfiletransfer.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.nollpointer.rnsfiletransfer.ImageShowAdapter;

import java.util.List;

public class ImagesRecycler extends RecyclerView {

    ImageShowAdapter adapter;


    public ImagesRecycler(@NonNull Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));

        setBackgroundColor(Color.WHITE);

        LinearLayoutManager linearLayout = new LinearLayoutManager(getContext());

        DividerItemDecoration dividerItemDecoration =new DividerItemDecoration(getContext(),
                linearLayout.getOrientation());

        setLayoutManager(linearLayout);
        addItemDecoration(dividerItemDecoration);
    }

    public void setImages(List<Bitmap> images){
        if(adapter == null) {
            adapter = new ImageShowAdapter(images);
            setAdapter(adapter);
        }else {
            adapter.setImages(images);
        }
    }

}
