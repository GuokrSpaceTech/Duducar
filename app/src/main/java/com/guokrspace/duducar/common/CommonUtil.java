package com.guokrspace.duducar.common;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import android.util.Log;
import com.guokrspace.duducar.AppExitUtil;
import com.guokrspace.duducar.communication.message.OrderDetail;
import com.guokrspace.duducar.database.PersonalInformation;
import com.guokrspace.duducar.util.PhoneUtils;
import java.util.ArrayList;

/**
 * Created by daddyfang on 15/11/13.
 */
public class CommonUtil {

  private static String IMSI;
  private static String IMEI;
  private static String MAC;

  public static boolean isLocationSuccess()
  {
    return locationSuccess;
  }

  public static void setLocationSuccess(boolean locationSuccess)
  {
    CommonUtil.locationSuccess = locationSuccess;
  }

  public static boolean locationSuccess = false;

  public static double getCurLng()
  {
    return curLng;
  }

  public static void setCurLng(double curLng)
  {
    CommonUtil.curLng = curLng;
  }

  public static double getCurLat()
  {
    return curLat;
  }

  public static void setCurLat(double curLat)
  {
    CommonUtil.curLat = curLat;
  }

  private static double curLng;
  private static double curLat;

  public static long getCurTime()
  {
    return curTime;
  }

  public static void setCurTime(long curTime)
  {
    CommonUtil.curTime = curTime;
  }

  private static long curTime;

  public static PersonalInformation getPersion()
  {
    return persion;
  }

  public static void setPersion(PersonalInformation persion)
  {
    CommonUtil.persion = persion;
  }

  private static PersonalInformation persion;

  public static OrderDetail tripOverOrderDetail = null;

  public static boolean isNetworkAvailable(Context context)
  {
    Context _context = context.getApplicationContext();
    NetworkInfo info = getNetworkInfo(_context);
    if (info != null)
    {
      return info.isAvailable();
    }
    return false;
  }

  private static NetworkInfo getNetworkInfo(Context context)
  {
    ConnectivityManager cm =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    return cm.getActiveNetworkInfo();
  }

  /*
   * 格式化为小数点后只有一位小数
   * @param dec
   * @return
   */
  public static String formatDecimal(String dec)
  {
    if (TextUtils.isEmpty(dec))
    {
      return "";
    }
    Double d = Double.parseDouble(dec);
    return String.format("%.1f", d);
  }

  public static String getIMSI()
  {
    if (IMSI == null)
    {
      setIMSI(PhoneUtils.getIMSI());
    }
    return IMSI;
  }

  public static void setIMSI(String IMSI)
  {
    CommonUtil.IMSI = IMSI;
  }

  public static String getIMEI()
  {
    if (IMEI == null)
    {
      setIMEI(PhoneUtils.getIMEI());
    }
    return IMEI;
  }

  public static void setIMEI(String IMEI)
  {
    CommonUtil.IMEI = IMEI;
  }

  public static String getMAC()
  {
    if (MAC == null)
    {
      setMAC(PhoneUtils.getMAC());
    }
    return MAC;
  }

  public static void setMAC(String MAC)
  {
    CommonUtil.MAC = MAC;
  }

  /**
   * @return 是否有权限需要运行时申请
   */
  @TargetApi(23)
  public static boolean getPermissions(final Activity act)
  {
    Log.e("getPermission", " start to getpermission");
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
      Log.e("getPermission", "SDK check ");
      ArrayList<String> permissions = new ArrayList<>();
      /***
       * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
       */
      // 定位精确位置
      if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_FINE_LOCATION)
          != PackageManager.PERMISSION_GRANTED)
      {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
      }
      if (ActivityCompat.checkSelfPermission(act, Manifest.permission.ACCESS_COARSE_LOCATION)
          != PackageManager.PERMISSION_GRANTED)
      {
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
      }
      if (ActivityCompat.checkSelfPermission(act, Manifest.permission.READ_PHONE_STATE)
          != PackageManager.PERMISSION_GRANTED)
      {
        permissions.add(Manifest.permission.READ_PHONE_STATE);
      }
      /*
       * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
			 */
      //// 读写权限
      //if (addPermission(act, permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      //    permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
      //}
      //// 读取电话状态权限
      //if (addPermission(act, permissions, Manifest.permission.READ_PHONE_STATE)) {
      //    permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
      //}
      if (ActivityCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED)
      {
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
      }
      if (ActivityCompat.checkSelfPermission(act, Manifest.permission.WRITE_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED)
      {
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
      }

      if (permissions.size() > 0)
      {
        ActivityCompat.requestPermissions(act, permissions.toArray(new String[permissions.size()]),
            SDK_PERMISSION_REQUEST);
        Log.e("PERMISSION", "REQUEST AFTER");
        return true;
      }
    }
    return false;
  }

  @TargetApi(23)
  private static boolean addPermission(Activity act, ArrayList<String> permissionsList,
      String permission)
  {
    if (act.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
    { // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
      //if (act.shouldShowRequestPermissionRationale(permission)){
      //  //用户没有选择do not ask again
      //    return true;
      //}else{
      //  permissionsList.add(permission);
      //  return false;
      //}
      permissionsList.add(permission);
      return true;
    }
    else
    {
      return false;
    }
  }

  public static void showMessageOKCancel(Context context, String message,
      DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener exitListener)
  {
    new AlertDialog.Builder(context).setMessage(message)
        .setPositiveButton("设  置", okListener)
        .setNegativeButton("退  出", exitListener)
        .setCancelable(false)
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog)
          {
            AppExitUtil.getInstance().exit();
          }
        })
        .create()
        .show();
  }

  private static void startSettingActivity(Activity act)
  {

    Intent i = new Intent();
    i.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
    i.addCategory(Intent.CATEGORY_DEFAULT);
    i.setData(Uri.parse("package:" + act.getPackageName()));
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    try
    {
      act.startActivity(i);
    }
    catch (Exception e)
    {
      Log.e("PickUpService", "start Setting failed");
    }
  }

  public static final int SDK_PERMISSION_REQUEST = 127;
  private static String permissionInfo;
}
