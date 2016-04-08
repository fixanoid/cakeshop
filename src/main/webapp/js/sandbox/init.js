
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};
    _.extend(Sandbox, Backbone.Events);

    Sandbox.decodeBytes = function(val) {
    	var useB64 = document.querySelector('#base64').checked;
        if (!useB64) {
            return val;
        }
        return Base64.decode(val);
    };

    Sandbox.encodeBytes = function(val) {
    	var useB64 = document.querySelector('#base64').checked;
        if (!useB64) {
            return val;
        }
        return Base64.encode(val);
    };

})();
