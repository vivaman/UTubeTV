/*
 * Copyright (C) 2013 Artur Termenji
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.distantfuture.iconicdroid;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.distantfuture.iconicdroid.icon.Icon;

/**
 * A custom {@link Drawable} which can display icons from icon fonts.
 */
public class IconicFontDrawable extends Drawable {

  private Context mContext;

  private Paint mIconPaint;
  private Paint mContourPaint;

  private Rect mPaddingBounds;
  private RectF mPathBounds;

  private Path mPath;

  private int mIconPadding;
  private int mContourWidth;

  private int mIntrinsicWidth;
  private int mIntrinsicHeight;

  private int mSavedAlpha = -1;
  private boolean mDrawContour;

  private Icon mIcon;
  private char[] mIconUtfChars;

  public IconicFontDrawable(Context context) {
    mContext = context.getApplicationContext();

    mIconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    mContourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mContourPaint.setStyle(Paint.Style.STROKE);

    mPath = new Path();

    mPathBounds = new RectF();
    mPaddingBounds = new Rect();
  }

  public IconicFontDrawable(Context context, final Icon icon) {
    this(context);
    updateIcon(icon);
  }

  /**
   * Loads and draws given {@link Icon}.
   */
  public void setIcon(final Icon icon) {
    updateIcon(icon);
    invalidateSelf();
  }

  /**
   * Set a color for the {@link Icon}.
   */
  public void setIconColor(int color) {
    mIconPaint.setColor(color);
    mSavedAlpha = Color.alpha(color);

    invalidateSelf();
  }

  /**
   * Set a padding for the {@link Icon}.
   */
  public void setIconPadding(int iconPadding) {
    mIconPadding = iconPadding;
    if (mDrawContour) {
      mIconPadding += mContourWidth;
    }

    invalidateSelf();
  }

  /**
   * Set contour params for the {@link Icon}.
   * You should call {@link #drawContour(boolean)} method to enable contour.
   */
  public void setContour(int contourColor, int contourWidth) {
    setContourColor(contourColor);
    setContourWidth(contourWidth);
    invalidateSelf();
  }

  /**
   * Set contour color for the {@link Icon}.
   * You should call {@link #drawContour(boolean)} method to enable contour.
   */
  public void setContourColor(int contourColor) {
    mContourPaint.setColor(contourColor);
    invalidateSelf();
  }

  /**
   * Set contour width for the {@link Icon}.
   * You should call {@link #drawContour(boolean)} method to enable contour.
   */
  public void setContourWidth(int contourWidth) {
    mContourWidth = contourWidth;
    mContourPaint.setStrokeWidth(mContourWidth);
    invalidateSelf();
  }

  /**
   * Enable/disable contour drawing.
   */
  public void drawContour(boolean drawContour) {
    mDrawContour = drawContour;

    if (mDrawContour) {
      mIconPadding += mContourWidth;
    } else {
      mIconPadding -= mContourWidth;
    }

    invalidateSelf();
  }

  @Override
  public void draw(Canvas canvas) {
    if (mIcon != null) {
      final Rect viewBounds = getBounds();

      updatePaddingBounds(viewBounds);
      updateTextSize(viewBounds);
      offsetIcon(viewBounds);

      mPath.close();

      if (mDrawContour) {
        canvas.drawPath(mPath, mContourPaint);
      }

      canvas.drawPath(mPath, mIconPaint);
    }
  }

  @Override
  public int getIntrinsicWidth() {
    return mIntrinsicWidth;
  }

  /**
   * Set intrinsic width, which is used by several controls.
   */
  public void setIntrinsicWidth(int intrinsicWidth) {
    mIntrinsicWidth = intrinsicWidth;
  }

  @Override
  public int getIntrinsicHeight() {
    return mIntrinsicHeight;
  }

  /**
   * Set intrinsic height, which is used by several controls.
   */
  public void setIntrinsicHeight(int intrinsicHeight) {
    mIntrinsicHeight = intrinsicHeight;
  }

  @Override
  public int getOpacity() {
    return PixelFormat.OPAQUE;
  }

  @Override
  public void setAlpha(int alpha) {
    // the statelist drawable that this drawable is a part of sets the drawables to match
    // it's alpha, we don't want that.  It will clobber the color set on the paint previously
    // so we use the savedalpha if the statelist drawable tries to set us to max alpha by default
    if (mSavedAlpha != -1 && alpha == 255)
      mIconPaint.setAlpha(mSavedAlpha);
    else
      mIconPaint.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    mIconPaint.setColorFilter(cf);
  }

  private void updateIcon(Icon icon) {
    mIcon = icon;
    mIconUtfChars = Character.toChars(icon.getIconUtfValue());
    mIconPaint.setTypeface(mIcon.getIconicTypeface().getTypeface(mContext));
  }

  private void updatePaddingBounds(Rect viewBounds) {
    if (mIconPadding >= 0 && !(mIconPadding * 2 > viewBounds.width()) && !(mIconPadding * 2 > viewBounds
        .height())) {
      mPaddingBounds.set(viewBounds.left + mIconPadding, viewBounds.top + mIconPadding, viewBounds.right - mIconPadding, viewBounds.bottom - mIconPadding);
    }
  }

  private void updateTextSize(Rect viewBounds) {
    float textSize = (float) viewBounds.height() * 2;
    mIconPaint.setTextSize(textSize);

    mIconPaint.getTextPath(mIconUtfChars, 0, mIconUtfChars.length, 0, viewBounds.height(), mPath);
    mPath.computeBounds(mPathBounds, true);

    float deltaWidth = ((float) mPaddingBounds.width() / mPathBounds.width());
    float deltaHeight = ((float) mPaddingBounds.height() / mPathBounds.height());
    float delta = (deltaWidth < deltaHeight) ? deltaWidth : deltaHeight;
    textSize *= delta;

    mIconPaint.setTextSize(textSize);

    mIconPaint.getTextPath(mIconUtfChars, 0, mIconUtfChars.length, 0, viewBounds.height(), mPath);
    mPath.computeBounds(mPathBounds, true);
  }

  private void offsetIcon(Rect viewBounds) {
    float startX = viewBounds.centerX() - (mPathBounds.width() / 2);
    float offsetX = startX - mPathBounds.left;

    float startY = viewBounds.centerY() - (mPathBounds.height() / 2);
    float offsetY = startY - (mPathBounds.top);

    mPath.offset(offsetX, offsetY);
  }
}