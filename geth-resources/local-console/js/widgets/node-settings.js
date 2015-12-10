(function() {
	var widget = {
		name: 'node-settings',
		title: 'Node Settings',
		size: 'small',

		initialized: false,
		shell: null,

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="networkid">Network ID</label>' +
			'    <input type="text" class="form-control" id="networkid">' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="identity">Identity</label>' +
			'    <input type="text" class="form-control" id="identity">' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="verbosity">Log Level</label>' +
			'			<select id="verbosity" class="form-control">' +
			'			  <option value="1">FINEST</option>' +
			'			  <option value="2">FINER</option>' +
			'			  <option value="3">FINE</option>' +
			'			  <option value="4">INFO</option>' +
			'			  <option value="5">WARNING</option>' +
			'			  <option value="6">SEVERE</option>' +
			'			</select>' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="mining">Commiting Transactions</label>' +
			'			<select id="mining" class="form-control">' +
			'			  <option value="true">Yes</option>' +
			'			  <option value="false">No</option>' +
			'			</select>' +
			'  </div>' +
			'  <div class="pull-right form-group">' +
			'    <a href="#"> <i class="fa fa-download"></i> Download Genesis Block</a>' +
			'  </div>'),

		// this may be overwritten by main runner
		ready: function() {
			this.render();
		},

		url: {
			info: '../node/settings',
			update: '../node/settings/update'
		},

		fetch: function() {
			var _this = this;

			$.when(
				utils.load({ url: this.url.info })
			).done(function(info) {
				$('#widget-' + _this.shell.id + ' #networkid').val( info.data.attributes.networkid ? info.data.attributes.networkid : '' );
				$('#widget-' + _this.shell.id + ' #identity').val( info.data.attributes.identity ? info.data.attributes.identity : '' );
				$('#widget-' + _this.shell.id + ' #verbosity').val( info.data.attributes.verbosity ? info.data.attributes.verbosity : '4' );
				$('#widget-' + _this.shell.id + ' #mining').val( info.data.attributes.mining ? 'true' : 'false' );
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

			utils.load({ url: widget.url.update, data: data });
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
