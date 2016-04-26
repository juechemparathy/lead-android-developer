package com.gaborbiro.marveldemo.provider.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo") public class Image implements Parcelable {

    public final String path;
    public final String extension;

    public Image(Parcel in) {
        path = in.readString();
        extension = in.readString();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        if (path != null ? !path.equals(image.path) : image.path != null) return false;
        if (extension != null ? !extension.equals(image.extension)
                              : image.extension != null) return false;

        return true;
    }

    @Override public int hashCode() {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return path + "..." + extension;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(extension);
    }

    public static final Parcelable.Creator<Image> CREATOR =
            new Parcelable.Creator<Image>() {

                @Override public Image createFromParcel(Parcel in) {
                    return new Image(in);
                }

                @Override public Image[] newArray(int size) {
                    return new Image[size];
                }
            };
}
