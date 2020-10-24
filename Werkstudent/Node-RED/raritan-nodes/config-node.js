module.exports = function(RED) {
    function ConfigNode(config) {
        RED.nodes.createNode(this,config);
        this.url = config.url;
        this.admin = config.admin;
        this.password = config.password;
    }
    RED.nodes.registerType("config-node",ConfigNode);
}