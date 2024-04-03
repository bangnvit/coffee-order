package com.bangnv.cafeorder.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.adapter.FoodGridAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionSearchListener
import com.bangnv.cafeorder.databinding.ActivityCategoryBinding
import com.bangnv.cafeorder.listener.IOnClickFoodItemListener
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.StringUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class CategoryActivity : AppCompatActivity() {

    private lateinit var mActivityCategoryBinding: ActivityCategoryBinding
    private var mListFood: MutableList<Food> = mutableListOf()
    private var displayFood: MutableList<Food> = mutableListOf()
    private lateinit var mFoodGridAdapter: FoodGridAdapter
    private var mCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityCategoryBinding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(mActivityCategoryBinding.root)

        getDataIntent()
        initListeners()
        getListFood()

        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()
    }

    private fun getDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            mCategory = bundleReceived[Constant.KEY_INTENT_CATEGORY_OBJECT] as Category?
            mActivityCategoryBinding.tvTitle.text = mCategory?.name
        }
    }

    private fun initListeners() {
        mActivityCategoryBinding.imgBack.setOnClickListener { onBackPressed() }

        mActivityCategoryBinding.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                filterFoodList(mActivityCategoryBinding.edtSearchName.text.toString())
            }
        })

        mActivityCategoryBinding.imgSearch.setOnClickListener {
            hideSoftKeyboard(this@CategoryActivity)
            mActivityCategoryBinding.edtSearchName.clearFocus()
        }
        mActivityCategoryBinding.edtSearchName.setOnActionSearchListener(
            { hideSoftKeyboard(this@CategoryActivity) },
            { mActivityCategoryBinding.edtSearchName.clearFocus() }
        )
    }

    private fun filterFoodList(key: String) {
        displayFood = if (key.trim().isEmpty()) {
            mListFood  // if there is no search keyword, display the original data
        } else {
            val normalizedKey = StringUtil.normalizeEnglishTextSearch(key)
            mListFood.filter { food ->
                val normalizedFoodName = StringUtil.normalizeEnglishTextSearch(food.name ?: "").trim()
                normalizedFoodName.contains(normalizedKey)
            }.toMutableList()
        }
        mFoodGridAdapter.updateData(displayFood)
    }

    private fun getListFood() {
        ControllerApplication[this].foodDatabaseReference
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mListFood.clear()
                    for (dataSnapshot in snapshot.children) {
                        val food = dataSnapshot.getValue(Food::class.java)
                        if (food != null && mCategory?.id == food.categoryId) {
                            mListFood.add(0, food)
                        }
                    }
                    displayListFood()
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }

    private fun displayListFood() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivityCategoryBinding.rcvData.layoutManager = gridLayoutManager
        mFoodGridAdapter = FoodGridAdapter(this, mListFood, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                GlobalFunction.goToFoodDetail(this@CategoryActivity, food)
            }
        })
        mActivityCategoryBinding.rcvData.adapter = mFoodGridAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivityCategoryBinding.layoutContent.setOnClickListener {
            hideSoftKeyboard(this@CategoryActivity)
            mActivityCategoryBinding.edtSearchName.clearFocus()
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityCategoryBinding.layoutSearch,
            mActivityCategoryBinding.edtSearchName,
            mActivityCategoryBinding.imgClear
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}