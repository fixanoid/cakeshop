
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    // ------------------ gist load ----------------

    Sandbox.loadFromGist = function(queryParams) {

        function getGistId(str) {
        	var idr = /[0-9A-Fa-f]{8,}/;
        	var match = idr.exec(str);
            if (match) {
                return match[0];
            }
        	return null;
        }

        var loadingFromGist = false;

        	var gistId;
        	var key = queryParams.gist;
        	if (key === '') {
        		var str = prompt("Enter the URL or ID of the Gist you would like to load.");
        		if (str !== '') {
        			gistId = getGistId(str);
        			loadingFromGist = !!gistId;
        		}
        	} else {
        		gistId = getGistId(key);
        		loadingFromGist = !!gistId;
        	}
        	$.ajax({
        		url: 'https://api.github.com/gists/' + gistId,
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

        return loadingFromGist;
    };

})();
