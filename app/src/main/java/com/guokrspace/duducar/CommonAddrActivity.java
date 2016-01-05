package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.guokrspace.duducar.common.CommonAddrType;
import com.guokrspace.duducar.model.AddrRowDescriptor;
import com.guokrspace.duducar.model.HistoryOrdersResponse;
import com.guokrspace.duducar.ui.RowViewGroup;
import com.guokrspace.duducar.util.SharedPreferencesUtils;
import com.guokrspace.duducar.util.Trace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyman on 16/1/4.
 */
public class CommonAddrActivity extends AppCompatActivity implements RowViewGroup.OnRowTabListener{

    public static final int SEARCH_COMMON_ADDR = 100;

    private Context context;
    private Toolbar mToolbar;
    private RowViewGroup rowGroupView;
    private List<AddrRowDescriptor> descriptors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commonaddress);
        this.context = CommonAddrActivity.this;
        initView();
    }

    private void initView() {
        initToolbar();

        this.rowGroupView = (RowViewGroup) findViewById(R.id.rowgroup);
        descriptors = new ArrayList<>();
        String homeAddr = (String) SharedPreferencesUtils.getParam(context, SharedPreferencesUtils.COMMON_ADDR_HOME, "");
        if (!TextUtils.isEmpty(homeAddr)) {
            descriptors.add(JSON.parseObject(homeAddr, AddrRowDescriptor.class));
        } else {
            descriptors.add(new AddrRowDescriptor(R.mipmap.home, "家", "点击设置", "", null, null));
        }
        String companyAddr = (String) SharedPreferencesUtils.getParam(context, SharedPreferencesUtils.COMMON_ADDR_COMPANY, "");
        if (!TextUtils.isEmpty(companyAddr)) {
            descriptors.add(JSON.parseObject(companyAddr, AddrRowDescriptor.class));
        } else {
            descriptors.add(new AddrRowDescriptor(R.mipmap.company, "公司", "点击设置", "", null, null));
        }
        rowGroupView.initializeData(descriptors, this);
        rowGroupView.notifyDataChanged();
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonAddrActivity.this.finish();
            }
        });
    }

    @Override
    public void onRowTab(CommonAddrType type) {
        Toast.makeText(context, type.name(), Toast.LENGTH_SHORT).show();
        Intent _intent = new Intent();
        _intent.putExtra("from", SearchActivity.COMMONADDRACTIVITY);
        _intent.putExtra("common_addr_type", type.getDesc());
        _intent.setClass(context, SearchActivity.class);
        startActivityForResult(_intent, SEARCH_COMMON_ADDR);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == SEARCH_COMMON_ADDR) {
                    if (data != null) {
                        String addrDesc = data.getStringExtra("addrType");
                        AddrRowDescriptor addrDescriptor = (AddrRowDescriptor) data.getSerializableExtra("addrInfo");
                        for (AddrRowDescriptor descriptor : descriptors) {
                            if (TextUtils.equals(addrDesc, descriptor.rowName)) {
                                descriptor.addrName = addrDescriptor.addrName;
                                descriptor.addrDetail = addrDescriptor.addrDetail;
                                descriptor.latitude = addrDescriptor.latitude;
                                descriptor.longitude = addrDescriptor.longitude;
                                rowGroupView.initializeData(descriptors, this);
                                rowGroupView.refreshData();
                                break;
                            }
                        }
                    }
                }
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
