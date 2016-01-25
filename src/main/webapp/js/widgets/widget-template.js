(function() {
	var widget = {
		name: 'widget-id',
		title: 'Widget Name That Appears on it',
		size: 'small', // 'small', 'medium', 'large', 'third'

		initialized: false,

		template: _.template('<ul class="widget-node-control">'+ // internal template
				'<li>List item 1</li>'+
				'<li>List item 2</li>'+
				'<li>List item 2</li>'+
				'<li>List item 2</li>'+
			'</ul>'),

		// this may be overwritten by main runner
		ready: function() {	// executed when the widget is preped and ready
			this.render();
		},

		url: 'TBD',

		setData: function(data) {
			// set up my dataset and reload if needed.
			// this.fetch();
		},

		init: function(data) { // executed by screen manager when registered
			this.shell = Tower.TEMPLATES.widget(this.title, this.size);	// external template shell

			this.initialized = true;
			this.ready();

			// pass the data to the setData
			// this.setData(data);
		},

		render: function() { // executed when placing on screen
			Tower.screenManager.grounds.append(this.shell.tpl);

			$('#widget-' + this.shell.id).html(this.template({}));
			$('#widget-' + this.shell.id + ' button').click(this._handler);
		},

		_handler: function(ev) {

		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
