package com.zipgo.zipgoassignment;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Place Model Class
 */
public class Place extends RealmObject implements Parcelable {

    @PrimaryKey
    private String id;
    private String placeName;
    private double latitude;
    private double longitude;

    public Place() {
    }

    /**
     * Constructor
     *
     * @param id        id
     * @param placeName placeName
     * @param latitude  latitude
     * @param longitude longitude
     */
    Place(String id, String placeName, double latitude, double longitude) {
        this.id = id;
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Constructor for Parcelable
     *
     * @param in Parcel
     */
    private Place(Parcel in) {
        id = in.readString();
        placeName = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    /**
     * Parcel Creator
     */
    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    /**
     * Get Place Id
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Get Place Name
     *
     * @return placeName
     */
    String getPlaceName() {
        return placeName;
    }

    /**
     * Get place latitude
     *
     * @return latitude
     */
    double getLatitude() {
        return latitude;
    }

    /**
     * get place longitude
     *
     * @return longitude
     */
    double getLongitude() {
        return longitude;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(placeName);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
