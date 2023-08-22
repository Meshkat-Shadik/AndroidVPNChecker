import UIKit
import Flutter
import NetworkExtension

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {
    var eventSink: FlutterEventSink?
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
    GeneratedPluginRegistrant.register(with: self)
      let controller = window?.rootViewController as! FlutterViewController
            let vpnChannel = FlutterEventChannel(name: "com.example.vpn_check_test/vpn", binaryMessenger: controller.binaryMessenger)
//                print("LISTENING")
                vpnChannel.setStreamHandler(self)

            // Get the shared NEVPNManager instance
            let vpnManager = NEVPNManager.shared()

            // Add an observer for the "status" key path of the vpnManager's connection property
            vpnManager.connection.addObserver(self, forKeyPath: "status", options: [.initial, .new], context: nil)

    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
    // Implement the observeValue(forKeyPath:of:change:context:) method to be notified when the VPN connection changes
        override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
            if keyPath == "status" {
                let vpnManager = NEVPNManager.shared()
                let status = vpnManager.connection.status
                switch status {
                case .connected:
                    eventSink?(true)
                case .disconnected:
                    eventSink?(false)
                default:
                    break
                }
            }
        }
}

extension AppDelegate: FlutterStreamHandler {
    func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
//        print("LISTENING")
        eventSink = events
        return nil
    }

    func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        return nil
    }
}
