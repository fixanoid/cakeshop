(function() {
	var widget = {
		name: 'peers-list',
		title: 'Peer List',
		size: 'medium',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="padding-left: 0px; padding-right: 0px; padding-top: 0px; padding-bottom: 10px;">' +
			'<table style="width: 100%; table-layout: fixed; background-color: inherit; margin-bottom: initial;" class="table">' +
			'	<tr><td style="font-weight: bold; width: 35px;">Peer</td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;" colspan="2"><%= o.nodeUrl %></td></tr>' +
			'	<tr><td style="font-weight: bold;">Info</td><td><%= o.nodeName %></td><td><%= o.nodeIP %></td></tr>' +
			//'	<tr><td style="font-weight: bold;">IPs</td><td><%= o.nodeIP %></td><td><%= o.status %></td></tr>' +
			'</table></td></tr>'),

		ready: function() {
			this.render();
		},

		url: 'api/node/peers',

		init: function() {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				var rows = [];

				if (info.data.attributes.length > 0) {
					_.each(info.data.attributes, function(peer) {
						rows.push( _this.templateRow({ o: peer }) );
					});

					$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );

					utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
				} else {
					// no peers
				}
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