var utils = {
	load : function(opts) {
		var config = {
			headers : {
				'janus_user' : 'V442113'
			},
			type : opts.method ? opts.method : 'POST',
			url : opts.url,
			contentType : opts.type ? opts.type : 'application/json',
			cache : false,
			async : true
		};

		if (opts.data) {
			config.data = JSON.stringify(opts.data);
		}

		return $.ajax(config);
	},


	prettyUpdate : function(oldValue, newValue, el) {
		if (oldValue !== newValue) {
			el.css({
				'opacity' : 0
			});

			setTimeout(function() {
				el.html($('<span>', {
					html : newValue
				}));

				el.css({
					'opacity' : 1
				});
			}, 500);
		}
	},

	capitalize : function(str) {
		return str.charAt(0).toUpperCase() + str.slice(1);
	}
};

var screenManager = {
	// DOM anchor for the widget field
	grounds : $('#grounds'),

	// section to widget mapping
	sectionMap : {},

	// widget id to widget mapping
	idMap : {},

	// widgets that have been loaded
	loaded : {},

	addWidget : function(widget) {
		// shared injects
		widget.init();

		// set section widget belongs to
		widget.section = _.invert(this.sectionMap)[widget.name];

		// to overwrite when the widget starts if we don't want it to render
		// right away.
		// widget.ready = function() { widget.render(); };

		this.loaded[widget.name] = widget;
		this.idMap[widget.shell.id] = widget;
	},

	show : function(opts) {
		if ((!opts) || (!opts.widgetId)) {
			return;
		}

		if (this.loaded[opts.widgetId]) {
			// been loaded, execute?
			if ($('#widget-shell-' + this.loaded[opts.widgetId].shell.id).css(
					'display') === 'none') {
				// Remove .panel-close
				$('#widget-shell-' + this.loaded[opts.widgetId].shell.id)
						.children().removeClass('panel-close');

				$('#widget-shell-' + this.loaded[opts.widgetId].shell.id).css({
					'display' : 'block'
				});
			}
		} else {
			// load widget and then run its payload
			$.getScript('js/widgets/' + opts.widgetId + '.js').then(
					function(jqxhr, settings, e) {
						screenManager.sectionMap[opts.section] = opts.widgetId;
					}).fail(function(jqxhr, settings, e) {
				delete screenManager.sectionMap[opts.section];
				console.log(opts.widgetId + ' loading failed with: ' + e);
			});
		}
	},

	hide : function(widget) {
		if ($('#widget-shell-' + widget.shell.id).css('display') !== 'none') {
			$('#widget-shell-' + widget.shell.id).css({
				'display' : 'none'
			});
		}
	},

	// clear the grounds of any displayed widgets
	// TODO: using show/hide CSS fuckery. Could be easier in the long run to
	// remove from DOM
	clear : function() {
		var _this = this;

		_.each(this.loaded, function(val, key) {
			// TODO: limit the scan to the currently visible section only?
			_this.hide(val);
		})
	}
}
