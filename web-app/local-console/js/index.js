$(function() {
	$(window).on("scroll", function(e) {
		if ($(window).scrollTop() > 50) {
			$("body").addClass("sticky");
		} else {
			$("body").removeClass("sticky");
		}
	});

	$(document).on("click", function(e) {
		e.preventDefault();
		var $item = $(".rad-dropmenu-item");
		if ($item.hasClass("active")) {
			$item.removeClass("active");
		}
	});

	$('.rad-chat-body').slimScroll({
		height: '450px',
		color: "#c6c6c6"
	});

	$('.rad-timeline-body').slimScroll({
		height: '450px',
		color: '#c6c6c6'
	});

	$(".rad-toggle-btn").on('click', function() {
		$(".rad-logo-container").toggleClass("rad-nav-min");
		$(".rad-sidebar").toggleClass("rad-nav-min");
		$(".rad-body-wrapper").toggleClass("rad-nav-min");
		setTimeout(function() {

		}, 200);
	});

	$("li.rad-dropdown > a.rad-menu-item").on('click', function(e) {
		e.preventDefault();
		e.stopPropagation();
		$(".rad-dropmenu-item").removeClass("active");
		$(this).next(".rad-dropmenu-item").toggleClass("active");
	});

	$(".fa-chevron-down").on("click", function() {
		var $ele = $(this).parents('.panel-heading');
		$ele.siblings('.panel-footer').toggleClass("rad-collapse");
		$ele.siblings('.panel-body').toggleClass("rad-collapse", function() {
			setTimeout(function() {

			}, 200);
		});
	});

	$(".fa-close").on("click", function() {
		var $ele = $(this).parents('.panel');
		$ele.addClass('panel-close');
		setTimeout(function() {
			$ele.parent().remove();
		}, 210);
	});

	$(".fa-rotate-right").on("click", function() {
		var $ele = $(this).parents('.panel-heading').siblings('.panel-body');
		$ele.append('<div class="overlay"><div class="overlay-content"><i class="fa fa-refresh fa-2x fa-spin"></i></div></div>');
		setTimeout(function() {
			$ele.find('.overlay').remove();
		}, 2000);
	});

	$("#rad-chat-send").on("click", function() {
		var value = $("#rad-chat-txt").val();
		var $ele = $(".rad-chat-body");
		var img = "https://lh4.googleusercontent.com/-GXmmnYTuWkg/AAAAAAAAAAI/AAAAAAAAAAA/oK6DEDS7grM/w56-h56/photo.jpg";
		if (value) {
			$("#rad-chat-txt").val('');
			$ele.append(getTempl(img, value, 'left'));
			setTimeout(function() {
				img = "http://www.gravatar.com/avatar/9099c2946891970eb4739e6455400913.png";
				$ele.append(getTempl(img, "Cool!!!", 'right'));
				$ele.slimScroll({
					scrollTo: $ele[0].scrollHeight
				});
			}, 2000);

			$ele.slimScroll({
				scrollTo: $ele[0].scrollHeight
			});

		}
	});

	$('.rad-chk-pin input[type=checkbox]').change(function(e) {
		$('body').toggleClass("flat-theme");
		$("#rad-color-opts").toggleClass("hide");
	});

	$('.rad-color-swatch input[type=radio]').change(function(e) {
		if ($('.rad-chk-pin input[type=checkbox]').is(":checked")){
			$('body').removeClass().addClass("flat-theme").addClass(this.value);
			$('.rad-color-swatch label').removeClass("rad-option-selected");
			$(this).parent().addClass("rad-option-selected");
			$(window).scrollTop(0);
		} else {
			return false;
		}
	});

	$(".rad-notification-item").on("click", function(e) {
		e.stopPropagation();
	});

	$(window).resize(function() {
		setTimeout(function() {

		}, 200);
	});

	function getTempl(img, text, position) {
		return '<div class="rad-list-group-item ' + position + '"><span class="rad-list-icon pull-' + position + '"><img class="rad-list-img" src=' + img + ' alt="me" /></span><div class="rad-list-content rad-chat"><span class="lg-text">Me</span><span class="sm-text"><i class="fa fa-clock-o"></i> ' + formatTime(new Date()) + '</span><div class="rad-chat-msg">' + text + '</div>';
	}

	function formatTime(date) {
		var hours = date.getHours();
		var minutes = date.getMinutes();
		var ampm = hours >= 12 ? 'pm' : 'am';
		hours = hours % 12;
		hours = hours ? hours : 12;
		minutes = minutes < 10 ? '0' + minutes : minutes;
		return hours + ':' + minutes + ' ' + ampm;
	}
});
