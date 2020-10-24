require('../install_node_noversion/dist/entry');
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var inlet_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Inlet");
var ns_idl = require("../install_node_noversion/dist/raritan/rpc/sensors/NumericSensor");
var auth = require('../install_node_noversion/dist/jsonrpc');

module.exports = function(RED) {
    function GetSensorPole(config) {
        RED.nodes.createNode(this,config);
        var node = this;
        var nodeFollowRedirects = config["follow-redirects"];
        var selectedSensor = config.sensors;
        var poleNo = config.poleNo; 
        this.configNode = RED.nodes.getNode(config.url);
        auth.setHost(this.configNode.url);
        auth.setUsername(this.configNode.admin); 
        auth.setPassword(this.configNode.password);
        var nameService = new auth.NameService();


        function parseMessage(msg){
            async function getPoleSensor(){
                if (!!msg.req){
                    selectedSensor = msg.req.query.sensor;
                    poleNo = msg.req.query.poleNo;
                }
                let pdu = new pdu_idl.Pdu((await nameService.lookup("/model/pdu/0"))[0].rid);
                let inlet = new inlet_idl.Inlet((await pdu.getInlets())[0][0].rid);
                let pole = (await inlet.getPoles())[0][poleNo-1] ;
                if (!!msg.req && msg.req.query.getSensor == 1){
                    msg.payload = Object.keys(pole);
                }
                else if (selectedSensor != null){
                    if (pole[selectedSensor] != null) {
                        let sensor = new ns_idl.NumericSensor(pole[selectedSensor].rid);
                        let reading = await sensor.getReading();
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

            getPoleSensor().then(() => {
                node.send(msg);
            }).catch((error) => {
                console.log(error);
            });
        }

        node.on('input', parseMessage);

    }
    RED.nodes.registerType("sensor-pole",GetSensorPole);    
}