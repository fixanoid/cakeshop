(function() {
	var widget = {
		name: 'node-settings',
		title: 'Node Settings',
		size: 'small',

		initialized: false,
		shell: null,

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="committing_transactions">Commiting Transactions</label>' +
			'	 <select id="committing_transactions" class="form-control">' +
			'	  <option value="true">Yes</option>' +
			'	  <option value="false">No</option>' +
			'	</select>' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="network_id">Network ID</label>' +
			'    <input type="text" class="form-control" id="network_id">' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="identity">Identity</label>' +
			'    <input type="text" class="form-control" id="identity">' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="log_level">Log Level</label>' +
			'	<select id="log_level" class="form-control">' +
			'	  <option value="6">TRACE</option>' +
			'	  <option value="5">DEBUG</option>' +
			'	  <option value="4">INFO</option>' +
			'	  <option value="3">WARN</option>' +
			'	  <option value="2">ERROR</option>' +
			'	  <option value="1">FATAL</option>' +
			'	</select>' +
			'  </div>'),
			// '  <div class="pull-right form-group">' +
			// '    <a href="#"> <i class="fa fa-download"></i> Download Genesis Block</a>' +
			// '  </div>'),

		// this may be overwritten by main runner
		ready: function() {
			this.render();
		},

		url: '../api/node/update',

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url })
			).done(function(info) {
				$('#widget-' + _this.shell.id + ' #network_id').val( info.data.attributes.network_id ? info.data.attributes.network_id : '' );
				$('#widget-' + _this.shell.id + ' #identity').val( info.data.attributes.identity ? info.data.attributes.identity : '' );
				$('#widget-' + _this.shell.id + ' #log_level').val( info.data.attributes.log_level ? info.data.attributes.log_level : '4' );
				$('#widget-' + _this.shell.id + ' #committing_transactions').val( info.data.attributes.committing_transactions ? 'true' : 'false' );
			});
		},

		init: function() {
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			$('#widget-' + this.shell.id)
				.css({ 'height': '240px', 'margin-bottom': '10px', 'overflow': 'auto' })
				.html( this.template({}) );

			$('#widget-' + this.shell.id + ' input').change(this._handler);
			$('#widget-' + this.shell.id + ' select').change(this._handler);

			this.fetch();
		},

		_handler: function(ev) {
			var _this = $(this),
			 action = _this.attr('id'),
			 val = _this.val()
			 data = {};

			data[action] = val;

			$.when(
				utils.load({ url: widget.url, data: data })
			).done(function(info) {
				// trigger event update
				$(document).trigger('WidgetInternalEvent', [ widget.name + '|updated|' + action] );
			});
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
