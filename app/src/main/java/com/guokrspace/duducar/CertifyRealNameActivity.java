package com.guokrspace.duducar;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.guokrspace.duducar.communication.ResponseHandler;
import com.guokrspace.duducar.communication.SocketClient;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.database.PersonalInformationDao;
import com.guokrspace.duducar.util.Trace;

import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * Created by hyman on 16/11/2.
 */
public class CertifyRealNameActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText etRealName;
    private EditText etIdNumber;
    private Button publishButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certify_realname);
        initView();
    }

    private void initView() {
        initToolbar();
        etRealName = (EditText) findViewById(R.id.realname_et);
        etIdNumber = (EditText) findViewById(R.id.idnumber_et);
        publishButton = (Button) findViewById(R.id.publish_button);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
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
                CertifyRealNameActivity.this.finish();
            }
        });
    }

    public void publishCertifyInfo(View view) {
        // 提交实名认证数据
        String realname = etRealName.getText().toString();
        String idnumber = etIdNumber.getText().toString();

        if (TextUtils.isEmpty(realname)) {
            showToast("请填写真实姓名！");
            return;
        }
        if (TextUtils.isEmpty(idnumber)) {
            showToast("身份证号不可为空！");
            return;
        }

        mProgressBar.setVisibility(View.VISIBLE);
        publishButton.setEnabled(false);

        SocketClient.getInstance().certifyRealname(realname, idnumber, new ResponseHandler(Looper.myLooper()) {
            @Override
            public void onSuccess(String messageBody) {
                Trace.e("CertifyRealNameActivity: " + messageBody);
                showFeedbackDialog("提交成功！");
                // 更新数据库
                PersonalInformationDao personalInformationDao = DuduApplication.getInstance().mDaoSession.getPersonalInformationDao();
                List<PersonalInformation> infos = personalInformationDao.queryBuilder().list();
                PersonalInformation personalInformation = null;
                if (!infos.isEmpty()) {
                    personalInformation = infos.get(0);
                }
                if (personalInformation != null) {
                    personalInformation.setRealname_certify_status("1");
                    personalInformationDao.update(personalInformation);
                }
            }

            @Override
            public void onFailure(String error) {
                showFeedbackDialog("提交失败，请稍后尝试！");
            }

            @Override
            public void onTimeout() {
                showFeedbackDialog("超时，请稍后尝试！");
            }
        });

    }

    private void showFeedbackDialog(String msg) {
        mProgressBar.setVisibility(View.INVISIBLE);
        final MaterialDialog dialog = new MaterialDialog(CertifyRealNameActivity.this);
        dialog.setMessage(msg);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setPositiveButton("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        publishButton.setEnabled(true);
    }

    private void showToast(String msg) {
        Toast.makeText(CertifyRealNameActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
