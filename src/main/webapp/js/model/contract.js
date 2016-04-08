
(function() {
    var Contract = window.Contract = Backbone.Model.extend({

        urlRoot: 'api/contract',
        url: function(path) {
            return this.urlRoot + (path ? '/' + path : '');
        },

        initialize: function() {
            this.id = this.get('address');
            if (this.get("abi") && this.get("abi").length > 0) {
                this.abi = JSON.parse(this.get("abi"));
            }
        },

        getMethod: function(methodName) {
            if (!this.abi) {
                return null;
            }
            return _.find(this.abi, function(m) { return m.type === "function" && m.name === methodName; });
        },

        readState: function() {
            var contract = this;
            return new Promise(function(resolve, reject) {
                if (!contract.abi) {
                    return reject();
                }

                var promises = [];
                contract.abi.forEach(function(method) {
                    if (method.constant === true && method.inputs.length === 0) {
                        promises.push(new Promise(function(resolve, reject) {
                            contract.read(method.name).then(function(res) {
                                resolve({method: method, result: res});
                            });
                        }));
                    }
                });
                Promise.all(promises).then(resolve, reject);
            });
        },

        /**
         * Returns result of Read call via Promise
         */
        read: function(method, args) {
            var contract = this;
            return new Promise(function(resolve, reject) {
                Client.post(contract.url('read'),
                    {
                        id: contract.id,
                        method: method,
                        args: args
                    }
                ).done(function(res, status, xhr) {
                    resolve(res.data); // return read result

                }).fail(function(xhr, status, errThrown) {
                    console.log('READ FAILED!!', status, errThrown);
                    reject(errThrown);
                });

            });
        },

        /**
         * Returns a Transaction ID via Promise
         */
        transact: function(method, args) {
            var contract = this;
            return new Promise(function(resolve, reject) {
                Client.post(contract.url('transact'),
                    {
                        id: contract.id,
                        method: method,
                        args: args
                    }
                ).done(function(res, status, xhr) {
                    resolve(res.data.id); // return tx id

                }).fail(function(xhr, status, errThrown) {
                    console.log('TXN FAILED!!', status, errThrown);
                    reject(errThrown);
                });

            });
        },
    });

    Contract.deploy = function(code, optimize, args, binary) {
        return new Promise(function(resolve, reject) {
            Client.post(Contract.prototype.url('create'),
                {
                    code: code,
                    code_type: 'solidity',
                    optimize: optimize,
                    args: args,
                    binary: binary
                }
            ).done(function(res, status, xhr) {
                var txid = res.data.id;
                Transaction.waitForTx(txid).then(function(tx) {
                    resolve(tx.get('contractAddress'));
                });
            });
        });
    };

    Contract.get = function(id) {
        return new Promise(function(resolve, reject) {
            Client.post(Contract.prototype.url('get'), { id: id }).
                done(function(res, status, xhr) {
                    resolve(new Contract(res.data.attributes));
                }).
				fail(function(xhr, status, errThrown) {
                    console.log('Contract load FAILED!!', status, errThrown);
                    reject(errThrown);
                });
        });
    };

    Contract.list = function(cb) {
        Client.post(Contract.prototype.url('list')).
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
        return new Promise(function(resolve, reject) {
            Client.post(Contract.prototype.url('compile'),
                {
                    code: code,
                    code_type: 'solidity',
                    optimize: optimize
                }
            ).done(function(res, status, xhr) {
                if (res.data && _.isArray(res.data)) {
                    var contracts = [];
                    res.data.forEach(function(d) {
                        var c = new Contract(d.attributes);
                        contracts.push(c);
                    });
                    resolve(contracts);
                }
            }).fail(function(xhr, status, errThrown) {
                reject(JSON.parse(xhr.responseText).errors);
            });
        });
    };

})();
