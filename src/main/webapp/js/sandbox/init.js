
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};
    _.extend(Sandbox, Backbone.Events);

    function trimBase64Nulls(s) {
        for (var i = 0; i < s.length; i++) {
            if (s.charCodeAt(i) === 0) {
                return s.substring(0, i);
            }
        }
        return s; // no nulls?
    }

    Sandbox.decodeBytes = function(val) {
    	var useB64 = document.querySelector('#base64').checked;
        if (!useB64) {
            return val;
        }
        return trimBase64Nulls(Base64.decode(val).trim());
    };

    Sandbox.encodeBytes = function(val) {
    	var useB64 = document.querySelector('#base64').checked;
        if (!useB64) {
            return val;
        }
        return Base64.encode(val);
    };

})();