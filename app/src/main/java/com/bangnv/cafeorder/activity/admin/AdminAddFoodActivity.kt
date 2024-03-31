package com.bangnv.cafeorder.activity.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.setBackgroundOnEditTextFocusChange
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionDoneListener
import com.bangnv.cafeorder.databinding.ActivityAdminAddFoodBinding
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.model.FoodObject
import com.bangnv.cafeorder.model.Image
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import java.util.*
import kotlin.collections.set

class AdminAddFoodActivity : BaseActivity() {

    private var mActivityAdminAddFoodBinding: ActivityAdminAddFoodBinding? = null
    private var isUpdate = false
    private var mFood: Food? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminAddFoodBinding = ActivityAdminAddFoodBinding.inflate(layoutInflater)
        setContentView(mActivityAdminAddFoodBinding!!.root)
        getDataIntent()
        initToolbar()
        initView()
        mActivityAdminAddFoodBinding!!.btnAddOrEdit.setOnClickListener { addOrEditFood() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextsListener()
    }

    private fun getDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mFood = bundleReceived[Constant.KEY_INTENT_FOOD_OBJECT] as Food?
        }
    }

    private fun initToolbar() {
        mActivityAdminAddFoodBinding!!.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminAddFoodBinding!!.toolbar.imgCart.visibility = View.GONE
        mActivityAdminAddFoodBinding!!.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            mActivityAdminAddFoodBinding!!.toolbar.tvTitle.text = getString(R.string.edit_food)
            mActivityAdminAddFoodBinding!!.btnAddOrEdit.text = getString(R.string.action_edit)
            mActivityAdminAddFoodBinding!!.edtName.setText(mFood!!.name)
            mActivityAdminAddFoodBinding!!.edtDescription.setText(mFood!!.description)
            mActivityAdminAddFoodBinding!!.edtPrice.setText(java.lang.String.valueOf(mFood!!.price))
            mActivityAdminAddFoodBinding!!.edtDiscount.setText(java.lang.String.valueOf(mFood!!.sale))
            mActivityAdminAddFoodBinding!!.edtImage.setText(mFood!!.image)
            mActivityAdminAddFoodBinding!!.edtImageBanner.setText(mFood!!.banner)
            mActivityAdminAddFoodBinding!!.chbPopular.isChecked = mFood!!.isPopular
            mActivityAdminAddFoodBinding!!.edtOtherImage.setText(getTextOtherImages())
        } else {
            mActivityAdminAddFoodBinding!!.toolbar.tvTitle.text = getString(R.string.add_food)
            mActivityAdminAddFoodBinding!!.btnAddOrEdit.text = getString(R.string.action_add)
        }
    }

    private fun getTextOtherImages(): String {
        var result = ""
        if (mFood == null || mFood!!.images == null || mFood!!.images!!.isEmpty()) {
            return result
        }
        for (image in mFood!!.images!!) {
            result = if (isEmpty(result)) {
                result + image.url
            } else {
                result + ";" + image.url
            }
        }
        return result
    }

    private fun addOrEditFood() {
        val strName = mActivityAdminAddFoodBinding!!.edtName.text.toString().trim { it <= ' ' }
        val strDescription = mActivityAdminAddFoodBinding!!.edtDescription.text.toString().trim { it <= ' ' }
        val strPrice = mActivityAdminAddFoodBinding!!.edtPrice.text.toString().trim { it <= ' ' }
        val strDiscount = mActivityAdminAddFoodBinding!!.edtDiscount.text.toString().trim { it <= ' ' }
        val strImage = mActivityAdminAddFoodBinding!!.edtImage.text.toString().trim { it <= ' ' }
        val strImageBanner = mActivityAdminAddFoodBinding!!.edtImageBanner.text.toString().trim { it <= ' ' }
        val isPopular = mActivityAdminAddFoodBinding!!.chbPopular.isChecked
        val strOtherImages = mActivityAdminAddFoodBinding!!.edtOtherImage.text.toString().trim { it <= ' ' }
        val listImages: MutableList<Image> = ArrayList()
        if (!isEmpty(strOtherImages)) {
            val temp = strOtherImages.split(";".toRegex()).toTypedArray()
            for (strUrl in temp) {
                val image = Image(strUrl)
                listImages.add(image)
            }
        }
        if (isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDescription)) {
            Toast.makeText(this, getString(R.string.msg_description_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strPrice)) {
            Toast.makeText(this, getString(R.string.msg_price_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strDiscount)) {
            Toast.makeText(this, getString(R.string.msg_discount_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_image_food_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (isEmpty(strImageBanner)) {
            Toast.makeText(this, getString(R.string.msg_image_banner_food_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update food
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["name"] = strName
            map["description"] = strDescription
            map["price"] = strPrice.toInt()
            map["sale"] = strDiscount.toInt()
            map["image"] = strImage
            map["banner"] = strImageBanner
            map["popular"] = isPopular
            if (listImages.isNotEmpty()) {
                map["images"] = listImages
            }
            ControllerApplication[this].foodDatabaseReference
                    .child(mFood!!.id.toString()).updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                        showProgressDialog(false)
                        Toast.makeText(this@AdminAddFoodActivity,
                                getString(R.string.msg_edit_food_success), Toast.LENGTH_SHORT).show()
                        hideSoftKeyboard(this)
                    }
            return
        }

        // Add food
        showProgressDialog(true)
        val foodId = System.currentTimeMillis()
        val food = FoodObject(foodId, strName, strDescription, strPrice.toInt(), strDiscount.toInt(), strImage, strImageBanner, isPopular)
        if (listImages.isNotEmpty()) {
            food.images = listImages
        }
        ControllerApplication[this].foodDatabaseReference
                .child(foodId.toString()).setValue(food) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    mActivityAdminAddFoodBinding!!.edtName.setText("")
                    mActivityAdminAddFoodBinding!!.edtDescription.setText("")
                    mActivityAdminAddFoodBinding!!.edtPrice.setText("")
                    mActivityAdminAddFoodBinding!!.edtDiscount.setText("")
                    mActivityAdminAddFoodBinding!!.edtImage.setText("")
                    mActivityAdminAddFoodBinding!!.edtImageBanner.setText("")
                    mActivityAdminAddFoodBinding!!.chbPopular.isChecked = false
                    mActivityAdminAddFoodBinding!!.edtOtherImage.setText("")
                    hideSoftKeyboard(this)
                    Toast.makeText(this, getString(R.string.msg_add_food_success), Toast.LENGTH_SHORT).show()
                }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivityAdminAddFoodBinding!!.layoutWrap.setOnTouchListener { _, _ ->
            hideSoftKeyboard(this@AdminAddFoodActivity)
            mActivityAdminAddFoodBinding!!.edtName.clearFocus()
            mActivityAdminAddFoodBinding!!.edtDescription.clearFocus()
            mActivityAdminAddFoodBinding!!.edtPrice.clearFocus()
            mActivityAdminAddFoodBinding!!.edtDiscount.clearFocus()
            mActivityAdminAddFoodBinding!!.edtImage.clearFocus()
            mActivityAdminAddFoodBinding!!.edtImageBanner.clearFocus()
            mActivityAdminAddFoodBinding!!.edtOtherImage.clearFocus()
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLayoutEditTextsListener() {
        //Layout Name: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding!!.layoutName,
            mActivityAdminAddFoodBinding!!.edtName,
            mActivityAdminAddFoodBinding!!.imgClearName
        )

        //Layout Description: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding!!.layoutDescription,
            mActivityAdminAddFoodBinding!!.edtDescription,
            mActivityAdminAddFoodBinding!!.imgClearDescription
        )
        mActivityAdminAddFoodBinding!!.edtDescription.setImeOptions(EditorInfo.IME_ACTION_NEXT)
        mActivityAdminAddFoodBinding!!.edtDescription.setRawInputType(InputType.TYPE_CLASS_TEXT)

        //Layout Image: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding!!.layoutImage,
            mActivityAdminAddFoodBinding!!.edtImage,
            mActivityAdminAddFoodBinding!!.imgClearImage
        )

        //Layout Image Banner: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding!!.layoutImageBanner,
            mActivityAdminAddFoodBinding!!.edtImageBanner,
            mActivityAdminAddFoodBinding!!.imgClearImageBanner
        )

        //Layout Other Image: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddFoodBinding!!.layoutOtherImage,
            mActivityAdminAddFoodBinding!!.edtOtherImage,
            mActivityAdminAddFoodBinding!!.imgClearOtherImage
        )
        mActivityAdminAddFoodBinding!!.edtOtherImage.setImeOptions(EditorInfo.IME_ACTION_DONE)
        mActivityAdminAddFoodBinding!!.edtOtherImage.setRawInputType(InputType.TYPE_CLASS_TEXT)
        mActivityAdminAddFoodBinding!!.edtOtherImage.setOnActionDoneListener(
            { hideSoftKeyboard(this@AdminAddFoodActivity) },
            { mActivityAdminAddFoodBinding!!.edtOtherImage.clearFocus() }
        )

        //Layout Price: Listener focus, NO clear text icon
        mActivityAdminAddFoodBinding!!.edtPrice.setBackgroundOnEditTextFocusChange( mActivityAdminAddFoodBinding!!.layoutPrice)

        //Layout Discount: Listener focus, NO clear text icon
        mActivityAdminAddFoodBinding!!.edtDiscount.setBackgroundOnEditTextFocusChange( mActivityAdminAddFoodBinding!!.layoutDiscount)
    }
}