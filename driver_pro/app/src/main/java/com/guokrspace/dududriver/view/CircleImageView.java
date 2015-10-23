package com.guokrspace.dududriver.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 
 * CircleImageView
 *  
 * created by hyman 2015-8-4
 *
 */
public class CircleImageView extends ImageView {

	private Paint paint = new Paint();

	public CircleImageView(Context context) {
		super(context);
	}

	public CircleImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		Drawable drawable = getDrawable();
		if (null != drawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

			Matrix mMatrix = new Matrix();
			mMatrix.reset();
			float ScaleW = (float) this.getWidth() / (bitmap.getWidth());
			float ScaleH = (float) this.getHeight() / (bitmap.getHeight());
			mMatrix.postScale(ScaleW, ScaleH);

			Bitmap Newbitmap = Bitmap.createBitmap(bitmap, 0, 0,
					bitmap.getWidth(), bitmap.getHeight(), mMatrix, true);

			Bitmap b = toRoundCorner(Newbitmap);

			final Rect rect = new Rect(0, 0, this.getWidth(), this.getHeight());
			paint.reset();
			canvas.drawBitmap(b, rect, rect, paint);

		} else {
			super.onDraw(canvas);
		}
	}

	private Bitmap toRoundCorner(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final int color = 0xff424242;
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		// 去锯齿
		paint.setAntiAlias(true); 
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		// int x = bitmap.getWidth();
		canvas.drawCircle(this.getWidth() / 2, this.getWidth() / 2,
				this.getWidth() / 2, paint);
		// public void drawCircle (float cx, float cy, float radius, Paint
		// paint)
		// 参数说明
		//
		// cx：圆心的x坐标。
		//
		// cy：圆心的y坐标。
		//
		// radius：圆的半径。
		//
		// paint：绘制时所使用的画笔。

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

}