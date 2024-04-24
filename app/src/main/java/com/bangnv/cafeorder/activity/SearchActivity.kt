package com.bangnv.cafeorder.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.CategorySearchAdapter
import com.bangnv.cafeorder.adapter.CategorySearchAdapter.IOnClickItemCategorySearch
import com.bangnv.cafeorder.adapter.FoodGridAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionSearchListener
import com.bangnv.cafeorder.databinding.ActivitySearchBinding
import com.bangnv.cafeorder.listener.IOnClickFoodItemListener
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.StringUtil
import com.bangnv.cafeorder.utils.StringUtil.normalizeEnglishText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import java.util.Locale


class SearchActivity : AppCompatActivity(){

    private lateinit var mActivitySearchBinding: ActivitySearchBinding
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var mListFood: MutableList<Food> = mutableListOf()
    private lateinit var mCategoryAll: Category
    private lateinit var mCategorySearchAdapter: CategorySearchAdapter
    private lateinit var mFoodGridAdapter: FoodGridAdapter
    private var categorySelected: Long = 0L

//    private var loadedItemCount: Int = 0        // Dùng cho phân trang, tính sau
//    private var hasMoreData: Boolean = true     // Dùng cho phân trang, tính sau

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivitySearchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(mActivitySearchBinding.root)

        initListeners()
        getListCategory()
        setupDisplayFood()
        loadFilteredData(0L, "") //khi mặc định là Tất cả + chưa có từ khóa tìm kiếm

        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
    }

    private fun initListeners() {
        //Layout Search Twhen open SearchActivity: Enhance user experience
        mActivitySearchBinding.edtSearchName.requestFocus()
        mActivitySearchBinding.layoutSearch.setBackgroundResource(R.drawable.bg_edittext_active)

        mActivitySearchBinding.imageBack.setOnClickListener {
            hideSoftKeyboard(this)
            finish()
        }

        mActivitySearchBinding.imgSearch.setOnClickListener {

            loadFilteredData(categorySelected, mActivitySearchBinding.edtSearchName.text.toString())

            hideSoftKeyboard(this)
            mActivitySearchBinding.edtSearchName.clearFocus()
            Log.d("SearchA cateSeled img: ", categorySelected.toString() )
        }
        mActivitySearchBinding.edtSearchName.setOnActionSearchListener(

            { loadFilteredData(categorySelected, mActivitySearchBinding.edtSearchName.text.toString())},

            { hideSoftKeyboard(this) },
            { mActivitySearchBinding.edtSearchName.clearFocus() },
            { Log.d("SearchA cateSeled img: ", categorySelected.toString()) }
        )
        mActivitySearchBinding.imgClear.setOnClickListener {
            mActivitySearchBinding.edtSearchName.setText("")
            loadFilteredData(categorySelected, mActivitySearchBinding.edtSearchName.text.toString())
        }
    }

    private fun getListCategory() {
        ControllerApplication[this@SearchActivity].categoryDatabaseReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mActivitySearchBinding.tvCategoryTitle.visibility = View.VISIBLE
                    mListCategory.clear()
                    for (dataSnapshot in snapshot.children) {
                        val category = dataSnapshot.getValue(Category::class.java)
                        if (category != null) {
                            mListCategory.add(0, category)
                        }
                    }
                    mCategoryAll = Category(0L, getString(R.string.label_all), "")
                    mListCategory.add(0, mCategoryAll!!)

                    //Setup Recyclerview Adapter category
                    val linearLayoutManager = LinearLayoutManager(this@SearchActivity,
                        LinearLayoutManager.HORIZONTAL, false)
                    mActivitySearchBinding.rcvCategory.layoutManager = linearLayoutManager
                    mCategorySearchAdapter = CategorySearchAdapter(
                        this@SearchActivity, mListCategory, object :IOnClickItemCategorySearch{
                            override fun onSelectedCategorySearch(categoryId: Long) {
                                categorySelected = categoryId
//                                loadedItemCount = 0 // reset biến đếm //Cho phân trang, tính sau
                                //TODO: gọi hàm search /filter theo id

                                loadFilteredData(categorySelected, mActivitySearchBinding.edtSearchName.text.toString())

                                Log.d("SearchA cateSel.ed CI: ", categorySelected.toString() )
                            }
                        })

                    mActivitySearchBinding.rcvCategory.adapter = mCategorySearchAdapter

                }

                override fun onCancelled(error: DatabaseError) {
                    mActivitySearchBinding.tvCategoryTitle.visibility = View.GONE
                }
            })
    }

    private fun setupDisplayFood() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivitySearchBinding.rcvData.layoutManager = gridLayoutManager
        mFoodGridAdapter =
            FoodGridAdapter(this@SearchActivity, mListFood, object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(this@SearchActivity, food)
                }
            })
        mActivitySearchBinding.rcvData.adapter = mFoodGridAdapter
    }


    fun loadFilteredData(categoryId: Long, searchKeyword: String) {
        mListFood.clear()
        val query: Query
        = if(categoryId == 0L){
            ControllerApplication[this].foodDatabaseReference
        } else {
            ControllerApplication[this].foodDatabaseReference
                .orderByChild("categoryId")
                .equalTo(categoryId.toDouble())
        }

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val food = snapshot.getValue(Food::class.java)
                    if (food != null) {

                        // So sánh tên chuẩn hóa với từ khóa chuẩn hóa từ người dùng
                        if (normalizeEnglishText(food.name).contains(normalizeEnglishText(searchKeyword), ignoreCase = true)) {
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
                mFoodGridAdapter.updateData(mListFood)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Xử lý lỗi nếu có
            }
        })
    }










    // Tạm thời chưa dùng
    private fun getInitListFood() {
        val queryInitListFood: Query = ControllerApplication[this].foodDatabaseReference
            .limitToFirst(Constant.MAX_ITEM_PER_LOAD_GRID)


        queryInitListFood.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mListFood.clear()
                for (dataSnapshot in snapshot.children) {
                    val food = dataSnapshot.getValue(Food::class.java)!!
                    if (isFoodResult(food)) {
                        mListFood.add(0, food)
                    }
                }
                displayListFoodResult()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Tạm thời chưa dùng
    private fun displayListFoodResult() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivitySearchBinding.rcvData.layoutManager = gridLayoutManager
        mFoodGridAdapter =
            FoodGridAdapter(this@SearchActivity, mListFood, object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(this@SearchActivity, food)
                }
            })
        mActivitySearchBinding.rcvData.adapter = mFoodGridAdapter
    }

    // Tạm thời chưa dùng
    private fun isFoodResult(food: Food?): Boolean {
        if (food == null) {
            return false
        }
        val key = mActivitySearchBinding.edtSearchName.text.toString().trim { it <= ' ' }
        val categoryId = mCategoryAll?.id
        return if (StringUtil.isEmpty(key)) {
            if (categoryId == 0L) {
                true
            } else food.categoryId == categoryId
        } else {
            val isMatch = normalizeEnglishText(food.name).toLowerCase(Locale.getDefault()).trim { it <= ' ' }
                .contains(normalizeEnglishText(key).toLowerCase(Locale.getDefault()).trim { it <= ' ' })
            if (categoryId == 0L) {
                isMatch
            } else isMatch && food.categoryId == categoryId
        }
    }





    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivitySearchBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(this@SearchActivity)
            mActivitySearchBinding.edtSearchName.clearFocus()
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearNoClearTextListeners(
            mActivitySearchBinding.layoutSearch,
            mActivitySearchBinding.edtSearchName,
            mActivitySearchBinding.imgClear
        )
    }

    @SuppressLint("LongLogTag")
    private fun showLogs() {
        Log.d("SearchActivity: categorySelected: ", categorySelected.toString() )
    }

}