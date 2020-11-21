import 'dart:async';
import 'dart:io';

import 'package:connectivity/connectivity.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:workmanager/workmanager.dart';

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
  String uuid = "b";
  String _connectionStatus = 'unknown';
  String _ssid = 'unknown';
  String _bssid = 'unknown';
  Timer timer;
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription =
        _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);

    // default duration = 15min
    Workmanager.registerPeriodicTask("1", "updateNetworkStatus",
      constraints: Constraints(
        networkType: NetworkType.connected,
    ));
    Workmanager.initialize(
        callBackDispatcher, // The top level function, aka callbackDispatcher
        isInDebugMode: true // If enabled it will post a notification whenever the task is running. Handy for debugging tasks
    );

    timer = Timer.periodic(Duration(minutes: 1),
        (Timer t) => _updatePost(phone, pwd, _bssid, 'b'));
  }

  void callBackDispatcher() {
    Workmanager.executeTask((taskName, inputData) {
      // _updatePost(phone, pwd, _bssid, "b");
      print("Hello");
      return Future.value(true);
    });
  }

  @override
  void dispose() {
    _connectivitySubscription.cancel();
    timer?.cancel();
    Workmanager.cancelByUniqueName("updateNetworkStatus");
    super.dispose();
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
            Text('ConnectivityType: $_connectionStatus\n'
                'SSID: $_ssid\n'
                'BSSID: $_bssid\n'
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

  Future<void> _updateConnectionStatus(ConnectivityResult result) async {
    setState(() => _connectionStatus = result.toString());
    setState(() async => _ssid = await _connectivity.getWifiName());
    setState(() async => _bssid = await _connectivity.getWifiBSSID());
  }

  Future<void> _updatePost(phone, password, bssid, macAddress) async {
    // set up POST request arguments
    String url = "http://be5a5dbe9d99.ngrok.io/api/check-in";
    Map<String, String> headers = {"Content-type": "application/json"};
    Map<String, dynamic> json = new Map();
    json['phoneNumber'] = phone;
    json['password'] = pwd;
    json['bssid'] = _bssid;
    json['macAddress'] = 'b';
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
    //   "password": "12345",
    //   "bssid": some_network,
    //   "macAddress": 9C-35-5B-5F-4C-D7,
    // }
  }
}
