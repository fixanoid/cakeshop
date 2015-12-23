(function() {
	var widget = {
		name: 'block-list',
		title: 'Block List',
		size: 'small',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),

		ready: function() {
			this.render();
		},

		url: '../api/node/peers',

		init: function() {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			// var _this = this;
			//
			// $.when(
			// 	utils.load({ url: this.url })
			// ).done(function(info) {
			// 	var rows = [];
			//
			// 	_.each(info.result, function(peer) {
			// 		rows.push( _this.templateRow({ o: peer }) );
			// 	});
			//
			// 	$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
			// });
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
