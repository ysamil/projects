require('../install_node_noversion/dist/entry');
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var outlet_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Outlet");
var inlet_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Inlet");
var pDeviceManager_idl = require("../install_node_noversion/dist/raritan/rpc/peripheral/DeviceManager");
var pDeviceSlot_idl = require("../install_node_noversion/dist/raritan/rpc/peripheral/DeviceSlot");
var auth = require("../install_node_noversion/dist/jsonrpc");


module.exports = function(RED) {
    function Settings(config) {
        RED.nodes.createNode(this,config);
        var node = this;

        var configNode = RED.nodes.getNode(config.url);
        auth.setHost(configNode.url);
        auth.setUsername(configNode.admin); 
        auth.setPassword(configNode.password);
        var nameService = new auth.NameService();

        function parseMessage(msg) {
            async function getInfo() {
                    let pdu_rid = await nameService.lookup("/model/pdu/0").catch(e => console.log(e));
                    let pdu = new pdu_idl.Pdu(pdu_rid[0].rid);
                    if(!!msg.req.query.sensorOutlet){
                        if(!!msg.req.query.getOutletNumber){
                            let numberOfOutlet = ((await pdu.getOutlets())[0]).length;
                            msg.payload = numberOfOutlet;
                        }   
                        else if(!!msg.req.query.getOutletSensor){
                            let outletNo = parseInt(msg.req.query.outletNo);
                            let outlet = new outlet_idl.Outlet((await pdu.getOutlets())[0][outletNo-1].rid);
                            let resolveSensors = (await outlet.getSensors())[0];
                            msg.payload = Object.keys(resolveSensors);
                        }
                    }
                    else if(!!msg.req.query.sensorOcp){
                            let numberOfOcp = ((await pdu.getOverCurrentProtectors())[0]).length;
                            msg.payload = numberOfOcp;
                    }
                    else if(!!msg.req.query.sensorInlet){
                        if(!!msg.req.query.getInletNumber){
                            let numberOfInlet = ((await pdu.getInlets())[0]).length;
                            msg.payload = numberOfInlet;
                        }
                        else if(msg.req.query.getInletSensor){
                            let inletNo = parseInt(msg.req.query.inletNo);
                            let inlet = new inlet_idl.Inlet((await pdu.getInlets())[0][inletNo-1].rid);
                            let resolveSensors = (await inlet.getSensors())[0];
                            msg.payload = Object.keys(resolveSensors);
                        }    
                    }
                    else if(!!msg.req.query.sensorPole){
                        let inlet = new inlet_idl.Inlet((await pdu.getInlets())[0][0].rid);
                        if(!!msg.req.query.getPoleNumber){
                            let numberOfPole = ((await inlet.getPoles())[0]).length;
                            msg.payload = numberOfPole;
                        }
                        else if(!!msg.req.query.getPoleSensor){
                            let poleNo = parseInt(msg.req.query.poleNo);
                            let pole = (await inlet.getPoles())[0][poleNo-1]
                            msg.payload = Object.keys(pole);
                        }    
                    }
                    else if(!!msg.req.query.sensorPeripheral){
                        let pDeviceManager = new pDeviceManager_idl.DeviceManager((await pdu.getPeripheralDeviceManager())[0].rid);
                        if(!!msg.req.query.getPeripheralNumber){
                            let numberOfPeripheral = ((await pDeviceManager.getDeviceSlots())[0]).length;
                            msg.payload = numberOfPeripheral;
                        }
                        else if(!!msg.req.query.isActuator){
                            let deviceNo = parseInt(msg.req.query.deviceNo);
                            let pDeviceSlot = new pDeviceSlot_idl.DeviceSlot((await pDeviceManager.getDeviceSlots())[0][deviceNo-1].rid);                        
                            let isActuator = (await pDeviceSlot.getDevice())[0].deviceID.isActuator;
                            msg.payload = isActuator.toString();
                        }    
                    }
            }         
            if(!!configNode.url){
                getInfo().then(() => {
                    node.send(msg);
                }).catch((error) => {
                    console.log(error);
                });
            }
            else {
                msg.payload = "Please create an HTTP end-point with 'settings' node!";
                node.send(msg);
            }
        }
        node.on('input', parseMessage);

        
    }
    RED.nodes.registerType("settings",Settings);    
}