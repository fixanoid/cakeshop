var demo = {
	on: false,

	add: function() {
		if (!this.on) {
			return 0;
		}

		return Math.ceil(Math.random() * 10);
	}
}

var Tower = {
	current: null,
	status: {
		peers: null,
		latestBlock: null,
		pedningTxn: null,
		status: null
	},

	screenManager: screenManager,

	section: {
		'default': function() {
			// these are perma-widgets
			var def = function() {
				$.when(
					utils.load({ url: '../api/node/status' })
				).done(function(response) {
					var status = response.data.attributes;

					if (status.status === 'running') {
						$('#default-node-status').html( $('<span>', { html: 'Running' }) );

						$('#default-node-status').parent().find('.fa')
						 .removeClass('fa-pause rad-txt-danger')
						 .addClass('fa-play rad-txt-success');
					} else {
						$('#default-node-status').html( $('<span>', { html: utils.capitalize(status.status) }) );

						$('#default-node-status').parent().find('.fa')
						 .removeClass('fa-play rad-txt-success')
						 .addClass('fa-pause rad-txt-danger');
					}

					utils.prettyUpdate(Tower.status.peerCount, status.peerCount + demo.add(), $('#default-peers'));
					utils.prettyUpdate(Tower.status.latestBlock, status.latestBlock + demo.add(), $('#default-blocks'));
					utils.prettyUpdate(Tower.status.pendingTxn, status.pendingTxn + demo.add(), $('#default-txn'));

					Tower.status = status;
				});
			};

			def();
			setInterval(def, 5000);
		},

		'console': function() {
			// reset screen, load widgets
			Tower.screenManager.clear();

			Tower.screenManager.show({ widgetId: 'node-info', section: 'console' });
			Tower.screenManager.show({ widgetId: 'node-control', section: 'console' });
			Tower.screenManager.show({ widgetId: 'node-settings', section: 'console' });
		},

		'peers': function() {
			// reset screen, load widgets
			Tower.screenManager.clear();

			Tower.screenManager.show({ widgetId: 'peers-add', section: 'peers' });
			Tower.screenManager.show({ widgetId: 'peers-list', section: 'peers' });
		},

		'api': function() {
			// reset screen, load widgets
			Tower.screenManager.clear();
		},

		'contracts': function() {
			// reset screen, load widgets
			Tower.screenManager.clear();
		},

		'explorer': function() {
			// reset screen, load widgets
			Tower.screenManager.clear();

			Tower.screenManager.show({ widgetId: 'block-detail', section: 'explorer', data: Tower.status.latestBlock });
			Tower.screenManager.show({ widgetId: 'block-list', section: 'explorer' });
			Tower.screenManager.show({ widgetId: 'block-view', section: 'explorer' });
		}
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

	$(document).on('click', function(e) {
		var $item = $('.rad-dropmenu-item');

		if ($item.hasClass('active')) {
			$item.removeClass('active');
		}
	});



	// Menu (burger) handler
	$('.rad-toggle-btn').on('click', function() {
		$('.rad-logo-container').toggleClass('rad-nav-min');
		$('.rad-sidebar').toggleClass('rad-nav-min');
		$('.rad-body-wrapper').toggleClass('rad-nav-min');

		setTimeout(function() {

		}, 200);
	});

	// Theme handler / switcher
	$('li.rad-dropdown > a.rad-menu-item').on('click', function(e) {
		e.preventDefault();
		e.stopPropagation();

		$('.rad-dropmenu-item').removeClass('active');
		$(this).next('.rad-dropmenu-item').toggleClass('active');
	});


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

				$ele.append('<div class="overlay"><div class="overlay-content"><i class="fa fa-refresh fa-2x fa-spin"></i></div></div>');

				setTimeout(function() {
					$ele.find('.overlay').remove();

					(Tower.screenManager.idMap[wid].fetch && Tower.screenManager.idMap[wid].fetch());
				}, 2000);
			}
		}
	});


	// Settings collapse / expand handler
	$('.rad-chk-pin input[type=checkbox]').change(function(e) {
		$('body').toggleClass('flat-theme');
		$('#rad-color-opts').toggleClass('hide');
	});

	// Theme checkbox handlers
	$('.rad-color-swatch input[type=radio]').change(function(e) {
		if ($('.rad-chk-pin input[type=checkbox]').is(':checked')){
			$('body').removeClass().addClass('flat-theme').addClass(this.value);
			$('.rad-color-swatch label').removeClass('rad-option-selected');
			$(this).parent().addClass('rad-option-selected');
			$(window).scrollTop(0);
		} else {
			return false;
		}
	});


	// Navigation menu handler
	$('.rad-sidebar li').click(function() {
		Tower.current = $(this).attr('id');

		$('.rad-sidebar li').removeClass('active');
		$(this).addClass('active');

		Tower.section[Tower.current]();

		$('.rad-page-title').html( $('<span>', { html: $(this).find('.rad-sidebar-item').html() }) );
	});




	// ---------- INIT -----------
	// Update perma-widgets
	Tower.section['default']();

	// Setting 'Console' as first section
	$('.rad-sidebar li').first().click();
});
