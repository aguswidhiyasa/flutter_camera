# flutter_camera

Inspired from [`camera`](https://pub.dev/packages/camera) plugin from flutter team, but make support for minSdkVersion 16


> The plugin still in development

### Installing

> The lib has not been published to [pub.dev](https://pub.dev/)

Clone and add to your `pubspec.yaml`

```
    dependencies:
        - flutter_camera
            git: "git_url_here"
```

#### Android
For android 10 support you must add `android:requestLegacyExternalStorage="true"` on `AndroidManifest.xml`

Example:
```
<application
        ...
        android:requestLegacyExternalStorage="true">
```

#### iOS

__Not Implemented yet__

### Hot to Use

``` 
import 'package:flutter_camera/flutter_camera.dart';
```

```
CameraController _controller;
Future<void> _initializeCompleterFuture;

@override
void initState() {
    _controller = CameraController(
        cameraLens: CameraLens.back,
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
    return Scaffold(
        appBar: AppBar(title: Text("Camera")),
        body: FutureBuilder<void>(
            future: _initializeCompleterFuture,
            builder: (context, snap) {
                if (snap.connectionState == ConnectionState.done) {
                return CameraPreview(_controller);
                } else {
                return Center(child: CircularProgressIndicator());
                }
            },
        ),
        floatingActionButton: FloatingActionButton(
          onPressed: () async {
            // Take picture
            String path = await _controller.takePickture();
          },
          child: Icon(Icons.camera_alt),
        ),
    );
}

```

#### Controller Property
property | description | default
---------|-------------|-------
cameraLens | CameraLens.front or CameraLens.back (optional) | CameraLens.back


### Todo
- [ ] Add iOS Support
- [ ] Fixing camera not found after closing activity
- [ ] Optimization
- [ ] Android camera rotating to 90deg image flipped
- [ ] ... _Later added_