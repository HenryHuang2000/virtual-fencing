import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import './home.dart';
import './login.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  final appTitle = 'Virtual Fencing Demo';
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        // appBar: AppBar(
        //   title: Text(appTitle),
        // ),
        body: LoginForm(),
      ),
      routes: <String, WidgetBuilder>{
        '/home': (context) => HomePage(),
      },
      theme: ThemeData(
        primarySwatch: Colors.pink,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
    );
  }
}
