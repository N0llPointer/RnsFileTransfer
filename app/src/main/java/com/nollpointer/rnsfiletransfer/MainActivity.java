package com.nollpointer.rnsfiletransfer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TimingLogger;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nollpointer.rnsfiletransfer.view.ImagesRecycler;
import com.nollpointer.rnsfiletransfer.view.InfoCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity{

    private static final String SEGMENT_CARD = "segment";
    private static final String POST_DECODE_SEGMENT_CARD = "post_segment";
    private static final String INITIAL_INFO_CARD = "initial";
    private static final String RESIDUE_CARD = "residue";
    private static final String MESSAGES_CARD = "messages";

    public static final int CAMERA_REQUEST_CODE = 11;

    private FirebaseDatabase database;
    private DatabaseReference messagesReference;

    private EditText editText;
    private TextView textView;
    private Toolbar toolbar;
    private LinearLayout infoCardsContainer;
    private FrameLayout contentContainer;
    private ImagesRecycler imagesRecycler;

    private TreeMap<String,InfoCard> infoCards;
    private TreeMap<Integer,Bitmap> images;

    private byte[] segments;

    private String[] messages;
    private String[] additionalMessages;

    private int[] residueSequences;

    private int[] modules = {2,5,7,9};

    private int[] basis;

    private LinearLayout.LayoutParams params;

    private boolean isNumberInsert = true;


    private int bitCountToEncrypt = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        //FirebaseApp.initializeApp(this);


        database = FirebaseDatabase.getInstance();
        messagesReference = database.getReference().child("messages");

        editText = findViewById(R.id.info_insert);
        textView = findViewById(R.id.number_info);
        toolbar = findViewById(R.id.toolbar);
        infoCardsContainer = findViewById(R.id.info_container);
        contentContainer = findViewById(R.id.content_container);

        toolbar.inflateMenu(R.menu.toolbar_menu);

        toolbar.setTitle("Тип: Числа");

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTypePicker(view);
            }
        });

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toolbar.setNavigationIcon(null);
                contentContainer.removeView(imagesRecycler);
            }
        });

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.decode_button:
                        generateNumber();
                        break;
                    case R.id.info_button:
                        showInfoDialog();
                        break;
                    case R.id.camera_button:
                        BitmapFactory.Options opts = new BitmapFactory.Options();
                        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        opts.inScaled = false;
                        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                                R.drawable.test_image,opts);
                        startImageEncrypting(icon);
                        break;
                    case R.id.foundations_button:
                        break;
                }

                return true;
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //if(charSequence.length() < 19 && charSequence.length() > 0)
                    //setNumberInfo(Long.parseLong(charSequence.toString()));

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        findViewById(R.id.encode_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startAction();
            }
        });

        infoCards = new TreeMap<>();

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 45;

        findBasis();
        countMaxBitQuantity();

        imagesRecycler = new ImagesRecycler(this);    //TODO объединить инциализацию view в отделальный метод

        initInfoCards();

    }

    private void startImageEncrypting(Bitmap image){

        int[] initialImagePixels = new int[image.getWidth()*image.getHeight()];

        image.getPixels(initialImagePixels,0,1000,0,0,1000,1000);

        Log.wtf("TAG INit",Integer.toHexString(initialImagePixels[0]));

        byte[] pixelSegments = divideImageInSegments(initialImagePixels);

        Log.wtf("TAG SEGS",Integer.toHexString(pixelSegments[0] & 0xff) + " " + Integer.toHexString(pixelSegments[1]& 0xff) + " " + Integer.toHexString(pixelSegments[2]& 0xff) + " " + Integer.toHexString(pixelSegments[3]& 0xff));

        ArrayList<Bitmap> images = new ArrayList<>(3);
        images.add(image);

        Log.wtf("TAG",initialImagePixels.length + " " + image.getHeight() + " " + image.getWidth());

        Bitmap img = createImageOutOfResidueFor2(pixelSegments);
        images.add(img);

        img = createImageOutOfResidueFor5(pixelSegments);
        images.add(img);

        img = createImageOutOfResidueFor7(pixelSegments);
        images.add(img);

        img = createImageOutOfResidueFor9(pixelSegments);
        images.add(img);
//        for(int i=0;i<4;i++) {
//            Bitmap bitmap = createImageOutOfResidue(pixelSegments,modules[i]);
//            images.add(bitmap);
//        }

        //int[] initialPixels = decodeResidueImagesIntoSegments(images.subList(1,5));


        images.add(combineImageSegmentsIntoImage(pixelSegments));

        showImages(images);

    }

    private byte[] divideImageInSegments(int[] image) {
        int size = image.length * 4;
        byte[] pixelArray = new byte[size];
        for (int i = 0; i < size; i += 4) {
            for (int j = 0; j < 4; j++)
                pixelArray[i + j] = (byte) ((image[i/4] << 8 * j) >> 24);
        }
        return pixelArray;

    }

    private Bitmap createImageOutOfResidue(byte[] pixelSegments,int module){ //TODO сделать динамическое определение длины изображения, и его размера


        int[] residueImagePixels = new int[pixelSegments.length/8];

        short[] intermediateImagePixels = new short[pixelSegments.length/4];

        Arrays.fill(intermediateImagePixels, (short) 0);

        Log.wtf("TAG",module + "");

        for (int i = 0; i < pixelSegments.length/4; i += 4) {
            intermediateImagePixels[i] =(short)(pixelSegments[i] & 0xff);
            for (int j = 1; j < 4; j
                    ++)
                intermediateImagePixels[i] |= (short) (((pixelSegments[i+j] & 0xff) % module) << bitCountToEncrypt);
        }

        for(int i=0,j=0;i<intermediateImagePixels.length;i+=2,j++){
            residueImagePixels[j] |= ((int) intermediateImagePixels[i]) << 16;
            residueImagePixels[j] |= intermediateImagePixels[i+1];
        }

        return Bitmap.createBitmap(residueImagePixels,1000,500,Bitmap.Config.ARGB_8888);
    }

    private Bitmap createImageOutOfResidueFor2(byte[] pixelSegments){
        int[] residueImagePixels = new int[pixelSegments.length/16];

        for(int i=0;i<pixelSegments.length;i+=16){
            int number = 0;
            for(int j=0;j<16;j++){
                number = number << 2;
                number |= (pixelSegments[i+j] & 0xff) % 2;
            }
            residueImagePixels[i/16] = number;
        }

        return Bitmap.createBitmap(residueImagePixels,500,500,Bitmap.Config.ARGB_8888);
    }

    private Bitmap createImageOutOfResidueFor5(byte[] pixelSegments){
        int[] residueImagePixels = new int[pixelSegments.length/8];

        for(int i=0;i<pixelSegments.length;i+=8){
            int number = 0;
            for(int j=0;j<8;j++){
                number = number << 4;
                number |= (pixelSegments[i+j] & 0xff) % 5;
            }
            residueImagePixels[i/8] = number;
        }

        return Bitmap.createBitmap(residueImagePixels,1000,500,Bitmap.Config.ARGB_8888);
    }

    private Bitmap createImageOutOfResidueFor7(byte[] pixelSegments){
        int[] residueImagePixels = new int[pixelSegments.length/8];

        for(int i=0;i<pixelSegments.length;i+=8){
            int number = 0;
            for(int j=0;j<8;j++){
                number = number << 4;
                number |= (pixelSegments[i+j] & 0xff) % 7;
            }
            residueImagePixels[i/8] = number;
        }

        return Bitmap.createBitmap(residueImagePixels,1000,500,Bitmap.Config.ARGB_8888);
    }

    private Bitmap createImageOutOfResidueFor9(byte[] pixelSegments){
        int[] residueImagePixels = new int[pixelSegments.length/8];

        for(int i=0;i<pixelSegments.length;i+=8){
            int number = 0;
            for(int j=0;j<8;j++){
                number = number << 4;
                number |= (pixelSegments[i+j] & 0xff) % 9;
            }
            residueImagePixels[i/8] = number;
        }

        return Bitmap.createBitmap(residueImagePixels,1000,500,Bitmap.Config.ARGB_8888);
    }

    private int countBitsToEncrypt(int module){  //Находит количк
        double count = Math.log(module) / Math.log(2);

//        count = Math.ceil(count);
//        count = Math.log(count) / Math.log(4);
        count = Math.ceil(count);

        return (int) count;
    }

    private int[] decodeResidueImagesIntoSegments(List<Bitmap> residueImages){
        int height = residueImages.get(0).getHeight();
        int width = residueImages.get(0).getWidth();
        int[] pixelSegments = new int[2 * height * width * 4];
        Arrays.fill(pixelSegments,0);

        for(int num = 0;num<residueImages.size();num++) {

            short[] pixelShorts = new short[width*height * 2];
            int[] pixelS = new int[width*height];
            Bitmap image = residueImages.get(num);
            image.getPixels(pixelS,0,width,0,0,width,height);

            for (int i = 0; i < pixelShorts.length; i += 2) {
                int pixel = pixelS[i / 2];
                pixelShorts[i] = (short) (pixel >> 16);
                pixelShorts[i + 1] = (short) ((pixel << 16) >> 16);
            }

            for (int i = 0; i < pixelSegments.length; i += 4) {
                short pixel = pixelShorts[i/4];
                pixelSegments[i] += (pixel >> bitCountToEncrypt * 3) * basis[num];
                pixelSegments[i + 1] += (pixel << 7 >> 13) * basis[num];
                pixelSegments[i + 2] += (pixel << 10 >> 13) * basis[num];
                pixelSegments[i + 3] += (pixel << 13 >> 13) * basis[num];
            }
        }
        int total = 1;
        for(int module: modules)
            total *= module;

        for(int i=0;i<pixelSegments.length;i++){
            pixelSegments[i] %= total;
        }

        return pixelSegments;
    }

    private Bitmap combineImageSegmentsIntoImage(byte[] pixelSegments){
        int[] pixels = new int[pixelSegments.length/4];
        //Arrays.fill(pixels,0);

        for(int i=0;i<pixels.length;i++){
            pixels[i] = pixelSegments[4*i] & 0xff;
            pixels[i] = (pixels[i] << 8) | (pixelSegments[4*i+1] & 0xff);
            pixels[i] = (pixels[i] << 8) | (pixelSegments[4*i+2] & 0xff);
            pixels[i] = (pixels[i] << 8) | (pixelSegments[4*i+3] & 0xff);
        }

        return Bitmap.createBitmap(pixels,1000,1000,Bitmap.Config.ARGB_8888);
    }



    private void showImages(List<Bitmap> images){

        imagesRecycler.setImages(images);
        contentContainer.addView(imagesRecycler);

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    }

    private void generateNumber(){
        Random random = new Random(System.currentTimeMillis());
        long number = random.nextLong();
        editText.setText(Long.toString(random.nextLong()),TextView.BufferType.NORMAL);
        setNumberInfo(number);
    }

    @SuppressLint("RestrictedApi")
    private void showTypePicker(View view){
        PopupMenu menu = new PopupMenu(this, view);
        menu.inflate(R.menu.type_pick_menu);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                isNumberInsert = menuItem.getItemId() == R.id.numbers_type;
                if(isNumberInsert) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setHint("1048576");
                    toolbar.setTitle("Тип: Числа");
                }else {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.setHint("Ваше сообщение");
                    toolbar.setTitle("Тип: Текст");
                }
                return true;
            }
        });
        menu.show();

    }

    private void initInfoCards(){
        InfoCard infoCard = new InfoCard(this);
        infoCardsContainer.addView(infoCard);
        infoCard.setLayoutParams(params);
        infoCards.put(SEGMENT_CARD,infoCard);

        infoCard = new InfoCard(this);
        infoCardsContainer.addView(infoCard);
        infoCard.setLayoutParams(params);
        infoCards.put(MESSAGES_CARD,infoCard);

        infoCard = new InfoCard(this);
        infoCardsContainer.addView(infoCard);
        infoCard.setLayoutParams(params);
        infoCards.put(RESIDUE_CARD,infoCard);

        infoCard = new InfoCard(this);
        infoCardsContainer.addView(infoCard);
        infoCard.setLayoutParams(params);
        infoCards.put(POST_DECODE_SEGMENT_CARD,infoCard);

        infoCard = new InfoCard(this);
        infoCardsContainer.addView(infoCard);
        infoCards.put(INITIAL_INFO_CARD,infoCard);
    }

    private void findBasis(){
        basis = new int[modules.length];

        int total = 1;
        for(int module: modules)
            total *= module;

        for(int i=0;i<basis.length;i++){
            int Pi = total / modules[i];
            int d = Pi % modules[i];
            int m = 1;
            while((d * m % modules[i]) != 1)
                m++;
            basis[i] = m * Pi;
        }

    }

    private void countMaxBitQuantity(){
        int maxModule = modules[0];
        for(int mod: modules)
            if(maxModule<mod)
                maxModule = mod;
        double log = Math.log(maxModule-1) / Math.log(2);
        double count = Math.ceil(log);
        bitCountToEncrypt = (int) count;
    }

    private void showInfoDialog(){
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("Ок",listener);
        String[] infoModule = new String[5];
        for(int i=0;i<4;i++){
            infoModule[i] = modules[i]  + " : " + basis[i];
        }
        int total = 1;
        for(int module: modules)
            total *= module;

        infoModule[4] = "Объем диапозона = " + total;
        builder.setItems(infoModule,null);
        builder.setTitle("Основания : Базис");
        builder.create().show();

    }

    private void startAction(){
        if(editText.getText().length() == 0)
            return;

       if(isNumberInsert)
           numberEncrypt();
       else
           textEncrypt();

    }

    private void textEncrypt(){
        TimingLogger logger = new TimingLogger("RNS","Text");

        divideTextInSegments();

        logger.addSplit("Divide is Done");

        encodeTextSegmentsIntoMessages();

        logger.addSplit("Encode is Done");

        decodeMessagesIntoTextResidueSequences();

        logger.addSplit("Decode is Done");

        decodeResidueSequencesIntoTextSegments();

        logger.addSplit("Decode in Residue is Done");

        combineTextFromSegments();

        logger.addSplit("Combine is Done");

        logger.dumpToLog();
    }

    private void numberEncrypt(){

        TimingLogger logger = new TimingLogger("RNS","Number");

        divideNumberInSegments();

        logger.addSplit("Divide is Done");

        encodeNumberSegmentsIntoMessages();

        logger.addSplit("Encode is Done");

        decodeMessagesIntoResidueSequences();

        logger.addSplit("Decode is Done");

        decodeResidueSequencesIntoNumberSegments();

        logger.addSplit("Decode in Residue is Done");

        combineNumberFromSegments();

        logger.addSplit("Combine is Done");

        logger.dumpToLog();

        sendMessagesOnServer();

    }





    private void divideTextInSegments(){
        String text = editText.getText().toString();

        char[] symbols = text.toCharArray();


        byte[] segments = new byte[symbols.length * 2];

        for(int i=0, j=0;i<symbols.length;i++,j+=2){

            segments[j] =(byte) ((int)symbols[i] >> 8);
            segments[j+1] =(byte) (((int)symbols[i] << 8) >> 8);
        }

        this.segments = segments;

    }


    private void encodeTextSegmentsIntoMessages(){
        String[] messages = new String[modules.length];

        byte[] symbols = new byte[segments.length * 2];

        final int DELTA = segments.length/2;

        Arrays.fill(messages,"");
        Arrays.fill(symbols, ((byte) 0));

        for(int i=0,k=0;i<segments.length;k+=i%2,i++){
            int number = segments[i] & 0xff;

            for(int j=0;j<modules.length;j++){
                int res =(byte) number % modules[j];
                symbols[k + j*DELTA] = (byte)(symbols[k + j*DELTA] * 10 + res);
                messages[j] += res;
            }
        }
        for(int i=0;i<symbols.length;i++)
            symbols[i] +=33;
        byte[] messageSymbols = new byte[DELTA];
        this.messages = messages.clone();
        for(int i=0;i<basis.length;i++){
            System.arraycopy(symbols,DELTA*i,messageSymbols,0,DELTA);
            messages[i] = "Decimal: " + messages[i] + "\nText: " + new String(messageSymbols);
        }
        fillTextEncryptCard(messages);
    }

    private void decodeMessagesIntoTextResidueSequences(){
        int size = messages[0].length();
        int[] residueSequences = new int[size];
        Arrays.fill(residueSequences,0);
        int factor = 10;
        for(int i=0;i<messages.length;i++){
            char[] array = messages[i].toCharArray();
            for(int j=0;j<size;j++){
                residueSequences[j] = residueSequences[j] * factor + Character.getNumericValue(array[j]);
            }
        }
        this.residueSequences = residueSequences;

    }

    private void decodeResidueSequencesIntoTextSegments(){

        byte[] segments = new byte[residueSequences.length];

        Arrays.fill(segments,(byte)1);

        int total = 1;
        for(int module: modules)
            total *= module;

        for(int i=0;i<residueSequences.length;i++){
            int number = residueSequences[i];
            int numberSegment = 0;
            int[] module = new int[4];
            module[0] = number/1000;
            module[1] = number /100 % 10;
            module[2] = number /10 % 10;
            module[3] = number %10;
            for(int j=0;j<basis.length;j++){
                numberSegment += module[j] * basis[j];
            }
            number = numberSegment % total;
            segments[i] =(byte) (numberSegment % total);
        }

        boolean isSame = Arrays.equals(segments,this.segments);

        this.segments = segments;

    }

    private void combineTextFromSegments(){
        char[] textSymbols = new char[segments.length/2];
        for(int i=0;i<segments.length;i+=2) {
            int symbol = 0;
            symbol = ((segments[i] & 0xff) << 8) + segments[i+1];
            textSymbols[i/2] = (char) symbol;
        }
        fillInitialTextCard(new String(textSymbols));

    }




    private void divideNumberInSegments(){
        long number = Long.parseLong(editText.getText().toString());

        byte[] segments = new byte[8];

        for(int i=0;i<segments.length;i++)
            segments[i] = (byte) ((number << 8*i) >> 56);

        this.segments = segments;

        fillSegmentCard(segments);

    }

    private void encodeNumberSegmentsIntoMessages(){
        String[] messages = new String[modules.length];
        String[] additional = new String[modules.length];
        long[] additionalNumbers = new long[modules.length];

        Arrays.fill(additionalNumbers,0);

        int[] bitsCount = new int[modules.length];
        for(int i=0;i<4;i++){
            bitsCount[i] =(int) Math.ceil( Math.log(modules[i])/Math.log(2));
        }

        Arrays.fill(messages,"");
        for(int i=0;i<segments.length;i++){
            int number = segments[i] & 0xff;
            for(int j=0;j<modules.length;j++){
                int n = number % modules[j];
                messages[j] += n;
                additionalNumbers[j] = (additionalNumbers[j] << bitsCount[j]) | n;
            }
        }

        for(int i=0;i<4;i++){
            additional[i] = Long.toString(additionalNumbers[i]);
        }

        this.messages = messages;
        this.additionalMessages = additional;

        fillEncryptCard(messages);
    }


    private void decodeMessagesIntoResidueSequences(){
        int size = messages[0].length();
        int[] residueSequences = new int[size];
        Arrays.fill(residueSequences,0);
        int factor = 10;
        for(int i=0;i<messages.length;i++){
            char[] array = messages[i].toCharArray();
            for(int j=0;j<size;j++){
                residueSequences[j] = residueSequences[j] * factor + Character.getNumericValue(array[j]);
            }
        }
        this.residueSequences = residueSequences;

        fillResidueCard(residueSequences);
    }

    private void decodeResidueSequencesIntoNumberSegments(){

        byte[] segments = new byte[residueSequences.length];

        Arrays.fill(segments,(byte)1);

        int total = 1;
        for(int module: modules)
            total *= module;

        for(int i=0;i<residueSequences.length;i++){
            int number = residueSequences[i];
            int numberSegment = 0;
            int[] module = new int[4];
            module[0] = number/1000;
            module[1] = number /100 % 10;
            module[2] = number /10 % 10;
            module[3] = number %10;
            for(int j=0;j<basis.length;j++){
                numberSegment += module[j] * basis[j];
            }
            number = numberSegment % total;
            segments[i] =(byte) (numberSegment % total);
        }

        boolean isSame = Arrays.equals(segments,this.segments);

        this.segments = segments;

        fillPostDecodeSegmentCard(segments,isSame);
    }


    private void combineNumberFromSegments(){
        long number = 0;
        for(int i=0;i<segments.length;i++)
            number = (number << 8) | (segments[i] & 0xff);

        fillInitialCard(number);
    }







    private void setNumberInfo(long number){
        textView.setText("Hex: " + Long.toHexString(number));
    }








    private void fillTextEncryptCard(String[] messages){
        HashMap<String,String> mapEncrypt = new HashMap<>();

        for(int i=0;i<messages.length;i++){
            mapEncrypt.put(Integer.toString(modules[i]), messages[i]);
        }
        hideInfoCards();
        setInfo(MESSAGES_CARD,"Сообщения для отправки",mapEncrypt);

    }

    private void fillInitialTextCard(String text){
        HashMap<String,String> mapInitial = new HashMap<>();

        mapInitial.put("Текст",text);

        setInfo(INITIAL_INFO_CARD,"Начальный текст",mapInitial);

    }




    private void fillSegmentCard(byte[] segments){

        HashMap<String,String> mapSegments = new HashMap<>();
        int number;

        for(int i=0;i<segments.length;i++) {
            number = segments[i] & 0xff;
            String text = "Decimal: " + (number) + "\n";
            text += "Binary: " + Integer.toBinaryString(number) + "\n";
            text += "Hex: " + Integer.toHexString(number);
            mapSegments.put(Integer.toString(i+1), text);
        }

        setInfo(SEGMENT_CARD,"Сегменты",mapSegments);

    }

    private void fillEncryptCard(String[] messages){
        HashMap<String,String> mapEncrypt = new HashMap<>();

        for(int i=0;i<messages.length;i++){
            mapEncrypt.put(Integer.toString(modules[i]), messages[i]);
        }

        setInfo(MESSAGES_CARD,"Сообщения для отправки",mapEncrypt);

    }

    private void fillResidueCard(int[] residueSequences){
        HashMap<String,String> mapResidue = new HashMap<>();

        for(int i=0;i<residueSequences.length;i++){
            mapResidue.put(Integer.toString(i+1), Integer.toString(residueSequences[i]));
        }

        setInfo(RESIDUE_CARD,"Остатки сегментов",mapResidue);
    }

    private void fillPostDecodeSegmentCard(byte[] segments, boolean isSame){

        HashMap<String,String> mapSegments = new HashMap<>();
        int number;

        for(int i=0;i<segments.length;i++) {
            number = segments[i] & 0xff;
            String text = "Decimal: " + (number) + "\n";
            text += "Binary: " + Integer.toBinaryString(number) + "\n";
            text += "Hex: " + Integer.toHexString(number);
            mapSegments.put(Integer.toString(i+1), text);
        }
        mapSegments.put("9 - Верность расшифровки",Boolean.toString(isSame));

        setInfo(POST_DECODE_SEGMENT_CARD,"Сегменты после расшифровки",mapSegments);
    }

    private void fillInitialCard(long number){
        HashMap<String,String> mapInitial = new HashMap<>();

        mapInitial.put("Начальное число","Decimal: " + Long.toString(number) + "\n" + "Hex: " + Long.toHexString(number));

        setInfo(INITIAL_INFO_CARD,"Начальное число",mapInitial);

    }



    private void setInfo(String key, String title, HashMap<String,String> info){
        InfoCard infoCard = infoCards.get(key);
        infoCard.setInfo(info);
        infoCard.setTitle(title);
        infoCard.setVisibility(View.VISIBLE);
    }

    private void hideInfoCards(){
        infoCards.get(SEGMENT_CARD).setVisibility(View.GONE);
        infoCards.get(POST_DECODE_SEGMENT_CARD).setVisibility(View.GONE);
        infoCards.get(RESIDUE_CARD).setVisibility(View.GONE);
    }


    private void sendMessagesOnServer(){
        for(int i=0;i<4;i++){
            Message newMessage = new Message(messages[i],additionalMessages[i],System.currentTimeMillis());
            messagesReference.push().setValue(newMessage);
        }
    }


}
