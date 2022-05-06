package com.thuypham.ptithcm.editvideo.cropper.util;

import android.content.res.Resources;
import android.graphics.Paint;

import com.thuypham.ptithcm.editvideo.R;

/**
 * Utility class for handling all of the Paint used to draw the CropOverlayView.
 */
public class PaintUtil {

    // Public Methods //////////////////////////////////////////////////////////

    /**
     * Creates the Paint object for drawing the crop window border.
     */
    public static Paint newBorderPaint(Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.border_thickness));
        paint.setColor(resources.getColor(R.color.border));

        return paint;
    }

    /**
     * Creates the Paint object for drawing the crop window guidelines.
     */
    public static Paint newGuidelinePaint(Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.guideline_thickness));
        paint.setColor(resources.getColor(R.color.guideline));

        return paint;
    }

    /**
     * Creates the Paint object for drawing the translucent overlay outside the crop window.
     *
     * @return the new Paint object
     */
    public static Paint newSurroundingAreaOverlayPaint(Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(resources.getColor(R.color.surrounding_area));

        return paint;
    }

    /**
     * Creates the Paint object for drawing the corners of the border
     */
    public static Paint newCornerPaint(Resources resources) {

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(resources.getDimension(R.dimen.corner_thickness));
        paint.setColor(resources.getColor(R.color.corner));

        return paint;
    }
}