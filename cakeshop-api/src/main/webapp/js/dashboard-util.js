var Utils = {
  debug: function(message) {
    var _ref;
    return typeof window !== 'undefined' && window !== null ? (_ref = window.console) != null ? _ref.log(message) : void 0 : void 0;
  },

  demoFuzz: function() {
    return window.demo ? Math.ceil(Math.random() * 10) : 0;
  },

  emit: function(e) {
    this.debug('Emitting ' + e);

    if (e) {
      $(document).trigger('WidgetInternalEvent', [ e ]);
    }
  },

  on: function(callback) {
    $(document).on('WidgetInternalEvent', callback);
  }
}

window.Dashboard.Utils = Utils;

// Adding event for sleep / wake
$(document).on('visibilitychange', function(e) {
  Dashboard.Utils.emit('tower-control|sleep|' + document.hidden);
});
