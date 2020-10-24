
require('../install_node_noversion/dist/entry');
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var ocp_idl = require('../install_node_noversion/dist/raritan/rpc/pdumodel/OverCurrentProtector');
var ocpSensor_idl = require('../install_node_noversion/dist/raritan/rpc/pdumodel/OverCurrentProtectorTripSensor');
var auth = require('../install_node_noversion/dist/jsonrpc');

module.exports = function(RED) {
    function GetOCPSensor(config) {
        RED.nodes.createNode(this,config);
        var node = this;
        this.configNode = RED.nodes.getNode(config.url);
        var ocpNo = config.ocpNo;
        auth.setHost(this.configNode.url);
        auth.setUsername(this.configNode.admin); 
        auth.setPassword(this.configNode.password);
        var nameService = new auth.NameService();

        function parseMessage(msg){
             if (!!msg.req)
                  ocpNo = msg.req.query.ocpNo;
             async function getOCPState() {
                  var pdu = new pdu_idl.Pdu((await nameService.lookup("/model/pdu/0"))[0].rid);
                  var ocp = new ocp_idl.OverCurrentProtector((await pdu.getOverCurrentProtectors())[0][ocpNo-1].rid);
                  var ocpTripSensor = new ocpSensor_idl.OverCurrentProtectorTripSensor((await ocp.getSensors())[0].trip.rid);
                  return (await ocpTripSensor.getState())[0].value;
             }     
             getOCPState().then(ret => {
                  msg.payload = ret;
                  node.send(msg);
             }).catch((error) => {
               console.log(error);
             });
        }

        node.on('input', parseMessage);

    }
    RED.nodes.registerType("sensor-ocp",GetOCPSensor);    

}