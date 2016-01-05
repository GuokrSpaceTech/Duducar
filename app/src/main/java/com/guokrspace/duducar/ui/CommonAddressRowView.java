package com.guokrspace.duducar.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guokrspace.duducar.R;
import com.guokrspace.duducar.model.AddrRowDescriptor;

/**
 * Created by hyman on 16/1/4.
 */
public class CommonAddressRowView extends LinearLayout {

    private Context context;
    private AddrRowDescriptor addrRowDescriptor;
    private ImageView rowIcon;
    private TextView rowName;
    private TextView addrNameTextView;
    private TextView addrDetailTextView;

    public CommonAddressRowView(Context context) {
        this(context, null);
    }

    public CommonAddressRowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CommonAddressRowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView(context);
    }

    private void initializeView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.commonaddress_row_view, this);
        rowIcon = (ImageView) findViewById(R.id.ca_row_icon);
        rowName = (TextView) findViewById(R.id.ca_row_name);
        addrNameTextView = (TextView) findViewById(R.id.address_name);
        addrDetailTextView = (TextView) findViewById(R.id.address_detail);
        this.context = context;
        setOrientation(VERTICAL);
    }

    public void initializeData(AddrRowDescriptor descriptor) {
        this.addrRowDescriptor = descriptor;
    }

    public void notifyDataChanged() {
        if (addrRowDescriptor != null) {
            rowIcon.setImageResource(addrRowDescriptor.iconResId);
            rowName.setText(addrRowDescriptor.rowName);
            addrNameTextView.setText(addrRowDescriptor.addrName);
            addrDetailTextView.setText(addrRowDescriptor.addrDetail);

        } else {
          setVisibility(GONE);
        }
    }

}
