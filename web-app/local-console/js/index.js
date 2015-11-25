var Tower = {
	current: null,
	status: null,

	screenManager: screenManager
};


Tower.section = {
	'default': function() {
		// these are perma-widgets
		var def = function() {
			$.when(
					utils.load({ url: 'json/status.json', method: 'GET' })
			).done(function(status) {
				// TODO: replace before flight

				Tower.status = status;

				if (status.status === 'running') {
					$('#default-node-status').html( $('<span>', { html: 'Running' }) );

					// TODO: change the icon according to the condition?
				}

				utils.prettyUpdate(Tower.status.peers, status.peers + Math.ceil(Math.random() * 10), $('#default-peers'));
				utils.prettyUpdate(Tower.status.latestBlock, status.latestBlock + Math.ceil(Math.random() * 10), $('#default-blocks'));
				utils.prettyUpdate(Tower.status.queuedTxn, status.queuedTxn + Math.ceil(Math.random() * 10), $('#default-txn'));
			});
		};

		def();
		setInterval(def, 5000);
	},
	'console': function() {
		// reset screen, load widgets
		Tower.screenManager.show('node-control');
		Tower.screenManager.show('node-settings');
	}
}

$(function() {
	$(window).on('scroll', function(e) {
		if ($(window).scrollTop() > 50) {
			$('body').addClass('sticky');
		} else {
			$('body').removeClass('sticky');
		}
	});

	$(document).on('click', function(e) {
		e.preventDefault();

		var $item = $('.rad-dropmenu-item');
		if ($item.hasClass('active')) {
			$item.removeClass('active');
		}
	});

	// $('.rad-chat-body').slimScroll({
	// 	height: '450px',
	// 	color: '#c6c6c6'
	// });


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

	// Widget collapse / expand handler
	$('.fa-chevron-down').on('click', function() {
		var $ele = $(this).parents('.panel-heading');
		$ele.siblings('.panel-footer').toggleClass('rad-collapse');
		$ele.siblings('.panel-body').toggleClass('rad-collapse', function() {
			setTimeout(function() {

			}, 200);
		});
	});

	// Widget close handler
	$('.fa-close').on('click', function() {
		var $ele = $(this).parents('.panel');
		$ele.addClass('panel-close');

		setTimeout(function() {
			$ele.parent().remove();
		}, 210);
	});

	// Widget refresh handler
	$('.fa-rotate-right').on('click', function() {
		var $ele = $(this).parents('.panel-heading').siblings('.panel-body');
		$ele.append('<div class="overlay"><div class="overlay-content"><i class="fa fa-refresh fa-2x fa-spin"></i></div></div>');
		setTimeout(function() {
			$ele.find('.overlay').remove();
		}, 2000);
	});

	// Widget collapse / expand handler
	$('.rad-chk-pin input[type=checkbox]').change(function(e) {
		$('body').toggleClass('flat-theme');
		$('#rad-color-opts').toggleClass('hide');
	});

	// Checkbox handlers
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

	$('.rad-notification-item').on('click', function(e) {
		e.stopPropagation();
	});


	// Navigation menu handler
	$('.rad-sidebar li').click(function() {
		Tower.current = $(this).attr('id');

		$('.rad-sidebar li').removeClass('active');
		$(this).addClass('active');

		Tower.section[Tower.current]();
	});

	// Resize handler
	$(window).resize(function() {
		setTimeout(function() {

		}, 200);
	});



	// ---------- INIT -----------
	// Update perma-widgets
	Tower.section['default']();

	// Setting 'Console' as first section
	$('.rad-sidebar li#console').click();

});
