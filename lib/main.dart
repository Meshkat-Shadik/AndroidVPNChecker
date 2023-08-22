import 'package:flutter/material.dart';
import 'package:vpn_check_test/vpn_checker.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(
    MaterialApp(
      theme: ThemeData(useMaterial3: true),
      home: const MyApp(),
    ),
  );
  await listenVPN();
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: const Text('VPN Checker'),
        centerTitle: true,
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            StreamBuilder<bool>(
              builder: (context, snapshot) {
                if (snapshot.hasData) {
                  return Column(
                    children: [
                      const Text('Current Vpn Status:'),
                      Text(
                        snapshot.data.toString(),
                        style: Theme.of(context).textTheme.headlineMedium,
                      ),
                    ],
                  );
                } else {
                  return const SizedBox();
                }
              },
              stream: vpn,
            ),
          ],
        ),
      ),
    );
  }
}
