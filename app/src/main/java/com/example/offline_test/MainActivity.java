package com.example.offline_test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.net.Proxy;
import java.util.Timer;
import java.util.TimerTask;

// gps 퍼미션 체크 하는거 백그라운드에 옮겨서 실행할지는 나중에 동작시켜서 확인.


public class MainActivity extends AppCompatActivity
{
    private GpsTracker gpsTracker;
    private Background backbone;
    static String address;
    static final int GET_STRING = 1;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    static String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private int permsRequestCode;
    private String[] permissions;
    private int[] grandResults;
    public Intent it;
    public String[] weatherlist = new String[15];
    public String TAG = "Main status";
    String filename = "weather";
    String ExtendStr = ".dat";

    ImageView[] icon = new ImageView[15];
    TextView[] today = new TextView[15];
    TextView[] ctd = new TextView[15];
    TextView[] temp = new TextView[15];
    TextView[] reh = new TextView[15];
    TextView[] pop = new TextView[15];
    TextView[] sr06 = new TextView[15];
    TextView[] tmx = new TextView[15];
    TextView[] tmn = new TextView[15];

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 절전모드 로 넘어가는것을 방지하기 위함
        setTitle("날씨 (기준 : 전북 익산시 모현동)");
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        boolean isWhiteListing = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isWhiteListing = pm.isIgnoringBatteryOptimizations(getApplicationContext().getPackageName());
        }
        if (!isWhiteListing) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
            startActivity(intent);
        }

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        // 로케이션 서비스 상황 확인.
        if (!checkLocationServicesStatus()) {

//            showDialogForLocationServiceSetting();
        }
        else {

            checkRunTimePermission();
        }
        it = new Intent(getApplicationContext(), Background.class);
        startService(it);

        icon[0] = (ImageView) findViewById(R.id.weather_icon0);
        icon[1] = (ImageView) findViewById(R.id.weather_icon1);
        icon[2] = (ImageView) findViewById(R.id.weather_icon2);
        icon[3] = (ImageView) findViewById(R.id.weather_icon3);
        icon[4] = (ImageView) findViewById(R.id.weather_icon4);
        icon[5] = (ImageView) findViewById(R.id.weather_icon5);
        icon[6] = (ImageView) findViewById(R.id.weather_icon6);
        icon[7] = (ImageView) findViewById(R.id.weather_icon7);
        icon[8] = (ImageView) findViewById(R.id.weather_icon8);
        icon[9] = (ImageView) findViewById(R.id.weather_icon9);
        icon[10] = (ImageView) findViewById(R.id.weather_icon10);
        icon[11] = (ImageView) findViewById(R.id.weather_icon11);
        icon[12] = (ImageView) findViewById(R.id.weather_icon12);
        icon[13] = (ImageView) findViewById(R.id.weather_icon13);
        icon[14] = (ImageView) findViewById(R.id.weather_icon14);

        today[0] = (TextView) findViewById(R.id.today_text0);
        today[1] = (TextView) findViewById(R.id.today_text1);
        today[2] = (TextView) findViewById(R.id.today_text2);
        today[3] = (TextView) findViewById(R.id.today_text3);
        today[4] = (TextView) findViewById(R.id.today_text4);
        today[5] = (TextView) findViewById(R.id.today_text5);
        today[6] = (TextView) findViewById(R.id.today_text6);
        today[7] = (TextView) findViewById(R.id.today_text7);
        today[8] = (TextView) findViewById(R.id.today_text8);
        today[9] = (TextView) findViewById(R.id.today_text9);
        today[10] = (TextView) findViewById(R.id.today_text10);
        today[11] = (TextView) findViewById(R.id.today_text11);
        today[12] = (TextView) findViewById(R.id.today_text12);
        today[13] = (TextView) findViewById(R.id.today_text13);
        today[14] = (TextView) findViewById(R.id.today_text14);

        ctd[0] = (TextView) findViewById(R.id.ctd_text0);
        ctd[1] = (TextView) findViewById(R.id.ctd_text1);
        ctd[2] = (TextView) findViewById(R.id.ctd_text2);
        ctd[3] = (TextView) findViewById(R.id.ctd_text3);
        ctd[4] = (TextView) findViewById(R.id.ctd_text4);
        ctd[5] = (TextView) findViewById(R.id.ctd_text5);
        ctd[6] = (TextView) findViewById(R.id.ctd_text6);
        ctd[7] = (TextView) findViewById(R.id.ctd_text7);
        ctd[8] = (TextView) findViewById(R.id.ctd_text8);
        ctd[9] = (TextView) findViewById(R.id.ctd_text9);
        ctd[10] = (TextView) findViewById(R.id.ctd_text10);
        ctd[11] = (TextView) findViewById(R.id.ctd_text11);
        ctd[12] = (TextView) findViewById(R.id.ctd_text12);
        ctd[13] = (TextView) findViewById(R.id.ctd_text13);
        ctd[14] = (TextView) findViewById(R.id.ctd_text14);

        temp[0] = (TextView) findViewById(R.id.temp_text0);
        temp[1] = (TextView) findViewById(R.id.temp_text1);
        temp[2] = (TextView) findViewById(R.id.temp_text2);
        temp[3] = (TextView) findViewById(R.id.temp_text3);
        temp[4] = (TextView) findViewById(R.id.temp_text4);
        temp[5] = (TextView) findViewById(R.id.temp_text5);
        temp[6] = (TextView) findViewById(R.id.temp_text6);
        temp[7] = (TextView) findViewById(R.id.temp_text7);
        temp[8] = (TextView) findViewById(R.id.temp_text8);
        temp[9] = (TextView) findViewById(R.id.temp_text9);
        temp[10] = (TextView) findViewById(R.id.temp_text10);
        temp[11] = (TextView) findViewById(R.id.temp_text11);
        temp[12] = (TextView) findViewById(R.id.temp_text12);
        temp[13] = (TextView) findViewById(R.id.temp_text13);
        temp[14] = (TextView) findViewById(R.id.temp_text14);

        reh[0] = (TextView) findViewById(R.id.reh_text0);
        reh[1] = (TextView) findViewById(R.id.reh_text1);
        reh[2] = (TextView) findViewById(R.id.reh_text2);
        reh[3] = (TextView) findViewById(R.id.reh_text3);
        reh[4] = (TextView) findViewById(R.id.reh_text4);
        reh[5] = (TextView) findViewById(R.id.reh_text5);
        reh[6] = (TextView) findViewById(R.id.reh_text6);
        reh[7] = (TextView) findViewById(R.id.reh_text7);
        reh[8] = (TextView) findViewById(R.id.reh_text8);
        reh[9] = (TextView) findViewById(R.id.reh_text9);
        reh[10] = (TextView) findViewById(R.id.reh_text10);
        reh[11] = (TextView) findViewById(R.id.reh_text11);
        reh[12] = (TextView) findViewById(R.id.reh_text12);
        reh[13] = (TextView) findViewById(R.id.reh_text13);
        reh[14] = (TextView) findViewById(R.id.reh_text14);

        pop[0] = (TextView) findViewById(R.id.pop_text0);
        pop[1] = (TextView) findViewById(R.id.pop_text1);
        pop[2] = (TextView) findViewById(R.id.pop_text2);
        pop[3] = (TextView) findViewById(R.id.pop_text3);
        pop[4] = (TextView) findViewById(R.id.pop_text4);
        pop[5] = (TextView) findViewById(R.id.pop_text5);
        pop[6] = (TextView) findViewById(R.id.pop_text6);
        pop[7] = (TextView) findViewById(R.id.pop_text7);
        pop[8] = (TextView) findViewById(R.id.pop_text8);
        pop[9] = (TextView) findViewById(R.id.pop_text9);
        pop[10] = (TextView) findViewById(R.id.pop_text10);
        pop[11] = (TextView) findViewById(R.id.pop_text11);
        pop[12] = (TextView) findViewById(R.id.pop_text12);
        pop[13] = (TextView) findViewById(R.id.pop_text13);
        pop[14] = (TextView) findViewById(R.id.pop_text14);

        sr06[0] = (TextView) findViewById(R.id.sr06_text0);
        sr06[1] = (TextView) findViewById(R.id.sr06_text1);
        sr06[2] = (TextView) findViewById(R.id.sr06_text2);
        sr06[3] = (TextView) findViewById(R.id.sr06_text3);
        sr06[4] = (TextView) findViewById(R.id.sr06_text4);
        sr06[5] = (TextView) findViewById(R.id.sr06_text5);
        sr06[6] = (TextView) findViewById(R.id.sr06_text6);
        sr06[7] = (TextView) findViewById(R.id.sr06_text7);
        sr06[8] = (TextView) findViewById(R.id.sr06_text8);
        sr06[9] = (TextView) findViewById(R.id.sr06_text9);
        sr06[10] = (TextView) findViewById(R.id.sr06_text10);
        sr06[11] = (TextView) findViewById(R.id.sr06_text11);
        sr06[12] = (TextView) findViewById(R.id.sr06_text12);
        sr06[13] = (TextView) findViewById(R.id.sr06_text13);
        sr06[14] = (TextView) findViewById(R.id.sr06_text14);

        tmx[0] = (TextView) findViewById(R.id.tmx_text0);
        tmx[1] = (TextView) findViewById(R.id.tmx_text1);
        tmx[2] = (TextView) findViewById(R.id.tmx_text2);
        tmx[3] = (TextView) findViewById(R.id.tmx_text3);
        tmx[4] = (TextView) findViewById(R.id.tmx_text4);
        tmx[5] = (TextView) findViewById(R.id.tmx_text5);
        tmx[6] = (TextView) findViewById(R.id.tmx_text6);
        tmx[7] = (TextView) findViewById(R.id.tmx_text7);
        tmx[8] = (TextView) findViewById(R.id.tmx_text8);
        tmx[9] = (TextView) findViewById(R.id.tmx_text9);
        tmx[10] = (TextView) findViewById(R.id.tmx_text10);
        tmx[11] = (TextView) findViewById(R.id.tmx_text11);
        tmx[12] = (TextView) findViewById(R.id.tmx_text12);
        tmx[13] = (TextView) findViewById(R.id.tmx_text13);
        tmx[14] = (TextView) findViewById(R.id.tmx_text14);

        tmn[0] = (TextView) findViewById(R.id.tmn_text0);
        tmn[1] = (TextView) findViewById(R.id.tmn_text1);
        tmn[2] = (TextView) findViewById(R.id.tmn_text2);
        tmn[3] = (TextView) findViewById(R.id.tmn_text3);
        tmn[4] = (TextView) findViewById(R.id.tmn_text4);
        tmn[5] = (TextView) findViewById(R.id.tmn_text5);
        tmn[6] = (TextView) findViewById(R.id.tmn_text6);
        tmn[7] = (TextView) findViewById(R.id.tmn_text7);
        tmn[8] = (TextView) findViewById(R.id.tmn_text8);
        tmn[9] = (TextView) findViewById(R.id.tmn_text9);
        tmn[10] = (TextView) findViewById(R.id.tmn_text10);
        tmn[11] = (TextView) findViewById(R.id.tmn_text11);
        tmn[12] = (TextView) findViewById(R.id.tmn_text12);
        tmn[13] = (TextView) findViewById(R.id.tmn_text13);
        tmn[14] = (TextView) findViewById(R.id.tmn_text14);

        final Handler handler = new Handler() {
            public void handleMessage(Message msg){

// "hour", "day", "temp", "tmx", "tmn", "sky", "pty", "wfKor", "wfEn", "pop", "r12", "s12", "ws", "wd", "wdKor", "wdEn", "reh", "r06", "s06"
//   0       1       2      3      4      5      6       7       8       9      10     11    12    13     14       15      16     17     18
                for (int i = 0; i < weatherlist.length; i++) {
                    String[] tmp = weatherlist[i].split(",");
                    Log.i(TAG, "index : " + i);
//                    Log.i(TAG, "handleMessage: "+ tmp[5].toString());

                    switch(tmp[5]){
                        case "1":
                            icon[i].setImageResource(R.drawable.ss);
                            break;
                        case "2":
                            icon[i].setImageResource(R.drawable.css);
                            break;
                        case "3":
                            icon[i].setImageResource(R.drawable.c);
                            break;
                        case "4":
                            icon[i].setImageResource(R.drawable.co);
                            break;
                        default:
                            Log.d(TAG, "handleMessage: icon not set");
                    }

                    switch(tmp[1]) {
                        case "0":
                            today[i].setText("오늘 "+tmp[0]+"시");
                            break;
                        case "1":
                            today[i].setText("내일 "+tmp[0]+"시");
                            break;
                        case "2":
                            today[i].setText("모레 "+tmp[0]+"시");
                            break;
                    }

                    temp[i].setText(tmp[2]+"'C");

                    tmx[i].setText("최고기온 "+tmp[3]+"'c");

                    tmn[i].setText("최저기온 " +tmp[4]+"'c");

                    if(Float.parseFloat(tmp[17]) > 0.0f) {
                        sr06[i].setText("강수량 : "+ tmp[17]);
                    } else if (Float.parseFloat(tmp[18]) > 0.0f) {
                        sr06[i].setText("적설량 : "+tmp[18]);
                    } else {
                        sr06[i].setText("화려한 조명");
                    }

                    pop[i].setText("강수확률 "+tmp[9]+"%");

                    reh[i].setText("습도 "+ tmp[16]+"%");

                }
            }
        };

        Timer maintimer = new Timer();
        TimerTask maintask = new TimerTask() {
            @Override
            public void run() {
                for (int i = 1; i <= weatherlist.length; i++){
                    try {
                        FileInputStream i_f = openFileInput(filename+i+ExtendStr);
                        byte[] txt = new byte[500];
                        i_f.read(txt);
                        i_f.close();
                        weatherlist[i-1] = (new String(txt)).trim();
                    } catch (Exception e){
                        Log.e(TAG, "onCreate: load Error", e);
                    }
                    Log.i(TAG, "onCreate: "+weatherlist[i-1]);
                }

                Message msg = handler.obtainMessage();
                handler.sendMessage(msg);


            }
        };

        maintimer.schedule(maintask,5000,(30*1000));
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {
            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            boolean check_result = true;
            // 모든 퍼미션을 허용했는지 체크합니다.
            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }
            if ( check_result ) {
                //위치 값을 가져올 수 있음
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {

                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.", Toast.LENGTH_LONG).show();
                    finish();
                }else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    public void checkRunTimePermission(){

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);


        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            // 3.  위치 값을 가져올 수 있음



        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;

            case GET_STRING:
                if (resultCode == RESULT_OK){
                    address = data.getStringExtra("Address");
                }
                break;
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
