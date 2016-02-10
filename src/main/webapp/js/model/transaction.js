
(function() {

    var Transaction = window.Transaction = Backbone.Model.extend({

        urlRoot: "api/transaction",
        url: function(path) {
            return this.urlRoot + (path ? "/" + path : "");
        },

        initialize: function() {
            this.id = this.get("address");
        },

    });

    Transaction.waitForTx = function(txId) {
        return new Promise(function(resolve, reject) {
            Client.stomp.subscribe("/topic/transaction/" + txId, function(res) {
                var txRes = JSON.parse(res.body);
                if (txRes.data && txRes.data.id === txId) {
                    resolve(new Transaction(txRes.data.attributes));
                }
            });
        });
    };

})();
