/**
 * Pimatic Switch, Frank van Leeuwen 2016
 *
 * Create a switch that communicates with the pimatic API.
 *
 * Setup of the device requires you to give input for the following parameters: 
 * 	- deviceId 	: pimatic id of switch
 *  - ip 		: ip address of raspberry pi which runs pimatic
 *  - port 		: port which pimatic listens to  
 *  - username  : username part of credentials used to communicate with pimatic service
 *  - password  : password part of credentials used to communicate with pimatic service 
 *
 */
 
import groovy.json.JsonSlurper
import com.google.common.base.Splitter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


preferences {
		input("deviceId", "string", title:"Device Id", description: "Pimatic device Id", defaultValue: "switch-id" , required: true, displayDuringSetup: true)
        input("ip", "string", title:"IP Address", description: "Pimatic server IP", defaultValue: "192.168.1.1" ,required: true, displayDuringSetup: true)
        input("port", "string", title:"Port", description: "Pimatic server port", defaultValue: "888" , required: true, displayDuringSetup: true)
        input("username", "string", title:"Username", description: "Pimatic username", defaultValue: "user" , required: true, displayDuringSetup: true)
        input("password", "password", title:"Password", description: "Pimatic password", defaultValue: "password" , required: true, displayDuringSetup: true)
}

metadata {
	definition (name: "Pimatic Switch", namespace: "frankvl76", author: "Frank van Leeuwen") {
		capability "Polling"
        capability "Switch"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
        standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
            state "off", label: '${currentValue}', action: "switch.on",
                  icon: "st.switches.switch.off", backgroundColor: "#ffffff"
            state "on", label: '${currentValue}', action: "switch.off",
                  icon: "st.switches.switch.on", backgroundColor: "#79b821"
        }
        main "switch"
        details(["switch"])
    }
}

// ------------------------------------------------------------------

// parse events into attributes
def parse(String description) {
    def map = [:]
    def descMap = parseDescriptionAsMap(description)
    //log.debug "descMap: ${descMap}"
    
    def body = new String(descMap["body"].decodeBase64())
    //log.debug "body: ${body}"
    
    def slurper = new JsonSlurper()
    def result = slurper.parseText(body)
    
    //log.debug "result for polling RFswitch: ${result}"
    
    if (result.containsKey("device"))
    {
    	def isOn = false
    	def list = result.device.attributes
        for (rec in list){
        	if (rec.label == "State"){
            	isOn = rec.value
            	break	
            }
        }
    	//log.debug "Current state, switch on is ${isOn}"
        if (isOn){
        	log.debug "Update tile, switch is on"
        	sendEvent(name: "switch", value: "on")
        } else {
        	log.debug "Update tile, switch is off"
        	sendEvent(name: "switch", value: "off")
        }
    }  	
}

// handle commands
def poll() {
	//log.debug "Executing 'poll' for device: ${deviceId}"
    getSwitchStatus()
}

// handle commands
def on() {
	//log.debug "Executing 'on'"
    sendEvent(name: "switch", value: "on")
    def uri = "/api/device/" + "${deviceId}" + "/turnOn"
    log.debug "Action is : ${uri}"
    postAction(uri)
}

def off() {
	//log.debug "Executing 'off'"
    sendEvent(name: "switch", value: "off")
    def uri = "/api/device/" + "${deviceId}" + "/turnOff"
    log.debug "Action is : ${uri}"
    postAction(uri)
}

private getSwitchStatus() {
	//log.debug "Getting switch status"
	def uri = "/api/devices/${deviceId}"
    postAction(uri)
}

// ------------------------------------------------------------------

private postAction(uri){
  setDeviceNetworkId(ip,port)  
  
  
  def userpass = encodeCredentials(username, password)
  log.debug("userpass: " + username) 
  
  def headers = getHeader(userpass)
  log.debug("headders: " + headers) 
  
  def hubAction = new physicalgraph.device.HubAction(
    method: "GET",
    path: uri,
    headers: headers
  )
  //log.debug("Executing hubAction on " + getHostAddress())
  hubAction    
}

// ------------------------------------------------------------------
// Helper methods
// ------------------------------------------------------------------

def parseDescriptionAsMap(description) {
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
}


def toAscii(s){
        StringBuilder sb = new StringBuilder();
        String ascString = null;
        long asciiInt;
                for (int i = 0; i < s.length(); i++){
                    sb.append((int)s.charAt(i));
                    sb.append("|");
                    char c = s.charAt(i);
                }
                ascString = sb.toString();
                asciiInt = Long.parseLong(ascString);
                return asciiInt;
    }

private encodeCredentials(username, password){
	log.debug "Encoding credentials"
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.encodeAsBase64().toString()
    //log.debug "ASCII credentials are ${userpassascii}"
    //log.debug "Credentials are ${userpass}"
    return userpass
}

private getHeader(userpass){
	log.debug "Getting headers"
    def headers = [:]
    headers.put("HOST", getHostAddress())
    headers.put("Authorization", userpass)
    //log.debug "Headers are ${headers}"
    return headers
}

private delayAction(long time) {
	new physicalgraph.device.HubAction("delay $time")
}

// Had to incorporate deviceId in network Id because the network Id needs to be unique!
private setDeviceNetworkId(ip,port){
  	def iphex = convertIPtoHex(ip)
  	def porthex = convertPortToHex(port)
  	device.deviceNetworkId = "${deviceId}$iphex:$porthex" 
  	log.debug "Device Network Id set to ${deviceId}${iphex}:${porthex}"
}

private getHostAddress() {
	return "${ip}:${port}"
}

private String convertIPtoHex(ipAddress) { 
    String hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02x', it.toInteger() ) }.join()
    return hex

}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04x', port.toInteger() )
    return hexport
}