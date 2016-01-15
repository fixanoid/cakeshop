(function() {
	var widget = {
		name: 'block-list',
		title: 'Block List',
		size: 'small',

		initialized: false,

		lastBlockNum: null,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td>Block</td><td>Age</td><td>TXNs</td></tr></thead>' +
		 '<tbody><%= rows %><tbody></table>'),

		templateRow: _.template('<tr><td>#<a href="#"><%= block.num %></a></td><td><%= block.age %><td><%= block.txnCount %></td></td></tr>'),

		ready: function() {
			this.render();
		},

		url: '../api/node/peers',

		setData: function(data) {
			this.lastBlockNum = data;
		},

		init: function(data) {
			this.setData(data);

			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			var rows = [], _this = this;

			if (this.lastBlockNum > 10) {
				_.times(10,
					function(n) {
						var b = {
							num: _this.lastBlockNum - n,
							age: '--',
							txnCount: 0
						};

						rows.push( _this.templateRow({ block: b }) );
				 	});
			}
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

			$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
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
