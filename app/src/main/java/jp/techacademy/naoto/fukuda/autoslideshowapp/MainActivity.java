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
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    Button mForwardButton; //進むボタン
    Button mStartPauseButton; //再生ボタン（＝停止ボタン）
    Button mBackButton; //戻るボタン
    Cursor cursor; //カーソルのインスタンス:メンバ変数として指定(→クラス全体で使用可）
    ImageView imageView;
    boolean autoFlag = true; //true 再生ON、再生ボタンの文字→停止に変更、進むボタン＆戻るボタン無効化。false 再生OFF。
    boolean permissionFlag = true; //true ユーザーが外部画像Permission許可。false 未許可。許可する(=trueになる)までボタン使えない。

    Timer mTimer;
    Handler mHandler = new Handler();
    double mTimerSec = 0.0;

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
                // 画像取得許可されているので、onCreate実行。
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合

        } else {
            getContentsInfo();
        }

        //1.再生 or 停止ボタンの動作。
        mStartPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (permissionFlag == false) {
                    // 画像取得未許可時の例外処理。許可ダイアログを表示する
                    Log.d("ANDROID", "permissionFlag = " + permissionFlag);

                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

                } else {
                    //画像取得許可しているので動作。
                    if (autoFlag) { //再生ON

                        if (mTimer == null) {
                            mTimer = new Timer();
                            mTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mTimerSec += 2.0;

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            mStartPauseButton.setText("停止");
                                            mForwardButton.setEnabled(false);
                                            mBackButton.setEnabled(false);
                                            getContentsInfoForward();
                                            autoFlag = !autoFlag;
                                        }
                                    });
                                }
                            }, 2000, 2000);
                            //2000ms　＝ 2秒。
                        }


                    } else { //再生OFF = 停止ON
                        if (mTimer != null) {
                            mTimer.cancel();
                            mTimer = null;
                        }
                        mStartPauseButton.setText("再生");
                        mForwardButton.setEnabled(true);
                        mBackButton.setEnabled(true);
                        Log.d("ANDROID", "mTimer : " + mTimer);
                        autoFlag = !autoFlag;

                    }

                }
            }

        });

        //2.進むボタンの動作。
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContentsInfoForward();

            }
        });

        //3.戻るボタンの動作。
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
                    permissionFlag = true; //画像処理許可されたのでTrueにする。
                    Log.d("ANDROID", "許可された。permissionFlag = " +permissionFlag);
                    getContentsInfo();
                } else{
                    Toast toast = Toast.makeText(this, "Please allow, if you want to use this app.",
                            Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_VERTICAL,0,0);
                    toast.show();
                    permissionFlag = false;
                    Log.d("ANDROID", "許可されなかった。permissionFlag = " +permissionFlag);
                }

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

            mStartPauseButton.setText("再生");
            setImageView();

            // リファクタリング。複数メソッドで共通する動作は、メソッドにしてしまう。setImageView();
            // indexからIDを取得し、そのIDから画像のURIを取得する
        } else {
            Toast toast = Toast.makeText(this, "No image is saved", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_VERTICAL,0,0);
            toast.show();
        }
    }

    private void getContentsInfoForward() {

        if(permissionFlag == false) {
            Log.d("ANDROID", "permissionFlag = " +permissionFlag);
            // 画像取得未許可時の例外処理。許可ダイアログを表示する。
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

        } else {

            if (cursor.moveToNext()) {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                setImageView();
            } else {
                cursor.moveToFirst();
                setImageView();
            }
        }

    }

    private void getContentsInfoBack() {

        if(permissionFlag == false) {
            Log.d("ANDROID", "permissionFlag = " +permissionFlag);
            // 画像取得未許可時の例外処理。許可ダイアログを表示する。
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);

        } else {

            if (cursor.moveToPrevious()) {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                setImageView();
            } else {
                cursor.moveToLast();
                setImageView();
            }
        }

    }

    private void setImageView() { //画像表示処理。進む・戻るに共通するのでRefactoringしたもの。
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        Log.d("ANDROID", "URI : " + imageUri.toString());
        imageView.setImageURI(imageUri);
    }

}