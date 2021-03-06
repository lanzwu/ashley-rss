package com.cashzhang.ashley;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import java.text.NumberFormat;
import java.util.Locale;

import static com.cashzhang.ashley.Constants.*;
import static com.cashzhang.ashley.AdapterFeedItems.Type;

/**
 * Created by zhangchi on 2018/2/11.
 */

public class ViewFeedItem extends View {
    private static final float READ_OPACITY = 0.5F;
    private static final Paint[] m_paints = new Paint[3];
    private static final int SCREEN = Resources.getSystem().getDisplayMetrics().widthPixels;
    private static final NumberFormat LOCALE_FORMAT = NumberFormat.getNumberInstance(Locale.getDefault());
    private static final int[] FONT_COLORS = {
            R.color.item_title_color, R.color.item_link_color, R.color.item_description_color,
    };
    private static final int[] FONT_SIZES = {
            R.dimen.item_title_size, R.dimen.item_link_size, R.dimen.item_description_size,
    };
    private final int m_height;
    private final String[] m_timeInitials;
    public FeedItem m_item;
    boolean m_hasImage;
    private Bitmap m_image;
    public boolean m_isViewRead = false;

    /**
     * The number of units that will be shown for the time since published. '2d 3h 31m'
     */
    private static final int TIME_PRECISION = 2;

    /**
     * Divide a number in milliseconds by this to get your unit value.
     */
    private static final double[] DIVISION_IN_MS = {
            31556952000.0, 86400000.0, 3600000.0, 60000.0,
    };

    /**
     * The modulus to take of the unit divisions.
     */
    private static final int[] DIVISIONS_MODULUS = {
            1, 365, 24, 60,
    };

    public ViewFeedItem(Context context, Type type) {
        super(context);
        Resources resources = context.getResources();

        float titleSize = resources.getDimension(R.dimen.item_title_size);
        float linkSize = resources.getDimension(R.dimen.item_link_size);
        float desSize = resources.getDimension(R.dimen.item_description_size);
        float imageSize = resources.getDimension(R.dimen.max_image_height);

        // Calculate the size of the view.
        float base = s_eightDp + titleSize * 2 + linkSize;
        switch (type) {
            case PLAIN:
                base += (float) (3.6 * desSize + getDp(4.0F));
            case PLAIN_SANS_DESCRIPTION:
                base += getDp(4.0F);
                m_hasImage = false;
                break;
            case IMAGE:
                base += (float) (3.6 * desSize + getDp(20.0F));
            case IMAGE_SANS_DESCRIPTION:
                base += imageSize;
                m_hasImage = true;
        }
        m_height = Math.round(base);

        m_timeInitials = resources.getStringArray(R.array.time_initials);

        initPaints(resources);
    }

    private static int getDp(float pixels) {
        float floatDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, s_displayMetrics);
        return Math.round(floatDp);
    }

    private static void initPaints(Resources resources) {
        for (int i = 0; m_paints.length > i; i++) {
            m_paints[i] = configurePaint(resources, FONT_SIZES[i], FONT_COLORS[i]);
            if (2 == i) {
                m_paints[2].setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            }
        }
    }

    static Paint configurePaint(Resources resources, int dimenResource, int colorResource) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(resources.getDimension(dimenResource));
        paint.setColor(resources.getColor(colorResource));
        paint.setHinting(Paint.HINTING_ON);
        return paint;
    }

    public void setBitmap(Bitmap bitmap) {
        m_image = bitmap;
        if (null != bitmap) {
            invalidate();
        }
    }

    public void setRead(boolean read) {
        m_isViewRead = read;
        setAlpha(read ? READ_OPACITY : 1.0F);
        setBackgroundResource(read ? R.drawable.selector_transparent : R.drawable.selector_white);
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void onDraw(Canvas canvas) {
      /* If the canvas is meant to draw a bitmap but it is null, draw nothing. */
        if (m_hasImage && null == m_image) {
            return;
        }

        float verticalPosition = drawBase(canvas);
        verticalPosition = drawBitmap(canvas, verticalPosition);
        if (null != m_item.m_desLines && 0 != m_item.m_desLines.length && null != m_item.m_desLines[0]) {
            if (m_hasImage) {
                verticalPosition += getDp(4.0F);
            }
            drawDes(canvas, verticalPosition);
        }
    }

    float drawBase(Canvas canvas) {
        boolean rtl = Constants.isTextRtl(m_item.m_title);
        //TODO
        float verticalPosition = m_paints[0].getTextSize() + s_eightDp;

        int startPadding = rtl ? SCREEN - s_eightDp : s_eightDp;
        int endPadding = rtl ? s_eightDp : SCREEN - s_eightDp;

        Paint.Align start = rtl ? Paint.Align.RIGHT : Paint.Align.LEFT;
        Paint.Align end = rtl ? Paint.Align.LEFT : Paint.Align.RIGHT;

        // Draw the time.
        m_paints[1].setTextAlign(end);
        canvas.drawText(getTime(m_item.m_time), endPadding, verticalPosition, m_paints[1]);

        String[] info = {m_item.m_title, m_item.m_urlTrimmed};

        // Draw the title and the url.
        for (int i = 0; 2 > i; i++) {
            m_paints[i].setTextAlign(start);
            canvas.drawText(info[i], startPadding, verticalPosition, m_paints[i]);
            verticalPosition += m_paints[i].getTextSize();
        }

        return m_hasImage ? verticalPosition : verticalPosition + getDp(4.0F);
    }

    float drawBitmap(Canvas canvas, float verticalPosition) {
        if (null != m_image) {
            canvas.drawBitmap(m_image, 0.0F, verticalPosition, m_paints[0]);
            return verticalPosition + m_image.getHeight() + getDp(16.0F);
        } else {
            return verticalPosition + getDp(4.0F);
        }
    }

    void drawDes(Canvas canvas, float verticalPos) {
        if (!m_item.m_desLines[0].isEmpty()) {
            boolean rtl = Constants.isTextRtl(m_item.m_desLines[0]);
            //TODO
            m_paints[2].setTextAlign(rtl ? Paint.Align.RIGHT : Paint.Align.LEFT);
            int horizontalPos = rtl ? SCREEN - s_eightDp : s_eightDp;

            for (String des : m_item.m_desLines) {
                canvas.drawText(des, horizontalPos, verticalPos, m_paints[2]);
                verticalPos += (float) (m_paints[2].getTextSize() * 1.2);
            }
        }
    }

    /**
     * Takes a millisecond epoch time and turns it into a readable string.
     *
     * @param time the epoch time in milliseconds that the item was published.
     * @return a string of the format '1y 13d 3h 16m' with the number of units show dependant on
     * ViewFeedItem.TIME_PRECISION.
     */
    private String getTime(long time) {
        Long timeAgo = System.currentTimeMillis() - time;

        StringBuilder builder = new StringBuilder(32);

        int i = 0;
        int count = 0;

        while (i < DIVISION_IN_MS.length && TIME_PRECISION > count) {
            long period = Math.round(timeAgo / DIVISION_IN_MS[i] % DIVISIONS_MODULUS[i]);
            if (0L != period) {
                builder.append(LOCALE_FORMAT.format(period));
                builder.append(m_timeInitials[i]);
                builder.append(' ');
                count++;
            }
            i++;
        }

        // If the length is nonzero, take the last ' ' off the end.
        if (0 < builder.length()) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int orientation = display.getRotation();
        switch (orientation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                setMeasuredDimension(Resources.getSystem()
                        .getDisplayMetrics().widthPixels, m_height);
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                //noinspection SuspiciousNameCombination
                setMeasuredDimension(Resources.getSystem()
                        .getDisplayMetrics().heightPixels, m_height);
        }
    }
}