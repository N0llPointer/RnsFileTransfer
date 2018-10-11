package com.nollpointer.rnsfiletransfer;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageWrapper implements Parcelable {

    private Bitmap image;

    public ImageWrapper(Bitmap image) {
        this.image = image;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    protected ImageWrapper(Parcel in) {
        image = (Bitmap) in.readValue(Bitmap.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(image);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ImageWrapper> CREATOR = new Parcelable.Creator<ImageWrapper>() {
        @Override
        public ImageWrapper createFromParcel(Parcel in) {
            return new ImageWrapper(in);
        }

        @Override
        public ImageWrapper[] newArray(int size) {
            return new ImageWrapper[size];
        }
    };
}
