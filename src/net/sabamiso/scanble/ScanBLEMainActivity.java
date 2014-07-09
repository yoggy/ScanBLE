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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ScanBLEMainActivity extends Activity implements
		BluetoothAdapter.LeScanCallback, OnItemClickListener {

	protected BluetoothManager bluetooth_manager;
	protected BluetoothAdapter bluetooth_adapter;
	protected Handler handler = new Handler();
	protected boolean is_scannning = false;

	ArrayList<BLEDeviceItem> device_list = new ArrayList<BLEDeviceItem>();
	ArrayList<BLEDeviceItem> device_list_copy = new ArrayList<BLEDeviceItem>(); // for list_view
	BLEDeviceItemAdapter list_view_adaptor;

	private static final int REQUEST_ENABLE_BT = 1;
	private static final int SCAN_DULATION = 1000;
	private static final int SCAN_INTERVAL = 500;

	ListView list_view;
	TextView empty_view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_ble_main);

		// setup listview
		list_view = (ListView) findViewById(R.id.listView1);
		list_view_adaptor = new BLEDeviceItemAdapter(this, device_list_copy);
		list_view.setAdapter(list_view_adaptor);
		list_view.setOnItemClickListener(this);
		
		empty_view = (TextView) findViewById(R.id.empty_view);
		
		if (checkBluetoothAdaptor() == false) {
			return;
		}
		if (setupBluetooth() == false) {
			return;
		}		
	}

	@Override
	public void onResume() {
		Log.d("ScanBLE", "onResume()");

		super.onResume();

		if (checkBluetoothEnable() == true) {
			startBLEScan();
		}
	}

	@Override
	public void onPause() {
		Log.d("ScanBLE", "onPause()");

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

		return true;
	}

	protected boolean setupBluetooth() {
		// check for the presence of bluetooth adaptor.
		bluetooth_manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		bluetooth_adapter = bluetooth_manager.getAdapter();
		if (bluetooth_adapter == null) {
			exit_message(R.string.bluetooth_not_supported);
			return false;
		}
		
		return true;
	}
	
	protected boolean checkBluetoothEnable() {
		if (!bluetooth_adapter.isEnabled()) {
			Intent enable_bt_intent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enable_bt_intent, REQUEST_ENABLE_BT);

			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("ScanBLE", "onActivityResult()");

		if (requestCode == REQUEST_ENABLE_BT
				&& resultCode == Activity.RESULT_CANCELED) {
			exit_message(R.string.bluetooth_not_enabled);
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
		
		// Order of function call : onCreate()->onResume()->call startActivityForResult()->onPause()->[intent]->onActivityResult()->onResume()->...
	}

	private void startBLEScan() {
		Log.d("ScanBLE", "startBLEScan()");

		if (is_scannning == false) {
			device_list.clear();
			bluetooth_adapter.startLeScan(this);
		}
		is_scannning = true;

		handler.postDelayed(handle_stop_ble_scan, SCAN_DULATION);
	}

	private void stopBLEScan(boolean flag) {
		Log.d("ScanBLE", "stopBLEScan()");

		if (is_scannning == true) {
			bluetooth_adapter.stopLeScan(this);
		}

		updateListView();

		is_scannning = false;

		if (flag == true) {
			handler.postDelayed(handle_start_ble_scan, SCAN_INTERVAL);
		} else {
			handler.removeCallbacks(handle_start_ble_scan);
			handler.removeCallbacks(handle_stop_ble_scan);
		}
	}

	@Override
	public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
		BLEDeviceItem item = new BLEDeviceItem();
		item.setName(device.getName());
		item.setAddress(device.getAddress());
		item.setRssi(Integer.toString(rssi));
		
		Log.d("ScanBLE", "onLEScan() : item=" + item.toString());
		device_list.add(item);
	}
	
	protected void updateListView() {
		Log.d("ScanBLE", "updateListView() : device_list.size()=" + device_list.size());

		device_list_copy.clear();
		for (BLEDeviceItem item : device_list) {
			device_list_copy.add(item);
		}
		list_view_adaptor.notifyDataSetChanged();
		list_view.invalidate();
		device_list.clear();
		
		if (device_list_copy.size() > 0) {
			empty_view.setVisibility(View.INVISIBLE);
		}
		else {
			empty_view.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
        BLEDeviceItem item = (BLEDeviceItem) list_view.getItemAtPosition(position);
		Log.d("ScanBLE", item.toString());

        stopBLEScan(false);

		// start BLEDeviceActivity
		final Intent intent = new Intent(this, BLEDeviceActivity.class);
        intent.putExtra("name", item.getName());
        intent.putExtra("address", item.getAddress());
        intent.setAction(Intent.ACTION_VIEW);
        startActivity(intent);
	}

	private void exit_message(int message_id) {
		Toast.makeText(this, message_id, Toast.LENGTH_LONG).show();
		finish();
	}

	protected Runnable handle_start_ble_scan = new Runnable() {
		@Override
		public void run() {
			startBLEScan();
		}
	};

	protected Runnable handle_stop_ble_scan = new Runnable() {
		@Override
		public void run() {
			stopBLEScan(true);
		}
	};
}
