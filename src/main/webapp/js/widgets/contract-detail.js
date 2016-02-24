(function() {
	var widget = {
		name: 'contract-detail',
		title: 'Contract Detail',
		size: 'medium',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),
		templateTxnRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td style="text-overflow: ellipsis; overflow: hidden;"><%= value %></td></tr>'),

		ready: function() {
			this.render();
		},

		setData: function(data) {
			this.contractId = data;
		},

		init: function(data) {
			this.setData(data);

			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			var _this = this;

			Contract.get(this.contractId).done(function(contract) {
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Contract ' + contract.get('name'));

				var rows = [],
				 keys = _.keys(contract.attributes);

				 rows.push( _this.templateRow({ key: utils.camelToRegularForm('id'), value: contract.id }) );

				_.each(keys, function(val, key) {
					if ( (!contract.attributes[val]) || (contract.attributes[val].length == 0) ) {
						return;
					}

					rows.push( _this.templateRow({ key: utils.camelToRegularForm(val), value: contract.attributes[val] }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
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
