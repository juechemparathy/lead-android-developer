package com.gaborbiro.marveldemo.provider.api.model;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo") public class Comic implements Parcelable {

    private static final String FOC_DATE_TYPE = "focDate";

    public final int id;
    public final String title;
    public final String description;
    public final Image thumbnail;

    Comic(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        thumbnail = in.readParcelable(Image.class.getClassLoader());
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeParcelable(thumbnail, 0);
    }

    public static final Parcelable.Creator<Comic> CREATOR =
            new Parcelable.Creator<Comic>() {

                @Override public Comic createFromParcel(Parcel in) {
                    return new Comic(in);
                }

                @Override public Comic[] newArray(int size) {
                    return new Comic[size];
                }
            };

    public String getThumbImageUri() {
        if (thumbnail == null) {
            return null;
        }
        return thumbnail.path + "/standard_medium." + thumbnail.extension;
    }

    public String getCoverImageUri() {
        if (thumbnail == null) {
            return null;
        }
        return thumbnail.path + "/standard_fantastic." + thumbnail.extension;
    }
}
