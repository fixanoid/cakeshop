(function() {
	var widget = {
		name: 'peers-neighborhood',
		title: 'Peer Neighborhood',
		size: 'small',
		knownPeers: [],
		ip: null,

		initialized: false,
		shell: null,

		template: _.template('<table style="width: 100%; table-layout: fixed;" class="table table-striped">' +
				 '<thead style="font-weight: bold;"><tr><td>Neighbor</td><td style="width: 50px;">Blocks</td><td style="width: 50px;">Add</td></tr></thead>' +
				 '<tbody></tbody></table>'),

		templateRow: _.template('<tr><td><%= neighbor.nodeIP %></td><td><%= neighbor.latestBlock %></td><td><a class="btn btn-primary btn-sm" href="#" data-enode="<%= neighbor.nodeUrl %>" id="neighbor-add"><i class="fa fa-plus"></i></a></td></tr>'),

		// this may be overwritten by main runner
		ready: function() {
			this.render();
		},

		url: 'api/node/add_peer',

		setData: function(data) {
			this.ip = data;
		},

		init: function(data) {
			this.setData(data);

			this.shell = Tower.TEMPLATES.widget(this.title, this.size);

			this.initialized = true;
			this.ready();

			// adding listener to add knownPeers
			$(document).on('WidgetInternalEvent', function(ev, action) {
				if (action.indexOf('peers-list|fetch|') === 0) {
					widget.knownPeers = [];

					var peers = JSON.parse(action.replace('peers-list|fetch|', ''));

					_.each(peers, function(peer) {
						widget.knownPeers.push(peer.nodeIP);
					});
				}
			});
		},

		fetch: function() {
			$('#widget-' + this.shell.id + ' > table > tbody').empty();

			var hood = [],
			 last = this.ip.split('.').splice(3),
			 split = this.ip.split('.').splice(0, 3).join('.');

			_.each(_.range(1, 256), function(i) {
				var ip = split + '.' + i;

				if (last == i) {
					return;
				} else if (_.indexOf(widget.knownPeers, ip) >= 0) {
					return;
				}

				hood.push(ip);
			});

			_.each(hood, function(ep) {
				var ep = window.location.protocol + '//' + ep + (window.location.port ? ':' + window.location.port : '') + '/ethereum-enterprise/ws',
				 stomp = Stomp.over(new SockJS(ep));
	
				stomp.debug = null;
	
				stomp.connect({}, function(frame) {
					// Connection successful
					stomp.subscribe('/topic/node/status', function(res) {
						var status = JSON.parse(res.body);
						status = status.data.attributes;

						stomp.disconnect();

						widget.showNeighbor(status);
					});
				});
			});
		},

		render: function() {
			Tower.screenManager.grounds.append(this.shell.tpl);

			$('#widget-' + this.shell.id).css({ 'height': '240px', 'margin-bottom': '10px', 'overflow-x': 'hidden', 'width': '100%' });
			$('#widget-' + this.shell.id).html( this.template() );
			$('#widget-' + this.shell.id + ' > table > tbody').on('click', '#neighbor-add', this._handler);

			this.fetch();
		},

		_handler: function(e) {
			e.preventDefault();

			var nodeUrl = $(this).data('enode'), _this = $(this);

			$.when(
				utils.load({ url: widget.url, data: { "args": nodeUrl } })
			).done(function(r) {
				setTimeout(function() {
					_this.fadeOut();
				}, 1000);
			});
		},

		showNeighbor: function(status) {
			if (!status || !status.nodeIP || !status.nodeUrl) 
				return;
			
			if (_.indexOf(widget.knownPeers, status.nodeIP) >= 0) {
				return;
			}

			$('#widget-' + this.shell.id + ' > table > tbody').append( this.templateRow({ neighbor: status }) );
		}
	};


	// register presence with screen manager
	Tower.screenManager.addWidget(widget);
})();
