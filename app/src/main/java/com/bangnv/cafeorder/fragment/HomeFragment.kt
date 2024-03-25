package com.bangnv.cafeorder.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.FoodDetailActivity
import com.bangnv.cafeorder.activity.MainActivity
import com.bangnv.cafeorder.adapter.FoodGridAdapter
import com.bangnv.cafeorder.adapter.FoodPopularAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.getTextSearch
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.showToastMessage
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentHomeBinding
import com.bangnv.cafeorder.listener.IOnClickFoodItemListener
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.StringUtil
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class HomeFragment : BaseFragment() {

    private var mFragmentHomeBinding: FragmentHomeBinding? = null
    private var mListFood: MutableList<Food>? = null
    private var mListFoodPopular: MutableList<Food>? = null
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private val mRunnableBanner = Runnable {
        if (mListFoodPopular == null || mListFoodPopular!!.isEmpty()) {
            return@Runnable
        }
        if (mFragmentHomeBinding!!.viewpager2.currentItem == mListFoodPopular!!.size - 1) {
            mFragmentHomeBinding!!.viewpager2.currentItem = 0
            return@Runnable
        }
        mFragmentHomeBinding!!.viewpager2.currentItem =
            mFragmentHomeBinding!!.viewpager2.currentItem + 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        getListFoodFromFirebase("")
        initListener()

        setupTouchOtherToClearAllFocus()
        setupLayoutSearchListener()

        return mFragmentHomeBinding!!.root
    }

    override fun initToolbar() {
        if (activity != null) {
            (activity as MainActivity?)!!.setToolBar(true, getString(R.string.home))
        }
    }

    private fun initListener() {
        mFragmentHomeBinding!!.edtSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(s: Editable) {
                filterFoodList(mFragmentHomeBinding!!.edtSearchName.text.toString())
            }
        })

        mFragmentHomeBinding!!.imgSearch.setOnClickListener {
            searchFood()
            hideSoftKeyboard(requireActivity())
            mFragmentHomeBinding!!.edtSearchName.clearFocus()
        }
        mFragmentHomeBinding!!.edtSearchName.setOnEditorActionListener { _: TextView?, actionId: Int, _: KeyEvent? ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFood()
                hideSoftKeyboard(requireActivity())
                mFragmentHomeBinding!!.edtSearchName.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }

    }

    private fun displayListFoodPopular() {
        val mFoodPopularAdapter =
            FoodPopularAdapter(getListFoodPopular(), object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    goToFoodDetail(food)
                }
            })
        mFragmentHomeBinding!!.viewpager2.adapter = mFoodPopularAdapter
        mFragmentHomeBinding!!.indicator3.setViewPager(mFragmentHomeBinding!!.viewpager2)
        mFragmentHomeBinding!!.viewpager2.registerOnPageChangeCallback(object :
            OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }

    private fun displayListFoodSuggest() {
        val gridLayoutManager = GridLayoutManager(activity, 2)
        mFragmentHomeBinding!!.rcvFood.layoutManager = gridLayoutManager
        val mFoodGridAdapter = FoodGridAdapter(mListFood, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                goToFoodDetail(food)
            }
        })
        mFragmentHomeBinding!!.rcvFood.adapter = mFoodGridAdapter
    }

    private fun getListFoodPopular(): MutableList<Food>? {
        mListFoodPopular = ArrayList()
        if (mListFood == null || mListFood!!.isEmpty()) {
            return mListFoodPopular
        }
        for (food in mListFood!!) {
            if (food.isPopular) {
                mListFoodPopular?.add(food)
            }
        }
        return mListFoodPopular
    }

    private fun getListFoodFromFirebase(key: String) {
        if (activity == null) {
            return
        }
        ControllerApplication[requireContext()].foodDatabaseReference.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mFragmentHomeBinding!!.layoutContent.visibility = View.VISIBLE
                mListFood = ArrayList()
                mListFoodPopular = ArrayList()
                for (dataSnapshot in snapshot.children) {
                    val food = dataSnapshot.getValue(Food::class.java) ?: continue
                    if (food.isPopular) {
                        mListFoodPopular?.add(food)
                    }
                    mListFood?.add(food)
                }
                // Nếu có từ khóa tìm kiếm, lọc danh sách đồ ăn
                if (key.isNotEmpty()) {
                    mListFood = mListFood?.filter { food ->
                        getTextSearch(food.name)
                            .toLowerCase(Locale.getDefault())
                            .trim { it <= ' ' }
                            .contains(getTextSearch(key).toLowerCase(Locale.getDefault()))
                    }?.toMutableList()
                }
                displayListFoodPopular()
                displayListFoodSuggest()
            }

            override fun onCancelled(error: DatabaseError) {
                showToastMessage(activity, getString(R.string.msg_get_date_error))
            }
        })
    }


    private fun filterFoodList(key: String) {
        val filteredList = if (key.isEmpty()) {
            mListFood  // Nếu không có từ khóa, trả về danh sách gốc
        } else {
            val normalizedKey = StringUtil.normalizeEnglishString(key)
            mListFood?.filter { food ->
                val normalizedFoodName = StringUtil.normalizeEnglishString(food.name ?: "")
                normalizedFoodName.contains(normalizedKey)
            }
        }
        if (filteredList != null) {
            displayFilteredFoodList(filteredList)
        }
    }


    private fun displayFilteredFoodList(filteredList: List<Food>) {
        val mFoodGridAdapter = FoodGridAdapter(filteredList, object : IOnClickFoodItemListener {
            override fun onClickItemFood(food: Food) {
                goToFoodDetail(food)
            }
        })
        mFragmentHomeBinding!!.rcvFood.adapter = mFoodGridAdapter
    }

    private fun searchFood() {
        val strKey = mFragmentHomeBinding!!.edtSearchName.text.toString().trim { it <= ' ' }
        filterFoodList(strKey)
    }

    private fun goToFoodDetail(food: Food) {
        val bundle = Bundle()
        bundle.putSerializable(Constant.KEY_INTENT_FOOD_OBJECT, food)
        startActivity(requireContext(), FoodDetailActivity::class.java, bundle)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mFragmentHomeBinding!!.layoutContent.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentHomeBinding!!.edtSearchName.clearFocus()
        }
        //S
        mFragmentHomeBinding!!.layoutChild.setOnTouchListener { _, _ ->
            hideSoftKeyboard(requireActivity())
            mFragmentHomeBinding!!.edtSearchName.clearFocus()
            false
        }
    }

    private fun setupLayoutSearchListener() {
        //Layout Search: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextListeners(
            mFragmentHomeBinding!!.layoutSearch,
            mFragmentHomeBinding!!.edtSearchName,
            mFragmentHomeBinding!!.imgClear,
            requireActivity()
        )
    }

    override fun onPause() {
        super.onPause()
        mHandlerBanner.removeCallbacks(mRunnableBanner)
    }

    override fun onResume() {
        super.onResume()
        mHandlerBanner.postDelayed(mRunnableBanner, 3000)
    }
}