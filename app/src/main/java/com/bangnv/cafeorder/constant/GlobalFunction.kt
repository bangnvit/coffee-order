package com.bangnv.cafeorder.constant

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.AdminMainActivity
import com.bangnv.cafeorder.activity.MainActivity
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionDoneListener
import com.bangnv.cafeorder.listener.IGetDateListener
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.utils.StringUtil.getDoubleNumber
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import java.text.Normalizer
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

object GlobalFunction {

    fun startActivity(context: Context, clz: Class<*>?) {
        val intent = Intent(context, clz)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun startActivity(context: Context, clz: Class<*>?, bundle: Bundle?) {
        val intent = Intent(context, clz)
        intent.putExtras(bundle!!)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @JvmStatic
    fun gotoMainActivity(context: Context) {
        if (user!!.isAdmin) {
            startActivity(context, AdminMainActivity::class.java)
        } else {
            startActivity(context, MainActivity::class.java)
        }
    }

    @JvmStatic
    fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager =
                activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (ex: NullPointerException) {
            ex.printStackTrace()
        }
    }

    fun showMessageError(activity: Activity?) {
        Toast.makeText(activity, Constant.GENERIC_ERROR, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun onClickOpenGmail(context: Context) {
        val emailIntent = Intent(
            Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", Constant.GMAIL, null
            )
        )
        context.startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    @JvmStatic
    fun onClickOpenSkype(context: Context) {
        try {
            val skypeUri = Uri.parse("skype:" + Constant.SKYPE_ID + "?chat")
            context.packageManager.getPackageInfo("com.skype.raider", 0)
            val skypeIntent = Intent(Intent.ACTION_VIEW, skypeUri)
            skypeIntent.component = ComponentName("com.skype.raider", "com.skype.raider.Main")
            context.startActivity(skypeIntent)
        } catch (e: Exception) {
            openSkypeWebview(context)
        }
    }

    private fun openSkypeWebview(context: Context) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("skype:" + Constant.SKYPE_ID + "?chat")
                )
            )
        } catch (exception: Exception) {
            val skypePackageName = "com.skype.raider"
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$skypePackageName")
                    )
                )
            } catch (anfe: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$skypePackageName")
                    )
                )
            }
        }
    }

    @JvmStatic
    fun onClickOpenFacebook(context: Context) {
        var intent: Intent
        try {
            var urlFacebook: String = Constant.PAGE_FACEBOOK
            val packageManager = context.packageManager
            val versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode
            if (versionCode >= 3002850) { //newer versions of fb app
                urlFacebook = "fb://facewebmodal/f?href=" + Constant.LINK_FACEBOOK
            }
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlFacebook))
        } catch (e: Exception) {
            intent = Intent(Intent.ACTION_VIEW, Uri.parse(Constant.LINK_FACEBOOK))
        }
        context.startActivity(intent)
    }

    @JvmStatic
    fun onClickOpenYoutubeChannel(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constant.LINK_YOUTUBE)))
    }

    @JvmStatic
    fun onClickOpenZalo(context: Context) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constant.ZALO_LINK)))
    }

    @JvmStatic
    fun onClickOpenDial(context: Context) {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Constant.PHONE_NUMBER}")))
    }

    @JvmStatic
    fun callPhoneNumber(activity: Activity) {
        try {
            if (Build.VERSION.SDK_INT > 22) {
                if (ActivityCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.CALL_PHONE
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        101
                    )
                    return
                }
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:" + Constant.PHONE_NUMBER)
                activity.startActivity(callIntent)
            } else {
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:" + Constant.PHONE_NUMBER)
                activity.startActivity(callIntent)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    @JvmStatic
    fun showToastMessage(context: Context?, message: String?) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun getTextSearch(input: String?): String {
        val nfdNormalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        val pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString).replaceAll("")
    }

    @JvmStatic
    fun showDatePicker(context: Context?, currentDate: String, getDateListener: IGetDateListener) {
        val mCalendar = Calendar.getInstance()
        var currentDay = mCalendar[Calendar.DATE]
        var currentMonth = mCalendar[Calendar.MONTH]
        var currentYear = mCalendar[Calendar.YEAR]
        mCalendar[currentYear, currentMonth] = currentDay
        if (!isEmpty(currentDate)) {
            val split = currentDate.split("/".toRegex()).toTypedArray()
            currentDay = split[0].toInt()
            currentMonth = split[1].toInt()
            currentYear = split[2].toInt()
            mCalendar[currentYear, currentMonth - 1] = currentDay
        }
        val callBack =
            OnDateSetListener { _: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                val date = getDoubleNumber(dayOfMonth) + "/" +
                        getDoubleNumber(monthOfYear + 1) + "/" + year
                getDateListener.getDate(date)
            }
        val datePicker = DatePickerDialog(
            context!!,
            callBack, mCalendar[Calendar.YEAR], mCalendar[Calendar.MONTH],
            mCalendar[Calendar.DATE]
        )
        datePicker.show()
    }

    fun setPasswordVisibility(
        isPasswordVisible: Boolean,
        editText: EditText,
        imageView: ImageView
    ) {
        val newVisibility = !isPasswordVisible
        if (newVisibility) { // Show password
            editText.transformationMethod = null
            imageView.setImageResource(R.drawable.ic_hide)
        } else { // Hide password
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_show)
        }
        // Nove cursor to end of EditText
        editText.setSelection(editText.text.length)
    }

    fun EditText.changeBackgroundOnFocusChange(layout: View) {
        this.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                layout.setBackgroundResource(R.drawable.bg_edittext_active)
            } else {
                layout.setBackgroundResource(R.drawable.bg_edittext_inactive)
            }
        }
    }

    fun EditText.addClearButtonListener(clearButton: ImageView) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                clearButton.visibility = if (s.isNullOrEmpty()) View.GONE else View.VISIBLE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    fun EditText.setOnActionNextListener(action: () -> Unit) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                action()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun EditText.setOnActionDoneListener(vararg actions: () -> Unit) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                actions.forEach { it.invoke() }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun EditText.setOnActionSearchListener(vararg actions: () -> Unit) {
        this.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                actions.forEach { it.invoke() }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    fun setupLayoutEditTextListeners(
        layoutEditText: View,
        editText: EditText,
        imgClear: ImageView,
        activity: Activity
    ) {
        // Change background when check focus
        editText.changeBackgroundOnFocusChange(layoutEditText)
        // set visibility icon clear
        editText.addClearButtonListener(imgClear)
        // Set text ""
        imgClear.setOnClickListener {
            editText.setText("")
        }
        // Action Done: auto Check
        editText.setOnActionDoneListener(
            { hideSoftKeyboard(activity) },
            { editText.clearFocus() }
        )
        editText.setOnActionSearchListener(
            { hideSoftKeyboard(activity) },
            { editText.clearFocus() }
        )
    }

    fun setupLayoutPasswordListeners(
        layoutEditText: View,
        edtPassword: EditText,
        activity: Activity
    ) {
        // Change background when check focus
        edtPassword.changeBackgroundOnFocusChange(layoutEditText)

        // Action Done: auto Check
        edtPassword.setOnActionDoneListener(
            { hideSoftKeyboard(activity) },
            { edtPassword.clearFocus() }
        )
    }
}