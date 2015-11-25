(function() {
	var widget = {
		name: 'node-settings',
		title: 'Node Settings',
		size: 'small',

		initialized: false,
		shell: Tower.TEMPLATES.widget(this.title, this.size),

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="nid">Network ID</label>' +
			'    <input type="text" class="form-control" id="nid">' +
			'  </div>' +
			'  <div class="form-group">' +
			'    <label for="log-level">Log Level</label>' +
			'			<select id="log-level" class="form-control">' +
			'			  <option>FINEST</option>' +
			'			  <option>FINER</option>' +
			'			  <option>FINE</option>' +
			'			  <option>CONFIG</option>' +
			'			  <option>INFO</option>' +
			'			  <option>WARNING</option>' +
			'			  <option>SEVERE</option>' +
			'			</select>' +
			'  </div>' +
			// '  <div class="form-group">' +
			// '    <label for="log-loc">Log Path Location</label>' +
			// '    <input type="text" class="form-control" id="log-loc">' +
			// '  </div>' +
			'  <div class="form-group">' +
			'    <label for="commit">Commiting Transactions</label>' +
			'			<select id="commit" class="form-control">' +
			'			  <option>Yes</option>' +
			'			  <option>No</option>' +
			'			</select>' +
			'  </div>' +
			'  <div class="pull-right form-group">' +
			'    <a href="#"> <i class="fa fa-download"></i> Download Genesis Block</a>' +
			'  </div>'),

		// this may be overwritten by main runner
		ready: function() {
			this.render();
		},

		url: 'TBD',

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
				// .slimScroll({
				// 		height: '240px',
				// 		color: '#c6c6c6'
				// 	});

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {

		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
