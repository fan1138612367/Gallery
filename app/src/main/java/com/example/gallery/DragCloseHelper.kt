package com.example.gallery

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.window.layout.WindowMetricsCalculator
import kotlin.math.abs

class DragCloseHelper(
    private val mContext: AppCompatActivity,
    private val maxExitY: Int = 500,                        //设置最大退出距离
    private val minScale: Float = 0.4f,                     //设置最小缩放尺寸
    private val parentV: View,                              //拖拽关闭的view
    private val childV: View
) {
    /**
     * 是否在滑动关闭中，手指还在触摸中
     */
    private var isSwipingToClose = false

    /**
     * 上次触摸坐标
     */
    private var mLastY = -1f
    private var mLastRawY = -1f
    private var mLastX = -1f
    private var mLastRawX = -1f

    /**
     * 上次触摸手指id
     */
    private var lastPointerId = 0

    /**
     * 当前位移距离
     */
    private var mCurrentTranslationY = 0f
    private var mCurrentTranslationX = 0f

    /**
     * 上次位移距离
     */
    private var mLastTranslationY = 0f
    private var mLastTranslationX = 0f

    /**
     * 正在恢复原位中
     */
    private var isResettingAnimate = false

    private var dragCloseListener: DragCloseListener? = null
    private var clickListener: ClickListener? = null

    /**
     * 按的状态
     */
    private var isPress = false
    private var longClickPerform = false
    private var longClickRunnable: LongClickRunnable? = null

    fun setDragCloseListener(dragCloseListener: DragCloseListener?) {
        this.dragCloseListener = dragCloseListener
    }

    fun setClickListener(clickListener: ClickListener?) {
        this.clickListener = clickListener
    }

    /**
     * 开始对长按事件计时
     */
    private fun checkForLongClick() {
        longClickPerform = false
        if (longClickRunnable == null) {
            longClickRunnable = LongClickRunnable()
        }
        parentV.postDelayed(longClickRunnable, ViewConfiguration.getLongPressTimeout().toLong())
    }

    /**
     * 重置点击事件
     */
    private fun resetClickEvent() {
        parentV.removeCallbacks(longClickRunnable)
        isPress = false
        longClickPerform = false
    }

    /**
     * 处理点击事件
     */
    private fun dealClickEvent() {
        if (isPress) {
            if (!longClickPerform) {
                //长按没有处理，开始执行单击
                parentV.removeCallbacks(longClickRunnable)
                clickListener?.onClick(childV, false)
            }
            //结束了按的状态
            isPress = false
        }
    }

    /**
     * 处理touch事件
     *
     * @param event
     * @return
     */
    fun handleEvent(event: MotionEvent): Boolean {
        //不拦截
        when {
            event.pointerCount > 1 -> {
                //如果有多个手指
                if (isSwipingToClose) {
                    //已经开始滑动关闭，恢复原状，否则需要派发事件
                    isSwipingToClose = false
                    resetCallBackAnimation()
                    return true
                }
                reset()
                resetClickEvent()
                return false
            }
            dragCloseListener?.intercept() == true -> {
                //被接口中的方法拦截，但是如果设置了点击事件，将继续执行点击逻辑
                if (clickListener != null) {
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            isPress = true
                            //开始延迟
                            checkForLongClick()
                        }
                        MotionEvent.ACTION_UP -> {
                            //处理事件
                            dealClickEvent()
                        }
                        MotionEvent.ACTION_CANCEL -> {
                            //取消事件
                            resetClickEvent()
                        }
                    }
                }
                isSwipingToClose = false
                return false
            }
            else -> when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    reset()
                    mLastY = event.y
                    mLastX = event.x
                    mLastRawY = event.rawY
                    mLastRawX = event.rawX
                    if (isInvalidTouch) {
                        //触摸点在状态栏的区域 或者 是无效触摸区域，则需要拦截
                        return true
                    }
                    //开始按
                    isPress = true
                    //开始延迟
                    checkForLongClick()
                    //初始化数据
                    lastPointerId = event.getPointerId(0)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (isInvalidTouch || mLastRawY == -1f) {
                        //无效触摸区域，则需要拦截
                        return true
                    }
                    if (mLastRawY == -1f) {
                        //解决触摸底部，部分有虚拟导航栏的手机会出现先move后down的问题，因此up和cancel的时候需要重置为-1
                        return true
                    }
                    if (lastPointerId != event.getPointerId(0)) {
                        //手指不一致，恢复原状
                        if (isSwipingToClose) {
                            resetCallBackAnimation()
                        }
                        reset()
                        return true
                    }
                    val currentY = event.y
                    val currentX = event.x
                    if (isSwipingToClose ||
                        abs(currentY - mLastY) > 2 * ViewConfiguration.get(mContext).scaledTouchSlop &&
                        abs(currentY - mLastY) > abs(currentX - mLastX) * 1.5
                    ) {
                        //已经触发或者开始触发，更新view
                        mLastY = currentY
                        mLastX = currentX

                        //一旦移动，按则取消
                        resetClickEvent()
                        val currentRawY = event.rawY
                        val currentRawX = event.rawX
                        if (!isSwipingToClose) {
                            //准备开始
                            isSwipingToClose = true
                            dragCloseListener?.dragStart()
                        }
                        //已经开始，更新view
                        mCurrentTranslationY = currentRawY - mLastRawY + mLastTranslationY
                        mCurrentTranslationX = currentRawX - mLastRawX + mLastTranslationX
                        var percent = 1 - abs(mCurrentTranslationY / childV.height)
                        if (percent > 1) {
                            percent = 1f
                        } else if (percent < 0) {
                            percent = 0f
                        }
                        parentV.background.mutate().alpha = (percent * 255).toInt()
                        dragCloseListener?.dragging(percent)
                        if (percent < minScale) {
                            percent = minScale
                        }
                        if (mCurrentTranslationY > 0) {
                            childV.translationY =
                                mCurrentTranslationY - (childV.height - maxExitY) * (1 - percent) / 2
                        } else {
                            childV.translationY =
                                mCurrentTranslationY + (childV.height - maxExitY) * (1 - percent) / 2
                        }
                        childV.translationX = mCurrentTranslationX
                        childV.scaleX = percent
                        childV.scaleY = percent
                        return true
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (isInvalidTouch) {
                        //无效触摸区域，则需要拦截
                        return true
                    }
                    mLastRawY = -1f
                    //处理点击事件
                    dealClickEvent()
                    //手指抬起事件
                    if (isSwipingToClose) {
                        if (mCurrentTranslationY > maxExitY) {
                            //会执行定制的离开动画
                            exitWithTranslation(mCurrentTranslationY)
                        } else {
                            resetCallBackAnimation()
                        }
                        isSwipingToClose = false
                        return true
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    //取消事件
                    resetClickEvent()
                    mLastRawY = -1f
                    if (isSwipingToClose) {
                        resetCallBackAnimation()
                        isSwipingToClose = false
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 退出动画
     *
     * @param currentY
     */
    private fun exitWithTranslation(currentY: Float) {
        val targetValue = if (currentY > 0) childV.height else -childV.height
        val anim = ValueAnimator.ofFloat(mCurrentTranslationY, targetValue.toFloat() / 2)
        anim.addUpdateListener { animation ->
            updateChildView(
                mCurrentTranslationX,
                animation.animatedValue as Float
            )
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                dragCloseListener?.dragClose(false)
                mContext.supportFinishAfterTransition()
                mContext.overridePendingTransition(R.anim.image_enter, R.anim.image_exit)
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        anim.setDuration(100).start()
    }

    /**
     * 重置数据
     */
    private fun reset() {
        isSwipingToClose = false
        mLastY = -1f
        mLastX = -1f
        mLastRawY = -1f
        mLastRawX = -1f
        mLastTranslationY = 0f
        mLastTranslationX = 0f
    }

    /**
     * 更新缩放的view
     */
    private fun updateChildView(transX: Float, transY: Float) {
        val percent = abs(transY / childV.height)
        var scale = 1 - percent
        if (scale < minScale) {
            scale = minScale
        }
        if (transY > 0) {
            childV.translationY = transY - (childV.height - maxExitY) * (1 - scale) / 2
        } else {
            childV.translationY = transY + (childV.height - maxExitY) * (1 - scale) / 2
        }
        childV.translationX = transX
        childV.scaleX = scale
        childV.scaleY = scale
    }

    /**
     * 恢复到原位动画
     */
    private fun resetCallBackAnimation() {
        if (isResettingAnimate || mCurrentTranslationY == 0f) {
            return
        }
        val ratio = mCurrentTranslationX / mCurrentTranslationY
        val animatorY = ValueAnimator.ofFloat(mCurrentTranslationY, 0F)
        animatorY.addUpdateListener { valueAnimator ->
            if (isResettingAnimate) {
                mCurrentTranslationY = valueAnimator.animatedValue as Float
                mCurrentTranslationX = ratio * mCurrentTranslationY
                mLastTranslationY = mCurrentTranslationY
                mLastTranslationX = mCurrentTranslationX
                updateChildView(mLastTranslationX, mCurrentTranslationY)
            }
        }
        animatorY.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                isResettingAnimate = true
            }

            override fun onAnimationEnd(animation: Animator) {
                if (isResettingAnimate) {
                    parentV.background.mutate().alpha = 255
                    mCurrentTranslationY = 0f
                    mCurrentTranslationX = 0f
                    isResettingAnimate = false
                    dragCloseListener?.dragCancel()
                }
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animatorY.setDuration(100).start()
    }

    /**
     * 处理长按事件
     */
    private inner class LongClickRunnable : Runnable {
        override fun run() {
            if (isPress) {
                clickListener?.onClick(childV, true)
                longClickPerform = true
            }
        }
    }

    interface DragCloseListener {
        /**
         * 是否有拦截
         *
         * @return
         */
        fun intercept(): Boolean

        /**
         * 开始拖拽
         */
        fun dragStart()

        /**
         * 拖拽中
         *
         * @param percent
         */
        fun dragging(percent: Float)

        /**
         * 取消拖拽
         */
        fun dragCancel()

        /**
         * 拖拽结束并且关闭
         *
         * @param isShareElementMode
         */
        fun dragClose(isShareElementMode: Boolean)
    }

    interface ClickListener {
        /**
         * 点击事件
         */
        fun onClick(view: View, isLongClick: Boolean)
    }

    /**
     * 是否有效点击，如果点击到了状态栏区域 或者 虚拟导航栏区域，则无效
     */
    private val isInvalidTouch: Boolean
        get() = mLastRawY < getStatusBarHeight(mContext) ||
                mLastRawY > getDpi(mContext) - getNavigationBarHeight(mContext)

    /**
     * 获取状态栏的高度
     */
    private fun getStatusBarHeight(context: Context): Int {
        val res = context.resources
        val resourceId = res.getIdentifier("status_bar_height", "dimen", "android")
        return res.getDimensionPixelSize(resourceId)
    }

    /**
     * 获取导航栏的高度
     */
    private fun getNavigationBarHeight(context: Context): Int {
        val res = context.resources
        val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return res.getDimensionPixelSize(resourceId)
    }

    /**
     * 获取屏幕原始尺寸高度，包括虚拟功能键高度
     */
    private fun getDpi(context: AppCompatActivity): Int {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        println(
            WindowMetricsCalculator.getOrCreate()
                .computeCurrentWindowMetrics(context).bounds.height()
        )
        windowManager.defaultDisplay.getRealMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }
}