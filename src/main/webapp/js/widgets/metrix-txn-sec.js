(function() {
	var extended = {
		name: 'metrix-txn-sec',
		title: 'Transactions/sec',
		size: 'large',

		hideLink: true,

		topic: '/topic/metrics/txnPerSec',


		subscribe: function() {
			utils.subscribe(this.topic, this.onData);
		},

		fetch: function() {
			$('#widget-' + widget.shell.id).html( '<div id="' + widget.name + '" class="epoch category10" style="width:100%; height: 210px;"></div>' );

			widget.chart = $('#' + widget.name).epoch({
			    type: 'time.area',
			    data: [ {
			    	label: 'TXN per SEC',
			    	values: [ { time: (new Date()).getTime() / 1000, y: 0 } ]
			    } ],
			    axes: ['left', 'right', 'bottom']
			});
		},

		onData: function(data) {
			if (!data || !data.result) {
				return;
			}
			var b = {
				time: data.result.timestamp,
				y: data.result.value
			};
			widget.chart.push([ b ]);
		},
	};


	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
})();