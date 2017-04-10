import utils from '../utils';

module.exports = function() {
	var extended = {
		name: 'accounts',
		title: 'Accounts',
		size: 'medium',

		url: 'api/wallet/list',
		url_create: 'api/wallet/create',
		url_lock: 'api/wallet/lock',
		url_unlock: 'api/wallet/unlock',

		hideLink: true,
		customButtons: '<li><i class="add-account fa fa-plus-circle"></i></li>',

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
		 		'<thead style="font-weight: bold;">' +
					'<tr>' +
						'<td>Account</td>' +
						'<td style="width: 200px;">Balance</td>' +
						'<td class="unlocked-col"> <i class="fa fa-lock" aria-hidden="true"></i> </td>' +
					'</tr>' +
				'</thead>' +
		 		'<tbody> <%= rows %> </tbody>' +
		 	'</table>'),

		templateRow: _.template('<tr>' +
				'<td class="value" contentEditable="false" style="text-overflow: ellipsis; white-space: nowrap; overflow: hidden;"><%= o.get("address") %></td>' +
				'<td style="width: 200px;"><%= o.balance %></td>' +
				'<td class="unlocked-col <% if( !o.get("unlocked") ){ %> locked  <% } else { %> unlocked <% } %> ">' +
					'<i class="fa fa-lock locked-icon" aria-hidden="true" data-account="<%= o.get("address") %>"></i>' +
					'<i class="fa fa-unlock unlocked-icon" aria-hidden="true" data-account="<%= o.get("address") %>"></i>' +
				'</td>' +
			'</tr>'),

		fetch: function() {
			var _this = this;
			Account.list().then(function(accounts) {
				var rows = [];
				accounts.forEach(function(acct) {
					var b = parseInt(acct.get('balance'), 10) / 1000000000000000000;

					if (b > 1000000000) {
						b = 'Unlimited';
					} else {
						b = b.toFixed(2);
					}

					acct.balance = b + ' ETH';
					rows.push( _this.templateRow({ o: acct }) );
				});

				$('#widget-' + _this.shell.id).html( _this.template({ rows: rows.join('') }) );
				utils.makeAreaEditable('#widget-' + _this.shell.id + ' .value');
			});
		},

		postRender: function() {
			var _this = this;
			$('#widget-shell-' + _this.shell.id + ' i.add-account').click(function(e) {
				$.when(
					utils.load({ url: _this.url_create })
				).done(function() {
					$(e.target).parent().parent().find('.fa-rotate-right').click();
				});

			});

			console.log(_this)
			$('#widget-' + _this.shell.id).on('click', '.locked-icon', function(e) {
				console.log('clicked', e);
				var account = $(e.target).data("account");
				$.when(
					utils.load({
						url: _this.url_unlock,
						data: {
							"account": account
						}
					})
				).done(function () {
					console.log('unlocked now')
				}).fail( function() {
					console.log('error')
				})
			});

			$('#widget-' + _this.shell.id).on("mouseenter", '.locked-icon', function(e) {
				// hover starts code here
				console.log('hovered over lock')
			});

			$('#widget-' + _this.shell.id).on("mouseleave", '.locked-icon', function(e) {
				// hover ends code here
				console.log('moved hover over lock')
			});
		}
	};

	var widget = _.extend({}, widgetRoot, extended);

	// register presence with screen manager
	Dashboard.addWidget(widget);
};
