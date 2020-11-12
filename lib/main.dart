import 'dart:async';
// import 'dart:html';
import 'package:connectivity/connectivity.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import 'package:geocoder/geocoder.dart';
import 'package:http/http.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
        // This makes the visual density adapt to the platform that you run
        // the app on. For desktop platforms, the controls will be smaller and
        // closer together (more dense) than on mobile platforms.
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
      home: GeolocationExample(title: 'Geolocation Example'),
    );
  }
}

class GeolocationExampleState extends State<GeolocationExample> {
  Geolocator _geolocator;
  Position _position;
  String _connectionStatus = 'Unknown';
  final Connectivity _connectivity = Connectivity();
  StreamSubscription<ConnectivityResult> _connectivitySubscription;

  @override
  void initState() {
    super.initState();

    _geolocator = Geolocator();
    initConnectivity();
    _connectivitySubscription =
        _connectivity.onConnectivityChanged.listen(_updateConnectionStatus);
  }

  bool isLoading = false;
  List<Address> results = [];

  Future<void> initConnectivity() async {
    ConnectivityResult result;
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      result = await _connectivity.checkConnectivity();
      print(result.toString());
    } on PlatformException catch (e) {
      print(e.toString());
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) {
      return Future.value(null);
    }

    return _updateConnectionStatus(result);
  }

  @override
  void dispose() {
    _connectivitySubscription.cancel();
    super.dispose();
  }

  Future<void> _updateConnectionStatus(ConnectivityResult result) async {

    switch (result) {

      case ConnectivityResult.mobile:
        setState(() => _connectionStatus = result.toString());
        _makePostRequest("Angela", 12345, _position.toString());
        break;
      case ConnectivityResult.none:
        setState(() => _connectionStatus = result.toString());
        break;
      case ConnectivityResult.wifi:
      setState(() => _connectionStatus = result.toString());
        break;
      default:
        setState(() => _connectionStatus = 'Failed to get connectivity.');
        break;
    }
  }

  Future<void> _makePostRequest(name, id, location) async {
    // set up POST request arguments
    String url = 'http://754013b77161.ngrok.io/api/records';
    Map<String, String> headers = {"Content-type": "application/json"};
    String json = '{"name": "${name}", "userId": "${id}", "location": "${location}"}';
    // make POST request
    Response response = await post(url, headers: headers, body: json);
    // check the status code for the result
    int statusCode = response.statusCode;
    print("Post Response Code: " + statusCode.toString());
    // this API passes back the id of the new item added to the body
    String body = response.body;
    print("Post Response Body: " + statusCode.toString());

    // {
    //   "title": "Hello",
    //   "body": "body text",
    //   "userId": 1,
    //   "id": 101
    // }
  }

  void funContainer() {
    _updateLocation();
    search(_position.latitude, _position.longitude);
    _makePostRequest("Leo", 09876, _position.toString());
  }

  void _updateLocation() async {
    try {
      Position newPosition = await Geolocator.getCurrentPosition(desiredAccuracy: LocationAccuracy.high)
          .timeout(new Duration(seconds: 5));

      setState(() {
        _position = newPosition;
      });
    } catch (e) {
      print('Error: ${e.toString()}');
    }
  }

  Future search(latitude, longitude) async {
    this.setState(() {
      this.isLoading = true;
    });

    try{
      print(latitude);
      print(longitude);
      var results = await Geocoder.google('AIzaSyCEewoe84r5nUHZSYE-o9zJogk5XwP17Mc').findAddressesFromCoordinates(new Coordinates(latitude, longitude));
      this.setState(() {
        print("Results: " + results.toString());
        this.results = results;
      });
    }
    catch(e) {
      print("Error occurred: $e");
    }
    finally {
      this.setState(() {
        this.isLoading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Flutter Geolocation Example'),
      ),
      body: Center(
          child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: <Widget>[
                Text(
                    'Latitude: ${_position != null ? _position.latitude.toString() : '0'},'
                        ' Longitude: ${_position != null ? _position.longitude.toString() : '0'}\n'
                        'Wifi: ${_connectionStatus}'
                )
              ]
          )
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: funContainer,
        tooltip: 'Get Location',
        child: Icon(Icons.location_searching_outlined),
      ),
    );
  }
}

class GeolocationExample extends StatefulWidget {
  GeolocationExample({Key key, this.title}) : super(key: key);

  // This widget is the home page of your application. It is stateful, meaning
  // that it has a State object (defined below) that contains fields that affect
  // how it looks.

  // This class is the configuration for the state. It holds the values (in this
  // case the title) provided by the parent (in this case the App widget) and
  // used by the build method of the State. Fields in a Widget subclass are
  // always marked "final".

  final String title;

  @override
  GeolocationExampleState createState() => GeolocationExampleState();
}