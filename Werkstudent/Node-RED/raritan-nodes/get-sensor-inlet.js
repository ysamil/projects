require('../install_node_noversion/dist/entry');
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var inlet_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Inlet");
var auth = require('../install_node_noversion/dist/jsonrpc');
var ns_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/NumericSensor");

module.exports = function(RED) {
    function GetSensorInlet(config) {
        RED.nodes.createNode(this,config);
        var node = this;
        var selectedSensor = config.sensors;
        this.configNode = RED.nodes.getNode(config.url);
        auth.setHost(this.configNode.url);
        auth.setUsername(this.configNode.admin); 
        auth.setPassword(this.configNode.password);
        var nameService = new auth.NameService();

        function parseMessage(msg){
            msg.topic = "inletSensor";
            if (!!msg.req)  
                    selectedSensor = msg.req.query.sensor;// this part for the dashboard
            async function getSensor(){
                let pdu = new pdu_idl.Pdu((await nameService.lookup("/model/pdu/0"))[0].rid);
                let inlet = new inlet_idl.Inlet((await pdu.getInlets())[0][0].rid);
                let resolveSensors = (await inlet.getSensors())[0];
                if (!!msg.req && msg.req.query.getSensor == 1 ){
                    msg.payload = Object.keys(resolveSensors);
                }
                else if (selectedSensor != null){
                    if(resolveSensors[selectedSensor]!= null){
                        let sensor = new ns_idl.NumericSensor(resolveSensors[selectedSensor].rid);
                        let reading = await sensor.getReading();
                        msg.payload = reading[0].value;
                        node.status({fill:"green",shape:"dot",text:"Sensor is available"});
                    }
                    else {
                        msg.payload = "Sensor is not available";
                        node.status({fill:"red",shape:"ring",text:"Sensor is not available"});
                    }
                }    
            }
          
            getSensor().then( ()=> {
                node.send(msg);
            }).catch((error) => {
                console.log(error);
            });
        }

        node.on('input', parseMessage);

    }
    RED.nodes.registerType("sensor-inlet",GetSensorInlet);    
}