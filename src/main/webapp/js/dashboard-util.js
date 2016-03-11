

(function() {
	var Utils = {
		debug: function(message) {
			var _ref;
			return typeof window !== "undefined" && window !== null ? (_ref = window.console) != null ? _ref.log(message) : void 0 : void 0;
		}
	}

	window.Dashboard.Utils = Utils;
})();
