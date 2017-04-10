import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'fund-accounts',
		title: 'Transfer Funds',
		size: 'small',

		url: 'api/wallet/fund',

		hideLink: true,

		template: _.template('  <div class="form-group">' +
		'    <label for="transfer-from">Transfer From Account</label>' +
		'    <input type="text" class="form-control" id="transfer-from">' +
		'    <label for="transfer-to">Transfer To Account</label>' +
		'    <input type="text" class="form-control" id="transfer-to">' +
		'  </div>'+
		'  <div class="form-group pull-right">' +
		'    <button type="button" class="btn btn-primary" id="transfer-btn">Transfer</button>' +
		'  </div>'+
		'  <div id="notification">' +
		'  </div>'),

		fetch: function() {

		},

		postRender: function() {
			$('#widget-' + this.shell.id).html( this.template({}) );
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
