(function() {
	var extended = {
		name: 'node-settings',
		title: 'Node Settings',
		size: 'small',

		hideLink: true,

		url:        'api/node/get',
		update_url: 'api/node/update',

		template: _.template(
			'<div class="form-group">' +
			'	<label for="committingTransactions">Commiting Transactions</label>' +
			'	<select id="committingTransactions" class="form-control">' +
			'		<option value="true">Yes</option>' +
			'		<option value="false">No</option>' +
			'	</select>' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="networkId">Network ID</label>' +
			'	<input type="text" class="form-control" id="networkId">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="identity">Identity</label>' +
			'	<input type="text" class="form-control" id="identity">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="logLevel">Log Level</label>' +
			'	<select id="logLevel" class="form-control">' +
			'		<option value="6">TRACE</option>' +
			'		<option value="5">DEBUG</option>' +
			'		<option value="4">INFO</option>' +
			'		<option value="3">WARN</option>' +
			'		<option value="2">ERROR</option>' +
			'		<option value="1">FATAL</option>' +
			'	</select>' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="extraParams">Extra Start-up Params</label>' +
			'	<input type="text" class="form-control" id="extraParams">' +
			'</div>' +
			'<div class="form-group">' +
			'	<label for="genesisBlock">Genesis Block</label>' +
			'	<textarea class="form-control" rows="5" id="genesisBlock"></textarea>' +
			'</div>'),


		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				$('#widget-' + _this.shell.id + ' #networkId').val( info.data.attributes.config.networkId ? info.data.attributes.config.networkId : '' );
				$('#widget-' + _this.shell.id + ' #identity').val( info.data.attributes.config.identity ? info.data.attributes.config.identity : '' );
				$('#widget-' + _this.shell.id + ' #logLevel').val( info.data.attributes.config.logLevel ? info.data.attributes.config.logLevel : '4' );
				$('#widget-' + _this.shell.id + ' #committingTransactions').val( info.data.attributes.config.committingTransactions ? 'true' : 'false' );
				$('#widget-' + _this.shell.id + ' #extraParams').val( info.data.attributes.config.extraParams ? info.data.attributes.config.extraParams : '' );
				$('#widget-' + _this.shell.id + ' #genesisBlock').val( info.data.attributes.config.genesisBlock ? info.data.attributes.config.genesisBlock : '' );

				_this.postFetch();
			});
		},

		render: function() {
			Dashboard.render.widget(this.name, this.shell.tpl);

			$('#widget-' + this.shell.id)
				.css({ 'height': '240px', 'margin-bottom': '10px', 'overflow': 'auto' })
				.html( this.template({}) );

			$('#widget-' + this.shell.id + ' .form-control').change(this._handler);

			this.fetch();
		},

		_handler: function(ev) {
			var _this = $(this),
			 action = _this.attr('id'),
			 val = _this.val(),
			 data = {};

			data[action] = val;

			$.when(
				utils.load({ url: widget.update_url, data: data })
			).done(function(info) {
				// trigger event update
				$(document).trigger('WidgetInternalEvent', [ widget.name + '|updated|' + action] );
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();
