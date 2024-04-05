package com.bangnv.cafeorder.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.admin.AdminAddFoodActivity
import com.bangnv.cafeorder.adapter.admin.AdminFoodAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionSearchListener
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentAdminHomeBinding
import com.bangnv.cafeorder.listener.IOnManagerFoodListener
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.StringUtil
import java.util.*

class AdminHomeFragment : Fragment() {

    private lateinit var mFragmentAdminHomeBinding: FragmentAdminHomeBinding
    private var mListFood: MutableList<Food> = mutableListOf()
    private var displayFood: MutableList<Food> = mutableListOf()
    private lateinit var mAdminFoodAdapter: AdminFoodAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminHomeBinding = FragmentAdminHomeBinding.inflate(inflater, container, false)
        initView()
        initListener()
        getListFood()
        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
        return mFragmentAdminHomeBinding.root
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentAdminHomeBinding.rcvFood.layoutManager = linearLayoutManager
        mAdminFoodAdapter = AdminFoodAdapter(requireContext(), mListFood, object : IOnManagerFoodListener {
            override fun onClickUpdateFood(food: Food?) {
                onClickEditFood(food)
            }

            override fun onClickDeleteFood(food: Food?) {
                deleteFoodItem(food)
            }
        })
        mFragmentAdminHomeBinding.rcvFood.adapter = mAdminFoodAdapter
    }

    private fun initListener() {
        mFragmentAdminHomeBinding.btnAddFood.setOnClickListener { onClickAddFood() }

        mFragmentAdminHomeBinding.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable) {
                filterFoodList(mFragmentAdminHomeBinding.edtSearchName.text.toString())
            }
        })
        mFragmentAdminHomeBinding.imgSearch.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentAdminHomeBinding.edtSearchName.clearFocus()
        }
        mFragmentAdminHomeBinding.edtSearchName.setOnActionSearchListener(
            { hideSoftKeyboard(requireActivity()) },
            { mFragmentAdminHomeBinding.edtSearchName.clearFocus() }
        )
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

    private fun getListFood() {
        if (activity == null) {
            return
        }
        ControllerApplication[requireActivity()].foodDatabaseReference
                .addChildEventListener(object : ChildEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                        val food = dataSnapshot.getValue(Food::class.java) ?: return
                        mListFood.add(0, food)
                        mAdminFoodAdapter.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood.isEmpty()) {
                            return
                        }
                        for (i in mListFood.indices) {
                            if (food.id == mListFood[i].id) {
                                mListFood[i] = food
                                break
                            }
                        }
                        mAdminFoodAdapter.notifyDataSetChanged()
                    }

                    @SuppressLint("NotifyDataSetChanged")
                    override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food == null || mListFood.isEmpty()) {
                            return
                        }
                        for (foodObject in mListFood) {
                            if (food.id == foodObject.id) {
                                mListFood.remove(foodObject)
                                break
                            }
                        }
                        mAdminFoodAdapter.notifyDataSetChanged()
                    }

                    override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                    override fun onCancelled(databaseError: DatabaseError) {}
                })
    }

    private fun filterFoodList(key: String) {
        displayFood = if (key.isEmpty()) {
            mListFood  // if there is no search keyword, display the original data
        } else {
            val normalizedKey = StringUtil.normalizeEnglishText(key)
            mListFood.filter { food ->
                val normalizedFoodName = StringUtil.normalizeEnglishText(food.name ?: "")
                normalizedFoodName.contains(normalizedKey)
            }.toMutableList()
        }
        mAdminFoodAdapter.updateData(displayFood)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mFragmentAdminHomeBinding.layoutContent.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentAdminHomeBinding.edtSearchName.clearFocus()
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mFragmentAdminHomeBinding.layoutSearch,
            mFragmentAdminHomeBinding.edtSearchName,
            mFragmentAdminHomeBinding.imgClear
        )
    }
}