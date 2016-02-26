(function() {
	var extended = {
		name: 'contract-current-state',
		title: 'Contract State',
		size: 'small',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
			'<thead style="font-weight: bold;"><tr><td>Method</td><td>Result</td></tr></thead>' +
			'<%= rows %></table>'),
		templateRow: _.template('<tr><td><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),

		setData: function(data) {
			this.data = data;
			this.contractId = data.id;
		},

		fetch: function() {
			var _this = this;

			Contract.get(this.contractId).done(function(contract) {
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(contract.get('name') + ' State');

				contract.readState().done(function(stateArray) {
					if (!stateArray || stateArray.length == 0) {
						// TODO: show error / message?
						return;
					}

					var rows = [];

					_.each(stateArray, function(state) {
						rows.push( _this.templateRow({ key: state.method, value: state.result }) );
					});

					$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				});
			});
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
