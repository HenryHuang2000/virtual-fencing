import 'dart:async';
import 'dart:io';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:connectivity/connectivity.dart';
// import 'package:local_auth/local_auth.dart';
import 'package:http/http.dart';
// import 'package:uuid/uuid.dart';

class HomePage extends StatefulWidget {
  HomePage({Key key, this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String _connectionStatus = 'unknown';
  String _ssid = 'unknown';
  String _bssid = 'unknown';
  String _macAddr = 'unknown';
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  Future<void> getNetworkData() async {
    String ssid = await Connectivity().getWifiName();
    print("SSID: $ssid");
    setState(() => _ssid = ssid);
    String bssid = await Connectivity().getWifiBSSID();
    print("BSSID: $bssid");
    setState(() => _bssid = bssid);
  }

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription =
        _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
    getNetworkData();
  }

  @override
  void dispose() {
    _connectivitySubscription.cancel();
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
            Text(
              'ConnectivityType: $_connectionStatus\n'
              'SSID: $_ssid\n'
              'BSSID: $_bssid\n'
            )
          ],
        ),
      ),
    );
  }

  Future<void> _updateConnectionStatus(ConnectivityResult result) async {
    setState(() => _connectionStatus = result.toString());
    setState(() async => _ssid = await _connectivity.getWifiName());
    setState(() async => _bssid = await _connectivity.getWifiBSSID());
  }

  Future<void> _makePostRequest(name, id, location) async {
    // set up POST request arguments
    String url = 'http://754013b77161.ngrok.io/api/records';
    Map<String, String> headers = {"Content-type": "application/json"};
    String json = '{"name": "$name", "userId": "$id", "location": "$location"}';
    // make POST request
    Response response = await post(url, headers: headers, body: json);
    // check the status code for the result
    int statusCode = response.statusCode;
    print("Post Response Code: " + statusCode.toString());
    // this API passes back the id of the new item added to the body
    String body = response.body;
    print("Post Response Body: " + statusCode.toString());

    // {
    //   "email": "user@gmail.com",
    //   "password": "12345",
    //   "BSSID": some_network,
    //   "MAC address": 9C-35-5B-5F-4C-D7,
    // }
  }
}
