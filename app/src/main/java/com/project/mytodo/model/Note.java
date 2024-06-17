package com.project.mytodo.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Note implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "text")
    public String text;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "done")
    public boolean done;

    @ColumnInfo(name = "end_time")
    public long endTime;

    public Note(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Note note = (Note) o;
        return uid == note.uid && timestamp == note.timestamp && done == note.done && endTime == note.endTime && Objects.equals(text, note.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, text, timestamp, done, endTime);
    }

    protected Note(Parcel in){
        uid = in.readInt();
        text = in.readString();
        timestamp = in.readLong();
        done = in.readByte() != 0;
        endTime = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(text);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (done ? 1 : 0));
        dest.writeLong(endTime);

    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };
}
