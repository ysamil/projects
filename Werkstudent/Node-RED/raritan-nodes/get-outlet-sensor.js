require('../install_node_noversion/dist/entry');
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var outlet_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Outlet");
var auth = require("../install_node_noversion/dist/jsonrpc");
var ns_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/NumericSensor");
var ss_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/StateSensor");

module.exports = function(RED) {
    function GetSensorOutlet(config) {
        RED.nodes.createNode(this,config);
        var node = this;
        var selectedSensor = config.sensors;
        var outletNo = config.outletNo;

        this.configNode = RED.nodes.getNode(config.url);
        auth.setHost(this.configNode.url);
        auth.setUsername(this.configNode.admin); 
        auth.setPassword(this.configNode.password);
        var nameService = new auth.NameService();
        function parseMessage(msg) {
            if (!!msg.req){
                selectedSensor = msg.req.query.sensor;
                outletNo = msg.req.query.outletNo; 
            }
            async function getSensor(){
                let pdu_rid = await nameService.lookup("/model/pdu/0").catch(e => console.log(e));
                let pdu = new pdu_idl.Pdu(pdu_rid[0].rid); 
                let outlet = new outlet_idl.Outlet((await pdu.getOutlets())[0][outletNo-1].rid);
                let resolveSensors = (await outlet.getSensors())[0];
                if(!!msg.req && msg.req.query.getSensor == 1){
                    msg.payload = Object.keys(resolveSensors);
                }
                else if (selectedSensor != null){
                    if (resolveSensors[selectedSensor] != null) {
                        let reading;
                        if ((resolveSensors[selectedSensor].rid).includes('NumericSensor')) {
                            let sensor = new ns_idl.NumericSensor(resolveSensors[selectedSensor].rid);
                            reading = await sensor.getReading();
                        }
                        else if ((resolveSensors[selectedSensor].rid).includes('StateSensor')) {
                            let sensor = new ss_idl.StateSensor(resolveSensors[selectedSensor].rid);
                            reading = await sensor.getState();
                        }
                        node.status({fill:"green",shape:"dot",text:"Sensor is available"});
                        msg.payload = reading[0].value;
                    }
                    else {
                        node.status({fill:"red",shape:"ring",text:"Sensor is not available"});
                        msg.payload = "Sensor is not available";
                    } 
                }
                return null;    
                
            }

            getSensor().then(() => {
                node.send(msg);
            }).catch((error) => {
                console.log(error);
            });
            
        }
        
        node.on('input', parseMessage);
    }

    RED.nodes.registerType("sensor-outlet",GetSensorOutlet);    
}

