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
		topic: '/topic/block',

		setData: function(data) {
			this.lastBlockNum = data;
		},

		init: function(data) {
			this.setData(data);

			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();

			// subscribe to get new blocks
			utils.subscribe(this.topic, this.onNewBlock);
		},

		onNewBlock: function(data) {
			var b = {
				num: data.number,
				age: data.timestamp,
				txnCount: data.transactions.length,
			};

			$('#widget-' + widget.shell.id + ' > table > tbody').prepend( widget.templateRow({ block: b }) );
		},

		BLOCKS_TO_SHOW: 100,
		fetch: function() {
			try {
				if (this.lastBlockNum != Tower.status.latestBlock) {
					this.lastBlockNum = Tower.status.latestBlock;
				}
			} catch (e) {}

			var displayLimit, promizes = [], rows = [], _this = this;

			if ( (this.lastBlockNum < this.BLOCKS_TO_SHOW) && (this.lastBlockNum >= 0) ) {
				displayLimit = this.lastBlockNum + 1;
			} else {
				displayLimit = this.BLOCKS_TO_SHOW;
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
			});
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			this.fetch();

			$('#widget-' + this.shell.id).css({ 'height': '240px', 'margin-bottom': '10px', 'overflow-x': 'hidden', 'width': '100%' });

			$('#widget-' + this.shell.id).on('click', 'a', this.showBlock);
		},

		showBlock: function(e) {
			e.preventDefault();

			Tower.screenManager.show({ widgetId: 'block-detail', section: 'explorer', data: $(this).text(), refetch: true });
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
