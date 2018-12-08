package com.example.yuhyojeong.ewha_1ofn;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.daum.android.map.MapActivity;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

import java.io.BufferedInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements MapView.MapViewEventListener, NavigationView.OnNavigationItemSelectedListener, MapView.POIItemEventListener {

    //////////////////////////////////////////////////// 변수 ////////////////////////////////////////////////////

    private static final String TAG = "메인 액티비티";

    Toolbar toolbar;
    NavigationView navigationView;
    DrawerLayout drawer;

    MapView mapView;
    FloatingActionButton floatingBtn;
    boolean floatingStatus = true; // true if first icon is visible, false if second one is visible.

    ArrayList<MapPOIItem> markers = new ArrayList<MapPOIItem>();

    // 실시간 데이터베이스 사용하기
    private FirebaseDatabase database;

    // 로그인 되어있는 경우,
    // 로그아웃 된 경우, login acitivity로 이동.
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String name;
    String email;
    Uri photoUrl;

    private FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                Log.i(TAG, "onAuthStateChanged:signed_in: " + user.getUid());

                // 로그인한 유저의 이름, 이메일, 사진 Url을 받아오자.
                name = user.getDisplayName();
                email = user.getEmail();
                photoUrl = user.getPhotoUrl();

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have one. Use
                // FirebaseUser.getIdToken() instead.
                // String uid = user.getUid();


                // 드로어 네비게이션 헤더에
                // 유저의 이름, 이메일을 출력한다.
                View nav_header_view = navigationView.getHeaderView(0);

                TextView nav_name_tv = (TextView) nav_header_view.findViewById(R.id.nav_tv_name);
                TextView nav_email_tv = (TextView) nav_header_view.findViewById(R.id.nav_tv_email);
                ImageView nav_photo = nav_header_view.findViewById(R.id.nav_imageView);

                nav_name_tv.setText(name);
                nav_email_tv.setText(email);


//
//                try {
//                    Log.d(TAG, "onAuthStateChanged: url 이미지 받아오기");
//                    URL url = new URL(photoUrl.toString());
//                    URLConnection conn = url.openConnection();
//                    conn.connect();
//                    BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
//                    Bitmap bm = BitmapFactory.decodeStream(bis);
//                    bis.close();
//                    nav_photo.setImageBitmap(bm);
//                } catch (Exception e) {
//
//                }

                Log.d(TAG, "onAuthStateChanged: url is : " + photoUrl.toString());
                Log.d(TAG, "onAuthStateChanged: 아직 안했어!!");

            } else {
                Log.i(TAG, "onAuthStateChanged:signed_out");
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        }
    };

//////////////////////////////////////////////////// 변수 끝 ////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 권한 얻기
        // 23 버전 이상부터는 위험 권한에 대해 동적으로 권한을 할당받아야 한다
        // 지도를 사용하기 위해서는 ACCESS_FINE_LOCATION, ACCES_COARSE_LOCATION 권한이 필요하다. 
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

        // mapView 띄우기
        mapView = new MapView(this);
        mapView.setDaumMapApiKey("7f79f8a60fed7aca35c2e2638c658363");
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

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
        mapView.setPOIItemEventListener(this);


        // 툴바
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에 드로어를 여는 메뉴 버튼을 달자 - 기능 on
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu); // 툴바 왼쪽에 드로어를 여는 메뉴 버튼을 달자 - 아이콘 설정
        getSupportActionBar().setTitle("");

        // 드로어 메뉴
        navigationView.setNavigationItemSelectedListener(this);

        // mAuth 리스너 붙이기.
        mAuth.addAuthStateListener(mAuthListener);

        // 데이터베이스에서 markers 불러오기
        // 지도에 markers 찍기
        // 파이어베이스 데이터베이스 사용하기
        database = FirebaseDatabase.getInstance();

        // 마커 데이터베이스 받아와서 지도에 표시하기
        database.getReference().child("chatroom").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatRoom chatRoom = snapshot.getValue(ChatRoom.class);
                    setMarkerOnMap(chatRoom);
                    Log.d(TAG, "onDataChange: "+chatRoom.title+" 마커를 붙였다");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    // 지도를 사용하기 위해 위험 권한이 필요하다.
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
    private void setMarkerOnMap(ChatRoom chatroom) {
        double latitude = chatroom.latitude;
        double longitude = chatroom.longitude;
        String title = chatroom.title;

        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);

        MapPOIItem marker = new MapPOIItem();

        if (title != null) {
            marker.setItemName(title);
        }
        marker.setTag(markers.size());
        marker.setMapPoint(mapPoint);

        // 오!!
        // 해당 POI Item과 관련된 정보를 저장하고 있는 임의의 객체를 저장하고자 할때 사용한다.
        // 사용자가 POI Item을 선택하는 경우 등에 선택된 POI Item과 관련된 정보를 손쉽게 접근할 수 있다.
        marker.setUserObject(chatroom);
        // 그 이후엔,
        // 해당 POI Item과 관련된 정보를 저장하고 있는 임의의 객체를 조회해서 리턴한 object를 사용할 수 있다.
        // marker.getUserObject();


        // MapPOIItem.MarkerType:
        // BluePin/YellowPin/RedPin
        // 마커가 클릭되지 않을때, 마커 모양 : 기본으로 제공하는 BluePin 마커 모양.
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
        // 마커를 클릭했을때, 마커 모양 : 기본으로 제공하는 RedPin 마커 모양.
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
                setMarkerOnMap(chatroom);


                String key = database.getReference().child("chatroom").push().getKey();
                chatroom.key = key;
                // markers 데이터베이스에 ChatRoom 객체 저장하기
                database.getReference().child("chatroom/"+key).setValue(chatroom);

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
        switch (item.getItemId()) {
            // 홈버튼이 눌린 경우라면 드로어메뉴를 열자
            case android.R.id.home: {
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

        if (id == R.id.nav_logout) {
            Log.d(TAG, "onNavigationItemSelected: 로그아웃 버튼 누름");
            Toast.makeText(this, "로그아웃 하자", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        } else if (id == R.id.nav_settings) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                        .setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                startActivity(intent);
            }
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    // MapView.POIItemEventListener
    @Override
    public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

    }

    // 마커 말풍선을 클릭하면 채팅방이 뜨도록 하자
    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

        ChatRoom chatRoom = (ChatRoom) mapPOIItem.getUserObject();
        Intent intent = new Intent(MainActivity.this, MessageActivity.class);
        database.getReference().child("chatroom"+chatRoom.key).child(mAuth.getCurrentUser().getUid()).setValue(true);
        intent.putExtra("title", chatRoom.title);
        startActivity(intent);
        finish();
        Toast.makeText(this, chatRoom.title, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onCalloutBalloonOfPOIItemTouched: "+chatRoom.title);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
