### android-network-state
Android network listener 兼容两种模式, 如果广播不能接受,就使用系统的NetworkCallback进行回调监听

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

### 结束
看到这点个赞呗

