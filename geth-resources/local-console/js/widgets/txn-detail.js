(function() {
	var widget = {
		name: 'txn-detail',
		size: 'medium',

		initialized: false,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 160px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),

		ready: function() {
			this.render();
		},

		url: '../api/transaction/get',

		setData: function(data) {
			this.txnAddy = data;

			this.title = 'Transaction #' + this.txnAddy;
		},

		init: function(data) {
			this.setData(data);

			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url, data: { id: _this.txnAddy } })
			).done(function(res) {
				var rows = [],
				 keys = _.sortBy(_.keys(res.data.attributes));;

				_.each(keys, function(val, key) {
					if (!res.data.attributes[val]) {
						return;
					}

					rows.push( _this.templateRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);

				$('#widget-' + _this.shell.id + ' .value').click(function(e) {
					var isEditable = !!$(this).prop('contentEditable');
					$(this).prop('contentEditable', isEditable);

					$(this).focus();
				});
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
