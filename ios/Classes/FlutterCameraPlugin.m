#import "FlutterCameraPlugin.h"
#if __has_include(<flutter_camera/flutter_camera-Swift.h>)
#import <flutter_camera/flutter_camera-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_camera-Swift.h"
#endif

@implementation FlutterCameraPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterCameraPlugin registerWithRegistrar:registrar];
}
@end
