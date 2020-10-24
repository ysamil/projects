require('../install_node_noversion/dist/entry');
var auth = require("../install_node_noversion/dist/jsonrpc");
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var pDeviceManager_idl = require("../install_node_noversion/dist/raritan/rpc/peripheral/DeviceManager");
var pDeviceSlot_idl = require("../install_node_noversion/dist/raritan/rpc/peripheral/DeviceSlot");
var stateSensor_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/StateSensor");
var numericSensor_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/NumericSensor");
var switch_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/Switch");


module.exports = function(RED) {
    function GetPDevicesSensor(config) {
        RED.nodes.createNode(this,config);
        var node = this;
        this.configNode = RED.nodes.getNode(config.url);
        var deviceNo = config.deviceNo;
        var state = config.state;
        auth.setHost(this.configNode.url );
        auth.setUsername(this.configNode.admin); 
        auth.setPassword(this.configNode.password);
        var nameService = new auth.NameService();
        
        function parseMessage(msg){
            if (!!msg.req){
                deviceNo = msg.req.query.pDeviceNo;
                state = msg.req.query.state;
            }
            async function getSensor() {
                var pdu = new pdu_idl.Pdu((await nameService.lookup("/model/pdu/0"))[0].rid);
                var pDeviceManager = new pDeviceManager_idl.DeviceManager((await pdu.getPeripheralDeviceManager())[0].rid);
                var pDeviceSlot = new pDeviceSlot_idl.DeviceSlot((await pDeviceManager.getDeviceSlots())[0][deviceNo-1].rid);
                msg.payload = "Serial: " + (await pDeviceSlot.getDevice())[0].deviceID.serial; 
                var readingType = (await pDeviceSlot.getDevice())[0].deviceID.type.readingtype;
                msg.payload = msg.payload + "\nReading type: "+ readingType;
                var temp;
                if(readingType == 0){
                    var numericSensor = new numericSensor_idl.NumericSensor((await pDeviceSlot.getDevice())[0].device.rid);
                    temp = await numericSensor.getReading();
                }
                else if(readingType == 1){
                    var stateSensor = new stateSensor_idl.StateSensor((await pDeviceSlot.getDevice())[0].device.rid);
                    temp = await stateSensor.getState();
                }
                msg.payload = msg.payload + "\nValue: " + temp[0].value;
                var isActuator = (await pDeviceSlot.getDevice())[0].deviceID.isActuator;
                msg.payload = msg.payload + "\nIs actuator: " + isActuator;
                if(isActuator == true){
                    var switchSensor = new switch_idl.Switch((await pDeviceSlot.getDevice())[0].device.rid);
                    switchSensor.setState(parseInt(state));
                }
                node.send(msg);
            }
            getSensor().catch(error => {
                console.log(error);
            });
        }

        node.on('input', parseMessage);
    }
    RED.nodes.registerType("sensor-peripheral",GetPDevicesSensor);  
}