package com.guokrspace.duducar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.alibaba.fastjson.JSONObject;
import com.guokrspace.duducar.common.Constants;
import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.database.PersonalInformationDao;
import com.guokrspace.duducar.util.Trace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by hyman on 16/11/2.
 */
public class EditPersonalInfoActivity extends AppCompatActivity implements View.OnClickListener {


    private Toolbar mToolbar;
    private EditText etNickname;
    private RadioGroup rgSex;
    private RadioButton rbMale, rbFemale;
    private EditText etAge;
    private EditText etIndustry;
    private EditText etCompany;
    private EditText etProfession;
    private EditText etSignature;
    private Button btnPublish;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);
        initView();
    }

    private void initView() {
        initToolbar();
        mProgressBar = (ProgressBar) findViewById(R.id.progressbar);
        etNickname = (EditText) findViewById(R.id.nickname_et);
        rgSex = (RadioGroup) findViewById(R.id.sex_rg);
        rbMale = (RadioButton) findViewById(R.id.male_rb);
        rbFemale = (RadioButton) findViewById(R.id.female_rb);
        etAge = (EditText) findViewById(R.id.age_et);
        etIndustry = (EditText) findViewById(R.id.industry_et);
        etCompany = (EditText) findViewById(R.id.company_et);
        etProfession = (EditText) findViewById(R.id.profession_et);
        etSignature = (EditText) findViewById(R.id.signature_et);
        btnPublish = (Button) findViewById(R.id.publish_info);
        btnPublish.setOnClickListener(this);

        // 将数据库中已有的数据实现在界面上
        List<PersonalInformation> infos = DuduApplication.getInstance().mDaoSession.getPersonalInformationDao().queryBuilder().list();
        PersonalInformation personalInformation = null;
        if (infos.size() > 0) {
            personalInformation = infos.get(0);
        }
        if (personalInformation != null) {
            String nickname = personalInformation.getNickname();
            if (!TextUtils.isEmpty(nickname)) {
                etNickname.setText(nickname);
                etNickname.setSelection(nickname.length());
            }
            String sex = personalInformation.getSex();
            if (!TextUtils.isEmpty(sex)) {
                int sexTag = Integer.parseInt(sex);
                rgSex.clearCheck();
                if (sexTag == 0) {
                    rbMale.setChecked(true);
                }
                if (sexTag == 1) {
                    rbFemale.setChecked(true);
                }
            }
            String age = personalInformation.getAge();
            if (!TextUtils.isEmpty(age)) {
                etAge.setText(age);
                etAge.setSelection(age.length());
            }
            String industry = personalInformation.getIndustry();
            if (!TextUtils.isEmpty(industry)) {
                etIndustry.setText(industry);
                etIndustry.setSelection(industry.length());
            }
            String company = personalInformation.getCompany();
            if (!TextUtils.isEmpty(company)) {
                etCompany.setText(company);
                etCompany.setSelection(company.length());
            }
            String profession = personalInformation.getProfession();
            if (!TextUtils.isEmpty(profession)) {
                etProfession.setText(profession);
                etProfession.setSelection(profession.length());
            }
            String signature = personalInformation.getSignature();
            if (!TextUtils.isEmpty(signature)) {
                etSignature.setText(signature);
                etSignature.setSelection(signature.length());
            }
        }
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
                EditPersonalInfoActivity.this.finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        int vid  = v.getId();
        switch (vid) {
            case R.id.publish_info:
                mProgressBar.setVisibility(View.VISIBLE);
                doPublish();
                break;
            default:
                break;
        }
    }

    private void doPublish() {
        final String nickname = etNickname.getText().toString();
        int sexTag = rgSex.getCheckedRadioButtonId() == rbFemale.getId() ? 1 : 0;
        final String sex = String.valueOf(sexTag);
        final String age = etAge.getText().toString();
        final String industry = etIndustry.getText().toString();
        final String company = etCompany.getText().toString();
        final String profession = etProfession.getText().toString();
        final String signature = etSignature.getText().toString();

        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("nickname", nickname);
        paramsMap.put("sex", sex);
        paramsMap.put("age", age);
        paramsMap.put("industry", industry);
        paramsMap.put("company", company);
        paramsMap.put("profession", profession);
        paramsMap.put("signature", signature);
        SocketClient.getInstance().updatePersonalInfo(paramsMap, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Trace.e("EditPersonalInfoActivity: " + messageBody);

                // 写入数据库
                PersonalInformationDao personalInformationDao = DuduApplication.getInstance().mDaoSession.getPersonalInformationDao();
                List<PersonalInformation> infos = personalInformationDao.queryBuilder().list();
                PersonalInformation personalInformation = null;
                if (!infos.isEmpty()) {
                    personalInformation = infos.get(0);
                }
                if (personalInformation != null) {
                    personalInformation.setNickname(nickname);
                    personalInformation.setSex(sex);
                    personalInformation.setAge(age);
                    personalInformation.setIndustry(industry);
                    personalInformation.setCompany(company);
                    personalInformation.setProfession(profession);
                    personalInformation.setSignature(signature);
                }
                personalInformationDao.update(personalInformation);
                showFeedbackDialog("提交成功！");

                //更新drawerview界面
                Intent intent = new Intent();
                intent.setAction(Constants.ACTION_REFRESH_DRAWERVIEW);
//                sendBroadcast(intent);
                sendOrderedBroadcast(intent, null);
            }


            @Override
            public void onFailure(String error) {
                showFeedbackDialog("提交失败，请稍后尝试！");
                Trace.e("EditPersonalInfoActivity->updatePersonalInfo " + error);
            }

            @Override
            public void onTimeout() {
                showFeedbackDialog("超时，请稍后尝试！");
                Trace.e("EditPersonalInfoActivity->updatePersonalInfo " + "request timeout");
            }
        });


    }

    private void showFeedbackDialog(String msg) {
        mProgressBar.setVisibility(View.INVISIBLE);
        final MaterialDialog dialog = new MaterialDialog(EditPersonalInfoActivity.this);
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
}
