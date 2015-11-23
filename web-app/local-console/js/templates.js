Tower.TEMPLATES = {};

Tower.TEMPLATES._widget = function(opts) {
	return '<div class="col-lg-' + opts.largeColumn + ' col-md-' + opts.mediumColumn + ' col-xs-' + opts.smallColumn + '">\n'+
			'	<div class="panel panel-default">\n'+
			'		<div class="panel-heading">\n'+
			'			<h3 class="panel-title">' + opts.title + '\n'+
			'				<ul class="rad-panel-action">\n'+
			'					<li><i class="fa fa-chevron-down"></i></li>\n'+
			'					<li><i class="fa fa-rotate-right"></i></li>\n'+
			'					<li><i class="fa fa-close"></i></li>\n'+
			'				</ul>\n'+
			'			</h3>\n'+
			'		</div>\n'+
			'		<div class="panel-body" id="widget-' + opts.id + '">\n'+
			'		</div>\n'+
			'	</div>\n'+
			'</div>'
}


Tower.TEMPLATES.widget = function(title, size) {
	var opts = {
		title: title,
		id: Math.ceil(Math.random() * 100000000),

		largeColumn: 3,
		mediumColumn: 4,
		smallColumn: 12
	};

	switch (size) {
		case 'medium':
			opts.largeColumn = 6;
			opts.mediumColumn = 12;
			opts.smallColumn = 12;

			break;

		case 'large':
			opts.largeColumn = 12;
			opts.mediumColumn = 6;
			opts.smallColumn = 12;

			break;

		case 'third':
			opts.largeColumn = 4;
			opts.mediumColumn = 6;
			opts.smallColumn = 12;

			break;
	}

	return {
		id: opts.id,
		tpl: Tower.TEMPLATES._widget(opts)
	};
}
