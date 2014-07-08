package net.sabamiso.scanble;

import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BLEDeviceActivity extends Activity {

	BluetoothManager bluetooth_manager;
	BluetoothAdapter bluetooth_adapter;
	BluetoothDevice  bluetooth_device;
	BluetoothGatt    bluetooth_gatt;
	List<BluetoothGattService> bluetooth_gatt_services;
	
	String device_name;
	String device_address;
	
	static final String KEY_CHAR_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
	static final String KEY_DESC_UUID = "00002902-0000-1000-8000-00805f9b34fb";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bledevice_activity);
	}

	@Override
	public void onResume() {
		super.onResume();

		Intent intent = getIntent();
		device_name = intent.getStringExtra("name");
		device_address = intent.getStringExtra("address");
		
		bluetooth_manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		bluetooth_adapter = bluetooth_manager.getAdapter();
		
		bluetooth_device = bluetooth_adapter.getRemoteDevice(device_address);
		if (bluetooth_device == null) {
			exit_message(R.string.error_bluetooth_adaptor_get_remote_device);
			return;
		}
		
		bluetooth_gatt = bluetooth_device.connectGatt(getApplicationContext(), false, bluetooth_gatt_callback);
		if (bluetooth_gatt == null) {
			exit_message(R.string.error_bluetooth_device_connect_gatt);
			return;
		}
		if (bluetooth_gatt.connect() == false) {
			exit_message(R.string.error_bluetooth_gatt_connect);
			return;
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		
		if (bluetooth_gatt_services != null) {
			bluetooth_gatt_services.clear();
			bluetooth_gatt_services = null;
		}
		if (bluetooth_gatt != null) {
			bluetooth_gatt.close();
			bluetooth_gatt = null;
		}
		if (bluetooth_device != null) {
			bluetooth_device = null;
		}
		if (bluetooth_adapter != null) {
			bluetooth_adapter = null;
		}
		if (bluetooth_manager != null) {
			bluetooth_manager = null;
		}
	}

	BluetoothGattCallback bluetooth_gatt_callback = new BluetoothGattCallback() {
		
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			switch(newState) {
			case BluetoothProfile.STATE_CONNECTED:
				Log.d("ScanBLE", "onConnectionStateChange() : STATE_CONNECTED -> call gatt.discoverServices()");
				if (gatt.discoverServices() == false) {
					Log.d("ScanBLE", "gatt.discoverService() failed...");
				}
				break;
			case BluetoothProfile.STATE_DISCONNECTED:
				Log.d("ScanBLE", "onConnectionStateChange() : STATE_DISCONNECTED -> call onBackPressed()");
				onBackPressed();
				break;
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Log.d("ScanBLE", "onServicesDiscovered() : received BluetoothGatt.GATT_SUCCESS");
				bluetooth_gatt_services = bluetooth_gatt.getServices();
				
				updateListView();
			}
		}
		
		// callback triggered as a result of a remote characteristic notification.
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic c) {
			Byte val = c.getValue()[0];
			Log.d("ScanBLE", "onCharacteristicChanged() val[0]=" + val);
		}

		// callback reporting the result of a characteristic read operation.
		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic c, int status) {
			Log.d("ScanBLE", "onCharacteristicRead()");
			
			if (status != BluetoothGatt.GATT_SUCCESS) {
				// read failed...
				return;
			}
		}
	};
		
	public void updateListView() {
		if (bluetooth_gatt_services == null) return;

		Log.d("ScanBLE", "updateListView() : bluetooth_gatt_services.size()=="+bluetooth_gatt_services.size());
		for (BluetoothGattService service : bluetooth_gatt_services) {
			Log.d("ScanBLE", "BluetoothGattService uuid=" + service.getUuid());
			List<BluetoothGattCharacteristic> cs = service.getCharacteristics();
			for (BluetoothGattCharacteristic c : cs) {
				Log.d("ScanBLE", "  BluetoothGattCharacteristic uuid=" + c.getUuid() + ", propery=" + toCharacteristicPropertiesString(c.getProperties()));	
				
				for (BluetoothGattDescriptor d : c.getDescriptors()) {
					Log.d("ScanBLE", "       BluetoothGattDescriptor uuid=" + d.getUuid() + ", permissions=" + toCharacteristicPropertiesString(c.getProperties()));	
				}
				
				// test for sensortag
				if (KEY_CHAR_UUID.equals(c.getUuid().toString()) == true) {
					// enable local
					bluetooth_gatt.setCharacteristicNotification(c, true);

					// enable remote
					BluetoothGattDescriptor desc = c.getDescriptor(UUID.fromString(KEY_DESC_UUID));
					desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					bluetooth_gatt.writeDescriptor(desc);
				}
			}
		}
	}
	
	private String toCharacteristicPropertiesString(int prop) {
		String str = "";
		
		if ((prop & BluetoothGattCharacteristic.PROPERTY_BROADCAST) != 0) {
			str += "PROPERTY_BROADCAST ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_EXTENDED_PROPS) != 0) {
			str += "PROPERTY_EXTENDED_PROPS ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
			str += "PROPERTY_INDICATE ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
			str += "PROPERTY_NOTIFY ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
			str += "PROPERTY_READ ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0) {
			str += "PROPERTY_SIGNED_WRITE ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
			str += "PROPERTY_WRITE ";
		}
		if ((prop & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) {
			str += "PROPERTY_WRITE_NO_RESPONSE ";
		}
		
		return str;
	}
	
	private void exit_message(int message_id) {
		Toast.makeText(this, message_id, Toast.LENGTH_LONG).show();
		onBackPressed();
	}
}
