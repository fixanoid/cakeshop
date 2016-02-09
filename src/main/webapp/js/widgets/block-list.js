(function() {
	var widget = {
		name: 'block-list',
		title: 'Block List',
		size: 'small',

		initialized: false,

		lastBlockNum: null,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td style="width:60px;">Block</td><td>Age</td><td style="width:45px;">TXNs</td></tr></thead>' +
		 '<tbody><%= rows %></tbody></table>'),

		templateRow: _.template('<tr><td>#<a href="#"><%= block.num %></a></td><td><%= moment.unix(block.age).fromNow() %></td><td <% if (block.txnCount == 0) { %>style="opacity: 0.2;"<% } %>><%= block.txnCount %></td></tr>'),

		ready: function() {
			this.render();
		},

		url: 'api/block/get',

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
			try {
				if (this.lastBlockNum != Tower.status.latestBlock) {
					this.lastBlockNum = Tower.status.latestBlock;
				}
			} catch (e) {}

			var displayLimit, promizes = [], rows = [], _this = this;

			if ( (this.lastBlockNum < 10) && (this.lastBlockNum >= 0) ) {
				displayLimit = this.lastBlockNum;
			} else {
				displayLimit = 100;
			}

			_.times(displayLimit,
				function(n) {
					promizes.push(
						utils.load({
							url: _this.url,
							data: { number: _this.lastBlockNum - n },
							complete: function(res) {
								rows.push( {
									num: res.responseJSON.data.attributes.number,
									age: res.responseJSON.data.attributes.timestamp,
									txnCount: res.responseJSON.data.attributes.transactions.length,
								} );
							}
						})
					);
			 	});

			$.when.apply($, promizes).done(function() {
				var rowsOut = [];
				rows = _.sortBy(rows, function(o) { return o.num; }).reverse();

				_.each(rows, function(b, index) {
					rowsOut.push( _this.templateRow({ block: b }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rowsOut.join('') }) );

				$('#widget-' + _this.shell.id + ' a').click(_this.showBlock);
			});
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			this.fetch();

			$('#widget-' + this.shell.id).css({ 'height': '240px', 'margin-bottom': '10px', 'overflow-x': 'hidden', 'width': '100%' });
		},

		showBlock: function(e) {
			e.preventDefault();

			Tower.screenManager.show({ widgetId: 'block-detail', section: 'explorer', data: $(this).text(), refetch: true });
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
