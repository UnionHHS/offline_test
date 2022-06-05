package com.example.offline_test;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class Background extends Service {
    public String TAG = "Background status";
    public String addresss;
    public GpsTracker gpsTracker;
    public String net_add = "http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone=";
    public double latitude = 0.0;
    public double longitude = 0.0;

    @Override
    public void onCreate() {
        Log.i("Service_status","Service Created");
        gpsTracker = new GpsTracker(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 여기다가 GPS처리 그리고 Gocode 처리
        // GPS와 Geocode 를 구동한 서비스(스래드이용)을 정리하고 XML,JSON 처리를 할것인지 아니면 스래드 한번 동작할때마다 할것인지 판단.
        // 돌릴때 필요한조건
        // 조건2 : 서비스 형식으로 어플이 꺼져있어도 시스템 리소스를 활용하여 N초(분) 마다 반복하여 GPS와 XML, JSON 데이터 처리 방법
        // 추가조건 : 첫실행시에는 인스턴트를 실행후 백서비스를 구동하지만 백서비스 구동후는 인스턴트 데이터만 처리.
        // -- 2020-06-14--
        // 조건문을 통해 address 변수데이터가 null 인경우는 데이터 처리를 하지 아니하고 특정 지역 데이터를 불러오는것으로 함
        // xml데이터를 같이 처리 하되 특정 지역 데이터를 기준으로 작성하는 파싱도 있어야함.
        // 인턴트를 이용하여 데이터를 처리한 값을 메인함수에 던져주고 새로고침이나 특정 액티비티를 갔다 왔을경우 변하게 하기
        // 실행했을때 포함
        // -- 2020-06-28--
        // GPS,Geocoder 구현완료
        // -- 2020-07-07--
        //
        Log.i("Service_status","Service Command Started");

        Timer tasktime = new Timer();

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                String regioncode;
                Intent itomain = new Intent(getApplicationContext(),MainActivity.class);
                Log.i("Timer_status","Timer Command Started");
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();
                addresss = getCurrentAddress(latitude,longitude);
//                intent.putExtra("address",address);

                Log.d("gps_status_lati",Double.toString(latitude));
                Log.d("gps_status_long",Double.toString(longitude));
                Log.d("gps_status_add",addresss);
                Log.i("Flag_status","Flag Command Start");


                Log.i("XML status", "start");

                GetXMLTask xmltask = new GetXMLTask();
                regioncode = "4514061000";
                xmltask.execute("http://www.kma.go.kr/wid/queryDFSRSS.jsp?zone="+regioncode);


//                for (int i = 0 ; i < tagnames.length; i++) {
//                    parser(tagnames[i]);
//                }
            }
        };
        tasktime.schedule(task,0,(30*1000));


        return super.onStartCommand(intent, flags, startId);
    }

    Document doc = null;
    String[] s = new String[15];

    private class GetXMLTask extends AsyncTask<String, Void, Document> {

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: "+getStatus());
            super.onCancelled();
        }

        @Override
        protected Document doInBackground(String... urls) {
            URL url;
            try {
                Log.i("GetXMLTask status", "doInBackground: start");
                Log.d(TAG, "doInBackground: "+urls[0]);
                url = new URL(urls[0]);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

            } catch (Exception e) {

                Log.e(TAG, "doInBackground: Failed get Xml from Url '" + e.getMessage()+"'");
            }

            return doc;
        }


        @Override
        protected void onPostExecute(Document doc) {

            String[] values = {"hour", "day", "temp", "tmx", "tmn", "sky", "pty", "wfKor", "wfEn", "pop", "r12", "s12", "ws", "wd", "wdKor", "wdEn", "reh", "r06", "s06"};
//            Log.i(TAG, "onPostExecute: Start");

//            Log.d(TAG, "onPostExecute: for line 3 step before");

//            Log.d(TAG, "onPostExecute: for line 2 step before");
            //data태그가 있는 노드를 찾아서 리스트 형태로 만들어서 반환
            NodeList nodeList = doc.getElementsByTagName("body");
//            Log.d(TAG, "onPostExecute: for line last step before");
            //data 태그를 가지는 노드를 찾음, 계층적인 노드 구조를 반환
            String filename = "weather";
            String ExtendStr = ".dat";
            for (int i = 1; i <= 15; i++) {
                String t = "";
                for (int j = 0; j < values.length; j++) {
                    for (int k = 0; k < nodeList.getLength(); k++) {

                        Node node = nodeList.item(k); //data 엘리먼트 노드
                        Element fstElmnt = (Element) node;

                        NodeList list = fstElmnt.getElementsByTagName(values[j]);

                        t += list.item(i).getChildNodes().item(0).getNodeValue() + ",";
                        s[i-1] = t;
//                        hour.add(list.item(i).getChildNodes().item(0).getNodeValue());
                    }
                }

                try{
                    FileOutputStream of = openFileOutput(filename+i+ExtendStr, Context.MODE_PRIVATE);
                    Log.i(TAG, "onPostExecute: file write ready");
                    of.write(t.getBytes());
                    Log.i(TAG, "onPostExecute: file writing");
                    of.close();
                    Log.i(TAG, "onPostExecute: file writed");
                } catch (Exception e){
                    Log.d(TAG, "onPostExecute: file write error");
                    e.printStackTrace();
                }
                Log.d(TAG, "onPostExecute: "+s[i-1]);
            }

//                isCancelled();
//                this.cancel(true);
            Log.d(TAG, "doInBackground: "+doc);



            super.onPostExecute(doc);
        }
    }

    public String getCurrentAddress(double latitude, double longitude) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    7);
        } catch (IOException ioException) {
            //네트워크 문제
//                Toast.makeText(mContext, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
//                Toast.makeText(mContext, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }

        if (addresses == null || addresses.size() == 0) {
//                Toast.makeText(mContext, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        }

        Address address = addresses.get(0);
        return address.getAddressLine(0).toString()+"\n";

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }
}
