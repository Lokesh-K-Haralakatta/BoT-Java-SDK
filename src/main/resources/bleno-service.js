/*
ble-service.js - Standalone Node JS script for BLE advertising and interacting with FINN APP
Created by Lokesh H K, September 24, 2019.
Released into the repository BoT-Java-SDK.
*/

// Using the bleno module
var bleno = require('bleno');

// Variables to hold device configuration information
var deviceID = null;
var makerID = null;
var deviceName = null;
var publicKey = null;
var multipair = null;
var alternateID = null;

// Check for required command line arguments, otherwise exit
if (process.argv.length < 6){
	console.log("Usage: node bleno-service.js makerID deviceID deviceName PublicKey [Multipair] [aid]");
	process.exit(-1);
}
else {
	// Extract device data from command line arguments
	makerID = process.argv[2]
	deviceID = process.argv[3]
	deviceName = process.argv[4]
	publicKey = process.argv[5]

	if (process.argv.length == 8){
		multipair = process.argv[6]
		alternateID = process.argv[7]
	}
}

// UUIDs for the characteristics
const SERVICE_UUID = "729BE9C4-3C61-4EFB-884F-B310B6FFFFD1"
const DEVICE_CHARACTERISTIC_UUID = "CAD1B513-2DA4-4609-9908-234C6D1B2A9C"
const DEVICE_INFO_CHARACTERISTIC_UUID = "CD1B3A04-FA33-41AA-A25B-8BEB2D3BEF4E"
const DEVICE_NETWORK_CHARACTERISTIC_UUID = "C42639DC-270D-4690-A8B3-6BA661C6C899"
const CONFIGURE_CHARACTERISTIC_UUID = "32BEAA1B-D20B-47AC-9385-B243B8071DE4"

// Once bleno starts, begin advertising our BLE address
bleno.on('stateChange', function(state) {
    console.log('State change: ' + state);
    if (state === 'poweredOn') {
        bleno.startAdvertising('Java-SDK-BLE-Service',[SERVICE_UUID]);
    } else {
        bleno.stopAdvertising();
    }
});
 
// Notify the console that we've accepted a connection
bleno.on('accept', function(clientAddress) {
    console.log("Accepted connection from address: " + clientAddress);
});
 
// Notify the console that we have disconnected from a client
bleno.on('disconnect', function(clientAddress) {
    console.log("Disconnected from address: " + clientAddress);
});
 
// When we begin advertising, create a new service and characteristic
bleno.on('advertisingStart', function(error) {
    if (error) {
        console.log("Advertising start error:" + error);
    } else {
        console.log("Advertising start success");
        const device_data = {
                'deviceID': deviceID,
                'makerID': makerID,
                'name': deviceName,
                'publicKey' : publicKey
               };
        if (multipair != null){
        	device_data.multipair = 1;
        	device_data.aid = alternateID;
        }
        console.log(device_data);
        
        bleno.setServices([          
            // Define a new service
            new bleno.PrimaryService({
                uuid : SERVICE_UUID,
                characteristics : [
                    // Define device characteristic within the service
                    new bleno.Characteristic({
                        value : null,
                        uuid : DEVICE_CHARACTERISTIC_UUID,
                        properties : ['read'],
                        // Send a message back to the client with the characteristic's value
                        onReadRequest : function(offset, callback) {
                            console.log("Read request received for device characteristic, Offset: "+offset);
                            this.value = new Buffer(JSON.stringify(device_data));
                            callback(this.RESULT_SUCCESS,this.value.slice(offset, this.value.length));
                        }
                     })
                ]
            })
        ]);
    }
});