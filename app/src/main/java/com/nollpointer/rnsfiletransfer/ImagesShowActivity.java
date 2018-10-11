package com.nollpointer.rnsfiletransfer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class ImagesShowActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_show);
        Toolbar toolbar = findViewById(R.id.images_show_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        RecyclerView recyclerView = findViewById(R.id.images_show_recycler);

        LinearLayoutManager linearLayout = new LinearLayoutManager(this);

        DividerItemDecoration dividerItemDecoration =new DividerItemDecoration(recyclerView.getContext(),
                linearLayout.getOrientation());

        recyclerView.setLayoutManager(linearLayout);
        recyclerView.addItemDecoration(dividerItemDecoration);

        //ImageShowAdapter adapter = new ImageShowAdapter(getIntent().<ImageWrapper>getParcelableArrayListExtra("Images"));

        //recyclerView.setAdapter(adapter);
    }
}
