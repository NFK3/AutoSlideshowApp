package jp.techacademy.naoto.fukuda.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button mForwardButton;
    Button mStartButton;
    Button mBackButton;
    Cursor cursor; //カーソルのインスタンス:メンバ変数として指定(→クラス全体で使用可）
    ImageView imageView;
    

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mForwardButton = (Button) findViewById(R.id.forward_button);
        mStartButton = (Button) findViewById(R.id.start_button);
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



    mStartButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getContentsInfo();

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
        );

        if (cursor.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            imageView.setImageURI(imageUri);
            Log.d("ANDROID", "URI : " + imageUri.toString());

        }cursor.close();
    }


    private void getContentsInfoForward(){

       /* // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        ); */

        if (cursor.moveToNext()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
          /*  int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            */

            imageView.setImageURI(imageUri);
            Log.d("ANDROID", "URI : " + imageUri.toString());
        }else {
            cursor.moveToFirst();
           /* int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
             */
            imageView.setImageURI(imageUri);
            Log.d("ANDROID", "URI : " + imageUri.toString());
        }
        cursor.close();
    }

    private void getContentsInfoBack(){


        if (cursor.moveToPrevious()) {
       /* // indexからIDを取得し、そのIDから画像のURIを取得する
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        */

        imageView.setImageURI(imageUri);
        Log.d("ANDROID", "URI : " + imageUri.toString());
    }else {
        cursor.moveToLast();
       /* int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
         */
        imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setImageURI(imageUri);
        Log.d("ANDROID", "URI : " + imageUri.toString());
    }
        cursor.close();
    }

}