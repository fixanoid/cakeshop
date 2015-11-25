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
	},

	prettyUpdate: function(oldValue, newValue, el) {
		if (oldValue !== newValue) {
			el.css({ 'opacity': 0 } );

			setTimeout(function() {
				el.html( $('<span>', { html: newValue }) );

				el.css({ 'opacity': 1 } );
			}, 500);
		}
	}
};


var screenManager = {
	grounds: $('#grounds'),
	loadedWidgets: {},

	addWidget: function(widget) {
		// shared injects
		widget.init();

		// to overwrite when the widget starts if we don't want it to render right away.
		// widget.ready = function() { widget.render(); };

		this.loadedWidgets[widget.name] = widget;
	},

	show: function(widgetId) {
		if (!widgetId) {
			return;
		}

		if (this.loadedWidgets[widgetId]) {
			// been loaded, execute?
		} else {
			// load widget and then run its payload
			$.getScript('js/widgets/' + widgetId + '.js').fail(function( jqxhr, settings, e ) {
				console.log( widgetId + ' loading failed with: ' +  e);
			});
		}
	}
}
