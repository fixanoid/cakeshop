(function() {
	var extended = {
		name: 'contract-paper-tape',
		title: 'Contract Paper Tape',
		size: 'small',

		url: 'api/contract/transactions/list',

		template: _.template('<table style="width: 100%; table-layout: fixed; background-color: #fcf8e3;" class="table"><%= rows %></table>'),
		templateRow: _.template('<tr style="border-bottom: 2px dotted #faebcc;"><td><%= text %></td></tr>'),

		setData: function(data) {
			this.data = data;

			this.contractName = data.name;
			this.contractId = data.id;
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: _this.url, data: { id: _this.contractId } })
			).fail(function(err) {
				// TODO: Error will robinson!
			}).done(function(txns) {
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.contractName + ' Paper Tape');

				var rows = [];

				_.each(txns.data, function(val, key) {
					var txn = val.attributes,
					 text = '';

					if (!txn.decodedInput) {
						// Contract creation
						text = _this.contractName + ' created at <a href="#">' + utils.truncAddress(txn.contractAddress) + '</a> on block <a href="#">' + txn.blockNumber + '</a>';
					} else {
						text = 'Called \'<span style="font-weight: bold; color: #375067;">' + txn.decodedInput.method +'</span>(<span style="font-weight: bold; color: #375067;">' + txn.decodedInput.args.join('</span>, <span style="font-weight: bold; color: #375067;">') + '</span>)\' that was committed on block #<a href="#">' + txn.blockNumber + '</a>';
					}

					rows.push( _this.templateRow({ text: text }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
