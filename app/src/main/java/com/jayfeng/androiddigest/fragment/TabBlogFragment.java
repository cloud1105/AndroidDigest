package com.jayfeng.androiddigest.fragment;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jayfeng.androiddigest.R;
import com.jayfeng.androiddigest.activity.OfflineActivity;
import com.jayfeng.androiddigest.activity.WebViewActivity;
import com.jayfeng.androiddigest.config.Config;
import com.jayfeng.androiddigest.service.HttpClientSpiceService;
import com.jayfeng.androiddigest.webservices.JsonRequest;
import com.jayfeng.androiddigest.webservices.json.DigestJson;
import com.jayfeng.androiddigest.webservices.json.OfflineJson;
import com.jayfeng.androiddigest.webservices.json.OfflineListJson;
import com.jayfeng.lesscode.core.AdapterLess;
import com.jayfeng.lesscode.core.EncodeLess;
import com.jayfeng.lesscode.core.ViewLess;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.List;

import in.srain.cube.views.ptr.PtrClassicFrameLayout;
import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;

public class TabBlogFragment extends BaseFragment {

    private static final int CONTEXT_ITEM_OPEN_IN_BROWSER = 0;

    private SpiceManager spiceManager = new SpiceManager(HttpClientSpiceService.class);

    private ListView listView;
    private List<OfflineJson> listData;
    private BaseAdapter adapter;

    private PtrClassicFrameLayout ptrFrame;
    private View errorView;

    private String url;

    public TabBlogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        url = getActivity().getIntent().getStringExtra(OfflineActivity.KEY_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_blog, container, false);
        listView = ViewLess.$(contentView, R.id.listview);
        errorView = ViewLess.$(contentView, R.id.error);

        ptrFrame = ViewLess.$(contentView, R.id.fragment_rotate_header_with_listview_frame);
        ptrFrame.setLastUpdateTimeRelateObject(this);
        ptrFrame.setPtrHandler(new PtrDefaultHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                requestNetworkData();
                errorView.setVisibility(View.GONE);
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, listView, header) ;
            }
        });

        registerForContextMenu(listView);

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
                String type = listData.get(position).getType();
                String url = listData.get(position).getUrl();
                String title = listData.get(position).getTitle();
                if (Config.BLOG_TYPE_DIR.equals(type)) {
                    Intent intent = new Intent(getActivity(), OfflineActivity.class);
                    intent.putExtra(OfflineActivity.KEY_URL, url);
                    intent.putExtra(OfflineActivity.KEY_TITLE, title);
                    startActivity(intent);
                } else if (Config.BLOG_TYPE_HTML.equals(type)) {
                    Intent intent = new Intent(getActivity(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.KEY_URL, url);
                    startActivity(intent);
                }
            }
        });
    }


    /*
     * =============================================================
     * request list data
     * =============================================================
     */

    private void requestNetworkData() {
        JsonRequest<OfflineListJson> request = new JsonRequest<>(OfflineListJson.class);
        request.setUrl(getListUrl());
        spiceManager.getFromCacheAndLoadFromNetworkIfExpired(request,
                getCacheKey(),
                DurationInMillis.NEVER, new RequestListener<OfflineListJson>() {
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
                    public void onRequestSuccess(OfflineListJson offlineListJson) {
                        fillAdapterToListView(offlineListJson);
                        errorView.setVisibility(View.GONE);
                        ptrFrame.refreshComplete();
                    }
                });
    }

    private void showCacheData() {
        spiceManager.getFromCache(OfflineListJson.class,
                getCacheKey(),
                DurationInMillis.ALWAYS_RETURNED, new RequestListener<OfflineListJson>() {
                    @Override
                    public void onRequestFailure(SpiceException spiceException) {
                    }

                    @Override
                    public void onRequestSuccess(OfflineListJson offlineListJson) {
                        fillAdapterToListView(offlineListJson);
                    }
                });
    }

    private void fillAdapterToListView(OfflineListJson offlineListJson) {
        if (offlineListJson == null) {
            return;
        }
        listData = offlineListJson;
        adapter = AdapterLess.$base(getActivity(),
                listData,
                new int[] { R.layout.fragment_blog_list_item, R.layout.fragment_blog_list_item_header},
                new AdapterLess.FullCallBack<OfflineJson>() {
                    @Override
                    public View getView(int i, View view, AdapterLess.ViewHolder viewHolder, OfflineJson offlineJson) {
                        ImageView iconView = viewHolder.$view(view, R.id.icon);
                        TextView titleView = viewHolder.$view(view, R.id.title);

                        if (Config.BLOG_TYPE_DIR.equals(offlineJson.getType())) {
                            iconView.setImageResource(R.mipmap.offline_type_folder);
                        } else if (Config.BLOG_TYPE_HTML.equals(offlineJson.getType())){
                            iconView.setImageResource(R.mipmap.blog_type_html);
                        }
                        titleView.setText(offlineJson.getTitle());
                        return view;
                    }

                    @Override
                    public int getItemViewType(int i) {
                        OfflineJson offlineJson = listData.get(i);
                        if (Config.BLOG_TYPE_HEADER.equals(offlineJson.getType())) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }

                    @Override
                    public boolean isEnabled(int i) {
                        OfflineJson offlineJson = listData.get(i);
                        if (Config.BLOG_TYPE_HEADER.equals(offlineJson.getType())) {
                            return false;
                        }
                        return true;
                    }
                });
        listView.setAdapter(adapter);
    }

    private String getListUrl() {
        if (url == null) {
            url = Config.getBlogListUrl();
        }
        return url;
    }

    private String getCacheKey() {
        return EncodeLess.$md5(getListUrl());
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("More");
        menu.add(0, CONTEXT_ITEM_OPEN_IN_BROWSER, 0, "Open in browser");
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case CONTEXT_ITEM_OPEN_IN_BROWSER:
                OfflineJson offlineJson = listData.get(menuInfo.position);
                Intent intent = new Intent();
                intent.setAction("android.intent.action.VIEW");
                Uri content_url = Uri.parse(offlineJson.getUrl());
                intent.setData(content_url);
                startActivity(intent);
                return true;
        }
        return super.onContextItemSelected(item);
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
