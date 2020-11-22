import 'dart:async';
import 'dart:io';
import 'dart:convert';

import 'package:connectivity/connectivity.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:wifi_info_flutter/wifi_info_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
// import 'package:workmanager/workmanager.dart';

class HomePage extends StatefulWidget {
  HomePage(
      {Key key,
      this.title,
      @required this.phoneNumber,
      @required this.password})
      : super(key: key);

  final String title;
  final String phoneNumber;
  final String password;

  @override
  _HomePageState createState() =>
      _HomePageState(phone: phoneNumber, pwd: password);
}

class _HomePageState extends State<HomePage> {
  _HomePageState({@required this.phone, @required this.pwd});

  String phone;
  String pwd;
  String _uuid = 'none';
  String _connectionStatus = 'unknown';
  String _bssid = 'unknown';
  Timer timer;
  final Connectivity _connectivity = Connectivity();
  final WifiInfo _wifiInfo = WifiInfo();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;
  final String url = "http://1a4d5746aaf3.ngrok.io/api/check-in";

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription =
        _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
    if (_uuid == 'none') {
      _registerDevice();
    }

    // default duration = 15min
    // Workmanager.registerPeriodicTask("1", "updateNetworkStatus",
    //   constraints: Constraints(
    //     networkType: NetworkType.connected,
    // ));
    // Workmanager.initialize(
    //     callBackDispatcher, // The top level function, aka callbackDispatcher
    //     isInDebugMode: true // If enabled it will post a notification whenever the task is running. Handy for debugging tasks
    // );

    timer = Timer.periodic(Duration(minutes: 1),
        (Timer t) => _updatePost(phone, pwd, _bssid, _uuid));
  }

  Future<String> postData(url, data) async {
    Map<String, String> headers = {"Content-type": "application/json"};
    Response response =
    await post(url, headers: headers, body: data.toString());
    // check the status code for the result
    int statusCode = response.statusCode;
    print("Post Response Code: " + statusCode.toString());
    // this API passes back the id of the new item added to the body
    String body = response.body;
    print("Post Response Body: " + body.toString());
    return body;
  }

  _registerDevice () async {
    Map<String, dynamic> sendPkt = new Map();
    sendPkt['phone'] = phone;
    sendPkt['bssid'] = _bssid;
    String res = await postData(url, sendPkt);
    Map data = json.decode(res);
    setState(() => _uuid = data['device_mac']);
  }

  // void callBackDispatcher() {
  //   Workmanager.executeTask((taskName, inputData) {
  //     // _updatePost(phone, pwd, _bssid, "b");
  //     print("Hello");
  //     return Future.value(true);
  //   });
  // }

  @override
  void dispose() {
    _connectivitySubscription.cancel();
    timer?.cancel();
    // Workmanager.cancelByUniqueName("updateNetworkStatus");
    super.dispose();
  }

  Future<void> _updateConnectionStatus(ConnectivityResult result) async {
    setState(() => _connectionStatus = result.toString());
    switch (result) {
      case ConnectivityResult.wifi:
        String wifiBSSID = await _wifiInfo.getWifiBSSID();
        setState(() {
          _connectionStatus = '$result\n'
              'Wifi BSSID: $wifiBSSID';
        });
    }
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initConnectivity() async {
    ConnectivityResult result;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      result = await _connectivity.checkConnectivity();
    } on PlatformException catch (e) {
      print(e.toString());
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) {
      return Future.value(null);
    }

    // Check to see if Android Location permissions are enabled
    // Described in https://github.com/flutter/flutter/issues/51529
    if (Platform.isAndroid) {
      print('Checking Android permissions');
      var status = await Permission.location.status;
      // Blocked?
      if (status.isUndetermined || status.isDenied || status.isRestricted) {
        // Ask the user to unblock
        if (await Permission.location.request().isGranted) {
          // Either the permission was already granted before or the user just granted it.
          print('Location permission granted');
        } else {
          print('Location permission not granted');
        }
      } else {
        print('Permission already granted (previous execution?)');
      }
    }

    return _updateConnectionStatus(result);
  }

  Future<void> _updatePost(phone, password, bssid, macAddress) async {
    // set up POST request arguments
    if (_uuid == 'none') return;
    Map<String, String> headers = {"Content-type": "application/json"};
    Map<String, dynamic> json = new Map();
    json['phoneNumber'] = phone;
    json['bssid'] = _bssid;
    json['macAddress'] = _uuid;
    // make POST request
    Response response =
    await post(url, headers: headers, body: json.toString());
    // check the status code for the result
    int statusCode = response.statusCode;
    print("Post Response Code: " + statusCode.toString());
    // this API passes back the id of the new item added to the body
    String body = response.body;
    print("Post Response Body: " + body.toString());

    // {
    //   "phone": "0490777777",
    //   "bssid": some_network,
    //   "macAddress": 9C-35-5B-5F-4C-D7,
    // }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Virtual Fencing Demo'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Text('ConnectivityType: $_connectionStatus'
                // 'SSID: $_ssid\n'
                // 'BSSID: $_bssid\n'
                'phone: $phone\n'
                'pwd: $pwd\n')
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
          onPressed: () => dispose(),
          tooltip: 'Update connection status',
          child: Icon(Icons.refresh)),
    );
  }
}
