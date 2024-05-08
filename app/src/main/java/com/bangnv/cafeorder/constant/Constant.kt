package com.bangnv.cafeorder.constant

interface Constant {
    companion object {
        const val GENERIC_ERROR = "General error, please try again later"
        const val PAGE_FACEBOOK = ""
        const val LINK_FACEBOOK = "https://www.facebook.com/bangnv26"
        const val LINK_YOUTUBE = "https://www.youtube.com/@bangnguyenvan7151"
        const val PHONE_NUMBER = "+84 356 265 666"
        const val GMAIL = "bangnv.it@gmail.com"
        const val SKYPE_ID = "bangofficial.no1_1" // "live:bangofficial.no1_1"
        const val ZALO_LINK = "https://zalo.me/0356265666"
        const val FIREBASE_URL = "https://cafeorder-f666-default-rtdb.firebaseio.com"
        const val NAME_PAYMENT_COD = "Thanh toán khi nhận tiền (COD)"
        const val NAME_PAYMENT_WALLET = "Thanh toán bằng Ví MoMo"
        const val TYPE_PAYMENT_COD = 1
        const val TYPE_PAYMENT_WALLET = 2
        const val PAYMENT_METHOD_COD = "Khi nhận hàng (COD)"
        const val PAYMENT_METHOD_WALLET = "Ví điện tử MoMo"
        const val CURRENCY = ".000 VNĐ"

        const val ADMIN_EMAIL_FORMAT = "@admin.com"
        const val DRIVER_EMAIL_FORMAT = "@driver.com"

        const val NEXT = "IME_ACTION_NEXT"
        const val DONE = "IME_ACTION_DONE"
        const val SEARCH = "IME_ACTION_SEARCH"

        // Key Intent
        const val KEY_INTENT_CATEGORY_OBJECT = "category_object"
        const val KEY_INTENT_FOOD_OBJECT = "food_object"
        const val KEY_INTENT_ORDER_OBJECT = "order_object"
        const val KEY_INTENT_ADMIN_ORDER_OBJECT = "admin_order_object"
        const val KEY_INTENT_FROM_HOME = "admin_order_object"
        const val KEY_INTENT_URL = "URL"


        // User Type
        const val TYPE_USER_ADMIN = 1       // Admin
        const val TYPE_USER_USER = 2       // User
        const val TYPE_USER_DRIVER = 3       // Driver

        //    Status Code:
        const val CODE_NEW_ORDER = 30       // Chờ xác nhận
        const val CODE_PREPARING = 31       // Đang chuẩn bị
        const val CODE_SHIPPING = 32        // Đang giao hàng
        const val CODE_COMPLETED = 33       // Đã giao thành công
        const val CODE_CANCELLED = 34       // Đã hủy
        const val CODE_FAILED = 35          // Thât bại
        const val CODE_NEW_MOMO_UNPAID = 1  // Phương thức thanh toán MoMo nhưng chưa tiến hành thanh toán,
                                                // sẽ không hiển thị ở danh sách nhìn thấy của Admin và người dùng
                                                // Mà chỉ đưa lên DB với trạng thái này thôi

        // Viết đây nhưng chưa làm cái liên quan ship, vận chuyển
        //Fees
        const val FEE_DEFAULT = 15      // Fee shipping
        const val FEE_SERVICE = 3       // Fee service
        const val FEE_PER_KM = 5        // Fee/km next

        //KM has same shipping fee
        const val KM_DEFAULT = 3


        //    Status Text
        const val TEXT_ALL_ORDER: String = "Tất cả"
        const val TEXT_NEW_ORDER: String = "Chờ xác nhận"
        const val TEXT_PREPARING: String = "Đang chuẩn bị"
        const val TEXT_SHIPPING: String = "Đang giao hàng"
        const val TEXT_COMPLETED: String = "Đã giao thành công"
        const val TEXT_CANCELLED: String = "Đã hủy"
        const val TEXT_FAILED: String = "Thất bại"


        // Max Item/ 1 load
        const val MAX_ITEM_PER_LOAD_LINEAR: Int = 10
        const val MAX_ITEM_PER_LOAD_GRID: Int = 20


        // MOMO Request
        const val MOMO_PARTNER_CODE = "MOMO"
        const val MOMO_PARTNER_NAME = "Test"
        const val MOMO_STORE_ID = "MomoTestStore"
        const val MOMO_ORDER_INFOR = "Thanh toán bằng MoMo"
        const val MOMO_REDIRECT_URL = "https://coffeenbnode.onrender.com/api/redirectUrl/success"
        const val MOMO_IPN_URL = "https://coffeenbnode.onrender.com/api/ipnUrl/success"
//        const val MOMO_REDIRECT_URL = "http://192.168.0.102:3006/api/redirectUrl/success"
//        const val MOMO_IPN_URL = "http://192.168.0.102:3006/api/ipnUrl/success"
        const val MOMO_LANG = "vi"
        const val MOMO_REQUEST_TYPE = "captureWallet"
        const val MOMO_AUTO_CAPTURE = true
        const val MOMO_EXTRA_DATA = ""
        const val MOMO_ORDER_GROUP_ID = ""

        //MOMO AccessKey:
        const val MOMO_ACCESS_KEY = "F8BBA842ECF85"

    }
}