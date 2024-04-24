package com.bangnv.cafeorder.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.adapter.MoreImageAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.database.FoodDatabase.Companion.getInstance
import com.bangnv.cafeorder.databinding.ActivityFoodDetailBinding
import com.bangnv.cafeorder.event.ReloadListCartEvent
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.utils.GlideUtils.loadUrl
import com.bangnv.cafeorder.utils.GlideUtils.loadUrlBanner
import org.greenrobot.eventbus.EventBus

class FoodDetailActivity : BaseActivity() {

    private lateinit var mActivityFoodDetailBinding: ActivityFoodDetailBinding
    private var mFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityFoodDetailBinding = ActivityFoodDetailBinding.inflate(layoutInflater)
        setContentView(mActivityFoodDetailBinding.root)
        getDataIntent()
        initToolbar()
        setDataFoodDetail()
        initListener()
    }

    private fun getDataIntent() {
        val bundle = intent.extras
        if (bundle != null) {
            mFood = bundle[Constant.KEY_INTENT_FOOD_OBJECT] as Food?
        }
    }

    private fun initToolbar() {
        mActivityFoodDetailBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityFoodDetailBinding.toolbar.imgCart.visibility = View.VISIBLE
        mActivityFoodDetailBinding.toolbar.tvTitle.text = getString(R.string.food_detail_title)
        mActivityFoodDetailBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun setDataFoodDetail() {
        if (mFood == null) {
            return
        }
        loadUrlBanner(mFood!!.banner, mActivityFoodDetailBinding.imageFood)
        if (mFood!!.sale <= 0) {
            mActivityFoodDetailBinding.tvSaleOff.visibility = View.GONE
            mActivityFoodDetailBinding.tvPrice.visibility = View.GONE
            val strPrice: String = formatNumberWithPeriods(mFood!!.price) + Constant.CURRENCY
            mActivityFoodDetailBinding.tvPriceSale.text = strPrice
        } else {
            mActivityFoodDetailBinding.tvSaleOff.visibility = View.VISIBLE
            mActivityFoodDetailBinding.tvPrice.visibility = View.VISIBLE
            val strSale = "Giảm " + mFood!!.sale + "%"
            mActivityFoodDetailBinding.tvSaleOff.text = strSale
            val strPriceOld: String = formatNumberWithPeriods(mFood!!.price) + Constant.CURRENCY
            mActivityFoodDetailBinding.tvPrice.text = strPriceOld
            mActivityFoodDetailBinding.tvPrice.paintFlags = mActivityFoodDetailBinding.tvPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val strRealPrice: String = formatNumberWithPeriods(mFood!!.realPrice) + Constant.CURRENCY
            mActivityFoodDetailBinding.tvPriceSale.text = strRealPrice
        }
        mActivityFoodDetailBinding.tvFoodName.text = mFood!!.name
        mActivityFoodDetailBinding.tvFoodDescription.text = mFood!!.description
        displayListMoreImages()
        setStatusButtonAddToCart()
    }

    private fun displayListMoreImages() {
        if (mFood!!.images == null || mFood!!.images!!.isEmpty()) {
            mActivityFoodDetailBinding.tvMoreImageLabel.visibility = View.GONE
            return
        }
        mActivityFoodDetailBinding.tvMoreImageLabel.visibility = View.VISIBLE
        val gridLayoutManager = GridLayoutManager(this, 2)
        mActivityFoodDetailBinding.rcvImages.layoutManager = gridLayoutManager
        val moreImageAdapter = MoreImageAdapter(mFood!!.images)
        mActivityFoodDetailBinding.rcvImages.adapter = moreImageAdapter
    }

    private fun setStatusButtonAddToCart() {
        if (isFoodInCart()) {
            mActivityFoodDetailBinding.tvAddToCart.setBackgroundResource(R.drawable.bg_gray_disable_shape_corner_circle)
            mActivityFoodDetailBinding.tvAddToCart.text = getString(R.string.added_to_cart)
            mActivityFoodDetailBinding.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.textColorPrimary))
            mActivityFoodDetailBinding.toolbar.imgCart.visibility = View.GONE
            mActivityFoodDetailBinding.tvGoToCart.visibility = View.VISIBLE
        } else {
            mActivityFoodDetailBinding.tvAddToCart.setBackgroundResource(R.drawable.bg_green_main_shape_corner_circle)
            mActivityFoodDetailBinding.tvAddToCart.text = getString(R.string.add_to_cart)
            mActivityFoodDetailBinding.tvAddToCart.setTextColor(ContextCompat.getColor(this, R.color.white))
            mActivityFoodDetailBinding.toolbar.imgCart.visibility = View.VISIBLE
            mActivityFoodDetailBinding.tvGoToCart.visibility = View.GONE
        }
    }

    private fun isFoodInCart(): Boolean {
        val list = getInstance(this)!!.foodDAO()!!.checkFoodInCart(mFood!!.id)
        return !list.isNullOrEmpty()
    }

    private fun initListener() {
        mActivityFoodDetailBinding.tvAddToCart.setOnClickListener { onClickAddToCart() }
        mActivityFoodDetailBinding.toolbar.imgCart.setOnClickListener { onClickAddToCart() }
        mActivityFoodDetailBinding.tvGoToCart.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("goToCart", true)
            startActivity(intent)
            finish()
        }
    }

    @SuppressLint("ResourceType", "InflateParams")
    private fun onClickAddToCart() {
        if (isFoodInCart()) {
            return
        }

        //Init Custom Dialog
        val viewDialog = Dialog(this@FoodDetailActivity)
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_sheet_cart)

        val imgFoodCart = viewDialog.findViewById<ImageView>(R.id.img_food_cart)
        val tvFoodNameCart = viewDialog.findViewById<TextView>(R.id.tv_food_name_cart)
        val tvFoodPriceCart = viewDialog.findViewById<TextView>(R.id.tv_food_price_cart)
        val tvSubtractCount = viewDialog.findViewById<TextView>(R.id.tv_subtract)
        val tvCount = viewDialog.findViewById<TextView>(R.id.tv_count)
        val tvAddCount = viewDialog.findViewById<TextView>(R.id.tv_add)
        val tvCancel = viewDialog.findViewById<TextView>(R.id.tv_cancel)
        val tvAddCart = viewDialog.findViewById<TextView>(R.id.tv_add_cart)
        loadUrl(mFood!!.image, imgFoodCart)
        tvFoodNameCart.text = mFood!!.name
        val totalPrice = mFood!!.realPrice
        val strTotalPrice: String = formatNumberWithPeriods(totalPrice) + Constant.CURRENCY
        tvFoodPriceCart.text = strTotalPrice
        mFood!!.count = 1
        mFood!!.totalPrice = totalPrice

        // Set listener
        tvSubtractCount.setOnClickListener {
            val count = tvCount.text.toString().toInt()
            if (count <= 1) {                return@setOnClickListener
            }
            val newCount = tvCount.text.toString().toInt() - 1
            tvCount.text = newCount.toString()
            val totalPrice1 = mFood!!.realPrice * newCount
            val strTotalPrice1: String = formatNumberWithPeriods(totalPrice1) + Constant.CURRENCY
            tvFoodPriceCart.text = strTotalPrice1
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice1
        }
        tvAddCount.setOnClickListener {
            val newCount = tvCount.text.toString().toInt() + 1
            tvCount.text = newCount.toString()
            val totalPrice2 = mFood!!.realPrice * newCount
            val strTotalPrice2: String = formatNumberWithPeriods(totalPrice2) + Constant.CURRENCY
            tvFoodPriceCart.text = strTotalPrice2
            mFood!!.count = newCount
            mFood!!.totalPrice = totalPrice2
        }
        tvCancel.setOnClickListener { viewDialog.dismiss() }
        tvAddCart.setOnClickListener {
            getInstance(this@FoodDetailActivity)!!.foodDAO()!!.insertFood(mFood)
            viewDialog.dismiss()
            setStatusButtonAddToCart()
            EventBus.getDefault().post(ReloadListCartEvent())
        }

        // Show dialog + set Customize
        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}