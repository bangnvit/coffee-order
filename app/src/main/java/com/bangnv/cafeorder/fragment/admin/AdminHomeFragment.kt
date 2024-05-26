package com.bangnv.cafeorder.fragment.admin

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
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
import com.bangnv.cafeorder.adapter.CategorySearchAdapter
import com.bangnv.cafeorder.adapter.admin.AdminFoodAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionSearchListener
import com.bangnv.cafeorder.constant.GlobalFunction.openActivity
import com.bangnv.cafeorder.databinding.FragmentAdminHomeBinding
import com.bangnv.cafeorder.listener.IOnManagerFoodListener
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.StringUtil
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.*

class AdminHomeFragment : Fragment() {

    private lateinit var mFragmentAdminHomeBinding: FragmentAdminHomeBinding
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var mListFood: MutableList<Food> = mutableListOf()
    private var displayFood: MutableList<Food> = mutableListOf()
    private lateinit var mCategorySearchAdapter: CategorySearchAdapter
    private lateinit var mAdminFoodAdapter: AdminFoodAdapter
    private lateinit var mCategoryAll: Category
    private var categorySelected: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mFragmentAdminHomeBinding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        initListener()
        getListCategory()
        getListFood()
        setupDisplayFood()
        loadFilteredData(0L, "") //khi mặc định là Tất cả + chưa có từ khóa tìm kiếm
        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
        return mFragmentAdminHomeBinding.root
    }

    private fun initListener() {
        mFragmentAdminHomeBinding.btnAddFood.setOnClickListener { onClickAddFood() }

        mFragmentAdminHomeBinding.imgSearch.setOnClickListener {

            loadFilteredData(categorySelected, mFragmentAdminHomeBinding.edtSearchName.text.toString())

            hideSoftKeyboard( requireActivity())
            mFragmentAdminHomeBinding.edtSearchName.clearFocus()
            Log.d("SearchA cateSeled img: ", categorySelected.toString() )
        }
        mFragmentAdminHomeBinding.edtSearchName.setOnActionSearchListener(

            { loadFilteredData(categorySelected, mFragmentAdminHomeBinding.edtSearchName.text.toString())},

            { hideSoftKeyboard( requireActivity()) },
            { mFragmentAdminHomeBinding.edtSearchName.clearFocus() },
            { Log.d("SearchA cateSeled img: ", categorySelected.toString()) }
        )
        mFragmentAdminHomeBinding.imgClear.setOnClickListener {
            mFragmentAdminHomeBinding.edtSearchName.setText("")
            loadFilteredData(categorySelected, mFragmentAdminHomeBinding.edtSearchName.text.toString())
        }
    }

    private fun getListCategory() {
        ControllerApplication[requireContext()].categoryDatabaseReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mFragmentAdminHomeBinding.tvCategoryTitle.visibility = View.VISIBLE
                    mListCategory.clear()
                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(Category::class.java)
                        if (category != null) {
                            mListCategory.add(category)
                        }
                    }
                    mCategoryAll = Category(0L, getString(R.string.label_all), "")
                    mListCategory.add(0, mCategoryAll)

                    //Setup Recyclerview Adapter category
                    val linearLayoutManager = LinearLayoutManager(requireContext(),
                        LinearLayoutManager.HORIZONTAL, false)
                    mFragmentAdminHomeBinding.rcvCategory.layoutManager = linearLayoutManager
                    mCategorySearchAdapter = CategorySearchAdapter(
                        requireContext(), mListCategory, object :
                            CategorySearchAdapter.IOnClickItemCategorySearch {
                            override fun onSelectedCategorySearch(categoryId: Long) {
                                categorySelected = categoryId
//                                loadedItemCount = 0 // reset biến đếm //Cho phân trang, tính sau
                                //TODO: gọi hàm search /filter theo id

                                loadFilteredData(categorySelected, mFragmentAdminHomeBinding.edtSearchName.text.toString())

                                Log.d("SearchA cateSel.ed CI: ", categorySelected.toString() )
                            }
                        })

                    mFragmentAdminHomeBinding.rcvCategory.adapter = mCategorySearchAdapter

                }

                override fun onCancelled(error: DatabaseError) {
                    mFragmentAdminHomeBinding.tvCategoryTitle.visibility = View.GONE
                }
            })
    }

    private fun setupDisplayFood() {
        val linearLayoutManagerFood = LinearLayoutManager(activity)
        mFragmentAdminHomeBinding.rcvFood.layoutManager = linearLayoutManagerFood
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

    fun loadFilteredData(categoryId: Long, searchKeyword: String) {
        mListFood.clear()
        val query: Query
                = if(categoryId == 0L){
            ControllerApplication[requireContext()].foodDatabaseReference
        } else {
            ControllerApplication[requireContext()].foodDatabaseReference
                .orderByChild("categoryId")
                .equalTo(categoryId.toDouble())
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val food = snapshot.getValue(Food::class.java)
                    if (food != null) {

                        // So sánh tên chuẩn hóa với từ khóa chuẩn hóa từ người dùng
                        if (StringUtil.normalizeEnglishText(food.name)
                                .contains(StringUtil.normalizeEnglishText(searchKeyword), ignoreCase = true)) {
                            // Xử lý các mục thỏa mãn yêu cầu
                            mListFood.add(0, food)
//                            loadedItemCount++ // dành cho phân trang, tính sau
                        }
                    }

                    // Kiểm tra xem đã tải đủ số lượng mục mong muốn chưa
                    //Dành cho phân trang, tính sau
//                    if (loadedItemCount >= Constant.MAX_ITEM_PER_LOAD) {
//                        break // Dừng khi đã tải đủ số lượng mục
//                    }
                }
                mAdminFoodAdapter.updateData(mListFood)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi nếu có
            }
        })
    }

    private fun onClickAddFood() {
        openActivity(requireActivity(), AdminAddFoodActivity::class.java)
    }

    private fun onClickEditFood(food: Food?) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, food)
        openActivity(requireActivity(), AdminAddFoodActivity::class.java, bundle)
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
                                        getString(R.string.msg_delete_food_successfully), Toast.LENGTH_SHORT).show()
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