import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:flutter_camera/flutter_camera.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  CameraController _controller;
  Future<void> _initializeCompleterFuture;

  @override
  void initState() {
    _controller = CameraController(
      cameraLens: CameraLens.back
    );
    _initializeCompleterFuture = _controller.initialize();
    super.initState();
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }


  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            Expanded(
              child: FutureBuilder<void>(
                future: _initializeCompleterFuture,
                builder: (context, snap) {
                  if (snap.connectionState == ConnectionState.done) {
                    return CameraPreview(_controller);
                  } else {
                    return Center(child: CircularProgressIndicator());
                  }
                },
              ),
            ),
            Container(
              child: FlatButton(onPressed: () async {
                String path = await _controller.takePickture();
                print(path);
              }, child: Text("Take pickture")),
            )
          ],
        )
      ),
    );
  }
}
