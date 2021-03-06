package com.tspoon.kotlist

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ListAdapter
import android.widget.AdapterView
import android.view.View
import android.util.Log
import java.util.HashMap
import java.util.ArrayList
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.database.DataSetObserver
import android.database.DataSetObservable
import com.tspoon.kotlist.KotlistView.RecyclerAdapter
import com.tspoon.kotlist.library.R

class KotlistView(context: Context, attrs: AttributeSet) : AdapterView<RecyclerAdapter>(context, attrs) {
    {
    }

    private val TAG: String = "KotlistView"

    private val mGestureDetector: GestureDetector = GestureDetector(getContext(), GestureListener());
    private var mAdapter: RecyclerAdapter? = null;

    private val mRecycleBin: RecycleBin = RecycleBin()

    var mItemCount: Int = 0;
    var mScrollY: Int = 0;

    var mOffsetY: Int = 0;

    var mLastVisibleItem: Int = 0
    var mFirstVisibleItem: Int = 0

    val mTempRect: Rect = Rect();

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return mGestureDetector.onTouchEvent(event);
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (mAdapter == null)
            return;

        var offset = 0
        if (getChildCount() == 0) {
            mLastVisibleItem = -1
        } else {
            offset = mScrollY + mOffsetY - getChildAt(0).getTop()
        }

        removeOffScreenViews(offset)
        layoutChildren(offset)
        positionChildren()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        mItemCount = mAdapter?.getCount() ?: 0;
    }

    override fun getAdapter(): RecyclerAdapter? {
        return mAdapter
    }

    override fun getSelectedView(): View? {
        throw UnsupportedOperationException()
    }

    override fun setSelection(position: Int) {
        throw UnsupportedOperationException()
    }

    override fun setAdapter(adapter: RecyclerAdapter) {
        mAdapter = adapter
        mItemCount = mAdapter?.getCount() ?: 0;
        requestLayout()
    }

    fun layoutChildren(offset: Int) {
        Log.d(TAG, "layoutChildren - Offset: " + offset);

        // Layout children on bottom
        var lastChildBottom = getChildAt(getChildCount() - 1)?.getBottom() ?: 0
        while (lastChildBottom + offset < getHeight() && mLastVisibleItem < mItemCount - 1) {
            mLastVisibleItem++
            val child = addChildAndMeasure(mLastVisibleItem, false)
            lastChildBottom += child.getMeasuredHeight()
        }

        // Layout children on top
        var firstChildTop = getChildAt(0).getTop()
        while (firstChildTop + offset > 0 && mFirstVisibleItem > 0) {
            mFirstVisibleItem--
            val child = addChildAndMeasure(mFirstVisibleItem, true)
            val childHeight = child.getMeasuredHeight()
            firstChildTop += childHeight

            mOffsetY -= childHeight
        }
    }

    fun positionChildren() {
        var top = mScrollY + mOffsetY;
        val parentWidth = getWidth()

        for (i in 0..getChildCount() - 1) {
            val child = getChildAt(i)

            val width = child.getMeasuredWidth()
            val height = child.getMeasuredHeight()
            val left = (parentWidth - width) / 2

            child.layout(left, top, left + width, top + height)
            top += height
        }
    }

    fun obtainView(position: Int): View {
        val cached: RecyclerView.ViewHolder? = mRecycleBin.get(mAdapter!!.getItemViewType(position))

        if (cached == null) {
            val viewHolder = mAdapter!!.onCreateViewHolder(this, position);
            mAdapter!!.bindViewHolder(viewHolder, position)
            viewHolder.itemView.setTag(R.id.KEY_ADAPTER_TAG, viewHolder);
            return viewHolder.itemView
        } else {
            mAdapter!!.bindViewHolder(cached, position)
            return cached.itemView
        }
    }

    fun addChildAndMeasure(position: Int, above: Boolean): View {
        Log.d(TAG, "addChildAndMeasure. Position: " + position + ", Above: " + above)

        val child = obtainView(position)

        val params = child.getLayoutParams();

        val index = if (above) 0 else -1
        addViewInLayout(child, index, params, true)

        val width = getWidth()
        child.measure(View.MeasureSpec.EXACTLY or width, View.MeasureSpec.UNSPECIFIED)

        return child
    }

    fun removeOffScreenViews(offset: Int) {
        var childCount = getChildCount();

        // We must ensure we always have at least one child

        // Remove views from top
        if (mLastVisibleItem != mItemCount && childCount > 1) {

            var firstChild = getChildAt(0);
            while (firstChild.getBottom() + offset < 0) {

                removeViewInLayout(firstChild);
                childCount--;

                mRecycleBin.put(mAdapter!!.getItemViewType(mFirstVisibleItem), firstChild.getTag(R.id.KEY_ADAPTER_TAG) as RecyclerView.ViewHolder)
                mFirstVisibleItem++;

                // Update list offset
                mOffsetY += firstChild.getMeasuredHeight();

                Log.v(TAG, "Removed view at top. First visible item: " + mFirstVisibleItem);

                if (childCount > 1) {
                    firstChild = getChildAt(0);
                } else {
                    break
                }
            }
        }


        // Remove views from bottom
        if (mFirstVisibleItem != 0 && childCount > 1) {

            var lastChild = getChildAt(childCount - 1);
            while (lastChild.getTop() + offset > getHeight()) {

                removeViewInLayout(lastChild);
                childCount--;

                mRecycleBin.put(mAdapter!!.getItemViewType(mLastVisibleItem), lastChild.getTag(R.id.KEY_ADAPTER_TAG) as RecyclerView.ViewHolder);
                mLastVisibleItem--;

                Log.v(TAG, "Removed view at bottom. Last visible item: " + mLastVisibleItem);

                if (childCount > 1) {
                    lastChild = getChildAt(childCount - 1);
                } else {
                    break
                }
            }
        }
    }

    fun scrollBy(distanceY: Int) {
        mScrollY += distanceY

        // Don't allow over-scroll or under-scroll
        if (mScrollY > 0) {
            mScrollY = 0
            return
        } else if (mLastVisibleItem == mItemCount - 1 && distanceY < 0) {
            val lastChild = getChildAt(getChildCount() - 1)
            if (lastChild.getBottom() < getHeight()) {
                mScrollY -= distanceY
                return
            }
        }

        // Only call if actually updated
        requestLayout()
    }

    private fun getIndexForClick(x: Int, y: Int): Int {
        for (i in 0..getChildCount() - 1) {
            getChildAt(i).getHitRect(mTempRect)
            if (mTempRect.contains(x, y)) {
                return i
            }
        }
        return AdapterView.INVALID_POSITION
    }

    private fun performClickAt(x: Int, y: Int) {
        val index = getIndexForClick(x, y)
        if (index != AdapterView.INVALID_POSITION) {
            val position = index + mFirstVisibleItem
            val view = getChildAt(index)
            val id = mAdapter!!.getItemId(position)
            performItemClick(view, position, id)
        }
    }

    private fun performLongClickAt(x: Int, y: Int) {
        val listener = getOnItemLongClickListener()
        if (listener == null) {
            return
        }

        val index = getIndexForClick(x, y)
        if (index != AdapterView.INVALID_POSITION) {
            val position = index + mFirstVisibleItem
            val view = getChildAt(index)
            val id = mAdapter!!.getItemId(position)
            listener.onItemLongClick(this, view, position, id)
        }
    }


    // Helper for detecting gestures e.g. scroll, click, long click
    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            scrollBy(-distanceY.toInt())
            return true
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            if (e != null) {
                performClickAt(e.getX().toInt(), e.getY().toInt())
            }
            return true
        }

        override fun onLongPress(e: MotionEvent?) {
            if (e != null && isLongClickable()) {
                performLongClickAt(e.getX().toInt(), e.getY().toInt())
            }
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }

    /*
     * Holds lists of our recycled views, put into buckets by view type
     */
    inner class RecycleBin {
        private val mItems: HashMap<Int, MutableList<RecyclerView.ViewHolder>> = HashMap()

        fun put(viewType: Int, view: RecyclerView.ViewHolder) {
            var views = mItems.getOrElse(viewType, { ArrayList<RecyclerView.ViewHolder>() });
            views.add(view)
            mItems.put(viewType, views);
        }

        fun get(viewType: Int): RecyclerView.ViewHolder? {
            var views = mItems.getOrElse(viewType, { ArrayList<RecyclerView.ViewHolder>() });
            if (views.size() > 0) {
                val holder = views.remove(0)
                Log.v(TAG, "Cache hit for type: " + viewType)
                return holder
            } else {
                Log.v(TAG, "Cache miss for type: " + viewType)
                return null;
            }
        }

    }

    public abstract class RecyclerAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ListAdapter {

        private val mDataSetObservable = DataSetObservable()

        override fun registerDataSetObserver(observer: DataSetObserver?) {
            mDataSetObservable.registerObserver(observer)
        }

        override fun unregisterDataSetObserver(observer: DataSetObserver?) {
            mDataSetObservable.unregisterObserver(observer)
        }

        override fun getCount(): Int {
            return getItemCount()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            throw UnsupportedOperationException()
        }

        override fun getViewTypeCount(): Int {
            return 0
        }

        override fun isEmpty(): Boolean {
            return getItemCount() == 0
        }

        override fun areAllItemsEnabled(): Boolean {
            return true
        }

        override fun isEnabled(position: Int): Boolean {
            return true
        }

        abstract override fun onCreateViewHolder(p0: ViewGroup?, p1: Int): RecyclerView.ViewHolder
    }

}