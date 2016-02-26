(function() {
	var extended = {
		name: 'node-info',
		title: 'Node Client Info',
		size: 'medium',

		url: 'api/node/get',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 100px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),


		subscribe: function() {
			// adding listener to reload the widget if identity is updated
			$(document).on('WidgetInternalEvent', function(ev, action) {
				if (action === 'node-settings|updated|identity') {
					widget.fetch();
				}
			});
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				var rows = [],
				 keys = _.sortBy(_.keys(info.data.attributes), function(key) {
					 // custom reorder of the returned keys
				   var customOrder = {
					   'nodeUrl': 1,
					   'nodeName': 2,
					   'nodeIP': 3,
					   'nodePort': 4,
					   'nodeRpcPort': 5,
					   'peerCount': 6,
					   'pendingTxn': 7,
					   'status': 8,
					   'mining': 9,
					   'latestBlock': 'a'
				   };

				   if (key in customOrder) {
					   return '' + customOrder[key];
				   }

				   return ('zzz' + key);
				});

				keys = utils.idAlwaysFirst(keys);

				// objects not shown in this widget
				keys = _.without(keys, 'config', 'peers');


				_.each(keys, function(val, key) {
					rows.push( _this.templateRow({ key: utils.camelToRegularForm(val), value: info.data.attributes[val] }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
