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

	subscribe: function(topic, handler) {
		if (Tower.stomp && Tower.stomp.connected === true) {
			Tower.debug('STOMP subscribing to ' + topic);

			return Tower.stomp.subscribe(topic, function(res) {
				var status = JSON.parse(res.body);
				status = status.data.attributes;

				handler(status);
			});
		}

		return false;
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
			(ret.toLowerCase() === 'txn') ||
			(ret.toLowerCase() === 'abi') ) {
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
	},

	copyToClipboard: function(e) {
	    var t = e.target,
		 c = t.dataset.copytarget,
		 inp = (c ? document.querySelector(c) : null);

	    // is element selectable?
	    if (inp && inp.select) {
	        // select text
	        inp.select();

	        try {
	    		// copy text
	        	document.execCommand('copy');
	        	inp.blur();
	        } catch (err) {}
	    }
	},

	truncAddress: function(addr) {
        var len = addr.startsWith('0x') ? 10 : 8;

        return addr.substring(0, len);
    }
};


var Client = {
	post: function(url, data) {
		var options = {
			url: url,
			data: data
		}

		return utils.load(options);
	}
};


var screenManager = {
	// DOM anchor for the widget field
	grounds: $('#grounds'),
	grid: $('#grounds').packery({
		columnWidth: '.widget-sizer',
		//rowHeight: '.widget-sizer',
		percentPosition: true,
		itemSelector: '.widget-shell',
		gutter: 0
	}),

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

	refresh: _.debounce(function() {
		screenManager.grid.packery('layout');
	}, 0),

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

		this.grid.packery('appended', $('#widget-shell-' + widget.shell.id)[0] );

		$('#widget-shell-' + widget.shell.id)
			.draggable({ handle: '.panel-heading' })
			.resizable({
				grid: [ 10, 10 ],
				resize: function( event, ui ) {
					// console.log(ui.element.attr('id'), ui.size);
					$('#widget-' + widget.shell.id).css({
						height: ui.size.height - 76,
						width: ui.size.width - 35
					});

					screenManager.refresh();
				}
			});

		this.grid.packery( 'bindUIDraggableEvents', $('#widget-shell-' + widget.shell.id) );


		this.refresh();
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

			this.refresh();
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
	},

	widgetControls: function() {
		$(document).on('click', function(e) {
			var el = $(e.target);

			if ( el.parent().parent().hasClass('rad-panel-action') ) {

				// Widget collapse / expand handler
				if ( el.hasClass('fa-chevron-down') ) {
					var $ele = el.parents('.panel-heading');

					$ele.siblings('.panel-footer').toggleClass('rad-collapse');
					$ele.siblings('.panel-body').toggleClass('rad-collapse', function() {
						setTimeout(function() {

						}, 200);
					});

				// Widget close handler
				} else if ( el.hasClass('fa-close') ) {
					var $ele = el.parents('.panel');
					$ele.addClass('panel-close');

					setTimeout(function() {
						$ele.parent().css({ 'display': 'none'});
					}, 210);

				// Widget refresh handler
				} else if ( el.hasClass('fa-rotate-right') ) {
					var wid = el.parents('.panel').parent().attr('id').replace('widget-shell-', ''),
					 $ele = el.parents('.panel-heading').siblings('.panel-body');


					$ele
						.append('<div class="overlay"><div class="overlay-content"><i class="fa fa-refresh fa-2x fa-spin"></i></div></div>')
						.scrollTop(0)
						.css('overflow-y', 'hidden');

					setTimeout(function() {
						$ele.find('.overlay').remove();
						$ele.css('overflow-y', 'auto');

						(Tower.screenManager.idMap[wid].fetch && Tower.screenManager.idMap[wid].fetch());
					}, 2000);
				} else if ( el.hasClass('fa-link') ) {
					var wid = el.parents('.panel').parent().attr('id').replace('widget-shell-', ''),
					 params = {
						section: Tower.screenManager.idMap[wid].section,
						widgetId: Tower.screenManager.idMap[wid].name
					 },
					 link = document.location.protocol + '//' + document.location.host + document.location.pathname + '#';

					if (Tower.screenManager.idMap[wid].data) {
						params.data = JSON.stringify(Tower.screenManager.idMap[wid].data);
					}

					link += $.param(params);

					// Notification tooltip
					$(el).tooltip({ placement: 'top' }).tooltip('show');

					setTimeout(function() {
						$(el).tooltip('destroy');
					}, 1000);

					$('#_clipboard').val(link);
					$('#_clipboard_button').click();
				}
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
