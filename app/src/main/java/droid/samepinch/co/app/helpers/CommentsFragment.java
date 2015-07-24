package droid.samepinch.co.app.helpers;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import droid.samepinch.co.app.R;
import droid.samepinch.co.data.dao.SchemaComments;
import droid.samepinch.co.data.dao.SchemaPosts;

/**
 * Created by cbenjaram on 7/23/15.
 */
public class CommentsFragment extends ListFragment {
    CommentsFragmentCallbackListener mCallback;
    private ListView mListView;
    private CursorAdapter mAdapter;
    private String mPostId;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // callback check
        try {
            mCallback = (CommentsFragmentCallbackListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.comments_fragment,
                container, false);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        Bundle iArgs = getArguments();
        mPostId = iArgs == null? null : iArgs.getString(SchemaPosts.COLUMN_UID);

        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        int num = getArguments().getInt(ARG_SECTION_NUMBER);
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor c = null;
        if(StringUtils.isNotBlank(mPostId)){
            c = getActivity().getContentResolver().query(SchemaComments.CONTENT_URI, null, SchemaComments.COLUMN_POST_DETAILS + "=?", new String[]{mPostId}, null);
        }

        mAdapter = new CommentsCursorAdapter(getActivity(), c, CursorAdapter.NO_SELECTION);
        mListView.setAdapter(mAdapter);
    }

    public static class CommentsCursorAdapter extends android.widget.CursorAdapter {

        public CommentsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView avatarNameView = (TextView) view.findViewById(R.id.avatar_name);
            TextView commentView = (TextView) view.findViewById(R.id.comment);

            int dotFnameIdx = cursor.getColumnIndexOrThrow(SchemaComments.COLUMN_DOT_FNAME);
            String name = cursor.getString(dotFnameIdx);
            avatarNameView.setText(name);

            int commentIdx = cursor.getColumnIndexOrThrow(SchemaComments.COLUMN_TEXT);
            String comment = cursor.getString(commentIdx);
            commentView.setText(comment);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup
                parent) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
//            ViewHolder holder = new ViewHolder();
//            holder.name = (TextView) view.findViewById(R.id.task_name);
//            holder.time = (TextView) view.findViewById(R.id.task_time);
//            holder.nameIndex = cursor.getColumnIndexOrThrow(SchemaComments.COLUMN_DOT_FNAME);
//            holder.timeIndex = cursor.getColumnIndexOrThrow(SchemaComments.COLUMN_CREATED_AT);
//            view.setTag(holder);
            return view;
        }
    }

    private static class ViewHolder {
        int nameIndex;
        int timeIndex;
        TextView name;
        TextView time;
    }

    // Container Activity must implement this interface
    public static interface CommentsFragmentCallbackListener {
        void onCommentClick(int position);
    }

}
