import 'dart:async';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:local_auth/local_auth.dart';
import 'package:localstorage/localstorage.dart';

import 'home.dart';

class LoginForm extends StatefulWidget {
  @override
  LoginState createState() {
    return LoginState();
  }
}

// Create a corresponding State class.
// This class holds data related to the form.
class LoginState extends State<LoginForm> {
  // Create a global key that uniquely identifies the Form widget and allows validation of the form.
  final _formKey = GlobalKey<FormState>();
  final phoneController = TextEditingController();
  final passwordController = TextEditingController();
  final LocalStorage storage = new LocalStorage('virtual_fencing');

  LocalAuthentication auth = LocalAuthentication();
  bool _canCheckBiometric;
  List<BiometricType> _availableBiometric;
  bool authorised = false;

  @override
  void initState() {
    _checkBiometric();
    _getAvailableBiometrics();
    String localPhone = storage.getItem("phone");
    String localPwd = storage.getItem("pwd");
    if (localPhone.isNotEmpty && localPwd.isNotEmpty) {
      phoneController.text = localPhone;
      passwordController.text = localPwd;
    }
    super.initState();
  }

  Future<void> _checkBiometric() async {
    bool canCheckBiometric;
    try {
      canCheckBiometric = await auth.canCheckBiometrics;
    } on PlatformException catch (e) {
      print(e);
    }
    if (!mounted) return;

    setState(() {
      _canCheckBiometric = canCheckBiometric;
    });
  }

  Future<void> _getAvailableBiometrics() async {
    List<BiometricType> availableBiometric;
    try {
      availableBiometric = await auth.getAvailableBiometrics();
    } on PlatformException catch (e) {
      print(e);
    }
    if (!mounted) return;

    setState(() {
      _availableBiometric = availableBiometric;
    });
  }

  Future<void> _authenticate() async {
    bool authenticated = false;
    try {
      authenticated = await auth.authenticateWithBiometrics(
          localizedReason: "Scan your finger print to authenticate",
          useErrorDialogs: true,
          stickyAuth: false);
    } on PlatformException catch (e) {
      print(e);
    }
    if (!mounted) return;

    setState(() => authorised = authenticated);
    // if (authenticated) {
    //   Navigator.pushNamed(context, '/home');
    // }
  }

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: Column(
        // Title text
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          Container(
            margin: new EdgeInsets.symmetric(vertical: 120.0),
            child: Text(
              'Log In',
              style: TextStyle(
                fontSize: 50,
              ),
            ),
          ),
          Container(
            // password input
            width: 300.0,
            margin: new EdgeInsets.symmetric(vertical: 10.0),
            child: TextFormField(
              decoration: InputDecoration(
                labelText: 'Enter your mobile number',
                contentPadding:
                    new EdgeInsets.symmetric(vertical: 10.0, horizontal: 20.0),
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(35.0)),
              ),
              keyboardType: TextInputType.number,
              inputFormatters: <TextInputFormatter>[
                FilteringTextInputFormatter.digitsOnly
              ],
              validator: (value) {
                if (value.isEmpty) {
                  return 'Mobile number must not be blank';
                } else if (value.length != 10) {
                  return 'Mobile number must be 10 digits long';
                } else if (!value.startsWith('04') && !value.startsWith('05')) {
                  return 'Mobile number must start with 04 or 05';
                }
                return null;
              },
              controller: phoneController,
            ),
          ),
          Container(
            // name input
            width: 300.0,
            margin: new EdgeInsets.symmetric(vertical: 10.0, horizontal: 50.0),
            child: TextFormField(
              decoration: InputDecoration(
                labelText: 'Enter your password',
                contentPadding:
                    new EdgeInsets.symmetric(vertical: 10.0, horizontal: 20.0),
                border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(35.0)),
              ),
              validator: (value) {
                if (value.isEmpty) {
                  return 'Name must not be blank';
                }
                return null;
              },
              controller: passwordController,
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(vertical: 18.0),
            child: RaisedButton(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(30.0),
                side: BorderSide(color: Colors.pink, width: 2),
              ),
              color: Colors.white,
              onPressed: () {
                // Validate returns true if the form is valid, or false otherwise.
                if (_formKey.currentState.validate()) {
                  // var phoneBytes = utf8.encode(phoneController.text);
                  // var pwdBytes = utf8.encode(passwordController.text);
                  storage.setItem("phone", phoneController.text);
                  storage.setItem("pwd", passwordController.text);
                  // _authenticate();
                  Navigator.push(
                      context,
                      MaterialPageRoute(
                          builder: (context) => HomePage(
                              phoneNumber: phoneController.text,
                              password: passwordController.text)));
                  // if form is valid, generate a uuid
                  // var uuid = Uuid();
                  // var id = uuid.v4().toString();

                  // If the form is valid, display a Snackbar.
                  // Scaffold.of(context)
                  //     .showSnackBar(SnackBar(content: Text('Processing Data')));

                  // display uuid generated
                  // return showDialog(
                  //     context: context,
                  //     builder: (context) {
                  //       return AlertDialog(
                  //           title: Text('New uuid'),
                  //           content: Text('Your uuid is: $id')
                  //       );
                  //     }
                  // );
                }
              },
              child: Text(
                'Log in',
                style: TextStyle(
                  color: CupertinoColors.darkBackgroundGray,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
