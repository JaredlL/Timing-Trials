package com.android.jared.linden.timingtrials.viewdata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView





class GenericListAdapter<T> internal constructor(val context: Context, val vhFact: GenericViewHolderFactory<T>): RecyclerView.Adapter<BaseHolder<T>>() {



    var mData: List<T> = listOf()
    val layoutInflater = LayoutInflater.from(context)
    var itemLongPress: ((data:Long) -> Unit) = {}
    //var selectable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<T> {
        return vhFact.createViewHolder(layoutInflater, parent)
    }



    override fun onBindViewHolder(holder: BaseHolder<T>, position: Int) {
        mData[position].let { data ->
            with(holder){
                itemView.tag = data
                bind(data)
                this.onLongPress = {id -> itemLongPress.invoke(id)}
            }
        }
    }

    fun setItems(newItems: List<T>){
        mData = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int{ return mData.count() }

}

abstract class BaseHolder<T>(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root){
    var onLongPress: (id:Long) -> Unit ={}
    abstract fun bind(data: T)
}

abstract class GenericBaseHolder<T_DATA: Any, T_BINDING: ViewDataBinding>(val binding: T_BINDING): BaseHolder<T_DATA>(binding)


abstract class GenericViewHolderFactory<T>{
    abstract fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?) : BaseHolder<T>
    abstract fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data:T) : View
    abstract fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?) : View

}