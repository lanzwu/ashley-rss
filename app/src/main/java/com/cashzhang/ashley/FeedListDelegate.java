package com.cashzhang.ashley;

import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by zhangchi on 2018/2/6.
 */

import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.ViewDelegate;


class FeedListDelegate implements ViewDelegate {
    @Override
    public boolean isReadyForPull(View view, float x, float y) {
        boolean ready = false;

      /* First we check whether we're scrolled to the top of current page. */
        if (null != s_fragmentFeeds) {
            AbsListView absListView = getCurrentTagListView();

            if (0 == absListView.getCount()) {
                ready = true;
            } else if (0 == absListView.getFirstVisiblePosition()) {
                View firstVisibleChild = absListView.getChildAt(0);
                ready = null != firstVisibleChild && 0 <= firstVisibleChild.getTop();
            }

            if (ready && absListView.isFastScrollEnabled() && absListView.isFastScrollAlwaysVisible()) {
                switch (absListView.getVerticalScrollbarPosition()) {
                    case View.SCROLLBAR_POSITION_LEFT:
                        return x > absListView.getVerticalScrollbarWidth();
                    case View.SCROLLBAR_POSITION_RIGHT:
                        return x < absListView.getRight() - absListView.getVerticalScrollbarWidth();
                }
            }
        }
        return ready;
    }

    private static ListView getCurrentTagListView() {
        return MainFragment.getTagListView();
    }
}