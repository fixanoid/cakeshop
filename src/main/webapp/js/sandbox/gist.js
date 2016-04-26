
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
        		success: function(response) {
        			if (response.data) {
        				for (var key in response.data.files) {
        					var content = response.data.files[key].content;
                            if (Sandbox.Filer.get(key) === content) {
                                Sandbox.activateTab(key);
                                return;
                            }
                            var fname = Sandbox.Filer.getUniqueKey(key);
                            Sandbox.addFileTab(fname, content, true);
        				}
        			}
        		}
        	});
        }

        return loadingFromGist;

    };

})();
