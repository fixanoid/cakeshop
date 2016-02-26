var widgetRoot = {
	initialized: false,

	ready: function() {
		this.render();
	},

	setData: function(data) {
		this.data = data;
	},

	fetch: function() { },

	init: function(data) {
		if (data) {
			this.setData(data);
		}

		this.shell = Tower.TEMPLATES.widget(this.title, this.size);

		this.initialized = true;
		this.ready();
	},

	render: function() {
		Tower.screenManager.grounds.append(this.shell.tpl);

		this.fetch();

		$('#widget-' + this.shell.id).css({
			'height': '240px',
			'margin-bottom': '10px',
			'overflow-x': 'hidden',
			'width': '100%'
		});
	}
};
