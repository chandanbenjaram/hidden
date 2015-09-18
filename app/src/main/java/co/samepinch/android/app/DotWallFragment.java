package co.samepinch.android.app;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.facebook.drawee.view.SimpleDraweeView;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;

import co.samepinch.android.app.helpers.Utils;
import co.samepinch.android.app.helpers.adapters.PostCursorRecyclerViewAdapter;
import co.samepinch.android.app.helpers.widget.SIMView;
import co.samepinch.android.data.dao.SchemaDots;
import co.samepinch.android.data.dao.SchemaPosts;
import co.samepinch.android.data.dto.User;

import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_FNAME;
import static co.samepinch.android.app.helpers.AppConstants.APP_INTENT.KEY_LNAME;
import static co.samepinch.android.app.helpers.AppConstants.K;

public class DotWallFragment extends Fragment {
    public static final String LOG_TAG = "DotWallFragment";

    PostCursorRecyclerViewAdapter mViewAdapter;
    LinearLayoutManager mLayoutManager;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.dot_wall, container, false);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);

        // dot uid
        String dotUid = getArguments().getString(K.KEY_DOT.name());

        Cursor cursor = getActivity().getContentResolver().query(SchemaDots.CONTENT_URI, null, SchemaDots.COLUMN_UID + "=?", new String[]{dotUid}, null);
        if (!cursor.moveToFirst()) {
            getActivity().finish();
        }
        int photoUrlIndex;
        final User user = Utils.cursorToUserEntity(cursor);
        collapsingToolbar.setTitle(user.getPinchHandle());

        ViewSwitcher vs = (ViewSwitcher) view.findViewById(R.id.dot_wall_switch);
        // tag map
        if (StringUtils.isBlank(user.getPhoto())) {
            vs.setDisplayedChild(1);
            TextView name = (TextView) view.findViewById(R.id.dot_wall_avatar_name);
            String fName = user.getFname();
            String lName = user.getLname();
            String initials = StringUtils.join(StringUtils.substring(fName, 0, 1), StringUtils.substring(lName, 0, 1));
            name.setText(initials);
        } else {
            vs.setDisplayedChild(0);
            SIMView avatar = (SIMView) view.findViewById(R.id.dot_wall_avatar);
            avatar.populateImageView(user.getPhoto());
        }

        TextView handle = (TextView) view.findViewById(R.id.dot_wall_avatar_handle);
        handle.setText(StringUtils.join(new String[]{user.getFname(), user.getLname()}, " "));

        // custom recycler
        RecyclerView rv = new RecyclerView(getActivity().getApplicationContext()) {
            @Override
            public void scrollBy(int x, int y) {
                try {
                    super.scrollBy(x, y);
                } catch (NullPointerException nlp) {
                    // muted
                }
            }
        };

        LinearLayout rvHolder = (LinearLayout) view.findViewById(R.id.holder_recyclerview);
        rvHolder.addView(rv);

        mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rv.setLayoutManager(mLayoutManager);
        setupRecyclerView(new String[]{dotUid}, rv);

        return view;
    }

    private void setupRecyclerView(String[] args0, RecyclerView recyclerView) {
        recyclerView.setHasFixedSize(true);
        Cursor cursor = getActivity().getContentResolver().query(SchemaPosts.CONTENT_URI, null, SchemaPosts.COLUMN_OWNER + "=?", args0, null);
        mViewAdapter = new PostCursorRecyclerViewAdapter(getActivity(), cursor);
        recyclerView.setAdapter(mViewAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }
}