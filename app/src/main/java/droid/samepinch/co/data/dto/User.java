package droid.samepinch.co.data.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by imaginationcoder on 7/1/15.
 */
public class User implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    String uid;
    String fname;
    String lname;
    String photo;

    public User() {
    }

    protected User(Parcel in) {
        uid = in.readString();
        fname = in.readString();
        lname = in.readString();
        photo = in.readString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(fname);
        dest.writeString(lname);
        dest.writeString(photo);
    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + uid;
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        User other = (User) obj;
//        if (uid != other.uid)
//            return false;
//        return true;
//    }
}