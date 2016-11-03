package com.guokrspace.duducar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.database.PersonalInformationDao;
import com.guokrspace.duducar.util.Trace;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;


public class PersonalInfoActivity extends AppCompatActivity implements View.OnClickListener {


    private DuduApplication mApplication;
    private Context mContext;
    private Toolbar mToolbar;
    private RelativeLayout personalCertifyLayout, carCertifyLayout;
    private Button editInfoBtn;
    private TextView tvNickname;
    private TextView tvIndustry;
    private TextView tvCompany;
    private TextView tvProfession;
    private TextView tvSignature;
    private TextView tvRealNameCertifyStatus;
    private TextView tvDriverCertifyStatus;

    private int realnameCertifyStatus = 0;
    private int driverCertifyStatus = 0;


    public PersonalInfoActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_personal_info);
        mApplication = (DuduApplication)getApplicationContext();
        mContext = this;
        initView();

        /*materialListView = (MaterialListView)findViewById(R.id.material_listview);

        for(PersonalInformation personalInformation:mApplication.mDaoSession.getPersonalInformationDao().queryBuilder().list())
        {
            Card card = new Card.Builder(mContext)
                    .setTag("SMALL_IMAGE_CARD")
                    .setDismissible()
                    .withProvider(new CardProvider<>())
                    .setLayout(R.layout.material_small_image_card)
                    .setTitle(personalInformation.getMobile())
                    .setTitleColor(Color.GRAY)
                    .setDescription("关于我的介绍...")
                    .setDescriptionColor(Color.GRAY)
                    .setDrawable(R.drawable.ic_drawer)
                    .setBackgroundColor(Color.WHITE)
                    .setDrawableConfiguration(new CardProvider.OnImageConfigListener() {
                        @Override
                        public void onImageConfigure(@NonNull final RequestCreator requestCreator) {
                            requestCreator.rotate(90.0f)
                                    .resize(150, 150)
                                    .centerCrop();
                        }
                    })
                    .endConfig()
                    .build();
            materialListView.getAdapter().add(card);

        }*/
    }

    private void initView() {
        //init toolbar
        initToolBar();
        personalCertifyLayout = (RelativeLayout) findViewById(R.id.certify_personal_layout);
        carCertifyLayout = (RelativeLayout) findViewById(R.id.certify_car_layout);
        editInfoBtn = (Button) findViewById(R.id.edit_info_btn);
        tvNickname = (TextView) findViewById(R.id.nickname_txt);
        tvIndustry = (TextView) findViewById(R.id.industry_txt);
        tvCompany = (TextView) findViewById(R.id.company_txt);
        tvProfession = (TextView) findViewById(R.id.profession_txt);
        tvSignature = (TextView) findViewById(R.id.signature_txt);
        tvRealNameCertifyStatus = (TextView) findViewById(R.id.personal_certify_status_txt);
        tvDriverCertifyStatus = (TextView) findViewById(R.id.car_certify_status_txt);

        personalCertifyLayout.setOnClickListener(this);
        carCertifyLayout.setOnClickListener(this);
        editInfoBtn.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // 从数据库读取数据显示
        List<PersonalInformation> personalInfos = DuduApplication.getInstance().mDaoSession.getPersonalInformationDao().queryBuilder().list();
        PersonalInformation personalInformation = personalInfos.get(0);
        if (personalInformation != null) {
            if (!TextUtils.isEmpty(personalInformation.getNickname())) {
                tvNickname.setText(personalInformation.getNickname());
            }
            if (!TextUtils.isEmpty(personalInformation.getIndustry())) {
                tvIndustry.setText(personalInformation.getIndustry());
            }
            if (!TextUtils.isEmpty(personalInformation.getCompany())) {
                tvCompany.setText(personalInformation.getCompany());
            }
            if (!TextUtils.isEmpty(personalInformation.getProfession())) {
                tvProfession.setText(personalInformation.getProfession());
            }
            if (!TextUtils.isEmpty(personalInformation.getSignature())) {
                tvSignature.setText(personalInformation.getSignature());
            }
            int realnameCertifyStatus = Integer.parseInt(personalInformation.getRealname_certify_status());
            int driverCertifyStatus = Integer.parseInt(personalInformation.getDriver_certify_status());
            String realNameCertifyDeS = Constants.getCertifyStatusDes(realnameCertifyStatus);
            String driverCertifyDes = Constants.getCertifyStatusDes(driverCertifyStatus);
            tvRealNameCertifyStatus.setText(realNameCertifyDeS);
            tvDriverCertifyStatus.setText(driverCertifyDes);
        }

        // 获取认证状态
        SocketClient.getInstance().checkCertifyStatus(new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Trace.e("PersonalInfoActivity-> " + messageBody);
                JSONObject respObj = JSON.parseObject(messageBody);
                if (respObj != null) {
                    realnameCertifyStatus = respObj.getInteger("realname_certify_status");
                    driverCertifyStatus = respObj.getInteger("driver_certify_status");
                    // 更新界面，并写入数据库
                    String realNameCertifyDeS = Constants.getCertifyStatusDes(realnameCertifyStatus);
                    String driverCertifyDes = Constants.getCertifyStatusDes(driverCertifyStatus);
                    tvRealNameCertifyStatus.setText(realNameCertifyDeS);
                    tvDriverCertifyStatus.setText(driverCertifyDes);


                    PersonalInformationDao personalInformationDao = DuduApplication.getInstance().mDaoSession.getPersonalInformationDao();
                    List<PersonalInformation> personalInformations = personalInformationDao.queryBuilder().list();
                    PersonalInformation personalInformation = null;
                    if (personalInformations.size() > 0) {
                        personalInformation = personalInformations.get(0);
                    }
                    if (personalInformation != null) {
                        if (personalInformation.getRealname_certify_status().equals(realnameCertifyStatus + "") &&
                                personalInformation.getDriver_certify_status().equals(driverCertifyStatus + "")) {
                            personalInformation.setRealname_certify_status(realnameCertifyStatus + "");
                            personalInformation.setDriver_certify_status(driverCertifyStatus + "");

                            personalInformationDao.update(personalInformation);
                        }
                    }
                }

            }

            @Override
            public void onFailure(String error) {
                Trace.e("PersonalInfoActivity->checkCertifyStatus", "request failure : " + error);
            }

            @Override
            public void onTimeout() {
                Trace.e("PersonalInfoActivity->checkCertifyStatus", "request timeout");
            }
        });

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
        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalInfoActivity.this.finish();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.certify_personal_layout:
                // 状态0为未提交、2为提交认证失败
                if (realnameCertifyStatus == 0 || realnameCertifyStatus == 2) {
                    startActivity(new Intent(mContext, CertifyRealNameActivity.class));
                }
                if (realnameCertifyStatus == 1) {
                    showDialog("认证处理中请等待结果");
                }
                if (realnameCertifyStatus == 3) {
                    showDialog("认证成功，不用重复认证！");
                }
                break;
            case R.id.certify_car_layout:
                Toast.makeText(mContext, "certify_car_layout", Toast.LENGTH_SHORT).show();
                break;
            case  R.id.edit_info_btn:
                Intent _intent = new Intent();
                _intent.setClass(mContext, EditPersonalInfoActivity.class);
                startActivity(_intent);
                break;
        }

    }

    private void showDialog(String msg) {
        final MaterialDialog dialog = new MaterialDialog(PersonalInfoActivity.this);
        dialog.setMessage(msg);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setPositiveButton("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


}
