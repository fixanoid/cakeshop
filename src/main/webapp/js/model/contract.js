
(function() {
    var Contract = window.Contract = Backbone.Model.extend({

        urlRoot: "api/contract",
        url: function(path) {
            return this.urlRoot + (path ? "/" + path : "");
        },

        initialize: function() {
            this.id = this.get("address");
        },

    });

    Contract.compile = function(code, optimize, cb) {
        Client.post(Contract.prototype.url("compile"),
            {
                code: code,
                code_type: "solidity"
            }
        ).done(function(res, status, xhr) {
            if (res.data && _.isArray(res.data)) {
                var contracts = [];
                res.data.forEach(function(d) {
                    var c = new Contract(d.attributes);
                    contracts.push(c);
                });
                if (cb) {
                    cb(contracts);
                }
            }
        }).fail(function(xhr, status, errThrown) {
            // TODO
            console.log("compilation failed: ", status);
            console.log("err: ", errThrown);
        });
    };

})();
