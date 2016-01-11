(function() {
	var widget = {
		name: 'block-view',
		title: 'Find Block / Transaction',
		size: 'small',

		initialized: false,
		shell: null,

		template: _.template(
			'  <div class="form-group">' +
			'    <label for="block-id">Identifier [number, hash, tag]</label>' +
			'    <input type="text" class="form-control" id="block-id">' +
			'  </div>'+
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="searchType" name="searchType" value="block" checked="checked"/>' +
			'      Block' +
			'    </label>' +
			'  </div>' +
			'  <div class="radio">' +
			'    <label>' +
			'      <input type="radio" id="searchType" name="searchType" value="txn"/>' +
			'      Transaction' +
			'    </label>' +
			'  </div>' +
			'  <div class="form-group pull-right">' +
			'    <button type="button" class="btn btn-primary">Find</button>' +
			'  </div>'+
			'  <div id="notification">' +
			'  </div>'),

		// this may be overwritten by main runner
		ready: function() {
			this.render();
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
				// .slimScroll({
				// 		height: '240px',
				// 		color: '#c6c6c6'
				// 	});

			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {
			var _this = widget,
			 id = $('#widget-' + _this.shell.id + ' #block-id'),
			 type = $('#widget-' + _this.shell.id + ' #searchType');

			if (id.val() && (type.val() == 'block') ) {
				Tower.screenManager.show({ widgetId: 'block-detail', section: 'explorer', data: id.val(), refetch: true });
			}
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
