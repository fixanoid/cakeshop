var utils = {
	load: function(opts) {
		return $.ajax({
					headers: { 'janus_user': 'V442113' },
					type: opts.method ? opts.method : 'POST',
					url : opts.url,
					contentType: opts.type ? opts.type : 'application/json',
					cache : false,
					async : true
				});
	}
};
