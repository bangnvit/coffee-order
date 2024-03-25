package com.bangnv.cafeorder.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.AdminAddFoodActivity
import com.bangnv.cafeorder.activity.AdminMainActivity
import com.bangnv.cafeorder.adapter.AdminFoodAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction.getTextSearch
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentAdminHomeBinding
import com.bangnv.cafeorder.fragment.BaseFragment
import com.bangnv.cafeorder.listener.IOnManagerFoodListener
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import java.util.*

class AdminHomeFragment : BaseFragment() {

    private var mFragmentAdminHomeBinding: FragmentAdminHomeBinding? = null
    private var mListFood: MutableList<Food>? = null
    private var mAdminFoodAdapter: AdminFoodAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminHomeBinding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        initView()
        initListener()
        getListFood("")
        return mFragmentAdminHomeBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as AdminMainActivity?)!!.setToolBar(getString(R.string.home))
        }
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminHomeBinding!!.rcvFood.layoutManager = linearLayoutManager
        mListFood = ArrayList()
        mAdminFoodAdapter = AdminFoodAdapter(mListFood, object : IOnManagerFoodListener {
            override fun onClickUpdateFood(food: Food?) {
                onClickEditFood(food)
            }

            override fun onClickDeleteFood(food: Food?) {
                deleteFoodItem(food)
            }
        })
        mFragmentAdminHomeBinding!!.rcvFood.adapter = mAdminFoodAdapter
    }

    private fun initListener() {
        mFragmentAdminHomeBinding!!.btnAddFood.setOnClickListener { onClickAddFood() }
        mFragmentAdminHomeBinding!!.imgSearch.setOnClickListener { searchFood() }
        mFragmentAdminHomeBinding!!.edtSearchName.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood()
                return@setOnEditorActionListener true
            }
            false
        }
        mFragmentAdminHomeBinding!!.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val strKey = s.toString().trim { it <= ' ' }
                if (strKey == "" || strKey.isEmpty()) {
                    searchFood()
                }
            }
        })
    }

    private fun onClickAddFood() {
        startActivity(requireActivity(), AdminAddFoodActivity::class.java)
    }

    private fun onClickEditFood(food: Food?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, food)
        startActivity(requireActivity(), AdminAddFoodActivity::class.java, bundle)
    }

    private fun deleteFoodItem(food: Food?) {
        AlertDialog.Builder(activity)
                .setTitle(getString(R.string.msg_delete_title))
                .setMessage(getString(R.string.msg_confirm_delete))
                .setPositiveButton(getString(R.string.action_ok)) { _: DialogInterface?, _: Int ->
                    if (activity == null) {
                        return@setPositiveButton
                    }
                    ControllerApplication[requireActivity()].foodDatabaseReference
                            .child(food!!.id.toString()).removeValue { _: DatabaseError?, _: DatabaseReference? ->
                                Toast.makeText(activity,
                                        getString(R.string.msg_delete_movie_successfully), Toast.LENGTH_SHORT).show()
                            }
                }
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show()
    }

    private fun searchFood() {
        val strKey = mFragmentAdminHomeBinding!!.edtSearchName.text.toString().trim { it <= ' ' }
        if (mListFood != null) {
            mListFood!!.clear()
        } else {
            mListFood = ArrayList()
        }
        getListFood(strKey)
//        hideSoftKeyboard(activity!!)
    }

    private fun getListFood(keyword: String?) {
        if (activity == null) {
            return
        }
        ControllerApplication[requireActivity()].foodDatabaseReference
                .addChildEventListener(object : ChildEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood == null || mAdminFoodAdapter == null) {
                            return
                        }
                        if (isEmpty(keyword)) {
                            mListFood!!.add(0, food)
                        } else {
                            if (getTextSearch(food.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                                            .contains(getTextSearch(keyword).toLowerCase(Locale.getDefault()).trim { it <= ' ' })) {
                                mListFood!!.add(0, food)
                            }
                        }
                        mAdminFoodAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood == null || mListFood!!.isEmpty() || mAdminFoodAdapter == null) {
                            return
                        }
                        for (i in mListFood!!.indices) {
                            if (food.id == mListFood!![i].id) {
                                mListFood!![i] = food
                                break
                            }
                        }
                        mAdminFoodAdapter!!.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood == null || mListFood!!.isEmpty() || mAdminFoodAdapter == null) {
                            return
                        }
                        for (foodObject in mListFood!!) {
                            if (food.id == foodObject.id) {
                                mListFood!!.remove(foodObject)
                                break
                            }
                        }
                        mAdminFoodAdapter!!.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }
}