package org.meltzg.jmlm.device;

 public class MTPDeviceInfo {
		public String deviceId;
        public String friendlyName;
        public String description;
        public String manufacturer;
        
//        public final String deviceId;
//        public final String friendlyName;
//        public final String description;
//        public final String manufacturer;
        
        public MTPDeviceInfo() {
        }
        
        public MTPDeviceInfo(String deviceId, String friendlyName, String description, String manufacturer) {
        	this.deviceId = deviceId;
        	this.friendlyName = friendlyName;
        	this.description = description;
        	this.manufacturer = manufacturer;
        }
    }