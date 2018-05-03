package com.example.hrdgame;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import adapter.GridPicListAdapter;
import utils.ScreenUtil;
/**
 * 程序主界面：显示默认图片列表、自选图片按钮
 *
 */
public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
    private PopupWindow mPopupWindow;
    private View mPopupView;
    private LayoutInflater mLayoutInflater;
    // GridView 显示图片
    private GridView mGvPicList;
    private List<Bitmap> mPicList;//GirdViews数据源
    private int[] mResPicId;// 主页图片资源ID
    // 返回码：系统图库
    private static final int RESULT_IMAGE = 100;
    // 返回码：相机
    private static final int RESULT_CAMERA = 200;
    // IMAGE TYPE
    private static final String IMAGE_TYPE = "image/*";
    // Temp照片路径
    public static String TEMP_IMAGE_PATH;
    // 本地图册、相机选择
    private String[] mCustomItems = new String[]{"本地图册", "相机拍照"};
    // 游戏类型N*N
    private int mType = 2;
    private TextView mTvType2;
    private TextView mTvType3;
    private TextView mTvType4;
    // 显示Type
    private TextView mTvPuzzleMainTypeSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();//初始化view
        // 数据适配器
        mGvPicList.setAdapter(new GridPicListAdapter(
                MainActivity.this, mPicList));
    }

    private void initViews() {
        // mType view
        mLayoutInflater = (LayoutInflater) getSystemService(
                LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(
                R.layout.xpuzzle_main_type_selected, null);
        //girdview
        mGvPicList = (GridView) findViewById(
                R.id.gv_xpuzzle_main_pic_list);
        mPicList = new ArrayList<Bitmap>();
        // 初始化Bitmap数据到list数据源中去
        mResPicId = new int[]{
                R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
                R.drawable.pic4, R.drawable.pic5, R.drawable.pic6,
                R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
                R.drawable.pic10, R.drawable.pic11, R.drawable.pic12,
                R.drawable.pic13, R.drawable.pic14,
                R.drawable.pic15, R.drawable.more};
        Bitmap[] bitmaps = new Bitmap[mResPicId.length];
        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = BitmapFactory.decodeResource(
                    getResources(), mResPicId[i]);
            mPicList.add(bitmaps[i]);
        }
        // 显示type
        mTvPuzzleMainTypeSelected = (TextView) findViewById(
                R.id.tv_puzzle_main_type_selected);
        TEMP_IMAGE_PATH =
                Environment.getExternalStorageDirectory().getPath() +
                        "/temp.png";//图片输出地址
        // Item点击监听
        mGvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long arg3) {
                if (position == mResPicId.length - 1) {
                    // 选择本地图库 相机
                    showDialogCustom();
                } else {
                    // 选择默认图片，进去游戏界面
                    Intent intent = new Intent(
                            MainActivity.this,
                            PuzzleMain.class);
                    intent.putExtra("picSelectedID", mResPicId[position]);
                    intent.putExtra("mType", mType);
                    startActivity(intent);
                }
            }
        });
        /**
         * 显示难度Type
         */
        mTvPuzzleMainTypeSelected.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 弹出popup window
                        popupShow(v);
                    }
                });
        mTvType2 = (TextView) mPopupView.findViewById(R.id.tv_main_type_2);
        mTvType3 = (TextView) mPopupView.findViewById(R.id.tv_main_type_3);
        mTvType4 = (TextView) mPopupView.findViewById(R.id.tv_main_type_4);
        // 监听事件
        mTvType2.setOnClickListener(this);
        mTvType3.setOnClickListener(this);
        mTvType4.setOnClickListener(this);
    }
    /**
     * 显示popup window
     *
     * @param view popup window
     */
    private void popupShow(View view) {
        //获取屏幕的密度
        int density = (int) ScreenUtil.getDeviceDensity(this);
        // 显示popup window
        mPopupWindow = new PopupWindow(mPopupView,
                200 * density, 50 * density);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        // 透明背景
        Drawable transpent = new ColorDrawable(Color.TRANSPARENT);
        mPopupWindow.setBackgroundDrawable(transpent);
        // 获取位置
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        //参数一：父view 参数二：没有设定对齐方向 参数三四：偏移量
        mPopupWindow.showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                location[0] - 40 * density,
                location[1] + 30 * density);
    }
    // 显示选择系统图库 相机对话框
    private void showDialogCustom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                MainActivity.this);
        builder.setTitle("选择：");
        builder.setItems(mCustomItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (0 == which) {
                            // 本地图册
                            PicFromAlbumFirst();
                        } else if (1 == which) {
                            // 系统相机
                            Intent intent = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            Uri photoUri=null;
                            //获取设备的系统版本
                            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            if (currentapiVersion < 24) {
                                //小于7.0的版本
                               photoUri = createLowVersionUri();
                            }else{
                                //大于7.0的版本
                                photoUri=createContentUri(createLowVersionUri());
                            }
                            //指定图片输出地址
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                            startActivityForResult(intent, RESULT_CAMERA);
                        }
                    }
                });
        builder.create().show();
    }
    /*
    返回系统版本低于7.0的uri
     */
    private Uri createLowVersionUri() {
        /*
        * 使用应用关联目录可以不进行运行时权限
        * */
        //创建File对象，用于存储拍照后的图片
        File img = new File(TEMP_IMAGE_PATH);
        try {
        if (img.exists()) {
            img.delete();
        }
            img.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(img);
    }
    /*
    返回系统版本高于7.0的uri
     */
    public Uri createContentUri(Uri uri) {
        //方法1.需要配置AndroidManifest.XML
        /*return FileProvider.getUriForFile(MainActivity.this,"com.example.hrdgame.fileprovider",
                new File(TEMP_IMAGE_PATH));*/
        //方法2.转换低版本uri为contenturi,不需要配置AndroidManifest.XML
        String filePath = uri.getPath();//得到图片的uri地址，uri统一资源标识符
        Cursor cursor = this.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            //拼接uri
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, filePath);
            return this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }
    }
    /*
    相册运行时权限请求
     */
    private void PicFromAlbumFirst() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            takePicFromAlbum();
        }
    }
    /*
    权限授予返回处理
     */
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        switch (requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    takePicFromAlbum();
                }else{
                    Toast.makeText(this,"你已拒绝了授权",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
    /*
    相册选择
     */
    private void takePicFromAlbum() {
        Intent picIntent = new Intent("android.intent.action.GET_CONTENT");
        picIntent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                IMAGE_TYPE);
        //打开相册
        startActivityForResult(picIntent, RESULT_IMAGE);
    }
        /*
        调用图库相机回调方法
        */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_IMAGE && data != null) {
                // 相册
                //获取设备的系统版本
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                String imagePath="";
                if (currentapiVersion < 19) {
                    //小于4.4的版本
                  imagePath=getImagePath(data.getData(),null);
                }else{
                    if(currentapiVersion>=19) {
                        //大于4.4的版本
                        if ("file".equalsIgnoreCase(data.getData().getScheme())) {
                            //file类型，直接获取图片路径即可
                            imagePath = data.getData().getPath();
                        } else if ("content".equalsIgnoreCase(data.getData().getScheme())) {
                            //content类型
                            imagePath = getImagePath(data.getData(), null);
                        } else if (DocumentsContract.isDocumentUri(this, data.getData())) {
                            //document类型，通过documentId处理
                            String docID = DocumentsContract.getDocumentId(data.getData());
                            if ("com.android.providers.media.documents".equals(data.getData().getAuthority())) {
                                String id = docID.split(":")[1];//解析出数字格式的ID
                                String selection = MediaStore.Images.Media._ID + "=" + id;
                                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                            } else if ("com.android.providers.downloads.documents".equals(data.getData().getAuthority())) {
                                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                                        Long.valueOf(docID));
                                imagePath = getImagePath(contentUri, null);
                            }
                        }
                    }
                }
                Intent intent = new Intent(
                        MainActivity.this,
                        PuzzleMain.class);
                intent.putExtra("mPicPath", imagePath);
                intent.putExtra("mType", mType);
                startActivity(intent);
            } else if (requestCode == RESULT_CAMERA) {
                // 相机
                Intent intent = new Intent(
                        MainActivity.this,
                        PuzzleMain.class);
                //传值——游戏难度，图片路径
                intent.putExtra("mPicPath", TEMP_IMAGE_PATH);
                intent.putExtra("mType", mType);
                startActivity(intent);
            }
        }
    }
    /*
    系统版本大于7.0获取图片真实路径
     */
    private String getImagePath(Uri uri,String selection){
        String path=null;
        Cursor cursor = this.getContentResolver().query(
                uri, null, selection, null, null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                path=cursor.getString(
                        cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
    /**
     * popup window item点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // Type
            case R.id.tv_main_type_2:
                mType = 2;
                mTvPuzzleMainTypeSelected.setText("2 X 2");
                break;
            case R.id.tv_main_type_3:
                mType = 3;
                mTvPuzzleMainTypeSelected.setText("3 X 3");
                break;
            case R.id.tv_main_type_4:
                mType = 4;
                mTvPuzzleMainTypeSelected.setText("4 X 4");
                break;
            default:
                break;
        }
        mPopupWindow.dismiss();
    }
}
