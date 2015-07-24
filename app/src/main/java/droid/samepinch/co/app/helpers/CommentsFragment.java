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

import droid.samepinch.co.app.R;
import droid.samepinch.co.data.dao.SchemaComments;
import droid.samepinch.co.data.dao.SchemaPostDetails;

/**
 * Created by cbenjaram on 7/23/15.
 */
public class CommentsFragment extends ListFragment {
    Activity mActivity;
    private ListView mListView;
    private CursorAdapter mAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.comments_fragment,
                container, true);
        mListView = (ListView) rootView.findViewById(android.R.id.list);
        return rootView;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        int num = getArguments().getInt(ARG_SECTION_NUMBER);
        ContentResolver resolver = getActivity().getContentResolver();
        Cursor c = resolver.query(SchemaComments.CONTENT_URI, null, null, null, null);
        if (c.moveToFirst()) {
            mAdapter = new CommentsCursorAdapter(getActivity(), c, CursorAdapter.NO_SELECTION);
            mListView.setAdapter(mAdapter);
        }
    }

    public static class CommentsCursorAdapter extends android.widget.CursorAdapter {

        public CommentsCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);
        }

        // The bindView method is used to bind all data to a given view
        // such as setting the text on a TextView.
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find fields to populate in inflated template
            TextView tvBody = (TextView) view.findViewById(R.id.tvBody);
            TextView tvPriority = (TextView) view.findViewById(R.id.tvPriority);
            // Extract properties from cursor
            int dotFnameIdx = cursor.getColumnIndexOrThrow(SchemaComments.COLUMN_DOT_FNAME);
            String body = cursor.getString(dotFnameIdx);

            int createdAtIdx = cursor.getColumnIndexOrThrow(SchemaComments.COLUMN_CREATED_AT);
            int priority = cursor.getInt(createdAtIdx);
            // Populate fields with extracted properties
            tvBody.setText(body);
            tvPriority.setText(String.valueOf(priority));
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
}
