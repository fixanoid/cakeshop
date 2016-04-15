
(function() {

    var Proxy = (function() {
        function Proxy(contract) {
            this._contract = contract;
            if (!contract.abi) {
                return;
            }
            var proxy = this;
            contract.abi.forEach(function(method) {
                if (method.type !== "function") {
                    return;
                }

                /**
                 * Process args based on ABI definitions
                 */
                function processInputArgs(args) {
                    var inputs = method.inputs;
                    var ret = [];

                    for (var i = 0; i < inputs.length; i++) {
                        var input = inputs[i],
                            arg   = args[i];
                        if (input.type.match(/^bytes\d+$/)) {
                            // base64 encode bytes
                            ret.push(Sandbox.encodeBytes(arg));
                        } else {
                            // all other input types, just accumulate
                            ret.push(arg);
                        }
                    }

                    return ret;
                }

                /**
                 * Process results based on ABI definitions
                 */
                function processOutputArgs(results) {
                    var outputs = method.outputs;

                    // console.log("outputs", outputs);
                    // console.log("results", results);

                    var ret = [];
                    for (var i = 0; i < outputs.length; i++) {
                        var output = outputs[i],
                            result = results[i];
                        if (output.type.match(/^bytes\d+$/)) {
                            // base64 decode bytes
                            ret.push(Sandbox.decodeBytes(result));
                        } else if (output.type.match(/^bytes\d+\[\d*\]$/) && _.isArray(result)) {
                            console.log("decoding result bytes32[]", result);
                            // base64 decode arrays of bytes
                            result = _.map(result, function(v) { return Sandbox.decodeBytes(v); });
                            console.log("decoded ", result);
                            ret.push(result);
                        } else {
                            // all other input types, just accumulate
                            ret.push(result);
                        }
                    }

                    if (outputs.length === 1) {
                        return ret[0]; // hmmm?
                    }
                    return ret;
                }

                // attach method to proxy
                proxy[method.name] = function() {
                    // process arguments based on ABI
                    var args = processInputArgs(Array.apply(null, arguments));

                    return new Promise(function(resolve, reject) {
                        if (method.constant === true) {
                            contract.read(method.name, args).then(function(res) {
                                resolve(processOutputArgs(res));
                            }, function(err) {
                                reject(err);
                            });
                        } else {
                            contract.transact(method.name, args).then(function(txId) {
                                resolve(txId);
                            }, function(err) {
                                reject(err);
                            });
                        }
                    });
                };
            });
        }

        Proxy.prototype.move = function(meters) {
            return alert(this.name + (" moved " + meters + "m."));
        };

        return Proxy;
    })();

    var Contract = window.Contract = Backbone.Model.extend({

        urlRoot: 'api/contract',
        url: function(path) {
            return this.urlRoot + (path ? '/' + path : '');
        },

        initialize: function() {
            this.id = this.get('address');
            if (this.get("abi") && this.get("abi").length > 0) {
                this.abi = JSON.parse(this.get("abi"));
                this.proxy = new Proxy(this);
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
                    // read all constant methods with no inputs
                    if (method.constant === true && method.inputs.length === 0) {
                        promises.push(new Promise(function(resolve, reject) {
                            contract.proxy[method.name]().then(function(res) {
                                resolve({method: method, result: res});
                            });
                        }));
                    }
                });
                Promise.all(promises).then(resolve, reject);
            });
        },

        /**
         * Returns result of read call via Promise.
         *
         * NOTE: this is a low-level method and not generally meant to be
         *       called directly. Instead, use the proxy method.
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
         *
         * NOTE: this is a low-level method and not generally meant to be
         *       called directly. Instead, use the proxy method.
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
