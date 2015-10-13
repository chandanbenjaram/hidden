package co.samepinch.android.app.helpers;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.squareup.otto.Subscribe;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.samepinch.android.app.ActivityFragment;
import co.samepinch.android.app.R;
import co.samepinch.android.app.helpers.adapters.TagsToManageRVAdapter;
import co.samepinch.android.app.helpers.intent.TagsPullService;
import co.samepinch.android.app.helpers.pubsubs.BusProvider;
import co.samepinch.android.app.helpers.pubsubs.Events;
import co.samepinch.android.data.dao.SchemaTags;

public class ManageTagsFragment extends Fragment {
    public static final String TAG = "ManageTagsFragment";

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.holder_recyclerview)
    FrameLayout frameLayout;

    ProgressDialog progressDialog;
    TagsToManageRVAdapter mTagsToManageRVAdapter;

    String mCurrUserId;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.KV.REQUEST_EDIT_TAG.getIntValue()) {
            if (resultCode == Activity.RESULT_OK) {
                Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, null, null, SchemaTags.COLUMN_NAME + " ASC");
                mTagsToManageRVAdapter.changeCursor(cursor);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment across configuration changes.
        setRetainInstance(true);

        // progress dialog properties
        progressDialog = new ProgressDialog(getActivity(),
                R.style.Theme_AppCompat_Dialog);
        progressDialog.setCancelable(Boolean.FALSE);

        // keep current logged in user id
        Map<String, String> userInfo = Utils.PreferencesManager.getInstance().getValueAsMap(AppConstants.API.PREF_AUTH_USER.getValue());
        mCurrUserId = userInfo.get(AppConstants.APP_INTENT.KEY_UID.getValue());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.manage_tags, container, false);
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // hack to get click working
                ((AppCompatActivity) getActivity()).onBackPressed();
            }
        });
        toolbar.setTitle("MANAGE TAGS");

        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.menu_blue);
        ab.setDisplayHomeAsUpEnabled(true);

        // setup recyler view
        setupRecyclerView();

        // call for intent
        Intent tagRefreshIntent =
                new Intent(getActivity().getApplicationContext(), TagsPullService.class);
        getActivity().startService(tagRefreshIntent);

        return view;
    }

    private void setupRecyclerView() {
        frameLayout.removeAllViews();
        // recycler view
        final RecyclerView rv = new RecyclerView(getActivity().getApplicationContext()) {
            @Override
            public void scrollBy(int x, int y) {
                try {
                    super.scrollBy(x, y);
                } catch (NullPointerException nlp) {
                    // muted
                }
            }
        };

        frameLayout.addView(rv);

        final GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity().getApplicationContext(), 3);
        rv.setLayoutManager(gridLayoutManager);
        rv.setHasFixedSize(true);
        rv.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        rv.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        int viewWidth = rv.getMeasuredWidth();
                        float cardViewWidth = getActivity().getResources().getDimension(R.dimen.cardview_layout_width);
                        int newSpanCount = (int) Math.floor(viewWidth / cardViewWidth);
                        gridLayoutManager.setSpanCount(newSpanCount);
                        gridLayoutManager.requestLayout();
                    }
                });

        Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, null, null, SchemaTags.COLUMN_NAME + " ASC");
        TagsToManageRVAdapter.ItemEventListener itemEventListener = new TagsToManageRVAdapter.ItemEventListener<String>() {
            @Override
            public void onClick(String tag) {
                Bundle args = new Bundle();
                args.putString(AppConstants.APP_INTENT.KEY_TAG.getValue(), tag);
                args.putString(AppConstants.APP_INTENT.KEY_UID.getValue(), mCurrUserId);
                // target
                args.putString(AppConstants.K.TARGET_FRAGMENT.name(), AppConstants.K.FRAGMENT_MANAGE_A_TAG.name());

                // intent
                Intent intent = new Intent(getActivity().getApplicationContext(), ActivityFragment.class);
                intent.putExtras(args);
                startActivityForResult(intent, AppConstants.KV.REQUEST_EDIT_TAG.getIntValue());
            }
        };

        // adapter
        mTagsToManageRVAdapter = new TagsToManageRVAdapter(getActivity(), cursor, itemEventListener, mCurrUserId);
        mTagsToManageRVAdapter.setHasStableIds(true);
        rv.setAdapter(mTagsToManageRVAdapter);
    }

    @Subscribe
    public void onTagsRefreshedEvent(Events.TagsRefreshedEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Cursor cursor = getActivity().getContentResolver().query(SchemaTags.CONTENT_URI, null, null, null, SchemaTags.COLUMN_NAME + " ASC");
                    mTagsToManageRVAdapter.changeCursor(cursor);
//                    dataSetChanged();
                } catch (Exception e) {
                    //e.printStackTrace();
                }
            }
        });
    }

    @Subscribe
    public void onTagsRefreshFailEvent(final Events.TagsRefreshFailEvent event) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(getView(), event.getMetaData().get(AppConstants.K.MESSAGE.name()), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.INSTANCE.getBus().register(this);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.INSTANCE.getBus().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}