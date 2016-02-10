
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

    Contract.deploy = function(code, optimize) {
        return new Promise(function(resolve, reject) {
            Client.post(Contract.prototype.url("create"),
                {
                    code: code,
                    code_type: "solidity",
                    optimize: optimize
                }
            ).done(function(res, status, xhr) {
                var txid = res.data.id;
                Transaction.waitForTx(txid).then(function(tx) {
                    resolve(tx.get("contractAddress"));
                });
            });
        });
    };

    Contract.get = function(id) {
        return new Promise(function(resolve, reject) {
            Client.post(Contract.prototype.url("get"), { id: id }).
                done(function(res, status, xhr) {
                    resolve(new Contract(res.data.attributes));
                });
        });
    };

    Contract.list = function(cb) {
        Client.post(Contract.prototype.url("list")).
            done(function(res, status, xhr) {
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
            });
    };

    Contract.compile = function(code, optimize, cb) {
        Client.post(Contract.prototype.url("compile"),
            {
                code: code,
                code_type: "solidity",
                optimize: optimize
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
