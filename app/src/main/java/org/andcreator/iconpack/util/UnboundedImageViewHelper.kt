package org.andcreator.iconpack.util

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_POINTER_DOWN
import android.view.MotionEvent.ACTION_POINTER_UP
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

/**
 * @author  yd
 * @date  2020/3/9 23:02
 */
class UnboundedImageViewHelper private constructor(private val imageView: ImageView): View.OnTouchListener,
    ScaleGestureDetector.OnScaleGestureListener{

    private lateinit var suppMatrix: Matrix

    /**
     * 初始矩阵(单位矩阵)
     */
    private val baseMatrix = Matrix()

    /**
     * 结果矩阵
     */
    private val drawableMatrix = Matrix()

    /**
     * 矩阵的位置Rect
     */
    private val displayRect = RectF()

    /**
     * View的高宽度
     */
    private var viewWidth = 0

    /**
     * View的高度
     */
    private var viewHeight = 0

    /**
     * 另一个点
     */
    private val offset = PointF(0F, 0F)

    /**
     * 一个点
     */
    private val downTouch = PointF()

    private val recordPointF = PointF()

    // 连击次数
    private var pointSize = 0
    // 按下时间，以此来判断区分点击和长按
    private var touchDownTime = 0L
    // 最大波动范围（手指抖动范围，规避滑动行为）
    private var maxFluctuation = -1
    // 按下位置
    private val touchDownPoint = PointF()
    // 是否激活本次点击
    private var active = false
    // 单次点击允许的最长手指按下时间
    private var maxKeepTime = 300L
    // 连击允许的超时时间
    private var continuouslyKeepTime = 50L
    // 点击事件任务
    private val clickTask = Runnable {
        callOnClick()
    }

    /**
     * 触摸点ID
     */
    private var pointerId = 0

    companion object {
        @SuppressLint("ClickableViewAccessibility")
        fun with(view: ImageView): UnboundedImageViewHelper {
            val helper = UnboundedImageViewHelper(view)
            view.setOnTouchListener(helper)
            return helper
        }
    }

    init {
        imageView.post {
            viewWidth = imageView.width - imageView.paddingLeft - imageView.paddingEnd
            viewHeight = imageView.height - imageView.paddingTop - imageView.paddingBottom
            suppMatrix = Matrix(imageView.imageMatrix)
            imageView.scaleType = ImageView.ScaleType.MATRIX
            imageView.imageMatrix = getDrawMatrix()
//            notifyImageChange(1F, 0f, 0f)
//            centerCrop()
        }
    }

    private val scaleGestureDetector = ScaleGestureDetector(imageView.context, this)

    /**
     * 设置结果矩阵
     */
    private fun notifyImageChange(scaleFactor: Float, focusX: Float, focusY: Float) {
        if (checkMatrixBounds(scaleFactor, focusX, focusY)) {
            imageView.scaleType = ImageView.ScaleType.MATRIX
            imageView.imageMatrix = getDrawMatrix()
        }
    }

    private fun centerCrop() : Boolean {

        val rect = getDisplayRect(getDrawMatrix()) ?: return false
        val height = rect.height()
        val width = rect.width()

        val moveX = (width.coerceAtMost(viewWidth.toFloat()) - width.coerceAtLeast(viewWidth.toFloat())) / 2
        val moveY = (height.coerceAtMost(viewHeight.toFloat()) - height.coerceAtLeast(viewHeight.toFloat())) / 2
        Log.e("scaleFactor", "moveX: $moveX viewWidth: $viewWidth width2: $width moveY $moveY")
        suppMatrix.postTranslate(moveX, moveY)

        val scaleFactor: Float
        scaleFactor = if ((viewWidth - width) > (viewHeight - height)) {
            Log.e("scaleFactor", "width")
            if (viewWidth - width < 0) {
                viewWidth / width
            } else {
                viewWidth / width
            }
        } else {
            Log.e("scaleFactor", "height")
            if (viewHeight - height < 0) {
                viewHeight / height
            } else {
                viewHeight / height
            }
        }

        Log.e("scaleFactor", "$scaleFactor")
        suppMatrix.postScale(scaleFactor, scaleFactor, viewWidth / 2F, viewHeight / 2F)
        imageView.scaleType = ImageView.ScaleType.MATRIX
        imageView.imageMatrix = getDrawMatrix()

        imageView.visibility = View.VISIBLE

        return true
    }

    /**
     * 检擦边界
     */
    private fun checkMatrixBounds(scaleFactor: Float, focusX: Float, focusY: Float): Boolean {
        // Matrix的位置值，默认左上角是0，0
        val rect = getDisplayRect(getDrawMatrix()) ?: return false
        val height = rect.height()
        val width = rect.width()

        var moveX = offset.x - recordPointF.x
        var moveY = offset.y - recordPointF.y

        if (rect.top + moveY > 0 ) {
            moveY = 0 - rect.top
        }else if (rect.bottom + moveY < viewHeight) {
            moveY = viewHeight - rect.bottom
        }
        if (rect.left + moveX > 0){
            moveX = 0 - rect.left
        }else if (rect.right + moveX < viewWidth){
            moveX = viewWidth - rect.right
        }

        suppMatrix.postTranslate(moveX, moveY)
        recordPointF.set(offset.x, offset.y)

        // 缩小
        if (scaleFactor < 1F) {
            // 如果两个对边都已经到达View边界，那么不执行缩小
            if (rect.top == 0F && rect.bottom == viewHeight.toFloat() || rect.left == 0F && rect.right == viewWidth.toFloat()) return true

            // 是否可以缩小
            if (width * scaleFactor > viewWidth && height * scaleFactor > viewHeight) {
                suppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)

                imageView.scaleType = ImageView.ScaleType.MATRIX
                imageView.imageMatrix = getDrawMatrix()
                notifyImageChange(1F, 0F, 0F)
                return false
            }

        }else if (scaleFactor > 1F) {
            suppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY)
        }
        return true
    }

    /**
     * 获取Matrix的位置值
     */
    private fun getDisplayRect(matrix: Matrix): RectF? {
        return imageView.drawable?.let { drawable ->
            // 初始displayRect
            displayRect.set(0F, 0F, drawable.intrinsicWidth.toFloat(), drawable.intrinsicHeight.toFloat())
            // 映射矩阵的位置Rect
            matrix.mapRect(displayRect)
            displayRect
        }
    }

    /**
     * 设置结果矩阵并返回
     */
    private fun getDrawMatrix(): Matrix {
        drawableMatrix.set(baseMatrix)
        drawableMatrix.postConcat(suppMatrix)
        return drawableMatrix
    }

    private var pointerUp = false

    private val clickListenerList = ArrayList<ClickListener>()

    private fun callOnClick() {
        if (pointSize < 1) {
            return
        }
        clickListenerList.forEach {
            it.onClick(imageView, pointSize)
        }
        // 事件被消耗，清空
        reset()
    }

    /**
     * 点击成功
     */
    private fun clickSuccessful() {
        active = false
        pointSize++
        imageView.removeCallbacks(clickTask)
//        imageView.postDelayed(clickTask, maxKeepTime + continuouslyKeepTime)
        imageView.post(clickTask)
    }

    /**
     * 重置
     */
    private fun reset() {
        pointSize = 0
        imageView.removeCallbacks(clickTask)
        touchDownTime = 0L
        touchDownPoint.set(0F, 0F)
        active = false
    }

    fun addClickListener(listener: ClickListener) {
        clickListenerList.add(listener)
    }

    interface ClickListener {
        fun onClick(view: View, count: Int)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        event?:return false
        v?:return false
        if (v.parent is ViewGroup){
            v.parent.requestDisallowInterceptTouchEvent(true)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downTouch.set(event.xById(), event.yById())
                // 按下，记录信息
                touchDownTime = System.currentTimeMillis()
                touchDownPoint.set(event.x, event.y)
                active = true
            }
            MotionEvent.ACTION_MOVE -> {

                if (pointerUp){
                    downTouch.set(event.xById(), event.yById())
                    pointerUp =! pointerUp
                }
                val x = event.xById()
                val y = event.yById()
                logger("x:$x y:$y")
                offset.x += x - downTouch.x
                offset.y += y - downTouch.y
                downTouch.set(x, y)
                notifyImageChange(1F, 0F, 0F)

                // 规避滑动
                if (maxFluctuation > 0 && (kotlin.math.abs(x - touchDownPoint.x) > maxFluctuation ||
                            kotlin.math.abs(y - touchDownPoint.y) > maxFluctuation)) {
                    reset()
                }
                if (x < 0 || y < 9 || x > viewWidth || y > viewHeight) {
                    reset()
                }
                // 发生超时，提前清理任务
                val now = System.currentTimeMillis()
                if (now - touchDownTime > maxKeepTime) {
                    reset()
                }
            }
            MotionEvent.ACTION_UP -> {
                val now = System.currentTimeMillis()
                if (now - touchDownTime > maxKeepTime) {
                    reset()
                } else {
                    clickSuccessful()
                }
            }
            ACTION_POINTER_UP -> {
                pointerUp = true
                recordPointF.set(0F, 0F)
                offset.set(0F, 0F)
                logger("触摸点松开")

            }
            MotionEvent.ACTION_CANCEL -> {
                // 触发取消时间，放弃本轮所有计数
                reset()
                return false
            }
            ACTION_POINTER_DOWN -> {
                // 多个指头，放弃事件
                reset()
                return false
            }
        }
        return scaleGestureDetector.onTouchEvent(event)
    }

    private fun MotionEvent.checkPointId() {
        val pointerIndex = this.findPointerIndex(pointerId)
        if (pointerIndex < 0) {
            pointerId = this.getPointerId(0)
        }
    }

    private fun MotionEvent.xById(): Float {
        checkPointId()
        return this.getX(this.findPointerIndex(pointerId))
    }

    private fun MotionEvent.yById(): Float {
        checkPointId()
        return getY(this.findPointerIndex(pointerId))
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        detector?: return false
        val scaleFactor = detector.scaleFactor
        //中心点
        val focusX =  detector.focusX
        val focusY = detector.focusY
        logger("scaleFactor: $scaleFactor focusX: $focusX  focusY: $focusY")

        if (java.lang.Float.isNaN(scaleFactor) || java.lang.Float.isInfinite(scaleFactor)) return false

        notifyImageChange(scaleFactor, focusX, focusY)
        return true
    }

}