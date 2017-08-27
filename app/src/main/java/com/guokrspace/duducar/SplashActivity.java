package com.guokrspace.duducar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.guokrspace.duducar.common.CommonUtil;
import java.util.HashMap;
import java.util.Map;

public class SplashActivity extends Activity {

    private  Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_);

        if(!CommonUtil.getPermissions(this)) {
            enterPreOrder();
        }

        Log.e("Daddy", "first page");

    }


    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
        @NonNull int[] grantResults)
    {
        switch(requestCode){
            case CommonUtil.SDK_PERMISSION_REQUEST:
                Map<String, Integer> perms = new HashMap<>();

                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

                for(int i = 0; i < permissions.length; i++){
                    perms.put(permissions[i], grantResults[i]);
                }
                int permission = perms.get(Manifest.permission.ACCESS_FINE_LOCATION);
                if(perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    //permission granted
                    enterPreOrder();
                } else {
                    Log.e("PERMISSION", "REQUEST PERMISSION");
                    // 拒绝并勾选了不在询问
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
                    {
                        CommonUtil.showMessageOKCancel(SplashActivity.this, "无法完成定位,请查看权限管理!",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                    startSettingActivity();
                                    AppExitUtil.getInstance().exit();
                                }
                            }, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    //直接退出
                                    dialog.dismiss();
                                    AppExitUtil.getInstance().exit();
                                }
                            });
                        return;
                    } else {
                        //permission denied , confirm the user to do again
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, CommonUtil.SDK_PERMISSION_REQUEST);
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

    }

    private void enterPreOrder()
    {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, PreOrderActivity.class);
                startActivity(intent);
                finish();
            }
        }, 1200);
    }

    private void startSettingActivity()
    {

        Intent i = new Intent();
        i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + this.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            startActivity(i);
        }
        catch (Exception e)
        {
            Log.e("PickUpService", "start Setting failed");
        }
    }


}
