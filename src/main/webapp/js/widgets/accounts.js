(function() {
	var extended = {
		name: 'accounts',
		title: 'Accounts',
		size: 'medium',

		url: 'api/wallet/list',

		hideLink: true,


		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 '<thead style="font-weight: bold;"><tr><td>Account</td><td style="width: 200px;">Balance</td></tr></thead>' +
		 '<tbody><%= rows %></tbody></table>'),

		templateRow: _.template('<tr><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o.address %></td><td style="width: 200px;"><%= o.balance %></td></tr>'),

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(res) {
				var rows = [],
				 list = res.data.attributes;

				_.each(list, function(val, key) {
					val.balance = parseInt(val.balance, 10) / 1000000000000000000;

					if (val.balance > 1000000000) {
						val.balance = 'Unlimited';
					} else {
						val.balance = val.balance.toFixed(2);
					}

					val.balance = val.balance + ' ETH';

					rows.push( _this.templateRow({ o: val }) );
				});


				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();
