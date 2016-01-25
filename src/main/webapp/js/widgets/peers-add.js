(function() {
	var widget = {
		name: 'peers-add',
		title: 'Add Peer',
		size: 'small',

		initialized: false,
		shell: null,

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="addy">Peer Node Address</label>' +
			'    <input type="text" class="form-control" id="addy">' +
			'  </div>'+
			'  <div class="form-group pull-right">' +
			'    <button type="button" class="btn btn-primary" id="restart">Add</button>' +
			'  </div>'+
			'  <div id="notification">' +
			'  </div>'),

		// this may be overwritten by main runner
		ready: function() {
			this.render();
		},

		url: '../api/node/add_peer',

		init: function() {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			$('#widget-' + this.shell.id)
				.css({ 'height': '240px', 'margin-bottom': '10px', 'overflow': 'auto' })
				.html( this.template({}) );
				// .slimScroll({
				// 		height: '240px',
				// 		color: '#c6c6c6'
				// 	});

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {
			var _this = widget,
			 input = $('#widget-' + _this.shell.id + ' #addy'),
			 notif = $('#widget-' + _this.shell.id + ' #notification');

			if (!input.val()) {
				return;
			}

			$.when(
				utils.load({ url: widget.url, data: { "args": input.val() } })
			).done(function(r) {
				notif.show();

				if ( (r) && (r.error) ) {
					notif
					 .addClass('text-danger')
					 .removeClass('text-success')
					 .html(r.error.message);

				} else {
					input.val('');

					notif
					 .removeClass('text-danger')
					 .addClass('text-success')
					 .html('Request to add peer is sent');

					setTimeout(function() {
						notif.fadeOut();
					}, 2000);
				}
			});
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
