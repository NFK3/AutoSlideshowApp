package jp.techacademy.naoto.fukuda.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button mForwardButton;
    Button mStartPauseButton;
    Button mBackButton;
    Cursor cursor; //カーソルのインスタンス:メンバ変数として指定(→クラス全体で使用可）
    ImageView imageView;
    boolean autoflag = true; //autoflag = ture 初期値、再生ON。false 停止ON。

    Timer mTimer;
    Handler mHandler = new Handler();
    double mTimerSec = 0.0;
    Date now = new Date();


    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mForwardButton = (Button) findViewById(R.id.forward_button);
        mStartPauseButton = (Button) findViewById(R.id.start_button);
        mBackButton = (Button) findViewById(R.id.back_button);
        imageView = (ImageView) findViewById(R.id.imageView1);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

        mStartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(autoflag){ //再生ON

                    if(mTimer == null) {
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mTimerSec += 2.0;

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        mStartPauseButton.setText("停止");
                                        getContentsInfoForward();
                                        autoflag =! autoflag;
                                    }
                                });
                            }
                        }, 2000, 2000);
                    }


                }else { //再生OFF = 停止ON
                    if(mTimer != null){
                        mTimer.cancel();
                        mTimer = null;
                    }
                        mStartPauseButton.setText("再生");
                        Log.d("ANDROID", "mTimer : " + mTimer);
                        autoflag =! autoflag;

                }

            }

        });

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContentsInfoForward();

            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContentsInfoBack();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
                //メンバ変数Cursor cursorに値は保存される。
        );

        if (cursor.moveToFirst()) {

            cursor.moveToFirst();
            mStartPauseButton.setText("再生");
            setImageView();
            // リファクタリング。複数メソッドで共通する動作は、メソッドにしてしまう。setImageView();
            // indexからIDを取得し、そのIDから画像のURIを取得する
        }
    }

    private void getContentsInfoForward() {

        if (cursor.moveToNext()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            setImageView();
        } else {
            cursor.moveToFirst();
            setImageView();
        }

    }

    private void getContentsInfoBack() {


        if (cursor.moveToPrevious()) {
            setImageView();
        } else {
            cursor.moveToLast();
            setImageView();
        }
    }

    private void setImageView() { //Refactaringした。
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        Log.d("ANDROID", "URI : " + imageUri.toString());
        imageView.setImageURI(imageUri);
    }

}