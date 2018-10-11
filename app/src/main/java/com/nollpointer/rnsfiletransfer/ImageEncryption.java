package com.nollpointer.rnsfiletransfer;

import android.graphics.Bitmap;

public class ImageEncryption {
    private Bitmap initialImage;

    public ImageEncryption(Bitmap initialImage) {
        this.initialImage = initialImage;
    }

    public Bitmap getInitialImage() {
        return initialImage;
    }

    public void setInitialImage(Bitmap initialImage) {
        this.initialImage = initialImage;
    }






    public interface Listener{

    }

    public interface ImageEncryptionControl{

    }

}
