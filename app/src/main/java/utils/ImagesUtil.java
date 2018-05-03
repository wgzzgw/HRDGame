package utils;

/**
 * Created by yy on 2018/5/2.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import com.example.hrdgame.PuzzleMain;
import com.example.hrdgame.R;

import java.util.ArrayList;
import java.util.List;

import model.ItemBean;

/**
 * 图像工具类：实现图像的分割与自适应
 *
 */
public class ImagesUtil {
    public ItemBean itemBean;//分割后的图片小块
    /**
     * 切图、初始状态（正常顺序）
     *
     * @param type        游戏难度
     * @param picSelected 选择的图片
     * @param context     context
     */
    public void createInitBitmaps(int type, Bitmap picSelected,
                                  Context context) {
        Bitmap bitmap = null;
        List<Bitmap> bitmapItems = new ArrayList<Bitmap>();
        // 每个Item的宽高
        int itemWidth = picSelected.getWidth() / type;
        int itemHeight = picSelected.getHeight() / type;
        //水平方向依次添加图片小块
        for (int i = 1; i <= type; i++) {
            for (int j = 1; j <= type; j++) {
                //参数2，3：源图片中x方向的起始像素，源图片中y方向的起始像素
                //参数4，5：截图宽度（单位为像素），截图高度（单位为像素）
                bitmap = Bitmap.createBitmap(
                        picSelected,
                        (j - 1) * itemWidth,
                        (i - 1) * itemHeight,
                        itemWidth,
                        itemHeight);
                bitmapItems.add(bitmap);
                itemBean = new ItemBean(
                        (i - 1) * type + j,
                        (i - 1) * type + j,
                        bitmap);
                //添加到算法工具类中去
                GameUtil.mItemBeans.add(itemBean);
            }
        }
        // 保存最后一个图片在拼图完成时填充
        PuzzleMain.mLastBitmap = bitmapItems.get(type * type - 1);
        // 设置最后一个为空Item
        bitmapItems.remove(type * type - 1);
        GameUtil.mItemBeans.remove(type * type - 1);
        Bitmap blankBitmap = BitmapFactory.decodeResource(
                context.getResources(), R.drawable.blank);
        //截取跟item一样大小的空白区域即可
        blankBitmap = Bitmap.createBitmap(
                blankBitmap, 0, 0, itemWidth, itemHeight);
        bitmapItems.add(blankBitmap);
        GameUtil.mItemBeans.add(new ItemBean(type * type, 0, blankBitmap));
        GameUtil.mBlankItemBean = GameUtil.mItemBeans.get(type * type - 1);
    }

    /**
     * 处理图片 放大、缩小到合适位置
     *
     * @param newWidth  缩放后Width
     * @param newHeight 缩放后Height
     * @param bitmap    bitmap
     * @return bitmap
     */
    public Bitmap resizeBitmap(float newWidth, float newHeight, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        //第一个参数是X轴的缩放大小，第二个参数是Y轴的缩放大小
        matrix.postScale(
                newWidth / bitmap.getWidth(),
                newHeight / bitmap.getHeight());
        Bitmap newBitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix, true);
        return newBitmap;
    }
}
