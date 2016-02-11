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
			ret = ret.substring(0, ret.indexOf(' Id')) +
				' ID' +
				ret.substring(ret.indexOf(' Id') + 4, ret.indexOf(' Id').length)
		}

		return ret;
	},

	idAlwaysFirst: function(arr) {
		// remove ID from wherever it is, and make it first
		arr = _.without(arr, 'id');

		// insert ID as first element
		arr.splice(0, 0, 'id');

		return arr;
	},

	makeAreaEditable: function(selector) {
		$(selector).click(function(e) {
			var isEditable = !!$(this).prop('contentEditable');
			$(this).prop('contentEditable', isEditable);

			$(this).focus();

			$(this).selectText();
		});
	}
};

var screenManager = {
	// DOM anchor for the widget field
	grounds: $('#grounds'),

	// section to widget mapping
	sectionMap: {},

	// widget id to widget mapping
	idMap: {},

	// widgets that have been queued for load
	queued: [],

	// widgets that have been loaded
	loaded: {},

	// init data for widgets
	initData: {},

	addWidget: function(widget) {
		// shared injects
		widget.init(this.initData[widget.name]);

		// set section widget belongs to
		_.each(Tower.screenManager.sectionMap, function(val, section) {
			_.each(val, function(v) {
				if (widget.name == v) {
					widget.section = section;
				}
			});
		});


		// to overwrite when the widget starts if we don't want it to render
		// right away.
		// widget.ready = function() { widget.render(); };

		this.loaded[widget.name] = widget;
		this.idMap[widget.shell.id] = widget;

		this.queued = _.without(this.queued, widget.name);
		delete this.initData[widget.name];
	},

	showSection: function(section, widgets) {
		// reset screen, load widgets
		screenManager.clear();

		// show registered section widgets
		_.each(widgets,
			function(val) {
				val.section = section;

				screenManager.show(val);
			});

		// show un-registered section widgets
		_.each(
			_.filter(screenManager.loaded, function(widget) { return widget.section === section }),
			function(val) {
				screenManager.show({ widgetId: val.name, section: section });
			});
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

			// if queued to load, exit here
			if ( this.queued.indexOf(opts.widgetId) >= 0) {
				return;
			}

			// mark as being queued
			this.queued.push(opts.widgetId);


			if (!screenManager.sectionMap[opts.section]) {
				screenManager.sectionMap[opts.section] = [];
			}

			screenManager.sectionMap[opts.section].push(opts.widgetId);

			// load widget and then run its payload
			$.getScript('js/widgets/' + opts.widgetId + '.js').fail(
				function(jqxhr, settings, e) {
					screenManager.sectionMap[opts.section] = _.without(screenManager.sectionMap[opts.section], opts.widgetId);
					screenManager.queued = _.without(screenManager.queued, opts.widgetId);

					Tower.debug(opts.widgetId + ' loading failed with: ' + e);
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
			if (Tower.current != val.section) {
				_this.hide(val);
			}
		});
	}
}

jQuery.fn.selectText = function() {
	var doc = document,
	 element = this[0];

	if (doc.body.createTextRange) {
		var range = document.body.createTextRange();
		range.moveToElementText(element);
		range.select();
	} else if (window.getSelection) {
		var selection = window.getSelection(),
		 range = document.createRange();

		range.selectNodeContents(element);
		selection.removeAllRanges();
		selection.addRange(range);
	}
};
