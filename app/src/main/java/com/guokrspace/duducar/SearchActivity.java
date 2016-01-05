package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
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
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.guokrspace.duducar.common.CommonAddrType;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.communication.fastjson.FastJsonTools;
import com.guokrspace.duducar.communication.message.SearchLocation;
import com.guokrspace.duducar.database.CommonUtil;
import com.guokrspace.duducar.database.DaoSession;
import com.guokrspace.duducar.database.SearchHistory;
import com.guokrspace.duducar.model.AddrRowDescriptor;
import com.guokrspace.duducar.util.SharedPreferencesUtils;
import com.guokrspace.duducar.util.Trace;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    public static final String PREORDERACTIVITY = "searchCommonAddr";
    public static final String COMMONADDRACTIVITY = "searchDestination";

    private static final String ARG_CITY = "city";
    private static final String ARG_FROM = "from";
    private static final String ARG_TYPE = "common_addr_type";

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggestionSearch = null;
    private int load_Index = 0;
    private LatLng mLoc;
    private LatLng mReqLoc;

    private Toolbar mToolbar;

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
    private LinearLayout commonAttrLayout;

    private List<PoiInfo> mDataset = new ArrayList<>();
    ResultListAdapter mAdapter;

    private String  mCity;
    private SearchLocation location;
    private Context mContext;

    private DaoSession dbSession;
    private DuduApplication mApplication;
    private String fromPage;
    private CommonAddrType addrType;
    private Button cancelSearchBtn;
    private TextView tvAddrHome;
    private Drawable homeIcon;
    private TextView tvAddrCompany;
    private Drawable companyIcon;
    private FrameLayout homeTabLayout;
    private FrameLayout companyTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        AppExitUtil.getInstance().addActivity(this);

        //Get ARGs
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            fromPage = bundle.getString(ARG_FROM);
            if (TextUtils.equals(fromPage, PREORDERACTIVITY)) {
                mCity = bundle.getString(ARG_CITY);
                location = (SearchLocation)bundle.get("location");
                if(location!=null)
                    mReqLoc = location.getLocation();
            } else if (TextUtils.equals(fromPage, COMMONADDRACTIVITY)) {
                addrType = CommonAddrType.getByDesc(bundle.getString(ARG_TYPE));
            }

        }


        mContext = this;
        mApplication =(DuduApplication)getApplicationContext();
        dbSession = mApplication.mDaoSession;

        //init toobar
        initToolBar();

        //Init the search components
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(this);

        //Init UI
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("搜索");

        commonAttrLayout = (LinearLayout) findViewById(R.id.common_addr_layout);
        editSearchKey = (EditText) findViewById(R.id.searchkey);
        editSearchKey.setText("");

        if (TextUtils.equals(fromPage, PREORDERACTIVITY)) {
            commonAttrLayout.setVisibility(View.VISIBLE);
            int mIconSize = this.getResources().getDimensionPixelSize(R.dimen.common_addr_icon_size);

            tvAddrHome = (TextView) findViewById(R.id.addr_home_tv);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                homeIcon = this.getResources().getDrawable(R.mipmap.home, this.getTheme());
            } else {
                homeIcon = this.getResources().getDrawable(R.mipmap.home);
            }
            homeIcon.setColorFilter(this.getResources().getColor(R.color.button_backgroud), PorterDuff.Mode.MULTIPLY);
            homeIcon.setBounds(0, 0, mIconSize, mIconSize);
            TextViewCompat.setCompoundDrawablesRelative(tvAddrHome, homeIcon, null, null, null);

            tvAddrCompany = (TextView) findViewById(R.id.addr_company_tv);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                companyIcon = this.getResources().getDrawable(R.mipmap.company, this.getTheme());
            } else {
                companyIcon = this.getResources().getDrawable(R.mipmap.company);
            }
            companyIcon.setColorFilter(this.getResources().getColor(R.color.button_backgroud), PorterDuff.Mode.MULTIPLY);
            companyIcon.setBounds(0, 0, mIconSize, mIconSize);
            TextViewCompat.setCompoundDrawablesRelative(tvAddrCompany, companyIcon, null, null, null);

            editSearchKey.setHint("输入格式:长沙 湖南大学");
        } else if (TextUtils.equals(fromPage, COMMONADDRACTIVITY)) {
            commonAttrLayout.setVisibility(View.GONE);
            switch (addrType) {
                case HOME:
                    editSearchKey.setHint("输入家庭住址");
                    break;
                case COMPANY:
                    editSearchKey.setHint("输入公司地址");
                    break;
                default:
                    break;
            }
        }

        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.searchkey);
        sugAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);

        cancelSearchBtn = (Button) findViewById(R.id.cancel_search);
        cancelSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(keyWorldsView.getText().toString())) {
                    keyWorldsView.setText("");
                }
            }
        });

        homeTabLayout = (FrameLayout)findViewById(R.id.homeTabLayout);
        homeTabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击常用地址”家“
                String addrInfo = (String) SharedPreferencesUtils.getParam(mContext, SharedPreferencesUtils.COMMON_ADDR_HOME, "");
                if (TextUtils.isEmpty(addrInfo)) {
                    Toast.makeText(mContext, "请到\"设置-常用地址\"中设置家庭地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                AddrRowDescriptor descriptor = JSON.parseObject(addrInfo, AddrRowDescriptor.class);
                Intent intent = new Intent();
                SearchLocation location = new SearchLocation();
                location.setLat(descriptor.latitude);
                location.setLng(descriptor.longitude);
                location.setAddress(descriptor.addrName);
                intent.putExtra("location", location);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        companyTabLayout = (FrameLayout) findViewById(R.id.companyTabLayout);
        companyTabLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击常用地址”公司“
                String addrInfo = (String) SharedPreferencesUtils.getParam(mContext, SharedPreferencesUtils.COMMON_ADDR_COMPANY, "");
                if (TextUtils.isEmpty(addrInfo)) {
                    Toast.makeText(mContext, "请到\"设置-常用地址\"中设置公司地址", Toast.LENGTH_SHORT).show();
                    return;
                }
                AddrRowDescriptor descriptor = JSON.parseObject(addrInfo, AddrRowDescriptor.class);
                Intent intent = new Intent();
                SearchLocation location = new SearchLocation();
                location.setLat(descriptor.latitude);
                location.setLng(descriptor.longitude);
                location.setAddress(descriptor.addrName);
                intent.putExtra("location", location);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

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
        if(TextUtils.equals(fromPage, COMMONADDRACTIVITY) || mReqLoc!=null) {
            keyWorldsView.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable keyWord) {
                    if(keyWord == null || keyWord.length() ==0){
                        return;
                    }
                    String key = keyWord.toString();
                    if(key.split(" ").length > 1){ // 存在空格
                        String[] keys = key.split(" ");
                        String city = "", k = "";
                        for(String s : keys){
                            if(Constants.citys.contains(s)){// 存在城市
                                city = s;
                            } else {
                                k += " " + s;
                            }
                        }
                        mPoiSearch.searchInCity(new PoiCitySearchOption()
                                .city(city)
                                .keyword(k));
//                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(k).city(city));
                    } else {
                        mPoiSearch.searchNearby(new PoiNearbySearchOption()
                                .keyword(key)
                                .location(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng()))
                                .sortType(PoiSortType.comprehensive));
//                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(key).city("长沙").location(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng())));
                    }

//                    mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    SearchHistory searchHistory = new SearchHistory();
                    searchHistory.setAddress(editSearchKey.getText().toString());
                    searchHistory.setDetails("");
                    dbSession.getSearchHistoryDao().insert(searchHistory);
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                }

                @Override
                public void onTextChanged(CharSequence keyWord, int arg1, int arg2, int arg3) {
                    if (keyWord.length() <= 0) {
                        return;
                    }
                    /**
                     * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
                     */
                    String key = keyWord.toString();
                    if(key.split(" ").length > 1){ // 存在空格
                        String[] keys = key.split(" ");
                        String city = "", k = "";
                        for(String s : keys){
                            if(Constants.citys.contains(s)){// 存在城市
                                city = s;
                            } else {
                                k += " " + s;
                            }
                        }
                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(k).city(city));
                    } else {
                        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(key).city("长沙").location(new LatLng(CommonUtil.getCurLat(), CommonUtil.getCurLng())));
                    }
//                    if(cs)
//                    mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).keyword(cs.toString()).city("长沙").city("常德"));
                }
            });
        }

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("搜索地点");
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchActivity.this.finish();
            }
        });
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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPoiSearch != null) mPoiSearch.destroy();
        if (mSuggestionSearch != null) mSuggestionSearch.destroy();
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

            return;
        }
        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {

            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
//            if(keyWorldsView.getText() != null && keyWorldsView.getText().length() > 0){
//                mSuggestionSearch.requestSuggestion((new SuggestionSearchOption()).location(new LatLng(CommonUtil.getCurLat(),CommonUtil.getCurLng()))
//                        .keyword(keyWorldsView.toString()));
//            }
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
            public TextView mSearchResultDescrptionItemTextView;
            public RelativeLayout mRLayout;
            public ViewHolder(View v) {
                super(v);
                mSearchResultItemTextView = (TextView)v.findViewById(R.id.resultTextView);
                mSearchResultDescrptionItemTextView = (TextView)v.findViewById(R.id.resultDescriptionTextView);
                mRLayout = (RelativeLayout)v.findViewById(R.id.itemRLayout);
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
            holder.mSearchResultDescrptionItemTextView.setText(mDataset.get(position).address);
            holder.mRLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Trace.e("hyman_poi", mDataset.get(position).address + " " + mDataset.get(position).name + " ");
                    PoiInfo info = mDataset.get(position);
                    mLoc = info.location;
                    if (TextUtils.equals(fromPage, PREORDERACTIVITY)) {
                        Intent intent = new Intent();
                        SearchLocation location = new SearchLocation();
                        location.setLat(mLoc.latitude);
                        location.setLng(mLoc.longitude);
                        location.setAddress(mDataset.get(position).name);
                        intent.putExtra("location", location);
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if (TextUtils.equals(fromPage, COMMONADDRACTIVITY)) {
                        AddrRowDescriptor descriptor = new AddrRowDescriptor(R.mipmap.home, addrType.getDesc(), info.name, info.address, mLoc.latitude, mLoc.longitude);
                        String commonAddrJson = JSON.toJSONString(descriptor);
                        switch (addrType) {
                            case HOME:
                                SharedPreferencesUtils.setParam(mContext, SharedPreferencesUtils.COMMON_ADDR_HOME, commonAddrJson);
                                break;
                            case COMPANY:
                                SharedPreferencesUtils.setParam(mContext, SharedPreferencesUtils.COMMON_ADDR_COMPANY, commonAddrJson);
                                break;
                        }
                        Intent addrIntent = new Intent();
                        addrIntent.putExtra("addrType", addrType.getDesc());
                        addrIntent.putExtra("addrInfo", descriptor);
                        setResult(RESULT_OK, addrIntent);
                        finish();
                    }
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
