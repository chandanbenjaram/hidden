package droid.samepinch.co.data.dto;

import android.database.Cursor;

import droid.samepinch.co.data.WallContract;

/**
 * Created by imaginationcoder on 6/25/15.
 */
public class PostItem {

    private Long id;
    private String content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public static PostItem fromCursor(Cursor cur){
        int idIdx = cur.getColumnIndex(WallContract.Posts._ID);
        int contentIdx = cur.getColumnIndex(WallContract.Posts.COLUMN_CONTENT);

        PostItem item = new PostItem();
        item.setId(cur.getLong(idIdx));
        item.setContent(cur.getString(contentIdx));

        return item;
    }
}
