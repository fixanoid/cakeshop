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
				//'<li><button type="button" class="btn btn-default" id="miner">Toggle Mining</button></li>'+
			'</ul>'),

		// this may be overwritten by main runner
		ready: function() {
			widget.render();
		},

		url: {
			nodeControl: 'api/node',
			minerControl: 'api/node/miner'
		},

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

			if (action === 'miner') {
				action = (Tower.status.mining ? 'stop' : 'start');

				$.when(
					utils.load({ url: widget.url.minerControl + '/' + action})
				).done(function() {
					_this.removeAttr('disabled');
				}).fail(function() {
					// TODO: fill in
				});

			} else {
				$.when(
					utils.load({ url: widget.url.nodeControl + '/' + action })
				).done(function() {
					_this.removeAttr('disabled');
				}).fail(function() {
					// TODO: fill in
				});
			}
		}
	};


	// register presence
	Tower.screenManager.addWidget(widget);
})();
