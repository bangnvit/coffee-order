package com.bangnv.cafeorder.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.CategoryActivity
import com.bangnv.cafeorder.activity.SearchActivity
import com.bangnv.cafeorder.adapter.CategoryAdapter
import com.bangnv.cafeorder.adapter.FoodGridAdapter
import com.bangnv.cafeorder.adapter.FoodPopularAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.showToastMessage
import com.bangnv.cafeorder.constant.GlobalFunction.startActivity
import com.bangnv.cafeorder.databinding.FragmentHomeBinding
import com.bangnv.cafeorder.listener.IOnClickFoodItemListener
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.model.Food
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {

    private lateinit var mFragmentHomeBinding: FragmentHomeBinding
    private var mListCategory: MutableList<Category> = mutableListOf()
    private var mListFood: MutableList<Food> = mutableListOf()
    private var mListFoodPopular: MutableList<Food> = mutableListOf()
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private lateinit var mFoodGridAdapter: FoodGridAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private val mRunnableBanner = Runnable {
        if (mListFoodPopular.isEmpty()) {
            return@Runnable
        }
        if (mFragmentHomeBinding.viewpager2.currentItem == mListFoodPopular.size - 1) {
            mFragmentHomeBinding.viewpager2.currentItem = 0
            return@Runnable
        }
        mFragmentHomeBinding.viewpager2.currentItem =
            mFragmentHomeBinding.viewpager2.currentItem + 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        mFragmentHomeBinding = FragmentHomeBinding.inflate(inflater, container, false)

        initView()
        getListFoodFromFirebase()
        getListCategory()

        initListener()

        return mFragmentHomeBinding.root
    }

    private fun initView() {
        if (activity == null) {
            return
        }
        gridLayoutManager = GridLayoutManager(activity, 2)
        mFragmentHomeBinding.rcvFood.layoutManager = gridLayoutManager
        mListFood = mutableListOf()
        mFoodGridAdapter =
            FoodGridAdapter(requireContext(), mListFood, object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(requireContext(), food)
                }
            })
        mFragmentHomeBinding.rcvFood.adapter = mFoodGridAdapter
    }


    private fun getListFoodFromFirebase() {
        if (activity == null) {
            return
        }
        ControllerApplication[requireContext()].foodDatabaseReference
            .addValueEventListener(
                object : ValueEventListener {
                    @SuppressLint("NotifyDataSetChanged")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        mFragmentHomeBinding.layoutContent.visibility = View.VISIBLE
                        mListFood.clear()
                        for (dataSnapshot in snapshot.children) {
                            val food = dataSnapshot.getValue(Food::class.java) ?: continue
                            if (food.isPopular) {
                                mListFoodPopular.add(food)
                            }
                            mListFood.add(food)
                        }
                        displayListFoodPopular()
                        mFoodGridAdapter.updateData(mListFood)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        showToastMessage(activity, getString(R.string.msg_get_date_error))
                    }
                })
    }

    private fun displayListFoodPopular() {
        val mFoodPopularAdapter =
            FoodPopularAdapter(getListFoodPopular(), object : IOnClickFoodItemListener {
                override fun onClickItemFood(food: Food) {
                    GlobalFunction.goToFoodDetail(requireContext(), food)
                }
            })
        mFragmentHomeBinding.viewpager2.adapter = mFoodPopularAdapter
        mFragmentHomeBinding.indicator3.setViewPager(mFragmentHomeBinding.viewpager2)
        mFragmentHomeBinding.viewpager2.registerOnPageChangeCallback(object :
            OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }

    private fun getListFoodPopular(): MutableList<Food> {
        mListFoodPopular = ArrayList()
        if (mListFood.isEmpty()) {
            return mListFoodPopular
        }
        for (food in mListFood) {
            if (food.isPopular) {
                mListFoodPopular.add(food)
            }
        }
        return mListFoodPopular
    }

    private fun initListener() {
        mFragmentHomeBinding!!.layoutSearch.setOnClickListener {
            startActivity(requireContext(), SearchActivity::class.java)
        }
    }

    private fun getListCategory() {
        if (activity == null) {
            return
        }
        ControllerApplication[requireContext()].categoryDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mListCategory.clear()
                for (dataSnapshot in snapshot.children) {
                    val category = dataSnapshot.getValue(Category::class.java)
                    if (category != null) {
                        mListCategory.add(0, category)
                    }
                }
                displayListCategories()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun displayListCategories() {
        val linearLayoutManager = LinearLayoutManager(activity,
            LinearLayoutManager.HORIZONTAL, false)
        mFragmentHomeBinding.rcvCategory.layoutManager = linearLayoutManager
        val categoryAdapter = CategoryAdapter(mListCategory, object : CategoryAdapter.IManagerCategoryListener {
            override fun clickItemCategory(category: Category?) {
                val bundle = Bundle()
                bundle.putSerializable(Constant.KEY_INTENT_CATEGORY_OBJECT, category)
                startActivity(requireActivity(), CategoryActivity::class.java, bundle)
            }
        })
        mFragmentHomeBinding.rcvCategory.adapter = categoryAdapter
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