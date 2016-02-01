package org.fullmetalfalcons.androidscouting;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Dan on 2/1/2016.
 */
public class ParcelableArrayList extends ArrayList<Object> implements Parcelable{


    public ParcelableArrayList(){

    }

    protected ParcelableArrayList(Parcel in) {
        ArrayList<Object> obj = new ArrayList<>(Arrays.asList(in.readArray(Object.class.getClassLoader())));
        for (Object o: obj){
            add(o);
        }

    }

    public static final Creator<ParcelableArrayList> CREATOR = new Creator<ParcelableArrayList>() {
        @Override
        public ParcelableArrayList createFromParcel(Parcel in) {
            return new ParcelableArrayList(in);
        }

        @Override
        public ParcelableArrayList[] newArray(int size) {
            return new ParcelableArrayList[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeArray(this.toArray(new Object[0]));
    }
}
