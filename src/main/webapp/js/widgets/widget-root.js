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
			title: this.title,
			size: this.size,
			hideLink: this.hideLink,
			hideRefresh: this.hideRefresh
		});

		this.initialized = true;
		this.ready();

		this.subscribe();
	},

	render: function() {
		Dashboard.grounds.append(this.shell.tpl);

		this.fetch();

		$('#widget-' + this.shell.id).css({
			'height': '240px',
			'margin-bottom': '10px',
			'overflow-x': 'hidden',
			'width': '100%'
		});

		this.postRender();
	},

	postRender: function() { },
};
