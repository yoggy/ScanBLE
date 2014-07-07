package net.sabamiso.scanble;

public class BLEDeviceItem {
	private String name = "";
	private String address = "";
	private String rssi = "";

	public BLEDeviceItem() {
	}

	public BLEDeviceItem(String name, String address, String rssi) {
		this.name = name;
		this.address = address;
		this.rssi = rssi;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getRssi() {
		return rssi;
	}

	public void setRssi(String rssi) {
		this.rssi = rssi;
	}
	
	public String toString() {
		String str = "";
		
		str += "{";
		str += "name:" + name + ", ";
		str += "address:" + address + ", ";
		str += "rssi:" + rssi + ", ";
		str += "}";
		
		return str;
	}
}