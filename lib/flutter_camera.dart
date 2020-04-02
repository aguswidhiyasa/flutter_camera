import 'dart:async';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';

MethodChannel _channel = const MethodChannel('bebena/flutter_camera');

enum CameraLens { front, rear, extenal }

enum ResolutionPreset { low, medium, high, veryHigh, ultraHigh, max }

final typeValues = EnumValues({
  "low": ResolutionPreset.low,
  "medium": ResolutionPreset.medium,
  "high": ResolutionPreset.high,
  "veryHigh": ResolutionPreset.veryHigh,
  "ultraHigh": ResolutionPreset.ultraHigh,
  "max": ResolutionPreset.max,
});

class EnumValues<T> {
  Map<String, T> map;
  Map<T, String> reverseMap;

  EnumValues(this.map);

  Map<T, String> get reverse {
    if (reverseMap == null) {
      reverseMap = map.map((k, v) => new MapEntry(v, k));
    }
    return reverseMap;
  }
}


String _parseCameraLensToString(CameraLens lens) {
  switch (lens) {
    case CameraLens.front:
      return "front";
    case CameraLens.rear:
      return "back";
    default: 
      return "back";
  }
}

class CameraController extends ValueNotifier<int> {

  CameraController({
    this.cameraLens,
    this.resolutionPreset = ResolutionPreset.medium
  }): super(0);

  final CameraLens cameraLens;
  final ResolutionPreset resolutionPreset;

  bool _isDisposed = false;

  int _textureId;

  Completer<void> _completer;

  StreamSubscription<dynamic> _eventSubscription;
  StreamSubscription<dynamic> _imageStreamSubscription;

  Future<void> initialize() async {
    _completer = Completer<void>();
    final Map<String, dynamic> init = await _channel.invokeMapMethod("init",
      <String, dynamic> {
        "cameraLens": _parseCameraLensToString(this.cameraLens),
        "cameraPreset": typeValues.reverse[this.resolutionPreset]
      }
    );
    _textureId = int.parse(init['textureId'].toString());
    _eventSubscription = EventChannel("bebena/flutter_camera/cameraEvents$_textureId")
      .receiveBroadcastStream()
      .listen(_listener);
    _completer.complete();
    return _completer.future;
  }

  Future<String> takePickture() async {
    final Map<String, dynamic> take = await _channel.invokeMapMethod("takePickture");
    return take["filePath"];
  }

  void _listener(dynamic event) {
    final Map<dynamic, dynamic> map = event;
    if (_isDisposed) {
      return;
    }

    switch (map['eventType']) {
      case 'error':
        // value = value.copyWith(errorDescription: event['errorDescription']);
        print(event['errorDescription']);
        break;
      case 'cameraClosing':
        print('cameraClosing');
        break;
    }
  }

  void dispose() async {
    await _channel.invokeMethod<void>("dispose");
  }

}

class CameraPreview extends StatelessWidget {

  final CameraController controller;

  const CameraPreview(this.controller);

  @override
  Widget build(BuildContext context) {
    return Texture(textureId: controller._textureId);
  }
}