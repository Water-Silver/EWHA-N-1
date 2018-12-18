package com.example.yuhyojeong.ewha_1ofn;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements MapView.MapViewEventListener, NavigationView.OnNavigationItemSelectedListener{

    //////////////////////////////////////////////////// 변수 ////////////////////////////////////////////////////

    private static final String TAG = "메인 액티비티";

    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawer;

    MapView mapView;
    FloatingActionButton floatingBtn;
    boolean floatingStatus = true; // true if first icon is visible, false if second one is visible.

    ArrayList<MapPOIItem> markers = new ArrayList<MapPOIItem>();

    //////////////////////////////////////////////////// 변수 끝 ////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 얻기
        // 23 버전 이상부터는 위험 권한에 대해 동적으로 권한을 할당받아야 한다
        if (Build.VERSION.SDK_INT >= 23) {
            // 권한을 모두 갖고 있는 경우
            if (PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    && PermissionUtil.checkPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {

            }
            // 갖고 있지 않은 권한이 있다면 요청해야 한다.
            // 권한을 요청 후 onRequestPermissionsResult 메소드를 콜백한다
            else {
                PermissionUtil.requestLocationPermissions(this);
            }
        }

        // Find View By Id
        floatingBtn = findViewById(R.id.main_floatingBtn);
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        navigationView = (NavigationView) findViewById(R.id.main_nav_view);
        drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        // 툴바
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에 드로어를 여는 메뉴 버튼을 달자
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setTitle("");

        navigationView.setNavigationItemSelectedListener(this);

        // mapView 띄우기
        mapView = new MapView(this);
        mapView.setDaumMapApiKey("7f79f8a60fed7aca35c2e2638c658363");
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        // 데이터베이스에서 markers 불러오기
        // 지도에 markers 찍기

        // floatingBtn을 클릭하는 경우 이벤트 처리
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (floatingStatus) { // GPS가 꺼져있는 경우, 킨다
                    Toast.makeText(MainActivity.this, "GPS ON", Toast.LENGTH_SHORT).show();
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);
                    floatingStatus = false;
                    floatingBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gps_off));
                } else if (!floatingStatus) { // GPS가 켜져있는 경우, 끈다
                    Toast.makeText(MainActivity.this, "GPS OFF", Toast.LENGTH_SHORT).show();
                    mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
                    mapView.setShowCurrentLocationMarker(false);
                    floatingStatus = true;
                    floatingBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_gps_on));
                }
            }
        });

        // mapView에 이벤트 리스너 붙이기
        mapView.setMapViewEventListener(this);

    } // end of onCreate

    // 권한을 요청 한 이후 콜백되는 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.REQUEST_LOCATION) {
            if (PermissionUtil.verifyPermission(grantResults)) {
                // 원하는 권한을 모두 갖고 있는 경우
            } else {
                // 권한요청을 승낙하지 않은 경우
                showRequestAgainDialog();
            }
        } else {

        }
    }

    // 권한요청을 승낙하지 않은 경우 설정에 들어가서 권한을 달라고 요청하는 다이얼로그를 띄운다
    private void showRequestAgainDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("이 권한은 꼭 필요한 권한이므로, 설정에서 활성화 부탁드립니다.");
        builder.setMessage("EWHA_1ofn > 권한 > 위치 on");
        // 버튼: 설정(디바이스의 애플리케이션 설정창을 띄운다), 취소
        builder.setPositiveButton("설정",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                                    .setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                            startActivity(intent);
                        }
                    }
                });

        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        builder.show();
    }


    // 위경도를 받아서 해당 위치로 카메라를 옮긴다
    private void moveCameraToThePosition(double latitude, double longitude) {
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);
        mapView.setMapCenterPoint(mapPoint, true);
    }

    // 위경도를 받아서 해당 위치에 마커를 표시한다.
    private void setMarkerOnMap(double latitude, double longitude, String title) {
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);

        MapPOIItem marker = new MapPOIItem();

        if (title != null) {
            marker.setItemName(title);
        }
        marker.setTag(markers.size());
        marker.setMapPoint(mapPoint);

        // 기본으로 제공하는 BluePin 마커 모양.
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        // markers 배열에 추가하고 log로 출력해본다
        markers.add(marker);
        Log.d(TAG, "setMarkerOnMap: " + markers.size());

        // 지도에 마커를 붙인다.
        mapView.addPOIItem(marker);

        // 카메라를 마커 위치로 이동한다.
        moveCameraToThePosition(latitude, longitude);
    }

    // 뒤로가기 버튼이 눌리는 경우
    // 혹시 드로어 레이아웃이 열려있으면 드로어를 닫는다
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /////////////////////// 지도 클릭 이벤트 처리 핸들러 //////////////////////////////////

    // @@@@ 탭이 너무 쉽게 눌려서 실수로 다이얼로그가 뜨게되면 닫아야 해서 귀찮음 @@@@
    // @@@@ 지도 롱 클릭이나, 더블 클릭하는 경우에 다이얼로그가 뜨게 되면 좋을것 같음 @@@@
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {
        // 지도를 한번 탭 했을때 찍히는 좌표
        final MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();

        // 방을 만드는 다이얼로그를 띄운다
        // 방 제목, 인원수, 메뉴명을 입력받고
        // 생성 버튼을 누른 경우라면
        // 그 값들과 mapPointGeo의 좌표를 나타내는
        // ChatRoom 객체를 생성한다.
        // 취소 버튼을 누르면 다이얼로그를 닫는다
        final CreateRoomDialog dialog = new CreateRoomDialog(this);
        dialog.setDialogListener(new DialogListener() {
            @Override
            public void onPositiveClicked(Object object) {
                ChatRoom chatroom = (ChatRoom) object;
                chatroom.latitude = mapPointGeo.latitude;
                chatroom.longitude = mapPointGeo.longitude;
                Toast.makeText(MainActivity.this, chatroom.latitude + "," + chatroom.longitude + " / " + chatroom.menu + " ...", Toast.LENGTH_SHORT).show();
                setMarkerOnMap(chatroom.latitude, chatroom.longitude, chatroom.title);
            }

            @Override
            public void onNegativeClicked() {
                dialog.cancel();
            }
        });

        dialog.show();
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        String str = String.format("MapView onMapViewDragStarted (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        String str = String.format("MapView onMapViewDragEnded (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        String str = String.format("MapView onMapViewMoveFinished (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
        String str = String.format("MapView onMapViewZoomLevelChanged (%d)", zoomLevel);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }


    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

    }

    //////////////////////// 툴바에 있는 홈 버튼이 눌리는 경우 이벤트 처리 ///////////////////////
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            // 홈버튼이 눌린 경우라면 드로어메뉴를 열자
            case android.R.id.home:
            {
                drawer.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    ///////////////////////// 드로어 레이아웃의 메뉴 클릭 이벤트 처리 ///////////////////////////

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

//
//    // 중심점 변경
//    // true면 앱 실행 시 애니메이션 효과가 나오고 false면 애니메이션이 나오지않음.
//    MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(37.5514579595, 126.951949155);
//        mapView.setMapCenterPoint(mapPoint,true);
//
//                // 줌 레벨 변경
//                // mapView.setZoomLevel(7, true);
//
//                // 중심점 변경 + 줌 레벨 변경
//                // mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(33.41, 126.52), 9, true);
//
//                // 줌 인
//                // mapView.zoomIn(true);
//
//                // 줌 아웃
//                // mapView.zoomOut(true);
//
//                // 기본 마커
//                MapPOIItem marker=new MapPOIItem();
//                marker.setItemName("한세사이버보안고등학교");
//                marker.setTag(0);
//                marker.setMapPoint(mapPoint);
//
//                // 기본으로 제공하는 BluePin 마커 모양.
//                marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
//                // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
//                marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
//
//                // 지도에 마커 붙이기
//                mapView.addPOIItem(marker);
//                }
//
//                }