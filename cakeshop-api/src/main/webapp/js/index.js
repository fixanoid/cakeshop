
var Tower = {
	ready: false,
	current: null,
	status: {},

	// Tower Control becomes ready only after the first status is received from the server
	isReady: function() {
		Tower.ready = true;

		// let everyone listening in know
		$(document).trigger('WidgetInternalEvent', [ 'tower-control|ready|true' ]);

		return true;
	},


	init: function() {
		Dashboard.setGrounds($('#grounds'));

		// Adding event for sleep / wake
		$(document).on('visibilitychange', function(e) {
			$(document).trigger('WidgetInternalEvent', [ 'tower-control|sleep|' + document.hidden]);
		});

		// Adding event for hash changes
		$(window).on('hashchange', this.processHash);


		// event handler registration for clipboard fuckery
		$('#_clipboard_button').on('click', utils.copyToClipboard);

		this.processHash();

		// Reusing socket from cakeshop.js
		Tower.stomp = Client.stomp;
		Tower.stomp_subscriptions = Client._stomp_subscriptions;

		Tower.section['default']();
	},


	processHash: function() {
		// http://localhost:8080/cakeshop/index.html#section=explorer&widgetId=txn-detail&data=0xd6398cb5cb5bac9d191de62665c1e7e4ef8cd9fe1e9ff94eec181a7b4046345c
		// http://localhost:8080/cakeshop/index.html#section=explorer&widgetId=block-detail&data=2
		if (window.location.hash) {
			var params = {}, hash = window.location.hash.substring(1, window.location.hash.length);

			_.each(hash.split('&'), function(pair) {
				pair = pair.split('=');
				params[pair[0]] = decodeURIComponent(pair[1]);
			});

			var werk = function() {
				if (params.section) {
					$('.tower-sidebar #' + params.section).click();
				}

				if (params.data) {
					try {
						params.data = JSON.parse(params.data);
					} catch (err) {}
				}

				if (params.widgetId) {
					Dashboard.show({ widgetId: params.widgetId, section: params.section ? params.section : Tower.current, data: params.data, refetch: true });
				}
			};

			// do when ready
			if (!Tower.ready) {
				$(document).on('WidgetInternalEvent', function(ev, action) {
					if (action.indexOf('tower-control|ready|') === 0) {
						werk();
					}
				});
			} else {
				werk();
			}
		}
	},

	section: {
		'default': function() {
			var statusUpdate = function(response) {
				var status = response.data.attributes;

				if (status.status === 'running') {
					$('#default-node-status').html( $('<span>', { html: 'Running' }) );

					$('#default-node-status').parent().find('.fa')
					 .removeClass('fa-pause tower-txt-danger')
					 .addClass('fa-play tower-txt-success');
				} else {
					$('#default-node-status').html( $('<span>', { html: utils.capitalize(status.status) }) );

					$('#default-node-status').parent().find('.fa')
					 .removeClass('fa-play tower-txt-success')
					 .addClass('fa-pause tower-txt-danger');
				}

				utils.prettyUpdate(Tower.status.peerCount, status.peerCount, $('#default-peers'));
				utils.prettyUpdate(Tower.status.latestBlock, status.latestBlock, $('#default-blocks'));
				utils.prettyUpdate(Tower.status.pendingTxn, status.pendingTxn, $('#default-txn'));

				Tower.status = status;

				// Tower Control becomes ready only after the first status is received from the server
				if (!Tower.ready) {
					Tower.isReady();
				}

				$(document).trigger('WidgetInternalEvent', [ 'node-status|announce' ]);
			};

			$.when(
				utils.load({ url: 'api/node/get' })
			).done(function(response) {
				statusUpdate(response);
			}).fail(function() {
				statusUpdate({
					status: 'DOWN',
					peerCount: 'n/a',
					latestBlock: 'n/a',
					pendingTxn: 'n/a'
				});
			});

			utils.subscribe('/topic/node/status', statusUpdate);
		},

		'console': function() {
			var widgets = [
				{ widgetId: 'node-info' },
				{ widgetId: 'node-control' },
				{ widgetId: 'node-settings' },
				{ widgetId: 'metrix-txn-sec' },
				{ widgetId: 'metrix-txn-min' },
				{ widgetId: 'metrix-blocks-min' }
			];

			Dashboard.showSection('console', widgets);
		},

		'peers': function() {
			var widgets = [
				{ widgetId: 'peers-add' },
				{ widgetId: 'peers-list' },
				{ widgetId: 'peers-neighborhood', data: Tower.status.nodeIP }
			];

			Dashboard.showSection('peers', widgets);
		},

		'api': function() {
			var widgets = [
				{ widgetId: 'doc-frame' }
			];

			Dashboard.showSection('api', widgets);
		},

		'contracts': function() {
			var widgets = [
				{ widgetId: 'contract-list' }
			];

			Dashboard.showSection('contracts', widgets);
		},

		'explorer': function() {
			var widgets = [
				{ widgetId: 'block-detail', data: Tower.status.latestBlock },
				{ widgetId: 'block-list', data: Tower.status.latestBlock },
				{ widgetId: 'block-view' }
			];

			Dashboard.showSection('explorer', widgets);
		},

		'wallet': function() {
			var widgets = [
				{ widgetId: 'accounts' }
			];

			Dashboard.showSection('wallet', widgets);
		},
	},


	debug: function(message) {
		var _ref;
		return typeof window !== 'undefined' && window !== null ? (_ref = window.console) !== null ? _ref.log(message) : void 0 : void 0;
    }
};



$(function() {
	$(window).on('scroll', function(e) {
		if ($(window).scrollTop() > 50) {
			$('body').addClass('sticky');
		} else {
			$('body').removeClass('sticky');
		}
	});

	// logo handler
	$("a.tower-logo").click(function(e) {
		e.preventDefault();
		$("#console").click();
	});

	// Menu (burger) handler
	$('.tower-toggle-btn').on('click', function() {
		$('.tower-logo-container').toggleClass('tower-nav-min');
		$('.tower-sidebar').toggleClass('tower-nav-min');
		$('.tower-body-wrapper').toggleClass('tower-nav-min');
	});


	// Navigation menu handler
	$('.tower-sidebar li').click(function(e) {
		if ($(this).attr('id') === 'sandbox') {
			return;
		}

		e.preventDefault();

		Tower.current = $(this).attr('id');

		$('.tower-sidebar li').removeClass('active');
		$(this).addClass('active');

		Tower.section[Tower.current]();

		$('.tower-page-title').html( $('<span>', { html: $(this).find('.tower-sidebar-item').html() }) );
	});




	// ---------- INIT -----------
	Tower.init();

	// add dispatcher listener
	// $(document).on('WidgetInternalEvent', function(ev, action) {
	// 	Tower.debug(ev, action);
	// });

	// Setting 'Console' as first section
	$('.tower-sidebar li').first().click();
});
