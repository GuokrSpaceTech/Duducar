package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.guokrspace.duducar.communication.message.SearchLocation;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private static final String ARG_CITY = "city";

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private int load_Index = 0;
    private LatLng mLoc;
    private LatLng mReqLoc;

    /**
     * 软键盘的控制
     */
    private InputMethodManager mSoftManager;

    /**
     * 搜索关键字输入窗口
     */
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private TextView editCity;
    private EditText editSearchKey;
    private RecyclerView recyclerView;
    private List<PoiInfo> mDataset = new ArrayList<>();
    ResultListAdapter mAdapter;

    private String  mCity;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Get ARGs
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            mCity = bundle.getString(ARG_CITY);
            SearchLocation location = (SearchLocation)bundle.get("location");
            if(location!=null)
                mReqLoc = location.getLocation();
        }

        mContext = this;

        //Init the search components
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        //Init UI
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("搜索");
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);

//        editCity = (TextView) findViewById(R.id.city);
//        editCity.setText(mCity);

        editSearchKey = (EditText) findViewById(R.id.searchkey);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView = (RecyclerView)findViewById(R.id.resultRecyclerView);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ResultListAdapter(mDataset);
        recyclerView.setAdapter(mAdapter);

        mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        /**
         * 当输入关键字变化时，动态更新建议列表
         */
        keyWorldsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable arg0) {
//                mPoiSearch.searchNearby(new PoiNearbySearchOption()
//                        .location(mReqLoc)
//                        .radius(20000) //20Km
//                        .keyword(editSearchKey.getText().toString()));
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                if (cs.length() <= 0) {
                    return;
                }
//                String city = ((TextView) findViewById(R.id.city)).getText().toString();
                /**
                 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                 */
                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString()).city("长沙"));


//                mPoiSearch.searchInCity((new PoiCitySearchOption())
//                        .city(mCity)
//                        .keyword(editSearchKey.getText().toString())
//                        .pageNum(load_Index));

                mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        Button searchButton = (Button)findViewById(R.id.search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchButtonProcess(view);
            }
        });
//        Button nextBatchData = (Button)findViewById(R.id.map_next_data);
//        nextBatchData.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                load_Index++;
//                searchButtonProcess(null);
//            }
//        });
    }

    /**
     * 影响搜索按钮点击事件
     *
     * @param v
     */
    public void searchButtonProcess(View v) {

        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                .location(mReqLoc)
                .radius(100000) //100Km
                .keyword(editSearchKey.getText().toString()));
        mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            finish();
            return true;
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onGetPoiResult(PoiResult result) {
        if (result == null
                || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(this, "未找到结果", Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (result.error == SearchResult.ERRORNO.NO_ERROR) {

            mDataset.clear();
            mDataset.addAll(result.getAllPoi());
            mAdapter.mDataset = mDataset;
            mAdapter.notifyDataSetChanged();

//            ((MainActivity)mContext).mBaiduMap.clear();
//            PoiOverlay overlay = new MyPoiOverlay(((MainActivity)mContext).mBaiduMap);
//            ((MainActivity)mContext).mBaiduMap.setOnMarkerClickListener(overlay);
//            overlay.setData(result);
//            overlay.addToMap();
//            overlay.zoomToSpan();
            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(mContext, strInfo, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {
        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(mContext, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, result.getName() + ": " + result.getAddress(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onGetSuggestionResult(SuggestionResult res) {
        if (res == null || res.getAllSuggestions() == null) {
            return;
        }
        sugAdapter.clear();
        for (SuggestionResult.SuggestionInfo info : res.getAllSuggestions()) {
            if (info.key != null)
                sugAdapter.add(info.key);
        }
        sugAdapter.notifyDataSetChanged();
    }

    public class ResultListAdapter extends RecyclerView.Adapter<ResultListAdapter.ViewHolder> {
        private List<PoiInfo> mDataset;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mSearchResultItemTextView;
            public ViewHolder(View v) {
                super(v);
                mSearchResultItemTextView = (TextView)v.findViewById(R.id.resultTextView);
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ResultListAdapter(List<PoiInfo> myDataset) {
            mDataset = myDataset;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_item, parent, false);
            // set the view's size, margins, paddings and layout parameters

            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            holder.mSearchResultItemTextView.setText(mDataset.get(position).name);
            holder.mSearchResultItemTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mLoc = mDataset.get(position).location;
                    Intent intent = new Intent();
                    SearchLocation location = new SearchLocation();
                    location.setLat(mLoc.latitude);
                    location.setLng(mLoc.longitude);
                    location.setAddress(mDataset.get(position).name);
                    intent.putExtra("location", location);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
