import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

const _channel = EventChannel('com.example.vpn_check_test/vpn');
final StreamController<bool> _vpnController =
    StreamController<bool>.broadcast();
Stream<bool> get vpn => _vpnController.stream;
StreamSubscription? subscription;

Future<void> listenVPN() async {
  subscription ??=
      _channel.receiveBroadcastStream().listen(_onEvent, onError: _onError);
}

void _onEvent(event) {
  // Handle time change event
  debugPrint('Time changed: $event');
  // time.add(event);
  _vpnController.add(event ?? false);
}

void _onError(error) {
  // Handle error
}

void dispose() {
  _vpnController.close();
}
