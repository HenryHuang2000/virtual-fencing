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
import 'package:uuid/uuid.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:date_format/date_format.dart';

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
  List<String> _records = [];
  Timer timer;
  final Connectivity _connectivity = Connectivity();
  final WifiInfo _wifiInfo = WifiInfo();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;
  final String url = "http://b331d74b1283.ngrok.io/api/check-in";

  @override
  void initState() {
    super.initState();
    initConnectivity();
    _connectivitySubscription =
        _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
    _initialiseLocals();

    timer = Timer.periodic(Duration(seconds: 30),
        (Timer t) => _updatePost());
  }

  _initialiseLocals () async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    String id = prefs.getString('id');
    List<String> records = prefs.getStringList('records');
    if (records != null) setState(() => _records = records);
    if (id != null) {
      setState(() => _uuid = id);
    } else {
      String uuid = Uuid().v4().toString();
      setState(() => _uuid = uuid);
      prefs.setString('id', _uuid);
    }
  }

  _updateRecords (record) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();
    if (_records == null) {
      List<String> newList = [];
      prefs.setStringList('records', newList);
      setState(() => _records = []);
    }
    _records.add(record);
    if (_records.length == 50) _records.remove(_records.first);
    setState(() => _records = _records);
    prefs.setStringList('records', _records);
  }

  String formatRecords (idx, request) {
    String record = _records[idx];
    Map data = json.decode(record);
    String ret;
    switch (request) {
      case "time":
        var timeObj = data['last_check_in'];
        if (timeObj == null) return "";
        ret = formatDate(DateTime.parse(timeObj), [dd, '-', mm, '-', yyyy, ' ', hh, ':', nn, ':', ss]);
        break;
      case "info":
        var infoObj = data['url'];
        if (infoObj == null) return "";
        print(infoObj);
        ret = infoObj.toString();
        break;
    }
    return (ret != null) ? ret : "";
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

  @override
  void dispose() {
    _connectivitySubscription.cancel();
    timer?.cancel();
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
        setState(() => _bssid = wifiBSSID);
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

  Future<void> _updatePost() async {
    // set up POST request arguments
    if (_uuid == 'none') return;
    String sendPkt = '{"phoneNumber": "$phone", "bssid": "$_bssid", "macAddress": "$_uuid"}';
    // make POST request
    String response = await postData(url, sendPkt);
    _updateRecords(response);

    Map resData = json.decode(response);

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
            Expanded(
              child: ListView.builder (
                scrollDirection: Axis.vertical,
                shrinkWrap: true,
                itemCount: _records.length,
                itemBuilder: (context, idx) {
                  return Padding(
                    padding: EdgeInsets.only(bottom: 16.0),
                    child: Card(
                      color: Colors.white,
                      child: Column (
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Padding(
                            padding: EdgeInsets.symmetric(vertical: 24.0, horizontal: 16.0),
                            child: Text(formatRecords(idx, "time"), style: TextStyle(
                              fontSize: 18.0,
                              height: 1.6,
                            ),),
                          ),
                          Padding(
                            padding: EdgeInsets.symmetric(vertical: 10.0, horizontal: 16.0),
                            child: Text(formatRecords(idx, "info"), style: TextStyle(
                              fontSize: 12.0,
                              height: 1.6,
                            ),),
                          )
                        ],
                      ),
                    )
                  );
                }
              ),
            ),
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
          onPressed: () => dispose(),
          tooltip: 'Update connection status',
          child: Icon(Icons.sync_disabled)),
    );
  }
}
