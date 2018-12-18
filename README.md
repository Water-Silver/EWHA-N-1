<h1>"EWHA-N-1"</h1>
<h2>N분의 1</h2>
혼밥러들이 배달 음식을 먹는 방법!
배달비와 최소주문 금액이 부담되는 혼밥러들이 모여 
음식을 같이 주문하고 나누어 먹을 수 있도록 매칭-채팅 서비스를 제공하는 앱입니다.

<h2>1. 설치 방법 및 사용법</h2>
디바이스를 연결하여 앱을 실행 후 구글 아이디를 이용하여 로그인 하세요.
지도에 표시된 채팅방에 들어가 해당 음식점에서 먹고 싶은 메뉴를 정하거나,
먹고 싶은 음식이 없을 시 새로 채팅방을 만들 수 있습니다.
<h2>2. 주요 기능 코드/API 설명<h2>
  <h3>2.1 가격을 1/n으로 나눠주는 기능 <h3>
    <h4>2.1.1 총 가격 입력받기<h4>
      --총가격 이미지 삽입---
     
     ```java
      @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialog_setting_cancel:
                dialogListener.onNegativeClicked();
                break;

            case R.id.dialog_setting_ok:
                SettingModel setting = new SettingModel(
                    menu.getText().toString().trim(),
                    Integer.parseInt(price.getText().toString().trim()),
                    place.getText().toString().trim(),                            
                    hour+"시 "+minute+"분"
                );
                dialogListener.onPositiveClicked(setting);
                break;
        }
       }
      ```
    
 MessageSettingDialog.java의 onClick 함수에서에서 구현합니다. 사용자가 채팅방 정보를 입력하고 설정 을 누르면 (dialog_setting_ok 인 경우), SettingModel의 객체를 생성합니다. SettingModel.java는 채팅방의 정보를 저장하는 클래스입니다. 
       SettingModel 의 생성자는 
       
     ```java
       public SettingModel(String menu, int price, String place, String time)
     ```   
       
 입니다. price.getText()으로 사용자가 입력한 가격을 받아오고, 생성자를 이용해서 이를 SettingModel.java의 price 필드에 저장합니다.
      
  

<h2>개발자 정보</h2>
<ul>
<li>
유효정(hjyu94): 
  <ul><li>지도 관련 기능 구현</li>
    <li>메인 화면에 지도 띄우기</li>
    <li>지도 클릭해서 채팅방 만들기</li>
    <li>채팅방 만든 후 마커 표시하기</li>
    <li>채팅방의 인원수 정보에 따라 클릭 가능한 마커인지 구분하여 지도에 마커 찍기</li>
    <li>각 메뉴별로 마커 모아보기</li>
    <li>채팅방 설정 기능 - 설정 버튼 클릭 후 메뉴, 장소, 시간 정하기</li>
    <li>채팅방 나가기 기능</li>
    <li>GPS를 이용하여 현재 위치 표시하기</li>
    <li>로그아웃 기능</li></ul>
</li>
<li>
  윤하은(Water-Silver): <ul><li>파이어베이스 - 구글 로그인, 채팅방 데이터베이스 연동</li>
  <li>구글 아이디를 이용하여 사용자를 식별하는 기능</li>
  <li>지도의 위치 정보를 받아와 채팅방 생성시 채팅방 이름과 위치 등의 정보를 저장 및 불러오기</li></ul>
</li>
<li>
  서현주(seohsj): <ul><li>데이터베이스</li>
  <li>메뉴판 데이터베이스 만들기</li></ul>
</li>
<li>
  최윤영(ILoveSpongebob) : <ul><li>채팅방안의 데이터베이스 </li>
  <li>채팅방 들어가면 메뉴창에 음식점 버튼 구현</li></ul>
</li>
</ul>
<h2>라이센스 정보</h2>
LGPL
