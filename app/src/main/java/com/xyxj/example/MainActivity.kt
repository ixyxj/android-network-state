package com.xyxj.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.xyxj.support.network.NetType
import com.xyxj.support.network.NetworkManager
import com.xyxj.support.network.OnNetworkChangeListener
import com.xyxj.support.network.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NetworkManager.get().register(this)
        NetworkManager.get().addObserver(object : OnNetworkChangeListener {
            override fun onDisConnect() {
                tv_state.text = "网络异常"
            }

            override fun onConnected(netType: NetType) {
                tv_state.text = "网络状态: ${netType.name}"
            }

        })
    }


    fun clickOpen(view: View) {
        Utils.toggleMobileData(this, true)
    }

    fun clickClose(view: View) {
        Utils.toggleMobileData(this, false)
    }
}
