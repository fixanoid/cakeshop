(function() {
	var widget = {
		name: 'node-info',
		title: 'Node Chain Client Info',
		size: 'medium',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 75px;"><%= key %></td><td style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),

		ready: function() {
			this.render();
		},

		url: '../api/node/get',

		init: function() {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				var rows = [];

				_.each(info.result, function(val, key) {
					rows.push( _this.templateRow({ key: key, value: val }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
			});
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			this.fetch();

			$('#widget-' + this.shell.id).css({ 'height': '240px', 'margin-bottom': '10px', 'overflow-x': 'hidden', 'width': '100%' });

		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
