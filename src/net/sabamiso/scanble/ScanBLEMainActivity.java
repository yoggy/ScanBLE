package net.sabamiso.scanble;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class ScanBLEMainActivity extends Activity implements
		BluetoothAdapter.LeScanCallback {

	protected BluetoothManager bluetooth_manager;
	protected BluetoothAdapter bluetooth_adapter;
	protected Handler handler = new Handler();
	protected boolean is_scannning = false;
	
	ArrayList<Item> device_list = new ArrayList<Item>();
	
	private static final int REQUEST_ENABLE_BT = 1;
	private static final int SCAN_DULATION = 2000;
	private static final int SCAN_INTERVAL = 500;
	
	ListView list_view;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_ble_main);

		list_view = (ListView) findViewById(R.id.listView1);
		
		if (checkBluetoothAdaptor() == false) {
			return;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				checkBluetoothEnable();
			}
		}, 1000);
	}

	@Override
	public void onPause() {
		stopBLEScan(false);
		super.onPause();
	}

	private boolean checkBluetoothAdaptor() {
		// check BLE capability
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			exit_message(R.string.ble_not_supported);
			return false;
		}

		// check for the presence of bluetooth adaptor.
		bluetooth_manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		bluetooth_adapter = bluetooth_manager.getAdapter();
		if (bluetooth_adapter == null) {
			exit_message(R.string.bluetooth_not_supported);
			return false;
		}

		return true;
	}

	private void checkBluetoothEnable() {
		if (!bluetooth_adapter.isEnabled()) {
			Intent enable_bt_intent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enable_bt_intent, REQUEST_ENABLE_BT);
		} else {
			startBLEScan();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			exit_message(R.string.bluetooth_not_enabled);
			return;
		}
		startBLEScan();
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startBLEScan() {
		if (is_scannning == false) {
			bluetooth_adapter.startLeScan(this);
			device_list.clear();
		}
		is_scannning = true;
		
		handler.postDelayed(handle_stop_ble_scan, SCAN_DULATION);
	}

	private void stopBLEScan(boolean flag) {
		if (is_scannning == true) {
			bluetooth_adapter.stopLeScan(this);
			updateDeviceList();
		}
		is_scannning = false;

		if (flag == true) {
			handler.postDelayed(handle_start_ble_scan, SCAN_INTERVAL);
		}
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Item item = new Item();
		item.setName(device.getName());
		item.setAddress(device.getAddress());
		item.setRssi(Integer.toString(rssi));

		Log.d("ScanBLE", item.toString());
		device_list.add(item);
	}

	private void updateDeviceList() {
		ItemAdapter adapter = new ItemAdapter(this, device_list);
		list_view.setAdapter(adapter);
	}

	private void exit_message(int message_id) {
		Toast.makeText(this, message_id, Toast.LENGTH_LONG).show();
		finish();
	}

	protected Runnable handle_start_ble_scan = new Runnable() {
		@Override
		public void run() {
			startBLEScan();
			Log.d("ScanBLE", "startBLEScan() called");
		}
	};
	
	protected Runnable handle_stop_ble_scan = new Runnable() {
		@Override
		public void run() {
			stopBLEScan(true);
			Log.d("ScanBLE", "stopBLEScan() called");
		}
	};
}
