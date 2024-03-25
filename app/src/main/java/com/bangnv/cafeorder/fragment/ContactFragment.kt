package com.bangnv.cafeorder.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.MainActivity
import com.bangnv.cafeorder.adapter.ContactAdapter
import com.bangnv.cafeorder.adapter.ContactAdapter.ICallPhone
import com.bangnv.cafeorder.constant.GlobalFunction.callPhoneNumber
import com.bangnv.cafeorder.databinding.FragmentContactBinding
import com.bangnv.cafeorder.model.Contact
import java.util.*

class ContactFragment : BaseFragment() {

    private var mContactAdapter: ContactAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val mFragmentContactBinding = FragmentContactBinding.inflate(inflater, container, false)
        mContactAdapter = ContactAdapter(activity, getListContact(), object : ICallPhone {
            override fun onClickCallPhone() {
                callPhoneNumber(activity!!)
            }
        })
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
//        val layoutManager = GridLayoutManager(activity, 2)
        mFragmentContactBinding.rcvData.isNestedScrollingEnabled = false
        mFragmentContactBinding.rcvData.isFocusable = false
        mFragmentContactBinding.rcvData.layoutManager = layoutManager
        mFragmentContactBinding.rcvData.adapter = mContactAdapter
        return mFragmentContactBinding.root
    }

    fun getListContact(): List<Contact> {
        val contactArrayList: MutableList<Contact> = ArrayList()
        contactArrayList.add(Contact(Contact.HOTLINE, R.drawable.ic_hotline))
        contactArrayList.add(Contact(Contact.ZALO, R.drawable.ic_zalo))
        contactArrayList.add(Contact(Contact.GMAIL, R.drawable.ic_gmail))
        contactArrayList.add(Contact(Contact.FACEBOOK, R.drawable.ic_facebook))
        contactArrayList.add(Contact(Contact.YOUTUBE, R.drawable.ic_youtube))
        contactArrayList.add(Contact(Contact.SKYPE, R.drawable.ic_skype))
        return contactArrayList
    }

    override fun onDestroy() {
        super.onDestroy()
        mContactAdapter!!.release()
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(false, getString(R.string.contact))
        }
    }
}