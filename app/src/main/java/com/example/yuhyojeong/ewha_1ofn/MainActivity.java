package com.example.yuhyojeong.ewha_1ofn;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapView;

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
    private DatabaseReference database;
    private DatabaseReference mChatroomRef;
    private ValueEventListener mChatroomListener;

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

        // 처음에 서울역을 보여준다.
        mapView.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(37.553811, 126.969630), 9, true);

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

        // 드로어 메뉴
        navigationView.setNavigationItemSelectedListener(this);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // mAuth 리스너 붙이기.
            mAuth.addAuthStateListener(mAuthListener);
            Log.d(TAG, "onCreate: 리스너를 붙였다");
        } else {
            Log.d(TAG, "onCreate: currentuser가 null 상태.....");
        }

        // 파이어베이스의 실시간 데이터베이스를 사용한다.
        database = FirebaseDatabase.getInstance().getReference();

        // 처음 Listener를 데이터베이스에 붙이는 경우
        // chatroom 하위 항목들이 변경되는 경우
        // oonDataChange() 가 호출될 것
        mChatroomRef = database.child("chatroom");
        mChatroomListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // New data at this path. This method will be called after every change in the
                // data at this path or a subpath.ㅁ

                // 지도에 있는 모든 마커를 지운다
                mapView.removeAllPOIItems();

                // 마커 ArrayList를 새로 생성해서 데이터베이스의 데이터들을 추가시킨다.
                markers = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = child.getValue(ChatRoom.class);

                    Log.d(TAG, "onDataChange: " + chatRoom.title + " 마커를 붙였다");
                    if (chatRoom.users.size() >= chatRoom.targetPop) {
                        chatRoom.status = "full";

                        Log.d(TAG, "onDataChange: 파이어베이스의 status를 full 로 변경한다");
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put("status", "full");
                        mChatroomRef.child(chatRoom.key).updateChildren(childUpdates);

                    } else {
                        chatRoom.status = "yet";
                    }

                    setMarkerOnMap(chatRoom);
                }
                Log.d(TAG, "onDataChange: 파이어베이스에서 마커를 불러온 뒤 " + markers.size() + "개의 마커를 붙였다!!");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Could not successfully listen for data, log the error
                Log.e(TAG, "messages:onCancelled:" + error.getMessage());
            }
        };

        mChatroomRef.addValueEventListener(mChatroomListener);
    } // end of onCreate'


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(mAuthListener);
        Log.d(TAG, "onDestroy: mAuthListener를 뗐다. mAuth에 여러개의 동일한 리스너가 중복되는것을 방지");
    }

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
    private void moveCameraToThePosition(double latitude, double longitude, int zoom) {
        MapPoint mapPoint = MapPoint.mapPointWithGeoCoord(latitude, longitude);

        // zoom 변수까지 설정한 경우라면 zoom 설정도 바꾼다.
        if (zoom != 0) mapView.setMapCenterPointAndZoomLevel(mapPoint, zoom, true);
        else mapView.setMapCenterPoint(mapPoint, true);

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

        // 해당 POI Item과 관련된 정보를 저장하고 있는 임의의 객체를 저장하고자 할때 사용한다.
        // 사용자가 POI Item을 선택하는 경우 등에 선택된 POI Item과 관련된 정보를 손쉽게 접근할 수 있다.
        marker.setUserObject(chatroom);
        // 그 이후엔,
        // 해당 POI Item과 관련된 정보를 저장하고 있는 임의의 객체를 조회해서 리턴한 object를 사용할 수 있다.
        // marker.getUserObject();


        // MapPOIItem.MarkerType:
        // BluePin/YellowPin/RedPin

        // 마커가 클릭되지 않을때,
        // 들어갈 수 있는 채팅방이라면, 선택 전에 BluePin 마커, 선택 후 RedPin 마커
        // 사람이 꽉찬 채팅방이면, 클릭 여부와 상관 없이 YellowPin 마커로 표시
        Log.d(TAG, "setMarkerOnMap: " + chatroom.title + "의 status: " + chatroom.status);

        if (chatroom.status.equals("yet")) {
            marker.setMarkerType(MapPOIItem.MarkerType.BluePin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);
        } else if (chatroom.status.equals("full")) {
            marker.setMarkerType(MapPOIItem.MarkerType.YellowPin);
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.YellowPin);
        }

        // markers 배열에 추가하고 log로 출력해본다
        markers.add(marker);
        Log.d(TAG, "setMarkerOnMap: 현재 " + markers.size() + "개의 마커가 있다");

        // 지도에 마커를 붙인다.
        mapView.addPOIItem(marker);
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
        Log.d(TAG, "onMapViewSingleTapped: 지도 클릭");

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
                android.util.Log.d(TAG, "onPositiveClicked: 실행");
                final ChatRoom chatroom = (ChatRoom) object;
                chatroom.latitude = mapPointGeo.latitude;
                chatroom.longitude = mapPointGeo.longitude;
                chatroom.status = "yet";
                setMarkerOnMap(chatroom);
                // 카메라를 마커 위치로 이동한다.
                moveCameraToThePosition(mapPointGeo.latitude, mapPointGeo.longitude, 9);

                String key = database.child("chatrooom").push().getKey();
                chatroom.key = key;
                Map<String, Object> chatroomVal = chatroom.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(key, chatroomVal);
                database.child("chatroom").updateChildren(childUpdates)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: 데이터 입력 완료");
                            }
                        });
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
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        String str = String.format("MapView onMapViewDragEnded (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude);
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        String str = String.format("MapView onMapViewMoveFinished (%f,%f)", mapPointGeo.latitude, mapPointGeo.longitude);
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int zoomLevel) {
        String str = String.format("MapView onMapViewZoomLevelChanged (%d)", zoomLevel);
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
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
            case R.id.action_menu_all: {
                String menu = "모든 메뉴";
                filter_menu(menu);
                return true;
            }
            case R.id.action_menu_chicken: {
                String menu = "치킨";
                filter_menu(menu);
                return true;
            }
            case R.id.action_menu_pizza: {
                String menu = "피자";
                filter_menu(menu);
                return true;
            }
            case R.id.action_menu_chinease: {
                String menu = "중국집";
                filter_menu(menu);
                return true;
            }
            case R.id.action_menu_bento: {
                String menu = "도시락";
                filter_menu(menu);
                return true;
            }
            case R.id.action_menu_fastfood: {
                String menu = "패스트푸드";
                filter_menu(menu);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void filter_menu(final String menu) {
        Toast.makeText(this, menu + " 모아보기", Toast.LENGTH_SHORT).show();
        mChatroomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapView.removeAllPOIItems();
                markers = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    ChatRoom chatRoom = child.getValue(ChatRoom.class);

                    if (menu.equals("모든 메뉴")) {
                        setMarkerOnMap(chatRoom);
                    } else if (chatRoom.menu.equals(menu)) {
                        Log.d(TAG, "onDataChange: " + chatRoom.title + " 마커를 붙였다");
                        setMarkerOnMap(chatRoom);
                    }

                    Log.d(TAG, "onDataChange: 파이어베이스에서 마커를 불러온 뒤 " + markers.size() + "개의 마커를 붙였다!!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }


    ///////////////////////// 드로어 레이아웃의 메뉴 클릭 이벤트 처리 ///////////////////////////

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            Log.d(TAG, "onNavigationItemSelected: 로그아웃 버튼 누름");

            // 구글로 로그인한 경우라면 구글 아이디 로그아웃
            if (AuthUI.getInstance() != null) {
                Log.d(TAG, "onNavigationItemSelected: 구글로그인한상태");
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                                // ...
                            }
                        });
            } else {
                Log.d(TAG, "onNavigationItemSelected: 구글로그인아닌상태");
                // 로그아웃
                mAuth.signOut();
            }
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
        } else if (id == R.id.nav_about) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("EWHA-N-1");
            builder.setMessage("혼밥러들이 배달 음식을 먹는 방법!\n\n" +
                    "배달비와 최소주문 금액이 부담되는 혼밥러들이 모여" +
                    "음식을 같이 주문하고 나누어 먹을 수 있도록 매칭-채팅 서비스를 제공하는 앱입니다.");
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
            builder.show();

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
        // 마커의 chatroom 정보 얻어오기
        ChatRoom chatRoom = (ChatRoom) mapPOIItem.getUserObject();
        if (!chatRoom.status.equals("full")) {
            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
            database.child("chatroom").child(chatRoom.key).child("users").child(mAuth.getCurrentUser().getUid()).setValue(true);
            intent.putExtra("title", chatRoom.title);
            intent.putExtra("key", chatRoom.key);

            // MainActivity 위에 MessageActivity를 띄우자
            // 뒤로가기를 하더라도 밑에 깔려있던 메인 액티비티가 띄워진다
            startActivity(intent);
        }
        Log.d(TAG, "onCalloutBalloonOfPOIItemTouched: " + chatRoom.title);
    }

    @Override
    public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {
    }

    @Override
    public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

    }
}
