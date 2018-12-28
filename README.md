### android-network-state
Android network listener 兼容两种模式, 如果广播不能接受,就使用系统的NetworkCallback进行回调监听, 纯kotlin实现.

### 效果
![image](https://raw.githubusercontent.com/ixyxj/android-network-state/master/screenshot/screenshot.gif)

### 使用
加入权限
```java
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
```
引用
```gradle
implementation 'com.xyxj.support:network-state:1.0.0'
```
在Application初始化, 回调默认延迟500毫秒,可以设置delay
```java
NetworkManager.get().register(this).delay(0)
```
监听
```java
NetworkManager.get().addObserver(object : OnNetworkChangeListener {
    override fun onDisConnect() {
        tv_state.text = "网络异常"
    }

    override fun onConnected(netType: NetType) {
        tv_state.text = "网络状态: ${netType.name}"
    }

})
```

### 问题
由于Android7.0取消了默认广播, 8.0取消了大量的广播, 之前静默注册的不能使用了, 使用动态注册.
动态开关网络API变动, 从ConnectivityManager的setMobileDataEnabled方法,改变到TelephonyManager的setDataEnabled方法.
由于系统安全原因, 系统版本高会报异常,谨慎使用.

### 结束
看到这点个赞呗

