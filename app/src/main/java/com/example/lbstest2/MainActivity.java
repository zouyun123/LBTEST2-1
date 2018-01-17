package com.example.lbstest2;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Manifest;
import java.util.logging.Handler;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public LocationClient mLocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate=true;
    private static final int NETUPDATE=10;
//    LayoutInflater inflater = LayoutInflater.from(this);
    //private Marker marker1;

    private class MyLatLng{
        private LatLng point;
        private String id;
        MyLatLng(double v, double v1, String id){
            point = new LatLng(v, v1);
            this.id = id;
        }
    }

    private android.os.Handler handler=new android.os.Handler(){
        public void handleMessage(Message msg){
            HashMap responseMap = parseJSONWithGSON((String) msg.obj);

            //实现了从服务器接受经纬度绘制单个点
//            LatLng point_1 = new LatLng(getLatitude("1", responseMap), getLongitude("1",
//                    responseMap));
//            drawmarker(point_1);
//            LatLng point_2 = new LatLng(getLatitude("2", responseMap), getLongitude("2",
//                    responseMap));
//            drawmarker(point_2);
////            System.out.println(point_1.latitude);
////            System.out.println(point_1.longitude);

            ArrayList<MyLatLng> myLatLngList = new ArrayList<>();
            myLatLngList.add(null);

//            for循环实现绘制多个点
            for(int i=1;i<4;i++) {
                String s = "";
                s = String.valueOf(i);
//            System.out.println(s);
                myLatLngList.add(i, new MyLatLng(getLatitude(s, responseMap), getLongitude(s,
                        responseMap), i + ":"));
//                LatLng point_i = new LatLng(getLatitude(s, responseMap), getLongitude(s,
//                        responseMap));
                drawmarker(myLatLngList.get(i),getPercent(s,responseMap));
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient=new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView=(MapView) findViewById(R.id.bmapView);
        baiduMap=mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        Button sendRequest=(Button) findViewById(R.id.send_request);
        sendRequest.setOnClickListener(this);
        //positionText=(TextView) findViewById(R.id.position_text_view);
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.
                permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.
                permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.
                permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String [] permissions=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            initLocation();
            mLocationClient.start();
        }
    }
    @Override
    public void onClick(View v){
        if(v.getId()==R.id.send_request){
            sendRequestWithOkHttp();
            //Toast.makeText(getApplicationContext(),"点击", Toast.LENGTH_SHORT).show();

        }
    }

    private void sendRequestWithOkHttp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url("http://192.168.1.21:8000/index/")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
//                    HashMap responseMap = parseJSONWithGSON(responseData);
//                    for (int i = 1; i < 10000; i++) {
//                        LatLng point_i = new LatLng(getLatitude("i", responseMap), getLongitude("i",
//                                responseMap));
//                        System.out.println(getLatitude("i", responseMap));
//                        drawmarker(point_i);
//                    }
                    Message tempMsg = handler.obtainMessage();
                    tempMsg.what = NETUPDATE;
                    tempMsg.obj=responseData;
                    handler.sendMessage(tempMsg);
//                    System.out.println(tempMsg.obj);

                } catch (Exception e) {
                    //System.out.println("lyichang:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void drawmarker(final MyLatLng myPoint, final double percent) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng point = myPoint.point;
                String id = myPoint.id;
//                View view=inflater.inflate(R.layout.layout1,null);
//                TextView textView = new TextView(getBaseContext());
//                textView.setText("自定义的覆盖物");
//                textView.setTextSize(16);
                if(percent<40&&percent>=0){
//                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(view);
                BitmapDescriptor mCurrentMarker=BitmapDescriptorFactory
                        .fromResource(R.drawable.green);
                MarkerOptions option = new MarkerOptions()
                        .position(point)
                        .icon(mCurrentMarker);
                option.title(id);
                baiduMap.addOverlay(option);
                }
                else if(percent<=70&&percent>=40){
                    BitmapDescriptor mCurrentMarker=BitmapDescriptorFactory
                            .fromResource(R.drawable.blue);
                    MarkerOptions option = new MarkerOptions()
                            .position(point)
                            .icon(mCurrentMarker);
                    option.title(id);
                    baiduMap.addOverlay(option);
                }
                else if(percent<=100&&percent>70){
                    BitmapDescriptor mCurrentMarker=BitmapDescriptorFactory
                            .fromResource(R.drawable.red);
                    MarkerOptions option = new MarkerOptions()
                            .position(point)
                            .icon(mCurrentMarker);
                    option.title(id);
                    baiduMap.addOverlay(option);
                }
            }
        });
    }

    /*
    private void parseJSONWithJSONObject(String jsonData){
        try {
            JSONArray jsonArray=new JSONArray(jsonData);
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject=jsonArray.getJSONObject(i);
                String latitude=jsonObject.getString("latitude");
                String longitude=jsonObject.getString("longitude");
                Log.d("MainActivity",longitude);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    */
    /*
    private void requestLocation(){
        mLocationClient.start();
        initLocation();

    }
    */
    private void initLocation(){
        LocationClientOption option=new LocationClientOption();
        option.setScanSpan(100);
        //option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);
    }
    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }
    private HashMap<String, String> parseJSONWithGSON(String jsonData) {
        Gson gson = new Gson();
        HashMap<String, String> hashMap = new HashMap<>();
//        List<Page> pageList = gson.fromJson(jsonData, new TypeToken<List<Page>>() {}.getType());

//        System.out.println("2----------------------------------------");
//        System.out.println(jsonData);
        hashMap = gson.fromJson(jsonData, hashMap.getClass());
//
        System.out.println("哈希表 : " + hashMap);
        System.out.println("哈希keyset : " + hashMap.keySet());
        System.out.println("哈希values : " + hashMap.values());
//
//        System.out.println("2----------------------------------------");
            /*for(Page page:pageList){
                Log.d("MainActivity","id is " + page.getId());
                Log.d("MainActivity","longitude is " + page.getLongitude());
                Log.d("MainActivity","latitude is " + page.getLaitude());
//                System.out.println("");
                System.out.println("MainActivity ：longitude is " + page.getLongitude());

            }*/
        return hashMap;
    }

//获取经纬度的方法
    double getLatitude(String id, HashMap responseMap){
        Map cmap = (LinkedTreeMap)responseMap.get(id);
        return Double.parseDouble(cmap.get("latitude") + "");
    }
    double getLongitude(String id, HashMap responseMap){
        Map cmap = (LinkedTreeMap)responseMap.get(id);
        return Double.parseDouble(cmap.get("longitude") + "");
    }
    double getPercent(String id, HashMap responseMap){
        Map cmap = (LinkedTreeMap)responseMap.get(id);
        return Double.parseDouble(cmap.get("percent") + "");
    }

    //设置标注点id的方法
    int setMarkerid(int i){
        int id;
        id=i;
        return id;
    }


    private void navigateTo(BDLocation location){
        if(isFirstLocate) {
            LatLng mylocation = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(mylocation);
            baiduMap.setMapStatus(mapStatusUpdate);
            mapStatusUpdate= MapStatusUpdateFactory.zoomTo(12);
            isFirstLocate = false;
        }



        // 定义Maker坐标点
//
//       LatLng point1 = new LatLng(30.190, 103.500);
//
//       LatLng point2 =new LatLng(30.500,103.620);
//       LatLng point3=new LatLng(29.860,103.291);
//       //构建Marker图标
//       BitmapDescriptor mCurrentMarker1 = BitmapDescriptorFactory
//               .fromResource(R.drawable.red);
//       BitmapDescriptor mCurrentMarker2=BitmapDescriptorFactory
//               .fromResource(R.drawable.blue);
//
//       BitmapDescriptor mCurrentMarker3=BitmapDescriptorFactory
//               .fromResource(R.drawable.green);
//       //构建MarkerOption，用于在地图上添加Marker
//       MarkerOptions option1 = new MarkerOptions()
//               .position(point1)
//               .icon(mCurrentMarker1);
//       option1.title("option1");
//       MarkerOptions option2 = new MarkerOptions()
//               .position(point2)
//               .icon(mCurrentMarker2);
//       option2.title("option2");
//       MarkerOptions option3 = new MarkerOptions()
//               .position(point3)
//               .icon(mCurrentMarker3);
//       option3.title("option3");
//       //在地图上添加Marker，并显示
//        baiduMap.addOverlay(option1);
//        baiduMap.addOverlay(option2);
//        baiduMap.addOverlay(option3);
//


        baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {


                /*
                if(marker.getTitle()=="option1"){
                    Toast.makeText(getApplicationContext(), "图标1", Toast.LENGTH_SHORT).show();
                    return false;
                }
                Toast.makeText(getApplicationContext(), "触发", Toast.LENGTH_SHORT).show();
                */
                Intent intent=new Intent(MainActivity.this,SecondActivity.class);
                intent.putExtra("id1",marker.getTitle());
                startActivity(intent);
                return false;
            }
        });
        /*
        MyLocationData.Builder locationBuilder=new MyLocationData.Builder();
        locationBuilder.latitude(26.915);
        locationBuilder.longitude(106.404);
        MyLocationData locationData=locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
        */

       /*
        MyLocationData.Builder locationBuilder1=new MyLocationData.Builder();
        locationBuilder1.latitude(26.000);
        locationBuilder1.longitude(106.000);
        MyLocationData locationData1=locationBuilder1.build();
        baiduMap.setMyLocationData(locationData1);*/

    }
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){
           if(location.getLocType()==BDLocation.TypeGpsLocation||location.getLocType()
                   ==BDLocation.TypeNetWorkLocation){
               navigateTo(location);
           }
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

}
