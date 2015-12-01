(function() {
	var widget = {
		name: 'node-control',
		title: 'Node Control',
		size: 'small',

		initialized: false,
		shell: null,

		template: _.template('<ul class="widget-node-control">'+
				'<li><button type="button" class="btn btn-default" id="restart">Restart Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="stop">Stop Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="start">Start Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="reset">Create New Chain</button></li>'+
			'</ul>'),

		// this may be overwritten by main runner
		ready: function() {
			widget.render();
		},

		url: 'web-app/node/control',

		init: function() {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			$('#widget-' + this.shell.id).html(widget.template({}));
			$('#widget-' + this.shell.id + ' button').click(widget._handler);
		},

		_handler: function(ev) {
			var _this = $(this),
			 action = $(this).attr('id');

			$(this).attr('disabled', 'disabled');

			$.when(
				utils.load({ method: 'GET', url: widget.url + '/' + action })
			).done(function() {
				_this.removeAttr('disabled');
			}).fail(function() {
				// TODO: fill in
			});
		}
	};


	// register presence
	Tower.screenManager.addWidget(widget);
})();
