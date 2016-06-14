var widgetRoot = {
	hideLink: false,
	hideRefresh: false,

	initialized: false,

	ready: function() {
		this.render();
	},

	setData: function(data) {
		this.data = data;
	},

	fetch: function() {
		this.postFetch();
	},

	postFetch: function() { },

	subscribe: function() { },

	init: function(data) {
		if (data) {
			this.setData(data);
		}

		this.shell = Dashboard.TEMPLATES.widget({
			name: this.name,
			title: this.title,
			size: this.size,
			hideLink: this.hideLink,
			hideRefresh: this.hideRefresh,
			customButtons: this.customButtons
		});

		this.initialized = true;
		this.ready();

		this.subscribe();
	},

	render: function() {
		Dashboard.render.widget(this.name, this.shell.tpl);

		this.fetch();

		$('#widget-' + this.shell.id).css({
			'height': '240px',
			'margin-bottom': '10px',
			'overflow-x': 'hidden',
			'width': '100%'
		});

		this.postRender();
		$(document).trigger("WidgetInternalEvent", ["widget|rendered|" + this.name]);
	},

	postRender: function() { },
};
