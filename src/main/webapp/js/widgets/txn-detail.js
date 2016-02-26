(function() {
	var extended = {
		name: 'txn-detail',
		size: 'medium',

		url: 'api/transaction/get',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped"><%= rows %></table>'),
		templateRow: _.template('<tr><td style="width: 160px;"><%= key %></td><td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= value %></td></tr>'),
		templateBlockRow: _.template('<tr><td style="width: 160px;"><%= key %></td><td style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><a href="#"><%= value %></a></td></tr>'),


		setData: function(data) {
			this.data = data;
			this.txnAddy = data;

			this.title = 'Transaction #' + this.txnAddy;
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url, data: { id: _this.txnAddy } })
			).fail(function(res) {
				$('#widget-' + _this.shell.id).html( '<h3 style="text-align: center;margin-top: 70px;">Unable to load transaction</h3>' );

				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html('Transaction Detail');
			}).done(function(res) {
				var mainTable = {
					'id': 0,
					'status': 1,
					'blockId': 2,
					'blockNumber': 3,
					'contractAddress': 4,
					'gasUsed': 5,
					'cumulativeGasUsed': 6
				},
				mainRows = [],
				secRows = [],
				keys = _.sortBy(_.keys(res.data.attributes), function(key) {
				   // custom reorder of the returned keys

					if (key in mainTable) {
						return '' + mainTable[key];
					}

					return ('zzz' + key);
			  	});


				keys = utils.idAlwaysFirst(keys);

				_.each(keys, function(val, key) {
					if (!res.data.attributes[val]) {
						return;
					}

					var template;

					if (val == 'blockNumber') {
						template = _this.templateBlockRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] });
					} else {
						var template = _this.templateRow({ key: utils.camelToRegularForm(val), value: res.data.attributes[val] });
					}


					if (val in mainTable) {
						mainRows.push( template );
					} else {
						secRows.push( template );
					}
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: mainRows.join('') }) +
					'<h3 style="margin-top: 30px;margin-left: 8px;">Transaction inputs &amp; parameters</h3>' +
					_this.template({ rows: secRows.join('') }) );


				$('#widget-shell-' + _this.shell.id + ' .panel-title span').html(_this.title);

				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
			});
		},

		postRender: function() {
			$('#widget-' + this.shell.id).on('click', 'a', function(e) {
				e.preventDefault();

				Tower.screenManager.show({ widgetId: 'block-detail', section: 'explorer', data: $(this).text(), refetch: true });
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
