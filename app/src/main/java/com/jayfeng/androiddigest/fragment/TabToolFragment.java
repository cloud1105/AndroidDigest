package com.jayfeng.androiddigest.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jayfeng.androiddigest.R;
import com.jayfeng.androiddigest.activity.ToolListActivity;
import com.jayfeng.androiddigest.config.Config;
import com.jayfeng.androiddigest.model.ToolCategory;
import com.jayfeng.lesscode.core.AdapterLess;
import com.jayfeng.lesscode.core.ViewLess;

import java.util.ArrayList;
import java.util.List;

public class TabToolFragment extends BaseFragment {

    private ListView listView;
    private List<ToolCategory> listData;
    private BaseAdapter adapter;

    public TabToolFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_tool, container, false);
        listView = ViewLess.$(contentView, R.id.listview);

        return contentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initToolCategoryData();
        fillDataToListView();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = listData.get(position).getKey();
                String title = listData.get(position).getTitle();
                Intent intent = new Intent(getActivity(), ToolListActivity.class);
                intent.putExtra(ToolListActivity.KEY_TITLE, title);
                intent.putExtra(ToolListActivity.KEY_TYPE, key);
                startActivity(intent);
            }
        });
    }

    private void fillDataToListView() {
        adapter = AdapterLess.$base(getActivity(),
                listData,
                R.layout.fragment_tool_list_item,
                new AdapterLess.CallBack<ToolCategory>() {
                    @Override
                    public View getView(int i, View view, AdapterLess.ViewHolder viewHolder, ToolCategory toolCategory) {
                        TextView titleView = viewHolder.$view(view, R.id.title);
                        TextView descriptionView = viewHolder.$view(view, R.id.description);

                        titleView.setText(toolCategory.getTitle());
                        descriptionView.setText(toolCategory.getDescription());

                        return view;
                    }
                });
        listView.setAdapter(adapter);
    }

    private void initToolCategoryData() {
        listData = new ArrayList<>();

        ToolCategory toolCategory = new ToolCategory();
        toolCategory.setKey(Config.TOOL_CATEGORY_COMPOMENT);
        toolCategory.setTitle(getString(R.string.tool_category_compoment_title));
        toolCategory.setDescription(getString(R.string.tool_category_compoment_description));
        listData.add(toolCategory);

        toolCategory = new ToolCategory();
        toolCategory.setKey(Config.TOOL_CATEGORY_LIBRARY);
        toolCategory.setTitle(getString(R.string.tool_category_library_title));
        toolCategory.setDescription(getString(R.string.tool_category_library_description));
        listData.add(toolCategory);

        toolCategory = new ToolCategory();
        toolCategory.setKey(Config.TOOL_CATEGORY_TOOL);
        toolCategory.setTitle(getString(R.string.tool_category_tool_title));
        toolCategory.setDescription(getString(R.string.tool_category_tool_description));
        listData.add(toolCategory);

        toolCategory = new ToolCategory();
        toolCategory.setKey(Config.TOOL_CATEGORY_CODE);
        toolCategory.setTitle(getString(R.string.tool_category_code_title));
        toolCategory.setDescription(getString(R.string.tool_category_code_description));
        listData.add(toolCategory);

        toolCategory = new ToolCategory();
        toolCategory.setKey(Config.TOOL_CATEGORY_DESIGN);
        toolCategory.setTitle(getString(R.string.tool_category_design_title));
        toolCategory.setDescription(getString(R.string.tool_category_design_description));
        listData.add(toolCategory);

        toolCategory = new ToolCategory();
        toolCategory.setKey(Config.TOOL_CATEGORY_PROJECT);
        toolCategory.setTitle(getString(R.string.tool_category_project_title));
        toolCategory.setDescription(getString(R.string.tool_category_project_description));
        listData.add(toolCategory);
    }
}
