import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:uuid/uuid.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final appTitle = 'Register';

    return MaterialApp(
      title: appTitle,
      home: Scaffold(
        appBar: AppBar(
          title: Text(appTitle),
        ),
        body: MyCustomForm(),
      ),
      theme: ThemeData(
        primarySwatch: Colors.blue,
        visualDensity: VisualDensity.adaptivePlatformDensity,
      ),
    );
  }
}

// Create a Form widget.
class MyCustomForm extends StatefulWidget {
  @override
  MyCustomFormState createState() {
    return MyCustomFormState();
  }
}

// Create a corresponding State class.
// This class holds data related to the form.
class MyCustomFormState extends State<MyCustomForm> {
  // Create a global key that uniquely identifies the Form widget and allows validation of the form.
  final _formKey = GlobalKey<FormState>();

  @override
  Widget build(BuildContext context) {
    return Form(
      key: _formKey,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: <Widget>[
          // name input
          TextFormField(
            decoration: InputDecoration(labelText: 'Enter your name'),
            validator: (value) {
              if (value.isEmpty) {
                return 'Name must not be blank';
              }
              return null;
            },
          ),

          // number input
          TextFormField(
            decoration: InputDecoration(labelText: 'Enter your mobile number'),
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
          ),

          Padding(
            padding: const EdgeInsets.symmetric(vertical: 16.0),
            child: ElevatedButton(
              onPressed: () {
                // Validate returns true if the form is valid, or false otherwise.
                if (_formKey.currentState.validate()) {
                  // if form is valid, generate a uuid
                  var uuid = Uuid();
                  var id = uuid.v4().toString();

                  // If the form is valid, display a Snackbar.
/*                  Scaffold.of(context)
                      .showSnackBar(SnackBar(content: Text('Processing Data')));*/

                  // display uuid generated
                  return showDialog(
                      context: context,
                      builder: (context) {
                        return AlertDialog(
                            title: Text('New uuid'),
                            content: Text('Your uuid is: $id'));
                      });
                }
              },
              child: Text('Submit'),
            ),
          ),
        ],
      ),
    );
  }
}
