package com.bangnv.cafeorder.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bangnv.cafeorder.ControllerApplication
import com.bangnv.cafeorder.R
import com.bangnv.cafeorder.activity.MainActivity
import com.bangnv.cafeorder.activity.SearchActivity
import com.bangnv.cafeorder.activity.WebViewActivity
import com.bangnv.cafeorder.adapter.CartAdapter
import com.bangnv.cafeorder.adapter.CartAdapter.IClickListener
import com.bangnv.cafeorder.adapter.PaymentMethodAdapter
import com.bangnv.cafeorder.constant.Constant
import com.bangnv.cafeorder.constant.GlobalFunction
import com.bangnv.cafeorder.constant.GlobalFunction.formatNumberWithPeriods
import com.bangnv.cafeorder.constant.GlobalFunction.hideSoftKeyboard
import com.bangnv.cafeorder.constant.GlobalFunction.openActivity
import com.bangnv.cafeorder.constant.GlobalFunction.setOnActionDoneListener
import com.bangnv.cafeorder.constant.GlobalFunction.showToastMessage
import com.bangnv.cafeorder.constant.MoMoSignature
import com.bangnv.cafeorder.database.AppApi
import com.bangnv.cafeorder.database.FoodDatabase.Companion.getInstance
import com.bangnv.cafeorder.database.MoMoApi
import com.bangnv.cafeorder.databinding.FragmentCartBinding
import com.bangnv.cafeorder.event.ReloadListCartEvent
import com.bangnv.cafeorder.model.Food
import com.bangnv.cafeorder.model.Order
import com.bangnv.cafeorder.model.Payment
import com.bangnv.cafeorder.model.baseresponse.RetrofitClients
import com.bangnv.cafeorder.model.request.MoMoRequest
import com.bangnv.cafeorder.model.request.OrderRequest
import com.bangnv.cafeorder.model.responseapi.MoMoResponse
import com.bangnv.cafeorder.model.responseapi.OrderResponse
import com.bangnv.cafeorder.prefs.DataStoreManager.Companion.user
import com.bangnv.cafeorder.utils.StringUtil.isEmpty
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CartFragment : Fragment() {

    private lateinit var mFragmentCartBinding: FragmentCartBinding
    private lateinit var mCartAdapter: CartAdapter
    private var mListFoodCart: MutableList<Food> = mutableListOf()
    private var mAmount = 0
    private lateinit var mMainActivity: MainActivity
    private var mPaymentSelected: Payment? = null


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mMainActivity = context
        }
    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        mFragmentCartBinding = FragmentCartBinding.inflate(inflater, container, false)

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }

        displayListFoodInCart()
        mFragmentCartBinding.tvOrderCart.setOnClickListener { onClickOrderCart() }
        mFragmentCartBinding.btnAddFood.setOnClickListener {

            openActivity(requireContext(), SearchActivity::class.java)

            // Test Momo with auto generate Order
//            val orderIdAuto = System.currentTimeMillis()
//            val moMoReQuestObjectTest = generateMoMoRequestObject(
//                orderIdAuto. toString(), "16000"
//            )
//            requestMoMoQRTest(moMoReQuestObjectTest)
        }

        setupTouchOtherToClearAllFocus()
        setupLayoutEditTextNoteListener()

        return mFragmentCartBinding.root
    }

    private fun displayListFoodInCart() {
        if (activity == null) {
            return
        }
        val linearLayoutManager = LinearLayoutManager(activity)
        mFragmentCartBinding.rcvFoodCart.layoutManager = linearLayoutManager
//        val itemDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL)
//        mFragmentCartBinding.rcvFoodCart.addItemDecoration(itemDecoration)
        initDataFoodCart()
    }

    private fun initDataFoodCart() {
        mListFoodCart = mutableListOf()
        mListFoodCart = getInstance(requireActivity())!!.foodDAO()!!.listFoodCart!!
        if (mListFoodCart.isEmpty()) {
            return
        }
        mCartAdapter = CartAdapter(mListFoodCart, object : IClickListener {
            override fun clickDeleteFood(food: Food?, position: Int) {
                deleteFoodFromCart(food, position)
            }

            override fun updateItemFood(food: Food?, position: Int) {
                getInstance(requireActivity())!!.foodDAO()!!.updateFood(food)
                mCartAdapter.notifyItemChanged(position)
                calculateTotalPrice()
            }
        })

        mFragmentCartBinding.rcvFoodCart.itemAnimator = null
        mFragmentCartBinding.rcvFoodCart.adapter = mCartAdapter

        if (mCartAdapter.itemCount == 0) {
            mFragmentCartBinding.layoutCartWrap.visibility = View.GONE
        } else {
            mFragmentCartBinding.layoutCartWrap.visibility = View.VISIBLE
        }
        calculateTotalPrice()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun clearCart() {
        mListFoodCart.clear()
        mCartAdapter.notifyDataSetChanged()
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        val listFoodCart = getInstance(requireActivity())!!.foodDAO()!!.listFoodCart
        if (listFoodCart.isNullOrEmpty()) {
            val strZero: String = formatNumberWithPeriods(0) + Constant.CURRENCY
            mFragmentCartBinding.tvSubTotalPrice.text = strZero
            mAmount = 0
            return
        }
        var subTotalPrice = 0
        for (food in listFoodCart) {
            subTotalPrice += food.totalPrice
        }
        val strSubTotalPrice: String = formatNumberWithPeriods(subTotalPrice) + Constant.CURRENCY
        mFragmentCartBinding.tvSubTotalPrice.text = strSubTotalPrice
        mAmount = subTotalPrice
    }

    private fun deleteFoodFromCart(food: Food?, position: Int) {
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.confirm_delete_food))
            .setMessage(getString(R.string.message_delete_food))
            .setPositiveButton(getString(R.string.delete)) { _: DialogInterface?, _: Int ->
                getInstance(requireActivity())!!.foodDAO()!!.deleteFood(food)
                mListFoodCart.removeAt(position)
                mCartAdapter.notifyItemRemoved(position)
                if (mCartAdapter.itemCount == 0) {
                    mFragmentCartBinding.layoutCartWrap.visibility = View.GONE
                } else {
                    mFragmentCartBinding.layoutCartWrap.visibility = View.VISIBLE
                }
                calculateTotalPrice()
            }
            .setNegativeButton(getString(R.string.dialog_cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .show()
    }

    @SuppressLint("ResourceType", "InflateParams")
    private fun onClickOrderCart() {
        if (mListFoodCart.isEmpty()) {
            return
        }

        //Init Custom Dialog
        val viewDialog = Dialog(requireContext())
        viewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewDialog.setContentView(R.layout.layout_bottom_sheet_order)

        // init ui
        val tvFoodsOrder = viewDialog.findViewById<TextView>(R.id.tv_foods_order)
        val spnPayment= viewDialog.findViewById<Spinner>(R.id.spn_payment)
        val edtNameOrder = viewDialog.findViewById<TextView>(R.id.edt_name_order)
        val edtPhoneOrder = viewDialog.findViewById<TextView>(R.id.edt_phone_order)
        val edtAddressOrder = viewDialog.findViewById<TextView>(R.id.edt_address_order)
        val tvSubTotal = viewDialog.findViewById<TextView>(R.id.tv_sub_total)
        val tvDeliveryFee = viewDialog.findViewById<TextView>(R.id.tv_delivery_fee)
        val tvTotalPrice = viewDialog.findViewById<TextView>(R.id.tv_total_price)
        val tvCancelOrder = viewDialog.findViewById<TextView>(R.id.tv_cancel_order)
        val tvCreateOrder = viewDialog.findViewById<TextView>(R.id.tv_create_order)

        //Init Spinner
        val  listPayment = mutableListOf(
            Payment(Constant.NAME_PAYMENT_COD, Constant.TYPE_PAYMENT_COD),
            Payment(Constant.NAME_PAYMENT_WALLET, Constant.TYPE_PAYMENT_WALLET)
        )
        val paymentMethodAdapter = PaymentMethodAdapter(requireContext(),
            R.layout.item_choose_option, listPayment)
        spnPayment.adapter = paymentMethodAdapter
        // Default payment method selection
        val defaultSelection = 1 // chỉ mục của mục bạn muốn chọn mặc định (0 cho mục đầu tiên, 1 cho mục thứ hai, và cứ thế)
        spnPayment.setSelection(defaultSelection)

        spnPayment.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
                mPaymentSelected = paymentMethodAdapter.getItem(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        // Set data
        tvFoodsOrder.text = getStringListFoodsOrder()
        tvSubTotal.text = mFragmentCartBinding.tvSubTotalPrice.text.toString()
            // thêm tiền ship và tính lại tổng
        val shippingFee = 15
        val totalPrice = mAmount + shippingFee
        val strShippingFee: String = formatNumberWithPeriods(shippingFee) + Constant.CURRENCY
        val strTotalPrice: String = formatNumberWithPeriods(totalPrice) + Constant.CURRENCY
        tvDeliveryFee.text = strShippingFee
        tvTotalPrice.text = strTotalPrice

        // Set listeners
        tvCancelOrder.setOnClickListener { viewDialog.dismiss() }
        tvCreateOrder.setOnClickListener {
            val strName = edtNameOrder.text.toString().trim { it <= ' ' }
            val strPhone = edtPhoneOrder.text.toString().trim { it <= ' ' }
            val strAddress = edtAddressOrder.text.toString().trim { it <= ' ' }
            val strNote = getStringNoteOrder()
            if (isEmpty(strName) || isEmpty(strPhone) || isEmpty(strAddress)) {
                showToastMessage(activity, getString(R.string.message_enter_infor_order))
            } else {
                val id = System.currentTimeMillis()
                val strEmail = user!!.email

                // Payment COD or Payment WALLET
                val paymentCode = if (mPaymentSelected!!.code == Constant.TYPE_PAYMENT_COD) {
                    Constant.CODE_NEW_ORDER
                } else {
                    Constant.CODE_NEW_MOMO_UNPAID
                }
                val order = Order(
                    id, strName, strEmail, strPhone, strAddress,mAmount, getStringListFoodsOrder(),
                    mPaymentSelected!!.code,strNote, paymentCode, shippingFee, totalPrice
                )

                // Set Data on Realtime Database
                setDataOnRealtimeDatabase(order, viewDialog)

            }
        }

        // Show dialog + set Customize
        viewDialog.show()
        GlobalFunction.customizeBottomSheetDialog(viewDialog)
    }

    private fun setDataOnRealtimeDatabase(order: Order, viewDialog: Dialog) {
        ControllerApplication[requireActivity()].bookingDatabaseReference
            .child(order.id.toString())
            .setValue(order) { databaseError: DatabaseError?, _: DatabaseReference? ->
                if (databaseError == null) {
                    // Write database success

                    if(order.payment == Constant.TYPE_PAYMENT_COD){
                        // Notification for admin (admins)

                        clearWidgetOrderAndCart(viewDialog)
                        sendNotiNewOrderToAdmins(viewDialog, order.email, order.id.toString())
                    } else {
                        // Create MoMoRequest object and Post Request
                        val moMoReQuestObject = generateMoMoRequestObject(
                            order.id.toString(), (1000 * order.totalPrice).toString()
                        )
                        Log.d("moMoRequest DB: ", "orderId: " + order.id.toString())
                        Log.d("moMoRequest DB: ", "amountMomo(totalPrice): " + order.totalPrice.toString())

                        requestMoMoQR(viewDialog, moMoReQuestObject)

                    }

                } else {
                    // Xảy ra lỗi khi ghi dữ liệu
                    Log.e("setDataOnRealtimeDB", "Error: ${databaseError.message}")
                }
            }
    }

    private fun clearWidgetOrderAndCart(viewDialog: Dialog) {
        hideSoftKeyboard(requireActivity())
        viewDialog.dismiss()

        mFragmentCartBinding.edtNote.setText("")
        getInstance(requireActivity())!!.foodDAO()!!.deleteAllFood()
        clearCart()
        mFragmentCartBinding.layoutCartWrap.visibility = View.GONE
    }


    private fun generateMoMoRequestObject(orderId: String, amount: String) : MoMoRequest{
        val partnerCode = Constant.MOMO_PARTNER_CODE
        val partnerName = Constant.MOMO_PARTNER_NAME
        val storeId = Constant.MOMO_STORE_ID
        val orderInfo = Constant.MOMO_ORDER_INFOR
        val redirectUrl = Constant.MOMO_REDIRECT_URL
        val ipnUrl = Constant.MOMO_IPN_URL
        val lang = Constant.MOMO_LANG
        val requestType = Constant.MOMO_REQUEST_TYPE
        val autoCapture = Constant.MOMO_AUTO_CAPTURE
        val extraData = Constant.MOMO_EXTRA_DATA
        val orderGroupId = Constant.MOMO_ORDER_GROUP_ID
        val requestId = "${partnerCode}${orderId}"

        val rawSignature = "accessKey=${Constant.MOMO_ACCESS_KEY}&amount=$amount&extraData=$extraData&ipnUrl=$ipnUrl&orderId=$orderId&orderInfo=$orderInfo&partnerCode=$partnerCode&redirectUrl=$redirectUrl&requestId=$requestId&requestType=$requestType"
        val signature = MoMoSignature.generateSignature(rawSignature)


        return MoMoRequest(
            partnerCode, partnerName, storeId, requestId, amount, orderId, orderInfo, redirectUrl,
            ipnUrl, lang, requestType, autoCapture, extraData, orderGroupId, signature
        )
    }

    private fun requestMoMoQR(viewDialog: Dialog, moMoRequest: MoMoRequest) {
        // Hàm này chỉ làm chức năng: Gửi Request lên:
        //      + Nếu response về resultCode == 0 (thành công)
        //          => server NodeJS tự cập nhật lại trên Firebase
        //      + Mình nhận response thì mở link mã QR trên app

        Log.d("moMoApi: ", "momorequest: " + moMoRequest.toString())

//        (activity as? MainActivity)?.showProgressDialog(true)

        val moMoApi = RetrofitClients.getMoMoInstance().create(MoMoApi::class.java)
        moMoApi.requestQRMoMo(moMoRequest).enqueue(object  : Callback<MoMoResponse> {
            override fun onResponse(call: Call<MoMoResponse>, response: Response<MoMoResponse>) {

//                (activity as? MainActivity)?.showProgressDialog(false)
                Log.d("moMoApi: ", "onResponse")

                if (response.isSuccessful) {
                    Log.d("moMoApi: ", "onResponse isSuccessful")

                    val moMoResponse = response.body()
                    if (moMoResponse != null) {

                        Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse != null")
                        val resultCode = moMoResponse.resultCode
                        val payUrl = moMoResponse.payUrl

                        if (resultCode == 0) {

                            Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse != null;  resultCode == 0")

                            if (!payUrl.isNullOrEmpty()) {
                                Log.d("MoMoResponse payUrl: ", payUrl)
                                Log.d("moMoApi: payUrl", payUrl)

                                clearWidgetOrderAndCart(viewDialog)

                                // Mở bằng trình duyệt
//                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
//                                requireContext().startActivity(intent)

                                // Mở bằng WebViewActivity (có WebView dạng view desktop)
                                val bundle = Bundle().apply {
                                    putString(Constant.KEY_INTENT_URL, payUrl)
                                }
                                openActivity(requireContext(), WebViewActivity::class.java, bundle)
                            }
                        } else {
                            Log.e("MoMoResponse rsCode!=0", "resultCode != 0 (Not Success)")
                            Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse != null;  resultCode !!!= 0")

                        }
                    } else {
                        // Đối tượng MoMoResponse trả về là null
                        Log.e("MoMoResponse bodyNull: ", "MoMoResponse object is null")
                        // Xử lý trường hợp này nếu cần thiết
                        Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse == null")

                    }
                } else {
                    // Response không thành công
                    Log.e("MoMoResponse notSuccess", "Response is not successful: Code: ${response.code()}")
                    if (response.body() != null) {
                        Log.e("MoMoResponse notSucc", "ResultCode: ${response.body()?.resultCode}")
                    }
                    // Xử lý trường hợp này nếu cần thiết
                    Log.d("moMoApi: ", "onResponse Not isSuccessful")
                }
            }

            override fun onFailure(call: Call<MoMoResponse>, t: Throwable) {
//                (activity as? MainActivity)?.showProgressDialog(false)
                // Xử lý trường hợp lỗi khi gửi request
                Log.e("MoMoResponse onFailure", "Request failed: ${t.message}")
                // Tùy thuộc vào nhu cầu của bạn, bạn có thể hiển thị thông báo cho người dùng, vv.
                Log.e("moMoApi: ", "onFailure")
            }
        })

    }

    private fun requestMoMoQRTest(moMoRequest: MoMoRequest) {
        // Hàm này chỉ làm chức năng: Gửi Request lên:
        //      + Nếu response về resultCode == 0 (thành công)
        //          => server NodeJS tự cập nhật lại trên Firebase
        //      + Mình nhận response thì mở link mã QR trên app

        (activity as? MainActivity)?.showProgressDialog(true)

        val moMoApi = RetrofitClients.getMoMoInstance().create(MoMoApi::class.java)
        moMoApi.requestQRMoMo(moMoRequest).enqueue(object  : Callback<MoMoResponse> {
            override fun onResponse(call: Call<MoMoResponse>, response: Response<MoMoResponse>) {

                (activity as? MainActivity)?.showProgressDialog(false)
                Log.d("moMoApi: ", "onResponse")

                if (response.isSuccessful) {
                    Log.d("moMoApi: ", "onResponse isSuccessful")


                    val moMoResponse = response.body()
                    if (moMoResponse != null) {

                        Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse != null")
                        val resultCode = moMoResponse.resultCode
                        val payUrl = moMoResponse.payUrl

                        if (resultCode == 0) {

                            Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse != null;  resultCode == 0")

                            if (!payUrl.isNullOrEmpty()) {
                                Log.d("MoMoResponse payUrl: ", payUrl)
                                Log.d("moMoApi: payUrl", payUrl)

                                // Mở bằng trình duyệt
//                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(payUrl))
//                                requireContext().startActivity(intent)

                                // Mở bằng WebViewActivity (có WebView dạng view desktop)
                                val bundle = Bundle().apply {
                                    putString("URL", payUrl)
                                }
                                openActivity(requireContext(), WebViewActivity::class.java, bundle)
                            }
                        } else {
                            Log.e("MoMoResponse rsCode!=0", "resultCode != 0 (Not Success)")
                            Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse != null;  resultCode !!!= 0")

                        }
                    } else {
                        // Đối tượng MoMoResponse trả về là null
                        Log.e("MoMoResponse bodyNull: ", "MoMoResponse object is null")
                        // Xử lý trường hợp này nếu cần thiết
                        Log.d("moMoApi: ", "onResponse isSuccessful moMoResponse == null")

                    }
                } else {
                    // Response không thành công
                    Log.e("MoMoResponse notSuccess", "Response is not successful: Code: ${response.code()}")
                    if (response.body() != null) {
                        Log.e("MoMoResponse notSucc", "ResultCode: ${response.body()?.resultCode}")
                    }
                    // Xử lý trường hợp này nếu cần thiết
                    Log.d("moMoApi: ", "onResponse Not isSuccessful")
                }
            }

            override fun onFailure(call: Call<MoMoResponse>, t: Throwable) {
                (activity as? MainActivity)?.showProgressDialog(false)
                // Xử lý trường hợp lỗi khi gửi request
                Log.e("MoMoResponse onFailure", "Request failed: ${t.message}")
                // Tùy thuộc vào nhu cầu của bạn, bạn có thể hiển thị thông báo cho người dùng, vv.
                Log.e("moMoApi: ", "onFailure")
            }
        })

    }

    private fun sendNotiNewOrderToAdmins(
        viewDialog: Dialog,
        userEmailRequest: String?,
        orderIdRequest: String
    ) {
        (activity as? MainActivity)?.showProgressDialog(true)

        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postNewOrder(OrderRequest(userEmailRequest, orderIdRequest)).enqueue(object : Callback<Unit> {

            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                (activity as? MainActivity)?.showProgressDialog(false)

                if (response.body() != null) {
                    Log.d("onSuccess", "Gửi thông báo thành công!")
                    // Thông báo cho cả tạo đơn thành công + thông báo
                    Toast.makeText(requireContext(), getString(R.string.msg_order_success), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                (activity as? MainActivity)?.showProgressDialog(false)
                Toast.makeText(context, "Lỗi onFailure: "+ t.message.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postNewOrder(OrderRequest(userEmailRequest, orderIdRequest)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postNewOrder(OrderRequest(userEmailRequest, orderIdRequest)).request().body}")

    }

    private fun sendNotificationToAdminsHasResponse(userEmailRequest: String?, orderIdRequest: String) {
        (activity as? MainActivity)?.showProgressDialog(true)

        val appApi: AppApi = RetrofitClients.getInstance().create(AppApi::class.java)
        appApi.postNewOrderHasResponse(OrderRequest(userEmailRequest, orderIdRequest)).enqueue(object : Callback<OrderResponse> {
            // thực chất không cần response về. nốt công làm ok thì để đây làm ví dụ cho project kotlin khác (trước kia làm cái này ở java)

            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {

                if (response.body() != null) {
                    (activity as? MainActivity)?.showProgressDialog(false)
                    Log.d("onSuccess", "Gửi thông báo thành công!")
                } else {
                    Toast.makeText(context, getString(R.string.msg_cant_connect_server), Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                (activity as? MainActivity)?.showProgressDialog(false)
                Toast.makeText(context, "Lỗi onFailure: "+ t.message.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        // Log ra đường link của request
        Log.d("Retrofit Request", "URL: ${appApi.postNewOrderHasResponse(OrderRequest(userEmailRequest, orderIdRequest)).request().url}")
        Log.d("Retrofit Request: ", "${appApi.postNewOrderHasResponse(OrderRequest(userEmailRequest, orderIdRequest)).request().body}")
    }

    private fun getStringListFoodsOrder(): String {
        if (mListFoodCart.isEmpty()) {
            return ""
        }
        var result = ""
        for (food in mListFoodCart) {
            result = if (isEmpty(result)) {
                ("- " + food.name + " (" + formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count)
            } else {
                (result + "\n" + ("- " + food.name + " (" + formatNumberWithPeriods(food.realPrice) + Constant.CURRENCY + ") "
                        + "- " + getString(R.string.quantity) + " " + food.count))
            }
        }
        return result
    }

    private fun getStringNoteOrder(): String {
        val note = mFragmentCartBinding.edtNote.text.toString().ifBlank {
            getString(R.string.str_no_note)
        }
        return note
    }

    private fun setupTouchOtherToClearAllFocus() {
        mFragmentCartBinding.layoutWrap.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentCartBinding.edtNote.clearFocus()
        }
        mFragmentCartBinding.layoutCartWrap.setOnClickListener {
            hideSoftKeyboard(requireActivity())
            mFragmentCartBinding.edtNote.clearFocus()
        }
    }

    private fun setupLayoutEditTextNoteListener() {
        GlobalFunction.setupLayoutEditTextWithIconClearListeners(
            mFragmentCartBinding.layoutNote,
            mFragmentCartBinding.edtNote,
            mFragmentCartBinding.imgClearNote
        )
        mFragmentCartBinding.edtNote.setOnActionDoneListener(
            { hideSoftKeyboard(requireActivity()) },
            { mFragmentCartBinding.edtNote.clearFocus() }
        )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: ReloadListCartEvent?) {
        displayListFoodInCart()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}