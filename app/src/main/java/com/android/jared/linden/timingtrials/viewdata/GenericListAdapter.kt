package com.android.jared.linden.timingtrials.viewdata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


//class ViewCourseListAdapter internal constructor(val context: Context): RecyclerView.Adapter<ViewCourseListAdapter.CourseListViewHolder>() {
//
//
//    inner class CourseListViewHolder(binding: ListItemCourseBinding): RecyclerView.ViewHolder(binding.root) {
//        private val _binding = binding
//
//        var longPress = {(rider):Course -> Unit}
//
//        fun bind(vm: CourseListViewWrapper){
//
//            _binding.apply{
//                courseVm = vm
//                courseLayout.setOnLongClickListener { longPress(vm.course)
//                    true
//                }
//
//                executePendingBindings()
//            }
//        }
//    }
//
//    var mData: List<CourseListViewWrapper> = listOf()
//    val layoutInflater = LayoutInflater.from(context)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseListViewHolder {
//
//        val binding = ListItemCourseBinding.inflate(layoutInflater, parent, false)
//        return CourseListViewHolder(binding)
//
//    }
//
//    override fun onBindViewHolder(holder: CourseListViewHolder, position: Int) {
//        mData.get(position).let { data ->
//            with(holder){
//                itemView.tag = data
//                holder.longPress = editRider
//                bind(data)
//            }
//        }
//    }
//
//    var editRider = {(rider):Course -> Unit}
//
//    fun setRiders(newData: List<CourseListViewWrapper>){
//        mData = newData
//        notifyDataSetChanged()
//    }
//
//    override fun getItemCount(): Int{ return mData.count() }
//
//}
//
//class RiderListAdapter internal constructor(val context: Context): RecyclerView.Adapter<RiderListAdapter.RiderViewHolder>() {
//
//
//    inner class RiderViewHolder(binding: ListItemRiderBinding): RecyclerView.ViewHolder(binding.root) {
//        private val _binding = binding
//
//        var longPress = {(rider):Rider -> Unit}
//
//        fun bind(riderVm: Rider){
//
//            _binding.apply{
//                rider = riderVm
//                riderLayout.setOnLongClickListener { longPress(riderVm)
//                    true
//                }
//
//                executePendingBindings()
//            }
//        }
//    }
//
//    var mRiders: List<Rider> = listOf()
//    val layoutInflater = LayoutInflater.from(context)
//    var selectable: Boolean = false
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RiderViewHolder {
//
//        val binding = DataBindingUtil.inflate<ListItemRiderBinding>(layoutInflater, R.layout.list_item_rider, parent, false)
//        return RiderViewHolder(binding)
//
//    }
//
//    override fun onBindViewHolder(holder: RiderViewHolder, position: Int) {
//        mRiders.get(position).let { rider ->
//            with(holder){
//                itemView.tag = rider
//                holder.longPress = editRider
//                bind(rider)
//            }
//        }
//    }
//
//    var editRider = {(rider):Rider -> Unit}
//
//    fun setRiders(newRiders: List<Rider>){
//        mRiders = newRiders
//        notifyDataSetChanged()
//    }
//
//    override fun getItemCount(): Int{ return mRiders.count() }
//
//}

class GenericListAdapter<T> internal constructor(val context: Context, val vhFact: GenericViewHolderFactory<T>): RecyclerView.Adapter<BaseHolder<T>>() {



    var mData: List<T> = listOf()
    val layoutInflater = LayoutInflater.from(context)
    var selectable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<T> {
        return vhFact.createViewHolder(layoutInflater, parent)
    }



    override fun onBindViewHolder(holder: BaseHolder<T>, position: Int) {
        mData.get(position).let { data ->
            with(holder){
                itemView.tag = data
                bind(data)
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
    abstract fun bind(data: T)
}

abstract class GenericBaseHolder<T_DATA: Any, T_BINDING: ViewDataBinding>(val binding: T_BINDING): BaseHolder<T_DATA>(binding)


abstract class GenericViewHolderFactory<T>{
    abstract fun createViewHolder(layoutInflator: LayoutInflater, parent: ViewGroup?) : BaseHolder<T>
    abstract fun createView(layoutInflator: LayoutInflater, parent: ViewGroup?, data:T) : View
    abstract fun createTitle(layoutInflator: LayoutInflater, parent: ViewGroup?) : View
}