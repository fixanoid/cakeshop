
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    // ------------------ gist load ----------------

    Sandbox.loadFromGist = function() {

        function getGistId(str) {
        	var idr = /[0-9A-Fa-f]{8,}/;
        	var match = idr.exec(str)[0];
        	return match;
        }

        var location_query_params = window.location.search.substr(1).split("=");
        var loadingFromGist = false;
        if (location_query_params.indexOf('gist') !== -1 && location_query_params.length >= 2) {
        	var index = location_query_params.indexOf('gist');
        	var gistId;
        	var key = location_query_params[index+1];
        	if (key === '') {
        		var str = prompt("Enter the URL or ID of the Gist you would like to load.");
        		if (str !== '') {
        			gistId = getGistId( str );
        			loadingFromGist = !!gistId;
        		}
        	} else {
        		gistId = getGistId( key );
        		loadingFromGist = !!gistId;
        	}
        	$.ajax({
        		url: 'https://api.github.com/gists/'+gistId,
        		jsonp: 'callback',
        		dataType: 'jsonp',
        		success: function(response){
        			if (response.data) {
        				for (var f in response.data.files) {
        					var key = Sandbox.fileKey(f);
        					var content = response.data.files[f].content;
        					if (key in window.localStorage && window.localStorage[key] != content) {
        						var count = '';
        						var otherKey = key + count;
        						while ((key + count) in window.localStorage) count = count - 1;
        						window.localStorage[key + count] = window.localStorage[key];
        					}
        					window.localStorage[key] = content;
        				}
        				SOL_CACHE_FILE = Sandbox.fileKey(Object.keys(response.data.files)[0]);
        				Sandbox.updateFiles();
        			}
        		}
        	});
        }

        return loadingFromGist;

    };

})();
