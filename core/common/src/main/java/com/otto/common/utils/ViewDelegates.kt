package com.otto.common.utils

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/**
 * 通过[id]获取View的属性委托
 * 只读属性, 并且具有延迟加载的特性, 非线程安全
 * 只可以在[Activity]中使用, 用于初始化布局文件中的[View]
 *
 */
fun <T : View> Activity.getView(@IdRes id: Int): ReadOnlyProperty<Any?, T> {
    return ActivityViewProperty(this, id)
}

/**
 * 通过[id]获取View的属性委托
 * 只读属性, 并且具有延迟加载的特性, 非线程安全
 * 只可以在[View]中使用
 *
 */
fun <T : View> View.getView(@IdRes id: Int): ReadOnlyProperty<Any?, T> {
    return ViewProperty(this, id)
}

fun <T : View> Fragment.getView(@IdRes id: Int): ReadOnlyProperty<Any?, T> {
    return FragmentViewProperty(this, id)
}

private class ViewProperty<V, T : View>(private val parent: View, private val id: Int) :
    ReadOnlyProperty<V, T> {

    private var view: T? = null

    override fun getValue(thisRef: V, property: KProperty<*>): T {
        return view ?: parent.findViewById<T>(id).also { view = it }
    }
}

private class ActivityViewProperty<V, T : View>(
    private val activity: Activity,
    private val id: Int
) : ReadOnlyProperty<V, T> {

    private var view: T? = null

    override fun getValue(thisRef: V, property: KProperty<*>): T {
        return view ?: activity.findViewById<T>(id).also { view = it }
    }
}

private class FragmentViewProperty<V, T : View>(
    private val fragment: Fragment,
    private val id: Int
) : ReadOnlyProperty<V, T> {

    private var owner: LifecycleOwner? = null

    private var view: T? = null

    init {
        // 直接使用observeForever
        // 因为使用observe的话, 在某些时候由于fragment的状态还没有到达START, 导致无法收到结果
        // 并且这个observer的生命周期和Fragment是一致的, 不会出现内存泄漏, 所以也不需要remove
        fragment.viewLifecycleOwnerLiveData.observeForever {
            if (it == null) {
                // owner 为null表示当前不在onCreateView和onDestroyView之间, view需要置空
                view = null
            }
            owner = it
        }
    }

    override fun getValue(thisRef: V, property: KProperty<*>): T {
        if (owner == null) {
            // owner为null表示fragment当前并不处于onCreateView到onDestroyView之前, 无法获取当前的View
            // 或者onCreateView返回了null的结果
            throw IllegalStateException("value is only accessible between onCreateView and onDestroyView")
        } else {
            return view ?: fragment.requireView().findViewById<T>(id).also { view = it }
        }
    }
}