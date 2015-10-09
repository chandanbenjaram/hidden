package co.samepinch.android.app.helpers.adapters;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by imaginationcoder on 7/15/15.
 * credit: https://gist.github.com/ssinss/e06f12ef66c51252563e
 */
public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

    int visibilityThreshold;
    private int loaderIndex = 0, previousTotal;
    boolean loadingMore;

    private LinearLayoutManager mLayoutManager;

    public EndlessRecyclerOnScrollListener(LinearLayoutManager layoutManager) {
        this(layoutManager, 5); // default 5
    }

    public EndlessRecyclerOnScrollListener(LinearLayoutManager layoutManager, int visibilityThreshold) {
        this.mLayoutManager = layoutManager;
        this.visibilityThreshold = visibilityThreshold;
    }


    @Override
    public void onScrolled(RecyclerView rv, int dx, int dy) {
        super.onScrolled(rv, dx, dy);

        // scroll-up event
        if (dy <= 0) {
            return;
        }


        int visibleItemCount = mLayoutManager.getChildCount();
        int totalItemCount = mLayoutManager.getItemCount();
        int firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

        if (loadingMore) {
            if (totalItemCount >= previousTotal) {
                loadingMore = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loadingMore && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibilityThreshold)) {
            // End has been reached
            onLoadMore(rv, totalItemCount + loaderIndex);
            loadingMore = true;
        }
    }

    public abstract void onLoadMore(RecyclerView rv, int current_page);
}