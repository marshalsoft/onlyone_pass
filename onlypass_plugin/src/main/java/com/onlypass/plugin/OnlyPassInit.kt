package com.onlypass.plugin

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap


class OnlyPassInit constructor(val context: Context,akey:String,
                              mid: String) {
    var apikey:String = akey
    var merchant_id:String = mid
    private var mcontext:Context

    lateinit var al:AlertDialog.Builder
    lateinit var bottomSheetDialog:BottomSheetDialog
    lateinit var respDate:String
    lateinit var BrowserWrp:ConstraintLayout
    lateinit var BtnWrp:LinearLayout
    lateinit var WebpayView:WebView
    lateinit var BlindWrpView:RelativeLayout
    lateinit var paidAmount:String
    init {
        mcontext = context
        bottomSheetDialog = BottomSheetDialog(context,R.style.CustomBottomSheetDialogTheme)
        al =  AlertDialog.Builder(context)
        paidAmount = "0"
    }
    fun PayNow(
        amount:String,
        description:String,
        email:String,
        mobile_number:String,
        currency:String,
        gatewayId:Int = 0,
        gateway_name:String = "",
        publicKey:String = "",
        myCallback: (result: APIResponse) -> Unit)
    {
        if(apikey.equals(""))
        {
            Toast.makeText(context,"Please privide apikey", Toast.LENGTH_SHORT).show()
        }else if(amount.equals(""))
        {
            Toast.makeText(context,"Please provide payment amount", Toast.LENGTH_SHORT).show()
        }else if(description.equals(""))
        {
            Toast.makeText(context,"Enter the description", Toast.LENGTH_SHORT).show()
        }else if(email.equals(""))
        {
            Toast.makeText(context,"Your email address is required.", Toast.LENGTH_SHORT).show()
        }else if(mobile_number.equals(""))
        {
            Toast.makeText(context,"Mobile number is required.", Toast.LENGTH_SHORT).show()
        }else if(currency.equals(""))
        {
            Toast.makeText(context,"Currency is required.", Toast.LENGTH_SHORT).show()
        }else if(gatewayId.equals(0))
        {
            Toast.makeText(context,"Gateway ID is required.", Toast.LENGTH_SHORT).show()
        }else if(gateway_name.equals(""))
        {
            Toast.makeText(context,"Gateway Name is required.", Toast.LENGTH_SHORT).show()
        }else if(publicKey.equals(""))
        {
            Toast.makeText(context,"Gateway Public Key is required.", Toast.LENGTH_SHORT).show()
        }else {
            bottomSheetDialog.setContentView(R.layout.web_pay)
            BrowserWrp = bottomSheetDialog.findViewById<ConstraintLayout>(R.id.BrowserWrp)!!
            WebpayView = bottomSheetDialog.findViewById<WebView>(R.id.webVpay)!!
            BlindWrpView  = bottomSheetDialog.findViewById<RelativeLayout>(R.id.blindWrp)!!
            val Closebtn = bottomSheetDialog.findViewById<ImageButton>(R.id.closebtn)!!
            Closebtn!!.setOnClickListener {
                bottomSheetDialog.dismiss()
            }
            var ref = GenerateRef().toString()
            val r = RequestInitData(
                gatewayId,
                ref,
                amount,
                true
            )
            bottomSheetDialog.show()
            val jsn = Gson()
            PostDInit("external/payments",apikey,merchant_id,r) { result ->
                Log.d("result_->",result.data.onlyPassReference.toString())
                if(result.status){
                    var data = SendObj(apikey.trim(),
                        merchant_id.trim(),
                        result.data.amountToPay.toString(),
                        description.trim(),
                        currency.trim(),
                        email.trim(),
                        mobile_number.trim(),
                        gatewayId,
                        gateway_name,
                        publicKey,
                        result.data.onlyPassReference
                    )
                    Log.d("result_>",data.toString())
                    WebpayView.visibility = View.VISIBLE
                    WebpayView.webViewClient = WebViewClient()
                    WebpayView.settings.javaScriptEnabled = true
                    WebpayView.settings.setSupportZoom(true)
                    WebpayView.loadUrl("file:///android_asset/index.html")
                    WebpayView.addJavascriptInterface(JsWebInterface(context), "OnlyPassCall")
                    WebpayView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, weburl: String) {

                        }
                    }
                    WebpayView.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView, newProgress: Int) {
                            if (newProgress == 100) {
                                BlindWrpView.visibility = View.INVISIBLE

                                var Stringdata = jsn.toJson(data)
                                WebpayView.loadUrl(
                                    "javascript:(function() {" +
                                            "loadBtns('" + Stringdata + "')" +
                                            "})()"
                                )
                            }
                        }
                    }
                }else{
                    bottomSheetDialog.dismiss()
                    al.create()
                    al.setTitle("Oops!")
                    al.setMessage(result.message+" \n\nAPI Key:c"+apikey+" \n\nMerchant ID: "+merchant_id)
                    al.show()
                }
//                myCallback(result)
            }
        }
    }
    fun GetGateways(
               amount:String,
               description:String,
               email:String,
               mobile_number:String,
               currency:String,
        myCallback: (result: APIResponse) -> Unit)
    {
       val r = RequestData(amount,"1",1,false)
       PostD("external/payments/channels",apikey,merchant_id,r) { result ->
           Log.d("result--",result.toString())
                myCallback(result)
            }
    }
    inner class JsWebInterface(context: Context) {
        @JavascriptInterface
        fun Show(close:Boolean,message: String?) {
            Log.d("Reew:",message!!)
            if(close) {
                bottomSheetDialog.dismiss()
            }
            al.create()
            al.setTitle("Alert")
            al.setMessage(message)
            al.show()
        }
    }
    private fun SetPayment(res:APIUserResponseItem,apikey: String,merchant_id:String,amount:String) {
        Log.d("result_>", respDate)
        BrowserWrp.visibility = View.VISIBLE
        BtnWrp.visibility = View.INVISIBLE
        var ref = GenerateRef().toString()
        res.ref = ref
        val jsn = Gson()
        var data = jsn.toJson(res)

        val r = RequestData(amount,res.ref,res.gatewayId,false)
        PostD("external/payments",apikey,merchant_id,r) { result ->
            Log.d("Rex:",result.toString())
            WebpayView.visibility = View.VISIBLE
            WebpayView.webViewClient = WebViewClient()
            WebpayView.settings.javaScriptEnabled = true
            WebpayView.settings.setSupportZoom(true)
            WebpayView.loadUrl("file:///android_asset/index.html")
            WebpayView.addJavascriptInterface(JsWebInterface(context), "OnlyPassCall")
            WebpayView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, weburl: String) {

                }
            }
            WebpayView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView, newProgress: Int) {
                    if (newProgress == 100) {
                        BlindWrpView.visibility = View.INVISIBLE
                        WebpayView.loadUrl(
                            "javascript:(function() {" +
                                    "loadBtns('" + respDate + "','" + res.gateway.name + "','" + data + "')" +
                                    "})()"
                        )
                    }
                }
            }
        }
    }
 public fun GenerateRef(digit:Int = 16):Long{
        var currentMilliSeconds:String = ""+Calendar.getInstance().timeInMillis
        var genDigit:Int = digit
        if(genDigit < 8)
            genDigit = 8
        if(genDigit>12)
            genDigit = 12
        var cut = currentMilliSeconds.length - genDigit
        currentMilliSeconds = currentMilliSeconds.substring(cut);
        return currentMilliSeconds.toLong()
    }
 public fun PostDInit(url:String,apikey:String,merchant_id:String,resD:RequestInitData,myCallback: (result: APIResponseInit) -> Unit)
{
    val queue = Volley.newRequestQueue(context)
    val BaseUrl = "https://devapi.onlypassafrica.com/api/v1/"
    val stringRequest = object: StringRequest(Request.Method.POST, BaseUrl+url,
        Response.Listener<String> { response ->
            var strResp = response.toString()
            try {
                val x = Gson()
                val jsonObj: JSONObject = JSONObject(strResp)
                val status: Boolean = jsonObj.getBoolean("status")
                val msg: String = jsonObj.getString("message")
                val d: String = jsonObj.getString("data")
                val data:ResPData = x.fromJson(d,ResPData::class.java)
                myCallback.invoke(APIResponseInit(status,msg,data))
            }catch (e:Exception)
            {
            val data:ResPData = ResPData(0.1F,"")
            myCallback.invoke(APIResponseInit(false,e.message.toString(),data))
            }
        },
        Response.ErrorListener {
            Log.d("error-> Reew:", it.toString())
            var msg = it.message.toString()
            if(it.message.toString() == "null")
            {
                msg = "Invalid merchant credentials."
            }
            val data:ResPData = ResPData(0.1F,"")
            myCallback.invoke(APIResponseInit(false,msg,data))
        })
  {
        @Throws(AuthFailureError::class)
        override fun getBody(): ByteArray {
            var js = Gson()
            if(resD.amount == "")
            {
              return "{}".toByteArray()
            }
            return js.toJson(resD).toString().toByteArray()
        }
        override fun getHeaders(): MutableMap<String, String> {
            val headers = HashMap<String, String>()
            headers["Accept"] = "application/json"
            headers["Content-Type"] = "application/json"
            headers["x-api-key"] = apikey
            headers["x-platform-id"] = merchant_id
            return headers
        }
    }
queue.add(stringRequest)
}
public fun PostD(url:String,apikey:String,merchant_id:String,resD:RequestData,myCallback: (result: APIResponse) -> Unit)
    {
        val queue = Volley.newRequestQueue(context)
        val BaseUrl = "https://devapi.onlypassafrica.com/api/v1/"
        val stringRequest = object: StringRequest(Request.Method.POST, BaseUrl+url,
            Response.Listener<String> { response ->
                var strResp = response.toString()
                try {
                    var jsn = Gson()
                    val jsonObj: JSONObject = JSONObject(strResp)
                    val status: Boolean = jsonObj.getBoolean("status")
                    val msg: String = jsonObj.getString("message")
                    val d: String = jsonObj.getString("data")
                    myCallback.invoke(APIResponse(status,msg,d))
                }catch (e:Exception)
                {
                    myCallback.invoke(APIResponse(false,e.message.toString(),""))
                }
            },
            Response.ErrorListener {
                Log.d("error-> Reew:", it.toString())
                var msg = it.message.toString()
                if(it.message.toString() == "null")
                {
                    msg = "Invalid merchant credentials."
                }
                myCallback.invoke(APIResponse(false,msg,""))
            })
        {
            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                var js = Gson()
                if(resD.amount == "")
                {
                    return "{}".toByteArray()
                }
                return js.toJson(resD).toString().toByteArray()
            }
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Accept"] = "application/json"
                headers["Content-Type"] = "application/json"
                headers["x-api-key"] = apikey
                headers["x-platform-id"] = merchant_id
                return headers
            }
        }
        queue.add(stringRequest)
    }
}
data class RequestInitData(
    val gatewayId:Int,
    val externalReference:String,
    val amount: String,
    val isDemo: Boolean
)
data class ResPData(
val amountToPay:Float,
val onlyPassReference:String
)
data class APIResponseInit(
    val status: Boolean,
    var message: String,
    val data:ResPData
)