require('../install_node_noversion/dist/entry');
var pdu_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Pdu");
var outlet_idl = require("../install_node_noversion/dist/raritan/rpc/pdumodel/Outlet");
var auth = require('../install_node_noversion/dist/jsonrpc');

module.exports = function(RED) {
    function SetOutlet(config) {
        RED.nodes.createNode(this,config);
        var node = this;
        var state = config.state;
        var outletNo = config.outletNo;
        this.configNode = RED.nodes.getNode(config.url);
        auth.setHost(this.configNode.url);
        auth.setUsername(this.configNode.admin); 
        auth.setPassword(this.configNode.password);
        var nameService = new auth.NameService();
        
        function parseMessage(msg){
            if (!!msg.req){
                state = msg.req.query.state;
                outletNo = msg.req.query.outletNo;
            }
            state = parseInt(state);
            
            async function setState(){
                let pdu = new pdu_idl.Pdu((await nameService.lookup("/model/pdu/0"))[0].rid);
                let outlet = new outlet_idl.Outlet((await pdu.getOutlets())[0][outletNo-1].rid);
                return outlet.setPowerState(state);  
            }      

            setState().then(ret => {
                if(ret == 0){
                    node.status({fill:"green",shape:"dot",text:"OK"});
                    msg.payload = "OK";
                }
                else if (ret == 1){
                    node.status({fill:"red",shape:"ring",text:"the outlet is not switchable"});
                    msg.payload = "the outlet is not switchable";
                }
                else if (ret == 3){
                    node.status({fill:"green",shape:"dot",text:"the outlet is disabled"});
                    msg.payload = "the outlet is disabled";
                }
                node.send(msg);
            }).catch((error) => {
                console.log(error);
            });
        }
        
        node.on('input', parseMessage);

    }

   

    RED.nodes.registerType("set-outlet-state",SetOutlet);
}