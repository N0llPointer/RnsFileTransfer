package com.nollpointer.rnsfiletransfer.view;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nollpointer.rnsfiletransfer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class InfoCard extends LinearLayout{
    private TextView title;
    private LinearLayout container;
    private ArrayList<InfoCardSection> sections;
    private ArrayList<InfoCardDivider> dividers;

    private HashMap<String,String> info;

    public InfoCard(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.info_card_layout,this);

        title = findViewById(R.id.info_card_title);
        container = findViewById(R.id.info_card_container);

        sections = new ArrayList<>();
        dividers = new ArrayList<>();

        setVisibility(GONE);
    }

    public InfoCard(Context context, HashMap<String,String> info) {
        this(context);

        this.info = info;

        initSections();
    }

    private void initSections(){
        ArrayList<String> keys = new ArrayList<>(info.keySet());

        InfoCardSection section;
        InfoCardDivider divider;
        for(String key: keys){
            section = new InfoCardSection(getContext());
            section.setName(key);
            section.setDescription(info.get(key));

            divider = new InfoCardDivider(getContext());

            dividers.add(divider);
            sections.add(section);


            container.addView(section);
            container.addView(divider);
        }

    }

    private void clearParentView(){

        for(InfoCardDivider divider: dividers){
            container.removeView(divider);
        }

        for (InfoCardSection section: sections){
            container.removeView(section);
        }

        sections.clear();
        dividers.clear();

    }

    public void setInfo(HashMap<String,String> info){
        this.info = info;
        clearParentView();
        initSections();
    }

    public void setTitle(String text){
        title.setText(text);
    }

    public String getTitle(){
        return title.getText().toString();
    }


    class InfoCardSection extends LinearLayout{

        private TextView name;
        private TextView description;

        public InfoCardSection(Context context) {
            super(context);

            LayoutInflater inflater = LayoutInflater.from(context);
            inflater.inflate(R.layout.info_card_section_layout,this);

            name = findViewById(R.id.info_card_section_name);
            description = findViewById(R.id.info_card_section_description);

        }

        public void setName(String text){
            name.setText(text);
        }

        public String getName(){
            return name.getText().toString();
        }

        public void setDescription(String text){
            description.setText(text);
        }

        public String getDescription(){
            return description.getText().toString();
        }

    }

    class InfoCardDivider extends View {
        public InfoCardDivider(Context context) {
            super(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,1);
            setLayoutParams(params);
            setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

}
