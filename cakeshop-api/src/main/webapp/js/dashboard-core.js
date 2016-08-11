// import this first because it sets a global all the rest need
import './widgets/widget-root';

const Scripts = {
  'accounts'               : require('./widgets/accounts'),
  'block-detail'           : require('./widgets/block-detail'),
  'block-list'             : require('./widgets/block-list'),
  'block-view'             : require('./widgets/block-view'),
  'contract-current-state' : require('./widgets/contract-current-state'),
  'contract-detail'        : require('./widgets/contract-detail'),
  'contract-list'          : require('./widgets/contract-list'),
  'contract-paper-tape'    : require('./widgets/contract-paper-tape'),
  'doc-frame'              : require('./widgets/doc-frame'),
  'metrix-blocks-min'      : require('./widgets/metrix-blocks-min'),
  'metrix-txn-min'         : require('./widgets/metrix-txn-min'),
  'metrix-txn-sec'         : require('./widgets/metrix-txn-sec'),
  'node-control'           : require('./widgets/node-control'),
  'node-info'              : require('./widgets/node-info'),
  'node-settings'          : require('./widgets/node-settings'),
  'peers-add'              : require('./widgets/peers-add'),
  'peers-list'             : require('./widgets/peers-list'),
  'peers-neighborhood'     : require('./widgets/peers-neighborhood'),
  'txn-detail'             : require('./widgets/txn-detail'),
};

window.Dashboard = {
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


	// debounced refreshing of the packery layout
	refresh: _.debounce(function() {
    Dashboard.grid.packery('layout');
	}, 0),

	// DOM anchor for the widget field and packery grid
	setGrounds: function(el) {
		Dashboard.grounds = el;
    Dashboard.grid = el.packery({
			columnWidth: '.widget-sizer',
			//rowHeight: '.widget-sizer',
			percentPosition: true,
			itemSelector: '.widget-shell',
      gutter: 0,
    });
	},

	// Optional module to enforce placement order
	render: {
		unstub: function(id) {
			$('#stub-' + id).remove();
		},

		stub: function(id) {
			var el = $('<div></div>', {
				id: 'stub-' + id,
				style: 'display: none;'
			});

			Dashboard.grounds.append(el);
		},

		widget: function(id, el) {
			$('#stub-' + id).replaceWith(el);
			Dashboard.render.unstub(id);
		}
	},

	addWidget: function(widget) {
		// shared injects
		widget.init(this.initData[widget.name]);

		// set section widget belongs to
		_.each(this.sectionMap, function(val, section) {
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

		// packery registration, and draggable + resizable init
		this.grid.packery('appended', $('#widget-shell-' + widget.shell.id)[0] );

		$('#widget-shell-' + widget.shell.id)
			.draggable({ handle: '.panel-heading' })
			.resizable({
				minHeight: $('.widget-sizer').height(),
				minWidth: 200,

				grid: [ 5, 5 ],
				resize: function( event, ui ) {
					$('#widget-' + widget.shell.id).css({
						height: ui.size.height - 76,
						width: ui.size.width - 35
					});

					Dashboard.refresh();
				}
			});

		this.grid.packery( 'bindUIDraggableEvents', $('#widget-shell-' + widget.shell.id) );


		this.refresh();
	},

	showSection: function(section, widgets) {
		// reset screen, load widgets
		Dashboard.clear();

		// show registered section widgets
		_.each(widgets,
			function(val) {
				val.section = section;

				Dashboard.show(val);
			});

		// show un-registered section widgets
		_.each(
			_.filter(Dashboard.loaded, function(widget) { return widget.section === section }),
			function(val) {
				Dashboard.show({ widgetId: val.name, section: section });
			});
	},

	show: function(opts) {
		if ((!opts) || (!opts.widgetId)) {
			return;
		}

		if (this.loaded[opts.widgetId]) {
      var $shell = $('#widget-shell-' + this.loaded[opts.widgetId].shell.id);
			// been loaded, execute?
			if ($shell.css('display') === 'none') {
				// Remove .panel-close
				$shell.children().removeClass('panel-close');

				$shell.css({
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


			if (!Dashboard.sectionMap[opts.section]) {
				Dashboard.sectionMap[opts.section] = [];
			}

			Dashboard.sectionMap[opts.section].push(opts.widgetId);

			// drop placement stub
			Dashboard.render.stub(opts.widgetId);

      Scripts[opts.widgetId]();
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
	clear: function(currentSection) {
		var _this = this;

		_.each(this.loaded, function(val, key) {
			if (currentSection != val.section) {
				_this.hide(val);
			}
		});
	},

	widgetControls: function() {
		$(document).on('click', function(e) {
			var el = $(e.target);

			if ( el.parent().parent().hasClass('panel-action') ) {

				// Widget collapse / expand handler
				if ( el.hasClass('fa-chevron-down') ) {
					var $ele = el.parents('.panel-heading');

					$ele.siblings('.panel-footer').toggleClass('panel-collapse');
					$ele.siblings('.panel-body').toggleClass('panel-collapse', function() {
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
					 $ele = el.parents('.panel-heading').siblings('.panel-body'),
					 postFetchFunc = function() {
 						$ele.find('.overlay').remove();
 						$ele.css('overflow-y', 'auto');
 					 };


					$ele
						.append('<div class="overlay"><div class="overlay-content"><i class="fa fa-refresh fa-2x fa-spin"></i></div></div>')
						.scrollTop(0)
						.css('overflow-y', 'hidden');


					Dashboard.idMap[wid].postFetch = postFetchFunc;
					(Dashboard.idMap[wid].fetch && Dashboard.idMap[wid].fetch());

					setTimeout(postFetchFunc, 2000);
				} else if ( el.hasClass('fa-link') ) {
					var wid = el.parents('.panel').parent().attr('id').replace('widget-shell-', ''),
					 params = {
						section: Dashboard.idMap[wid].section,
						widgetId: Dashboard.idMap[wid].name
					 },
					 link = document.location.protocol + '//' + document.location.host + document.location.pathname + '#';

					if (Dashboard.idMap[wid].data) {
						params.data = JSON.stringify(Dashboard.idMap[wid].data);
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

// INIT
$(function() {
	Dashboard.widgetControls();

	// misc hallo
	try {
		console.log(
			' _______ __________________   \n'+
			' \\      \\\\______   \\______ \\  \n'+
			' /   |   \\|     ___/|    |  \\ \n'+
			'/    |    \\    |    |    `   \\ \n'+
			'\\____|__  /____|   /_______  / \n'+
			'        \\/                 \\/ \n' +
			'This app is a product of NPD. \n'+
			'Interested? Ping R556615. Bye! ');
	} catch(e) {}
});
