package com.android.guru.cryptox

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.android.guru.cryptox.Adapter.CoinAdapter
import com.android.guru.cryptox.Common.Common
import com.android.guru.cryptox.Interface.ILoadMore
import com.android.guru.cryptox.Model.CoinModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity(),ILoadMore {


    internal var items:MutableList<CoinModel> = ArrayList()
    internal lateinit var adapter: CoinAdapter
    internal lateinit var client:OkHttpClient
    internal lateinit var request: Request

    override fun onLoadMore() {
        if (items.size <= Common.MAX_COIN_LOAD)
            loadNext10Coin(items.size)
        else
            Toast.makeText(this@MainActivity,"Data max is "+Common.MAX_COIN_LOAD,Toast.LENGTH_SHORT)
                    .show()
    }

    private fun loadNext10Coin(index: Int) {

        client = OkHttpClient()
        request = Request.Builder()
                .url(String.format("https://api.coinmarketcap.com/v1/ticker/?start=%d&limit=10",index))
                .build()

        swipe_to_refresh.isRefreshing=true
        client.newCall(request)
                .enqueue(object :Callback
                {
                    override fun onFailure(call: Call?, e: IOException?) {
                       Log.d("ERROR",e.toString())
                    }

                    override fun onResponse(call: Call?, response: Response) {
                        val body = response.body()!!.string()
                        val gson=Gson()
                        val newItems = gson.fromJson<List<CoinModel>>(body,object:TypeToken<List<CoinModel>>(){}.type)
                        runOnUiThread {
                            items.addAll(newItems)
                            adapter.setLoaded()
                            adapter.updateData(items)

                            swipe_to_refresh.isRefreshing=false
                        }
                    }


                })

    }

    private fun loadFrist10Coin() {

        client = OkHttpClient()
        request = Request.Builder()
                .url(String.format("https://api.coinmarketcap.com/v1/ticker/?start=0&limit=10"))
                .build()

        client.newCall(request)
                .enqueue(object :Callback
                {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.d("ERROR",e.toString())
                    }

                    override fun onResponse(call: Call?, response: Response) {
                        val body = response.body()!!.string()
                        val gson=Gson()
                        items = gson.fromJson(body,object:TypeToken<List<CoinModel>>(){}.type)
                        runOnUiThread {
                            adapter.updateData(items)

                        }
                    }


                })

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //code that displays the content in full screen mode
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//int flag, int mask

        setContentView(R.layout.activity_main)

        swipe_to_refresh.post { loadFrist10Coin() }
        swipe_to_refresh.setOnRefreshListener {
            items.clear()
            loadFrist10Coin()
            setUpAdapter()
        }

        coin_recycler_view.layoutManager = LinearLayoutManager(this)
        setUpAdapter()
    }

    private fun setUpAdapter() {
        adapter = CoinAdapter(coin_recycler_view,this@MainActivity,items)
        coin_recycler_view.adapter = adapter
        adapter.setLoadMore(this)
    }
}
