/*
 *
 *  * Copyright (c) 2018.
 *  * Created by Josua Lengwenath
 *
 */

package com.dertyp7214.appstore.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dertyp7214.appstore.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FragmentBottomSheetUser : BottomSheetDialogFragment() {
    private var mListener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val recyclerView = view as RecyclerView
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        if (arguments != null)
            recyclerView.adapter = ItemAdapter(arguments!!.getInt(ARG_ITEM_COUNT))
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        val parent = parentFragment
        if (parent != null) {
            mListener = parent as Listener?
        } else {
            mListener = context as Listener?
        }
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onItemClicked(position: Int, checked: Boolean)
    }

    private inner class ViewHolder internal constructor(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(inflater.inflate(R.layout.fragment_item_list_dialog_item, parent, false)) {

        internal val text: CheckBox = itemView.findViewById(R.id.text)

        init {
            text.setOnCheckedChangeListener { _, isChecked ->
                if (mListener != null) {
                    mListener!!.onItemClicked(adapterPosition, isChecked)
                    dismiss()
                }
            }
        }
    }

    private inner class ItemAdapter internal constructor(private val mItemCount: Int) : RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.text.text = position.toString()
        }

        override fun getItemCount(): Int {
            return mItemCount
        }
    }

    companion object {

        private const val ARG_ITEM_COUNT = "item_count"

        fun newInstance(itemCount: Int): FragmentBottomSheetUser {
            val fragment = FragmentBottomSheetUser()
            val args = Bundle()
            args.putInt(ARG_ITEM_COUNT, itemCount)
            fragment.arguments = args
            return fragment
        }
    }
}
