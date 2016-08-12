import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'node-control',
		title: 'Node Control',
		size: 'small',

		hideLink: true,
		hideRefresh: true,

		url: {
			nodeControl: 'api/node',
		},

		template: _.template('<ul class="widget-node-control">'+
				'<li><button type="button" class="btn btn-default" id="restart">Restart Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="stop">Stop Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="start">Start Node</button></li>'+
				'<li><button type="button" class="btn btn-default" id="reset">Create New Chain</button></li>'+
			'</ul>'),

		postRender: function() {
			$('#widget-' + this.shell.id).html(widget.template({}));
			$('#widget-' + this.shell.id + ' button').click(widget._handler);
		},

		_handler: function(ev) {
			var _this = $(this),
			 action = $(this).attr('id');

			$(this).attr('disabled', 'disabled');

			$.when(
				utils.load({ url: widget.url.nodeControl + '/' + action })
			).done(function() {
				_this.removeAttr('disabled');
			}).fail(function() {
				// TODO: fill in
			});
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence
	Dashboard.addWidget(widget);
};
