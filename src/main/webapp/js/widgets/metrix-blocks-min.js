(function() {
	var extended = {
		name: 'metrix-blocks-min',
		title: 'Metrix: Blocks per Minute',
		size: 'medium',

		hideLink: true,

		topic: '/topic/metrix/blocks-min',


		subscribe: function() {
			utils.subscribe(this.topic, this.onData);
		},

		onData: function(data) {
			if ( (data) && (data.demo) ) {
				widget.chart.push([{ time: (new Date()).getTime(), y: Math.floor(Math.random() * 1000) + 1 }]);
			}

//			var b = {
//				time: (new Date()).getTime(),
//				y: data.datapoint
//			};
//
//			widget.chart.push([ b ]);
		},

		postRender: function() {
			$('#widget-' + widget.shell.id).html( '<div id="' + widget.name + '" class="epoch category10" style="width:100%; height: 210px;"></div>' );

			widget.chart = $('#' + widget.name).epoch({
			    type: 'time.area',
			    data: [ {
			    	label: 'Blocks per MIN',
			    	values: [ { time: (new Date()).getTime(), y: 0 } ] 
			    } ],
			    axes: ['left', 'right', 'bottom']
			});

			// DEMO ANCHOR, REMOVE WHEN REAL DATA EXISTS
			setInterval(function() { widget.onData({demo: true}); }, 60 * 1000);
		}
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();
