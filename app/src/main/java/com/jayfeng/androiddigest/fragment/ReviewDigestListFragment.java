package com.jayfeng.androiddigest.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.jayfeng.androiddigest.R;
import com.jayfeng.androiddigest.activity.WebViewActivity;
import com.jayfeng.androiddigest.config.Config;
import com.jayfeng.androiddigest.service.HttpClientSpiceService;
import com.jayfeng.androiddigest.webservices.JsonRequest;
import com.jayfeng.androiddigest.webservices.json.ReviewDigestJson;
import com.jayfeng.androiddigest.webservices.json.ReviewDigestListJson;
import com.jayfeng.lesscode.core.AdapterLess;
import com.jayfeng.lesscode.core.ToastLess;
import com.jayfeng.lesscode.core.ViewLess;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class ReviewDigestListFragment extends BaseFragment implements OnScrollListener {

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    private ListView listView;
    private List<ReviewDigestJson> listData;
    private BaseAdapter adapter;

    private PtrClassicFrameLayout ptrFrame;
    private View errorView;
    private View footerView;
    private int visibleLastIndex;
    private boolean noMoreData = false;

    private int page = Config.PAGE_START;
    private int size = Config.PAGE_SIZE;

    public ReviewDigestListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_digest_review, container, false);
        listView = ViewLess.$(contentView, R.id.listview);
        errorView = ViewLess.$(contentView, R.id.error);
        footerView = LayoutInflater.from(getActivity())
                .inflate(R.layout.common_list_footer, null, false);
        listView.addFooterView(footerView);

        listView.setOnScrollListener(this);

        ptrFrame = ViewLess.$(contentView, R.id.fragment_rotate_header_with_listview_frame);
        ptrFrame.setLastUpdateTimeRelateObject(this);
        ptrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                resetPage();
                requestNetworkData();
                errorView.setVisibility(View.GONE);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, listView, header) ;
            }
        });

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showCacheData();
        ptrFrame.postDelayed(new Runnable() {
            @Override
            public void run() {
                ptrFrame.autoRefresh();
            }
        }, 100);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 0 || position >= listData.size()) {
                    return;
                }
                ReviewDigestJson reviewDigestJson = listData.get(position);
                String url = reviewDigestJson.getUrl();
                if (!TextUtils.isEmpty(url)) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.KEY_URL, url);
                    startActivity(intent);
                } else {
                    ToastLess.$(getActivity(), "文章地址为空，无法打开此详情页。");
                }
            }
        });
    }

    /*
     * =============================================================
     * first page data
     * =============================================================
     */

    private void requestNetworkData() {
        JsonRequest<ReviewDigestListJson> request = new JsonRequest<>(ReviewDigestListJson.class);
        request.setUrl(Config.getReviewDigestListUrl(page, size));
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request,
                "review_digest_list_page_" + page + "_size_" + size,
                DurationInMillis.NEVER, new RequestListener<ReviewDigestListJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                        ptrFrame.refreshComplete();
                        ptrFrame.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (listView.getAdapter() == null || listView.getAdapter().getCount() == 0) {
                                    errorView.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 400);

                    }

                    @Override
                    public void onRequestSuccess(ReviewDigestListJson reviewDigestListJson) {
                        fillAdapterToListView(reviewDigestListJson);
                        errorView.setVisibility(View.GONE);
                        ptrFrame.refreshComplete();
                    }
                });
    }

    private void showCacheData() {
        spiceManager.getFromCache(ReviewDigestListJson.class,
                "review_digest_list_page_" + page + "_size_" + size,
                DurationInMillis.ALWAYS_RETURNED, new RequestListener<ReviewDigestListJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                    }

                    @Override
                    public void onRequestSuccess(ReviewDigestListJson reviewDigestListJson) {
                        fillAdapterToListView(reviewDigestListJson);
                        resetPage();
                    }
                });
    }

    private void fillAdapterToListView(ReviewDigestListJson reviewDigestListJson) {
        if (reviewDigestListJson == null) {
            return;
        }
        listData = reviewDigestListJson;
        adapter = AdapterLess.$base(getActivity(),
                listData,
                R.layout.fragment_digest_review_list_item,
                new AdapterLess.CallBack<ReviewDigestJson>() {
                    @Override
                    public View getView(int i, View view, AdapterLess.ViewHolder viewHolder, ReviewDigestJson reviewDigestJson) {
                        TextView titleView = viewHolder.$view(view, R.id.title);
                        TextView abstractView = viewHolder.$view(view, R.id.abstracts);
                        SimpleDraweeView draweeView = viewHolder.$view(view,R.id.thumbnail);

                        if (TextUtils.isEmpty(reviewDigestJson.getTitle())) {
                            titleView.setVisibility(View.GONE);
                        } else {
                            titleView.setText(reviewDigestJson.getTitle());
                            TextPaint titlePaint = titleView.getPaint();
                            titlePaint.setFakeBoldText(true);
                            titleView.setVisibility(View.VISIBLE);
                        }

                        if (TextUtils.isEmpty(reviewDigestJson.getAbstractStr())) {
                            abstractView.setVisibility(View.GONE);
                        } else {
                            abstractView.setText(reviewDigestJson.getAbstractStr());
                            abstractView.setVisibility(View.VISIBLE);
                        }

                        if (!TextUtils.isEmpty(reviewDigestJson.getThumbnail())) {
                            Uri uri = Uri.parse(reviewDigestJson.getThumbnail());
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setUri(uri)
                                    .setAutoPlayAnimations(true)
                                    .build();
                            draweeView.setController(controller);
                            draweeView.setVisibility(View.VISIBLE);
                        } else {
                            draweeView.setVisibility(View.GONE);
                        }

                        return view;
                    }
                });
        listView.setAdapter(adapter);
    }

    /*
     * =============================================================
     * more page data
     * =============================================================
     */

    private void moreNetworkData() {
        int nextPage = page + 1;
        JsonRequest<ReviewDigestListJson> request = new JsonRequest<>(ReviewDigestListJson.class);
        request.setUrl(Config.getReviewDigestListUrl(nextPage, size));
        spiceManager.execute(request, new RequestListener<ReviewDigestListJson>() {
            @Override
            public void onRequestFailure(SpiceException spiceException) {
            }

            @Override
            public void onRequestSuccess(ReviewDigestListJson reviewDigestListJson) {
                moreAdapterToListView(reviewDigestListJson);
            }
        });
    }

    private void moreAdapterToListView(ReviewDigestListJson reviewDigestListJson) {
        if (reviewDigestListJson == null) {
            return;
        }
        listData.addAll(reviewDigestListJson);
        if (reviewDigestListJson.size() < Config.PAGE_SIZE) {
            if (listView.getFooterViewsCount() > 0) {
                listView.removeFooterView(footerView);
            }
            noMoreData = true;
        }
        adapter.notifyDataSetChanged();
        page++;
    }

    private void resetPage() {
        page = Config.PAGE_START;
        size = Config.PAGE_SIZE;
        noMoreData = false;
        if (listView.getFooterViewsCount() == 0) {
            listView.addFooterView(footerView);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int lastIndex = adapter.getCount();
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
                && visibleLastIndex == lastIndex
                && !noMoreData) {
            moreNetworkData();
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
    }

    @Override
    public void onStart() {
        spiceManager.start(getActivity());
        super.onStart();
    }

    @Override
    public void onStop() {
        if (spiceManager.isStarted()) {
            spiceManager.shouldStop();
        }
        super.onStop();
    }

}
