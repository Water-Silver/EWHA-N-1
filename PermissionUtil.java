package com.example.yuhyojeong.ewha_1ofn;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

public class PermissionUtil {

    // 유저가 권한을 갖고 있으면 true를 반환하고, 아닌 경우는 false를 반환한다.
    public static boolean checkPermissions(Activity activity, String permission){
        int permissionResult = ActivityCompat.checkSelfPermission(activity, permission);
        // 유저가 권한을 갖고 있는 경우. PackageManager.PERMISSION_GRANTED 가 반환된다
        if(permissionResult == PackageManager.PERMISSION_GRANTED) return true;
        else return false;
    }

    public static final int REQUEST_LOCATION = 1;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // PERMISSIONS_LOCATION 의 권한들을 요청한다
    // 그 이후, onRequestPermissionsResult 메소드를 콜백한다
    public static void requestLocationPermissions(Activity activity){
        ActivityCompat.requestPermissions(activity, PERMISSIONS_LOCATION, REQUEST_LOCATION);
    }

    // 파라매터로 받아야 할 권한 배열을 넘겨주고, 권한을 모두 갖고 있으면 true를 반환한다.
    public static boolean verifyPermission(int[] grantResults) {
        if(grantResults.length<1){
            return false;
        }
        for(int result : grantResults){
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        // 앞의 체크들에 걸리지 않고 모두 통과한 상태라면 true를 반환한다
        return true;
    }
}
