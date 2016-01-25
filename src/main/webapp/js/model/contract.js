
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
        ).done(function(res) {
            if (res.data && res.data.type === "contract") {
                var c = new Contract(res.data.attributes);
                if (cb) {
                    cb(c);
                }
            }
        });
    };

})();
