package com.xyxj.support.network

import android.annotation.TargetApi
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telephony.TelephonyManager
import java.lang.reflect.Method
import java.util.*


/**
 * For more information, you can visit https://github.com/xieyangxuejun or contact me by xieyangxuejun@gmail.com
 * @author silen
 * @time 2018/12/28 21:10
 * @des
 * Copyright (c) 2018 in FORETREE
 */
class NetworkManager private constructor() {
    private var mReceiver: NetworkStateReceiver? = null
    private var mCompat: NetworkStateCompat? = null

    init {
        mReceiver = NetworkStateReceiver()
    }

    companion object {
        private var mInstance: NetworkManager? = null
        @JvmStatic
        val ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
        @JvmStatic
        val ANDROID_NET_CHANCE_CUSTOM_ACTION = "android.xyxj.net.conn.CONNECTIVITY_CHANGE"

        fun get(): NetworkManager = mInstance
            ?: synchronized(NetworkManager::class.java) {
            mInstance ?: NetworkManager().apply {
                mInstance = this
            }
        }
    }

    /**
     * 初始化
     */
    fun register(context: Context): NetworkManager{
        context.applicationContext.registerReceiver(getReceiver(), IntentFilter().apply {
            addAction(ANDROID_NET_CHANCE_CUSTOM_ACTION)
            addAction(ANDROID_NET_CHANGE_ACTION)
        })
        return this
    }

    fun delay(timeMillis: Long) {
        mReceiver?.setDelay(timeMillis)
    }

    fun unRegister(context: Context) {
        context.applicationContext.unregisterReceiver(mReceiver ?: return)
    }

    /**
     * 添加监听
     */
    fun addObserver(onNetworkChangeListener: OnNetworkChangeListener) {
        mReceiver?.addObserver(onNetworkChangeListener)
    }

    fun registerCompat(context: Context, callback: ConnectivityManager.NetworkCallback) {
        mCompat = NetworkStateCompat(context, callback).apply { registerNetworkCallback() }
    }

    fun unRegisterCompat() {
        mCompat?.unRegisterNetworkCallback()
    }

    fun getNetType(): NetType = mReceiver?.getNetType()!!

    fun isNetAvailable(): Boolean = mReceiver?.isNetAvailable()!!

    private fun getReceiver(): NetworkStateReceiver = mReceiver?:NetworkStateReceiver().apply { mReceiver = this }
}

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class NetworkStateCompat(
    private val context: Context,
    private val callback: ConnectivityManager.NetworkCallback,
    private val manager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
) {
    fun registerNetworkCallback() {
        val builder = NetworkRequest.Builder()
        val request = builder.build()
        manager.registerNetworkCallback(request, callback)
    }

    fun unRegisterNetworkCallback() {
        manager.unregisterNetworkCallback(callback)
    }
}

class NetworkStateReceiver: BroadcastReceiver() {
    private var mNetworkObservers = arrayListOf<OnNetworkChangeListener>()
    private var mNetAvailable = false
    private var mNetType: NetType = NetType.NONE
    private val mHandler: Handler = Handler(Looper.getMainLooper())
    private var mDelay: Long = 500

    fun addObserver(onNetworkChangeListener: OnNetworkChangeListener) {
        if (!mNetworkObservers.contains(onNetworkChangeListener)) {
            mNetworkObservers.add(onNetworkChangeListener)
        }
    }

    fun removeObserver(onNetworkChangeListener: OnNetworkChangeListener) {
        if (mNetworkObservers.contains(onNetworkChangeListener)) {
            mNetworkObservers.remove(onNetworkChangeListener)
        }
    }

    fun setDelay(timeMillis: Long) {
        this.mDelay = timeMillis
    }

    override fun onReceive(context: Context, intent: Intent?) {
        mHandler.postDelayed({
            val action = intent?.action
            if (action != null &&
                action.equals(NetworkManager.ANDROID_NET_CHANCE_CUSTOM_ACTION, true) ||
                action.equals(NetworkManager.ANDROID_NET_CHANGE_ACTION, true)) {
                mNetAvailable = Utils.isNetworkAvailable(context)
                if (mNetAvailable) mNetType = Utils.getAPNType(context)
                if (mNetworkObservers.isNotEmpty()) {
                    mNetworkObservers.forEach {
                        if (mNetAvailable) {
                            it.onConnected(mNetType)
                        } else {
                            it.onDisConnect()
                        }
                    }
                }
            }
        }, mDelay)
    }

    fun getNetType(): NetType = mNetType
    fun isNetAvailable(): Boolean = mNetAvailable

}

interface OnNetworkChangeListener {
    fun onConnected(netType: NetType)
    fun onDisConnect()
}

object Utils {
    fun isNetworkAvailable(context: Context): Boolean {
        val mgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = mgr.allNetworkInfo
        if (info != null) {
            for (i in info.indices) {
                if (info[i].state == NetworkInfo.State.CONNECTED) {
                    return true
                }
            }
        }
        return false
    }

    fun isNetworkConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable
            }
        }
        return false
    }

    fun isWifiConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mWiFiNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable
            }
        }
        return false
    }

    fun isMobileConnected(context: Context?): Boolean {
        if (context != null) {
            val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mMobileNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable
            }
        }
        return false
    }

    fun getConnectedType(context: Context?): Int {
        if (context != null) {
            val mConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mNetworkInfo = mConnectivityManager.activeNetworkInfo
            if (mNetworkInfo != null && mNetworkInfo.isAvailable) {
                return mNetworkInfo.type
            }
        }
        return -1
    }

    fun getAPNType(context: Context): NetType {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo ?: return NetType.NONE
        val nType = networkInfo.type

        if (nType == ConnectivityManager.TYPE_MOBILE) {
            return if (networkInfo.extraInfo.toLowerCase(Locale.getDefault()) == "cmnet") {
                NetType.CMNET
            } else {
                NetType.CMWAP
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            return NetType.WIFI
        }
        return NetType.NONE
    }

    fun toggleWIFI(context: Context, enable: Boolean) {
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).run {
            if (enable != isWifiEnabled) {
                isWifiEnabled = enable
            }
        }
    }

    /**
     * 移动网络开关
     */
    fun toggleMobileData(context: Context, enabled: Boolean) {
        val conMgr = context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            // 取得ConnectivityManager类
            val conMgrClass: Class<*>? = Class.forName(conMgr.javaClass.name)
            // 取得ConnectivityManager类中的对象mService
            val iConMgrField = conMgrClass?.getDeclaredField("mService")
            // 设置mService可访问
            iConMgrField?.isAccessible = true
            // 取得mService的实例化类IConnectivityManager
            val iConMgr: Any? = iConMgrField?.get(conMgr)
            // 取得IConnectivityManager类
            val iConMgrClass: Class<*>? = Class.forName(iConMgr!!.javaClass.name)
            // 取得IConnectivityManager类中的setMobileDataEnabled(boolean)方法
            val setMobileDataEnabledMethod:Method? =
                    iConMgrClass?.getDeclaredMethod("setMobileDataEnabled", java.lang.Boolean.TYPE)
            // 设置setMobileDataEnabled方法可访问
            setMobileDataEnabledMethod?.isAccessible = true
            // 调用setMobileDataEnabled方法
            setMobileDataEnabledMethod?.invoke(iConMgr, enabled)
        } catch (e: Exception) {
            e.printStackTrace()
            toggleMobileData2(context, enabled)
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun toggleMobileData2(context: Context, enabled: Boolean) {
        val manager = context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        try {
            manager.isDataEnabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}