package com.thuypham.ptithcm.editvideo.widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ScaleGestureDetectorCompat
import com.thuypham.ptithcm.editvideo.R
import java.util.*

class ZoomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = 0
) : AppCompatImageView(context, attrs, defStyleRes), OnScaleGestureListener {

    private var listener: ZoomImageListener? = null
    private var startScaleType: ScaleType? = null

    // These matrices will be used to move and zoom image
    private val imgMatrix = Matrix()
    private var startMatrix = Matrix()
    private val matrixValues = FloatArray(9)
    private var startValues: FloatArray? = null
    private var minScale = MIN_SCALE
    private var maxScale = MAX_SCALE

    //the adjusted scale bounds that account for an image's starting scale values
    private var calculatedMinScale = MIN_SCALE
    private var calculatedMaxScale = MAX_SCALE
    private val bounds = RectF()
    /**
     * Returns whether the image is translatable.
     *
     * @return true if translation of image is allowed, false otherwise
     */
    /**
     * Set the image's translatable state.
     *
     * @param translatable true to enable translation, false to disable it
     */
    var isTranslatable = false
    /**
     * Returns the zoomable state of the image.
     *
     * @return true if pinch-zooming of the image is allowed, false otherwise.
     */
    /**
     * Set the zoomable state of the image.
     *
     * @param zoomable true to enable pinch-zooming of the image, false to disable it
     */
    var isZoomable = false
    /**
     * Gets double tap to zoom state.
     *
     * @return whether double tap to zoom is enabled
     */
    /**
     * Sets double tap to zoom state.
     *
     * @param doubleTapToZoom true if double tap to zoom should be enabled
     */
    var doubleTapToZoom = false
    /**
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     *
     * @return true if image bounds are restricted to the view's edges, false otherwise
     */
    /**
     * Set the restrictBounds status of the image.
     * If restricted bounds are enabled, the image will not be allowed to translate
     * farther inward than the edges of the view's bounds, unless the corresponding
     * dimension (width or height) is smaller than those of the view's frame.
     *
     * @param restrictBounds true if image bounds should be restricted to the view's edges, false otherwise
     */
    var restrictBounds = false
    /**
     * Returns status of animateOnReset. This causes the image to smoothly animate back
     * to its start position when reset. Default value is true.
     *
     * @return true if animateOnReset is enabled, false otherwise
     */
    /**
     * Set whether or not the image should animate when resetting.
     *
     * @param animateOnReset true if image should animate when resetting, false to snap
     */
    var animateOnReset = false
    /**
     * Whether or not the image should automatically center itself when it's dragged partially or
     * fully out of view.
     *
     * @return true if image should center itself automatically, false if it should not
     */
    /**
     * Set whether or not the image should automatically center itself when it's dragged
     * partially or fully out of view.
     *
     * @param autoCenter true if image should center itself automatically, false if it should not
     */
    var autoCenter = false
    private var doubleTapToZoomScaleFactor = 0f
    private val last = PointF(0f, 0f)
    private var startScale = 1f
    private var scaleBy = 1f

    /**
     * Get the current scale factor of the image, in relation to its starting size.
     *
     * @return the current scale factor
     */
    var currentScaleFactor = 1f
        private set
    private var previousPointerCount = 1
    private var currentPointerCount = 0
    private var scaleDetector: ScaleGestureDetector? = null
    private var resetAnimator: ValueAnimator? = null
    private var gestureDetector: GestureDetector? = null
    private var doubleTapDetected = false
    private var singleTapDetected = false

    init {
        val gestureListener: GestureDetector.OnGestureListener =
            object : SimpleOnGestureListener() {
                override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                    if (e.action == MotionEvent.ACTION_UP) {
                        doubleTapDetected = true
                    }
                    singleTapDetected = false
                    return false
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    singleTapDetected = true
                    return false
                }

                override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                    singleTapDetected = false
                    return false
                }

                override fun onDown(e: MotionEvent): Boolean {
                    return true
                }

                override fun onLongPress(e: MotionEvent?) {
                    super.onLongPress(e)
                    listener?.onLongClick()
                }
            }
        scaleDetector = ScaleGestureDetector(context, this@ZoomImageView)
        gestureDetector = GestureDetector(context, gestureListener)
        ScaleGestureDetectorCompat.setQuickScaleEnabled(scaleDetector, false)
        startScaleType = scaleType
        val values = context.obtainStyledAttributes(attrs, R.styleable.ZoomImageView)
        isZoomable = values.getBoolean(R.styleable.ZoomImageView_zoomImage_zoomable, true)
        isTranslatable =
            values.getBoolean(R.styleable.ZoomImageView_zoomImage_translatable, true)
        animateOnReset =
            values.getBoolean(R.styleable.ZoomImageView_zoomImage_animateOnReset, true)
        autoCenter = values.getBoolean(R.styleable.ZoomImageView_zoomImage_autoCenter, true)
        restrictBounds =
            values.getBoolean(R.styleable.ZoomImageView_zoomImage_restrictBounds, false)
        doubleTapToZoom =
            values.getBoolean(R.styleable.ZoomImageView_zoomImage_doubleTapToZoom, true)
        minScale = values.getFloat(R.styleable.ZoomImageView_zoomImage_minScale, MIN_SCALE)
        maxScale = values.getFloat(R.styleable.ZoomImageView_zoomImage_maxScale, MAX_SCALE)
        doubleTapToZoomScaleFactor =
            values.getFloat(R.styleable.ZoomImageView_zoomImage_doubleTapToZoomScaleFactor, 2f)
        values.recycle()
        verifyScaleRange()
    }

    private fun verifyScaleRange() {
        check(minScale < maxScale) { "minScale must be less than maxScale" }
        check(minScale >= 0) { "minScale must be greater than 0" }
        check(maxScale >= 0) { "maxScale must be greater than 0" }
        if (doubleTapToZoomScaleFactor > maxScale) {
            doubleTapToZoomScaleFactor = maxScale
        }
        if (doubleTapToZoomScaleFactor < minScale) {
            doubleTapToZoomScaleFactor = minScale
        }
    }

    /**
     * Set the minimum and maximum allowed scale for zooming. `minScale` cannot
     * be greater than `maxScale` and neither can be 0 or less. This will result
     * in an [IllegalStateException].
     *
     * @param minScale minimum allowed scale
     * @param maxScale maximum allowed scale
     */
    fun setScaleRange(minScale: Float, maxScale: Float) {
        this.minScale = minScale
        this.maxScale = maxScale
        startValues = null
        verifyScaleRange()
    }

    /**
     * Gets the double tap to zoom scale factor.
     *
     * @return double tap to zoom scale factor
     */
    fun getDoubleTapToZoomScaleFactor(): Float {
        return doubleTapToZoomScaleFactor
    }

    /**
     * Sets the double tap to zoom scale factor. Can be a maximum of max scale.
     *
     * @param doubleTapToZoomScaleFactor the scale factor you want to zoom to when double tap occurs
     */
    fun setDoubleTapToZoomScaleFactor(doubleTapToZoomScaleFactor: Float) {
        this.doubleTapToZoomScaleFactor = doubleTapToZoomScaleFactor
        verifyScaleRange()
    }

    fun setImageListener(listener: ZoomImageListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun setScaleType(scaleType: ScaleType?) {
        if (scaleType != null) {
            super.setScaleType(scaleType)
            startScaleType = scaleType
            startValues = null
        }
    }

    /**
     * Set enabled state of the view. Note that this will reset the image's
     * [android.widget.ImageView.ScaleType] to its pre-zoom state.
     *
     * @param enabled enabled state
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) {
            scaleType = startScaleType
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        scaleType = startScaleType
    }

    /**
     * {@inheritDoc}
     */
    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        scaleType = startScaleType
    }

    /**
     * {@inheritDoc}
     */
    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        scaleType = startScaleType
    }

    /**
     * {@inheritDoc}
     */
    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        scaleType = startScaleType
    }

    /**
     * Update the bounds of the displayed image based on the current matrix.
     *
     * @param values the image's current matrix values.
     */
    private fun updateBounds(values: FloatArray) {
        if (drawable != null) {
            bounds[values[Matrix.MTRANS_X],
                    values[Matrix.MTRANS_Y],
                    drawable.intrinsicWidth * values[Matrix.MSCALE_X] + values[Matrix.MTRANS_X]] =
                drawable.intrinsicHeight * values[Matrix.MSCALE_Y] + values[Matrix.MTRANS_Y]
        }
    }

    /**
     * Get the width of the displayed image.
     *
     * @return the current width of the image as displayed (not the width of the [ImageView] itself.
     */
    private val currentDisplayedWidth: Float
        get() = if (drawable != null) drawable.intrinsicWidth * matrixValues[Matrix.MSCALE_X] else 0f

    /**
     * Get the height of the displayed image.
     *
     * @return the current height of the image as displayed (not the height of the [ImageView] itself.
     */
    private val currentDisplayedHeight: Float
        get() = if (drawable != null) drawable.intrinsicHeight * matrixValues[Matrix.MSCALE_Y] else 0f

    /**
     * Remember our starting values so we can animate our image back to its original position.
     */
    private fun setStartValues() {
        startValues = FloatArray(9)
        startMatrix = Matrix(imageMatrix)
        startMatrix.getValues(startValues)
        calculatedMinScale = minScale * startValues!![Matrix.MSCALE_X]
        calculatedMaxScale = maxScale * startValues!![Matrix.MSCALE_X]
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isClickable && isEnabled && (isZoomable || isTranslatable)) {
            if (scaleType != ScaleType.MATRIX) {
                super.setScaleType(ScaleType.MATRIX)
            }

            if (startValues == null) {
                setStartValues()
            }
            currentPointerCount = event.pointerCount

            //get the current state of the image matrix, its values, and the bounds of the drawn bitmap
            imgMatrix.set(imageMatrix)
            imgMatrix.getValues(matrixValues)
            updateBounds(matrixValues)
            scaleDetector?.onTouchEvent(event)
            gestureDetector?.onTouchEvent(event)

            if (doubleTapToZoom && doubleTapDetected) {
                doubleTapDetected = false
                singleTapDetected = false
                if (matrixValues[Matrix.MSCALE_X] != startValues!![Matrix.MSCALE_X]) {
                    reset()
                } else {
                    val zoomMatrix = Matrix(imgMatrix)
                    zoomMatrix.postScale(
                        doubleTapToZoomScaleFactor,
                        doubleTapToZoomScaleFactor,
                        scaleDetector?.focusX ?: 0f,
                        scaleDetector?.focusY ?: 0f
                    )
                    animateScaleAndTranslationToMatrix(zoomMatrix)
                }
                return true
            } else if (!singleTapDetected) {
                /* if the event is a down touch, or if the number of touch points changed,
                 * we should reset our start point, as event origins have likely shifted to a
                 * different part of the screen*/
                if (event.actionMasked == MotionEvent.ACTION_DOWN ||
                    currentPointerCount != previousPointerCount
                ) {
                    last[scaleDetector?.focusX ?: 0f] = scaleDetector?.focusY ?: 0f
                } else if (event.actionMasked == MotionEvent.ACTION_MOVE) {
                    val focusX = scaleDetector!!.focusX
                    val focusY = scaleDetector!!.focusY
                    if (allowTranslate()) {
                        //calculate the distance for translation
                        val xDistance = getXDistance(focusX, last.x)
                        val yDistance = getYDistance(focusY, last.y)
                        imgMatrix.postTranslate(xDistance, yDistance)
                    }
                    if (allowZoom()) {
                        imgMatrix.postScale(scaleBy, scaleBy, focusX, focusY)
                        currentScaleFactor =
                            matrixValues[Matrix.MSCALE_X] / startValues!![Matrix.MSCALE_X]
                    }
                    imageMatrix = imgMatrix
                    last[focusX] = focusY
                }
                if (event.actionMasked == MotionEvent.ACTION_UP ||
                    event.actionMasked == MotionEvent.ACTION_CANCEL
                ) {
                    scaleBy = 1f
                    resetImage()
                }
            }

            if (singleTapDetected && !doubleTapDetected && event.actionMasked == MotionEvent.ACTION_UP)
                listener?.onImageClick()

            parent.requestDisallowInterceptTouchEvent(disallowParentTouch())

            //this tracks whether they have changed the number of fingers down
            previousPointerCount = currentPointerCount
            return super.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }

    private fun disallowParentTouch() =
        currentPointerCount > 1 || currentScaleFactor > 1.0f || isAnimating

    private fun allowTranslate() =
        isTranslatable && currentScaleFactor > 1.0f

    private fun allowZoom() = isZoomable

    private val isAnimating: Boolean
        get() = resetAnimator != null && resetAnimator!!.isRunning

    private fun resetImage() {
        startValues ?: return
        if (matrixValues[Matrix.MSCALE_X] <= startValues!![Matrix.MSCALE_X]) reset() else center()
    }

    /**
     * This helps to keep the image on-screen by animating the translation to the nearest
     * edge, both vertically and horizontally.
     */
    private fun center() {
        if (!autoCenter) return
        animateTranslationX()
        animateTranslationY()
    }
    /**
     * Reset image back to its starting size. If `animate` is false, image
     * will snap back to its original size.
     *
     * @param animate animate the image back to its starting size
     */
    /**
     * Reset image back to its original size. Will snap back to original size
     * if animation on reset is disabled via [.setAnimateOnReset].
     */
    @JvmOverloads
    fun reset(animate: Boolean = animateOnReset) {
        if (animate) {
            animateToStartMatrix()
        } else {
            imageMatrix = startMatrix
        }
    }

    /**
     * Animate the matrix back to its original position after the user stopped interacting with it.
     */
    private fun animateToStartMatrix() {
        animateScaleAndTranslationToMatrix(startMatrix)
    }

    /**
     * Animate the scale and translation of the current matrix to the target
     * matrix.
     *
     * @param targetMatrix the target matrix to animate values to
     */
    private fun animateScaleAndTranslationToMatrix(targetMatrix: Matrix) {
        val targetValues = FloatArray(9)
        targetMatrix.getValues(targetValues)
        val beginMatrix = Matrix(imageMatrix)
        beginMatrix.getValues(matrixValues)

        //difference in current and original values
        val xsDiff = targetValues[Matrix.MSCALE_X] - matrixValues[Matrix.MSCALE_X]
        val ysDiff = targetValues[Matrix.MSCALE_Y] - matrixValues[Matrix.MSCALE_Y]
        val xtDiff = targetValues[Matrix.MTRANS_X] - matrixValues[Matrix.MTRANS_X]
        val ytDiff = targetValues[Matrix.MTRANS_Y] - matrixValues[Matrix.MTRANS_Y]

        resetAnimator = ValueAnimator.ofFloat(0f, 1f)
        resetAnimator?.addUpdateListener(object : AnimatorUpdateListener {
            val activeMatrix = Matrix(imageMatrix)
            val values = FloatArray(9)
            override fun onAnimationUpdate(animation: ValueAnimator) {
                val animValue = animation.animatedValue as Float
                activeMatrix.set(beginMatrix)
                activeMatrix.getValues(values)
                values[Matrix.MTRANS_X] = values[Matrix.MTRANS_X] + xtDiff * animValue
                values[Matrix.MTRANS_Y] = values[Matrix.MTRANS_Y] + ytDiff * animValue
                values[Matrix.MSCALE_X] = values[Matrix.MSCALE_X] + xsDiff * animValue
                values[Matrix.MSCALE_Y] = values[Matrix.MSCALE_Y] + ysDiff * animValue
                activeMatrix.setValues(values)
                imageMatrix = activeMatrix
            }
        })
        resetAnimator?.addListener(object : SimpleAnimatorListener() {
            override fun onAnimationEnd(animation: Animator) {
                imageMatrix = targetMatrix
            }
        })
        resetAnimator?.duration = RESET_DURATION
        resetAnimator?.start()
    }

    private fun animateTranslationX() {
        if (currentDisplayedWidth > width) {
            //the left edge is too far to the interior
            if (bounds.left > 0) {
                animateMatrixIndex(Matrix.MTRANS_X, 0f)
            } else if (bounds.right < width) {
                animateMatrixIndex(Matrix.MTRANS_X, bounds.left + width - bounds.right)
            }
        } else {
            //left edge needs to be pulled in, and should be considered before the right edge
            if (bounds.left < 0) {
                animateMatrixIndex(Matrix.MTRANS_X, 0f)
            } else if (bounds.right > width) {
                animateMatrixIndex(Matrix.MTRANS_X, bounds.left + width - bounds.right)
            }
        }
    }

    private fun animateTranslationY() {
        if (currentDisplayedHeight > height) {
            //the top edge is too far to the interior
            if (bounds.top > 0) {
                animateMatrixIndex(Matrix.MTRANS_Y, 0f)
            } else if (bounds.bottom < height) {
                animateMatrixIndex(Matrix.MTRANS_Y, bounds.top + height - bounds.bottom)
            }
        } else {
            //top needs to be pulled in, and needs to be considered before the bottom edge
            if (bounds.top < 0) {
                animateMatrixIndex(Matrix.MTRANS_Y, 0f)
            } else if (bounds.bottom > height) {
                animateMatrixIndex(Matrix.MTRANS_Y, bounds.top + height - bounds.bottom)
            }
        }
    }

    private fun animateMatrixIndex(index: Int, to: Float) {
        val animator = ValueAnimator.ofFloat(matrixValues[index], to)
        animator.addUpdateListener(object : AnimatorUpdateListener {
            val values = FloatArray(9)
            var current = Matrix()
            override fun onAnimationUpdate(animation: ValueAnimator) {
                current.set(imageMatrix)
                current.getValues(values)
                values[index] = animation.animatedValue as Float
                current.setValues(values)
                imageMatrix = current
            }
        })
        animator.duration = RESET_DURATION
        animator.start()
    }

    /**
     * Get the x distance to translate the current image.
     *
     * @param toX   the current x location of touch focus
     * @param fromX the last x location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private fun getXDistance(toX: Float, fromX: Float): Float {
        var xdistance = toX - fromX
        if (restrictBounds) {
            xdistance = getRestrictedXDistance(xdistance)
        }

        //prevents image from translating an infinite distance offscreen
        if (bounds.right + xdistance < 0) {
            xdistance = -bounds.right
        } else if (bounds.left + xdistance > width) {
            xdistance = width - bounds.left
        }
        return xdistance
    }

    /**
     * Get the horizontal distance to translate the current image, but restrict
     * it to the outer bounds of the [ImageView]. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     *
     * @param xdistance the current desired horizontal distance to translate
     * @return the actual horizontal distance to translate with bounds restrictions
     */
    private fun getRestrictedXDistance(xdistance: Float): Float {
        var restrictedXDistance = xdistance
        if (currentDisplayedWidth >= width) {
            if (bounds.left <= 0 && bounds.left + xdistance > 0 && !scaleDetector!!.isInProgress) {
                restrictedXDistance = -bounds.left
            } else if (bounds.right >= width && bounds.right + xdistance < width && !scaleDetector!!.isInProgress) {
                restrictedXDistance = width - bounds.right
            }
        } else if (!scaleDetector!!.isInProgress) {
            if (bounds.left >= 0 && bounds.left + xdistance < 0) {
                restrictedXDistance = -bounds.left
            } else if (bounds.right <= width && bounds.right + xdistance > width) {
                restrictedXDistance = width - bounds.right
            }
        }
        return restrictedXDistance
    }

    /**
     * Get the y distance to translate the current image.
     *
     * @param toY   the current y location of touch focus
     * @param fromY the last y location of touch focus
     * @return the distance to move the image,
     * will restrict the translation to keep the image on screen.
     */
    private fun getYDistance(toY: Float, fromY: Float): Float {
        var ydistance = toY - fromY
        if (restrictBounds) {
            ydistance = getRestrictedYDistance(ydistance)
        }

        //prevents image from translating an infinite distance offscreen
        if (bounds.bottom + ydistance < 0) {
            ydistance = -bounds.bottom
        } else if (bounds.top + ydistance > height) {
            ydistance = height - bounds.top
        }
        return ydistance
    }

    /**
     * Get the vertical distance to translate the current image, but restrict
     * it to the outer bounds of the [ImageView]. If the current
     * image is smaller than the bounds, keep it within the current bounds.
     * If it is larger, prevent its edges from translating farther inward
     * from the outer edge.
     *
     * @param ydistance the current desired vertical distance to translate
     * @return the actual vertical distance to translate with bounds restrictions
     */
    private fun getRestrictedYDistance(ydistance: Float): Float {
        var restrictedYDistance = ydistance
        if (currentDisplayedHeight >= height) {
            if (bounds.top <= 0 && bounds.top + ydistance > 0 && !scaleDetector!!.isInProgress) {
                restrictedYDistance = -bounds.top
            } else if (bounds.bottom >= height && bounds.bottom + ydistance < height && !scaleDetector!!.isInProgress) {
                restrictedYDistance = height - bounds.bottom
            }
        } else if (!scaleDetector!!.isInProgress) {
            if (bounds.top >= 0 && bounds.top + ydistance < 0) {
                restrictedYDistance = -bounds.top
            } else if (bounds.bottom <= height && bounds.bottom + ydistance > height) {
                restrictedYDistance = height - bounds.bottom
            }
        }
        return restrictedYDistance
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {

        //calculate value we should scale by, ultimately the scale will be startScale*scaleFactor
        scaleBy = startScale * detector.scaleFactor / matrixValues[Matrix.MSCALE_X]

        //what the scaling should end up at after the transformation
        val projectedScale = scaleBy * matrixValues[Matrix.MSCALE_X]

        //clamp to the min/max if it's going over
        if (projectedScale < calculatedMinScale) {
            scaleBy = calculatedMinScale / matrixValues[Matrix.MSCALE_X]
        } else if (projectedScale > calculatedMaxScale) {
            scaleBy = calculatedMaxScale / matrixValues[Matrix.MSCALE_X]
        }
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        startScale = matrixValues[Matrix.MSCALE_X]
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        scaleBy = 1f
    }

    private open inner class SimpleAnimatorListener : Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator) {}
        override fun onAnimationEnd(animation: Animator) {}
        override fun onAnimationCancel(animation: Animator) {}
        override fun onAnimationRepeat(animation: Animator) {}
    }

    interface ZoomImageListener {
        fun onImageClick()
        fun onLongClick()
    }

    companion object {
        private const val RESET_DURATION = 200L
        private const val MIN_SCALE = 1f
        private const val MAX_SCALE = 5f
    }
}