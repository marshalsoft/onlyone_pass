package com.onlyonepass.test

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.onlyonepass.test.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
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
import com.onlypass.plugin.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var  selectedCurrency:String
    lateinit var lodding:ProgressBar
    lateinit var payment_gateway:Spinner
    lateinit var  selectedGateway:APIUserResponseItem
    var initialPay:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialPay = false
        lodding = findViewById<ProgressBar>(R.id.lodding)

        val currencylist = arrayOf("NGN","USD")
        selectedCurrency = "NGN";
        val currency_spinner = findViewById<Spinner>(R.id.currency_spinner)
        payment_gateway = findViewById<Spinner>(R.id.payment_gateway)
        val ArrayApatar = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,currencylist)
        currency_spinner.adapter = ArrayApatar
        currency_spinner.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCurrency = currencylist[position]
            }
        }
        var apikey = "pk_35377fab-fd9c-43e8-9b4c-19632eaedeee"// "pk_35b34815-d913-4269-932d-332c882a81b0"
        val mid = "69730512"//"79627428"
        val pay = OnlyPassInit(
            this,
            apikey,
            mid)
        val r = RequestData("","1",1,false)
            pay.GetGateways(
                "100",
                "sjkj",
                "marshalgfx@gmail.com",
                "08161235924",
                "NGN"
            ) { result ->
                lodding.visibility = View.INVISIBLE
                val js = Gson()
                if (result.status) {

//                    var res = js.fromJson<APIUserResponse>(result.data, APIUserResponse::class.java)
                    val res: JSONArray = JSONArray(result.data)
//                     Log.d("result_> 3", res.toString())
                    var GateWaylist = ArrayList<APIUserResponseItem>()
                    var Glist = listOf<String>()
                    for (i in 0 until res.length()) {
                        val itemString = res.getJSONObject(i)
                        val item =  js.fromJson<APIUserResponseItem>(itemString.toString(), APIUserResponseItem::class.java)
                        GateWaylist.add(item)

                        Log.d("result_> 3", item.toString()+"|$i")
                        Log.d("result_> 4", GateWaylist.toString())
                    }
                    Glist = GateWaylist.map { it.gateway.name }
                    payment_gateway.visibility = View.VISIBLE

                    val GatewayArrayApatar = ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,Glist)
                    Log.d("result_> 3", GateWaylist.toString())
                    payment_gateway.adapter = GatewayArrayApatar
                    payment_gateway.onItemSelectedListener =  object : AdapterView.OnItemSelectedListener{
                        override fun onNothingSelected(parent: AdapterView<*>?) {

                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedGateway = GateWaylist.get(position)
                        }
                    }
                }
        }
    }

    fun Payment(view: View) {
    var amount = amountTxt.text.toString()
    var descp = descripionTxt.text.toString()
    var em = emailTxt.text.toString()
    var mob = mobileTxt.text.toString()
    val pay = OnlyPassInit(
            this,
        "pk_35377fab-fd9c-43e8-9b4c-19632eaedeee",
            //"pk_35b34815-d913-4269-932d-332c882a81b0",
            "69730512"//"79627428"
    )
            pay.PayNow(
                amount,
                descp,
                em,
                mob,
                selectedCurrency,
                selectedGateway.gatewayId,
                selectedGateway.gateway.name,
                selectedGateway.publicKey,
            ){ result ->
                Log.d("result_> 5", result.toString())
            }

    }

}
data class ReP(
    var stats:String
)