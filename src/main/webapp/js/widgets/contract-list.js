(function() {
	var widget = {
		name: 'contract-list',
		title: 'Deployed Contract List',
		size: 'medium',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td>ID</td><td>Contract</td><td>Deploy Date</td></tr></thead>' +
		 '<tbody><%= rows %></tbody></table>'),

		templateRow: _.template('<tr><td><a href="#" data-id="<%= contract.id %>"><%= utils.truncAddress(contract.id) %></a></td><td><%= contract.name %></td><td><%= contract.date %></td></tr>'),

		ready: function() {
			this.render();
		},

		init: function(data) {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},


		fetch: function() {
			var _this = this,
			 rowsOut = [];

			Contract.list(function(contracts) {
				_.each(contracts, function(c) {
					var co = {
						name: c.get('name'),
						date: moment.unix(c.get('createdDate')).format('YYYY-MM-DD hh:mm A'),
						id: c.id
					};

					rowsOut.push( _this.templateRow({ contract: co }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rowsOut.join('') }) );
	        });
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			this.fetch();

			$('#widget-' + this.shell.id).css({ 'height': '240px', 'margin-bottom': '10px', 'overflow-x': 'hidden', 'width': '100%' });

			$('#widget-' + this.shell.id).on('click', 'a', this.showContractDetail);
		},

		showContractDetail: function(e) {
			e.preventDefault();

			Tower.screenManager.show({ widgetId: 'contract-detail', section: 'contracts', data: $(this).data('id'), refetch: true });
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
