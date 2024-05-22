package com.bangnv.cafeorder.activity.admin

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.BaseActivity
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionDoneListener
import com.bangnv.cafeorder.databinding.ActivityAdminAddCategoryBinding
import com.bangnv.cafeorder.model.Category
import com.bangnv.cafeorder.utils.StringUtil
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import java.util.HashMap

class AdminAddCategoryActivity : BaseActivity()  {

    private lateinit var mActivityAdminAddCategoryBinding: ActivityAdminAddCategoryBinding
    private var isUpdate = false
    private var mCategory: Category? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivityAdminAddCategoryBinding = ActivityAdminAddCategoryBinding.inflate(layoutInflater)
        setContentView(mActivityAdminAddCategoryBinding.root)

        getDataIntent()
        initToolbar()
        initView()
        mActivityAdminAddCategoryBinding.btnAddOrEdit.setOnClickListener { addOrEditCategory() }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextsListener()
    }

    private fun getDataIntent() {
        val bundleReceived = intent.extras
        if (bundleReceived != null) {
            isUpdate = true
            mCategory = bundleReceived[Constant.KEY_INTENT_CATEGORY_OBJECT] as Category?
        }
    }

    private fun initToolbar() {
        mActivityAdminAddCategoryBinding.toolbar.imgBack.visibility = View.VISIBLE
        mActivityAdminAddCategoryBinding.toolbar.imgCart.visibility = View.GONE
        mActivityAdminAddCategoryBinding.toolbar.imgBack.setOnClickListener { onBackPressed() }
    }

    private fun initView() {
        if (isUpdate) {
            mActivityAdminAddCategoryBinding.toolbar.tvTitle.text = getString(R.string.edit_category)
            mActivityAdminAddCategoryBinding.btnAddOrEdit.text = getString(R.string.action_edit)
            mActivityAdminAddCategoryBinding.edtName.setText(mCategory!!.name)
            mActivityAdminAddCategoryBinding.edtImage.setText(mCategory!!.image)
        } else {
            mActivityAdminAddCategoryBinding.toolbar.tvTitle.text = getString(R.string.add_category)
            mActivityAdminAddCategoryBinding.btnAddOrEdit.text = getString(R.string.action_add)
        }
    }

    private fun addOrEditCategory() {
        val strName = mActivityAdminAddCategoryBinding.edtName.text.toString().trim { it <= ' ' }
        val strImage = mActivityAdminAddCategoryBinding.edtImage.text.toString().trim { it <= ' ' }
        if (StringUtil.isEmpty(strName)) {
            Toast.makeText(this, getString(R.string.msg_name_category_require), Toast.LENGTH_SHORT).show()
            return
        }
        if (StringUtil.isEmpty(strImage)) {
            Toast.makeText(this, getString(R.string.msg_url_category_require), Toast.LENGTH_SHORT).show()
            return
        }

        // Update food
        if (isUpdate) {
            showProgressDialog(true)
            val map: MutableMap<String, Any> = HashMap()
            map["name"] = strName
            map["image"] = strImage
            ControllerApplication[this].categoryDatabaseReference
                .child(mCategory!!.id.toString()).updateChildren(map) { _: DatabaseError?, _: DatabaseReference? ->
                    showProgressDialog(false)
                    Toast.makeText(this@AdminAddCategoryActivity,
                        getString(R.string.msg_edit_category_success), Toast.LENGTH_SHORT).show()
                    GlobalFunction.hideSoftKeyboard(this)
                }
            return
        }

        // Add Category
        // Add category
        showProgressDialog(true)
        val categoryId = System.currentTimeMillis()
        val category = Category(categoryId, strName, strImage)
        ControllerApplication[this].categoryDatabaseReference.child(categoryId.toString())
            .setValue(category) { _: DatabaseError?, _: DatabaseReference? ->
                showProgressDialog(false)
                mActivityAdminAddCategoryBinding.edtName.setText("")
                mActivityAdminAddCategoryBinding.edtImage.setText("")
                GlobalFunction.hideSoftKeyboard(this)
                Toast.makeText(this, getString(R.string.msg_add_category_successfully),
                    Toast.LENGTH_SHORT).show()
            }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupTouchOtherToClearAllFocus() {
        mActivityAdminAddCategoryBinding.layoutWrap.setOnTouchListener { _, _ ->
            GlobalFunction.hideSoftKeyboard(this@AdminAddCategoryActivity)
            mActivityAdminAddCategoryBinding.edtName.clearFocus()
            mActivityAdminAddCategoryBinding.edtImage.clearFocus()
            false
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupLayoutEditTextsListener() {
        //Layout Name: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddCategoryBinding.layoutName,
            mActivityAdminAddCategoryBinding.edtName,
            mActivityAdminAddCategoryBinding.imgClearName
        )
        
        //Layout Image: Listener focus, clear text icon
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mActivityAdminAddCategoryBinding.layoutImage,
            mActivityAdminAddCategoryBinding.edtImage,
            mActivityAdminAddCategoryBinding.imgClearImage
        )
        
        mActivityAdminAddCategoryBinding.edtImage.imeOptions = EditorInfo.IME_ACTION_DONE
        mActivityAdminAddCategoryBinding.edtImage.setRawInputType(InputType.TYPE_CLASS_TEXT)
        mActivityAdminAddCategoryBinding.edtImage.setOnActionDoneListener(
            { GlobalFunction.hideSoftKeyboard(this@AdminAddCategoryActivity) },
            { mActivityAdminAddCategoryBinding.edtImage.clearFocus() }
        )
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}