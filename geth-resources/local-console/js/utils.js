var utils = {
	load: function(opts) {
		var config = {
			headers: {
				'janus_user': 'V442113'
			},
			type: opts.method ? opts.method : 'POST',
			url: opts.url,
			contentType: opts.type ? opts.type : 'application/json',
			cache: false,
			async: true
		};

		if (opts.data) {
			config.data = JSON.stringify(opts.data);
		}

		if (opts.complete) {
			config.complete = opts.complete;
		}

		return $.ajax(config);
	},

	prettyUpdate: function(oldValue, newValue, el) {
		if (oldValue !== newValue) {
			el.css({
				'opacity': 0
			});

			setTimeout(function() {
				el.html($('<span>', {
					html: newValue
				}));

				el.css({
					'opacity': 1
				});
			}, 500);
		}
	},

	prettyMoneyPrint: function(val) {
		if (val) {
			var sign = '';

			if (val < 0) {
				sign = '-';
			}

			return sign + '$' + Math.abs(val).toFixed(2).replace(/\d(?=(\d{3})+\.)/g, '$&,');
		}
	},

	capitalize: function(str) {
		return str.charAt(0).toUpperCase() + str.slice(1);
	},

	camelToRegularForm: function(t) {
		var ret = t.replace(/([a-z])([A-Z])/g, '$1 $2').replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3').replace(/^./, function(str){ return str.toUpperCase(); });

		if ( (ret.toLowerCase() === 'id') ||
			(ret.toLowerCase() === 'url') ||
			(ret.toLowerCase() === 'txn') ) {
			ret = ret.toUpperCase();
		} else if (ret.toLowerCase().indexOf(' url') >= 0) {
			ret = ret.substring(0, ret.indexOf(' Url')) +
				' URL' +
				ret.substring(ret.indexOf(' Url') + 4, ret.indexOf(' Url').length)
		} else if (ret.toLowerCase().indexOf(' txn') >= 0) {
			ret = ret.substring(0, ret.indexOf(' Txn')) +
				' TXN' +
				ret.substring(ret.indexOf(' Txn') + 4, ret.indexOf(' Txn').length)
		} else if (ret.toLowerCase().indexOf(' id') >= 0) {
			ret = ret.substring(0, ret.indexOf(' id')) +
				' ID' +
				ret.substring(ret.indexOf(' ID') + 4, ret.indexOf(' ID').length)
		}

		return ret;
	}
};

var screenManager = {
	// DOM anchor for the widget field
	grounds: $('#grounds'),

	// section to widget mapping
	sectionMap: {},

	// widget id to widget mapping
	idMap: {},

	// widgets that have been loaded
	loaded: {},

	// init data for widgets
	initData: {},

	addWidget: function(widget) {
		// shared injects
		widget.init(this.initData[widget.name]);

		// set section widget belongs to
		widget.section = _.invert(this.sectionMap)[widget.name];

		// to overwrite when the widget starts if we don't want it to render
		// right away.
		// widget.ready = function() { widget.render(); };

		this.loaded[widget.name] = widget;
		this.idMap[widget.shell.id] = widget;

		delete this.initData[widget.name];
	},

	show: function(opts) {
		if ((!opts) || (!opts.widgetId)) {
			return;
		}

		if (this.loaded[opts.widgetId]) {
			// been loaded, execute?
			if ($('#widget-shell-' + this.loaded[opts.widgetId].shell.id).css('display') === 'none') {
				// Remove .panel-close
				$('#widget-shell-' + this.loaded[opts.widgetId].shell.id)
					.children().removeClass('panel-close');

				$('#widget-shell-' + this.loaded[opts.widgetId].shell.id).css({
					'display': 'block'
				});
			}

			if ( (opts.data) && this.loaded[opts.widgetId].setData ) {
				this.loaded[opts.widgetId].setData(opts.data);

				if (opts.refetch) {
					this.loaded[opts.widgetId].fetch();
				}
			}
		} else {
			if (opts.data) {
				this.initData[opts.widgetId] = opts.data;
			}

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

	hide: function(widget) {
		if ($('#widget-shell-' + widget.shell.id).css('display') !== 'none') {
			$('#widget-shell-' + widget.shell.id).css({
				'display': 'none'
			});
		}
	},

	// clear the grounds of any displayed widgets
	// TODO: using show/hide CSS fuckery. Could be easier in the long run to
	// remove from DOM
	clear: function() {
		var _this = this;

		_.each(this.loaded, function(val, key) {
			// TODO: limit the scan to the currently visible section only?
			_this.hide(val);
		})
	}
}
