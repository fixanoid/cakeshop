
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
                this.proxy = new Contract.Proxy(this);
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
        read: function(options) {
            var contract = this;
            return new Promise(function(resolve, reject) {
                Client.post(contract.url('read'),
                    {
                        from: options.from,
                        id: contract.id,
                        method: options.method,
                        args: options.args
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
        transact: function(options) {
            var contract = this;
            return new Promise(function(resolve, reject) {
                Client.post(contract.url('transact'),
                    {
                        from: options.from,
                        id: contract.id,
                        method: options.method,
                        args: options.args
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

    Contract.preprocess = function(src) {
        var contracts = Contract.parseSource(src);
        return _.map(contracts, function(c) { return (c.modified_src ? c.modified_src : c.src); }).join("\n");
    };

    Contract.parseSource = function(src) {
        var contracts = [];

        // Find each contract definition
        var c = [], contract_name;
        src.split(/\n/).forEach(function(line) {
            var matches = line.match(/contract +(.*?)( +is.*?)? *\{/);
            if (matches) {
                if (c && c.length > 0) { // found a new contract, add prev one to array
                    contracts.push({name: contract_name, src: c.join("\n")});
                    c = [];
                    contract_name = null;
                }

                contract_name = matches[1];
                c = [line];
                if (line.match(/\{[^\{]*?\}/)) { // single-line contract def
                    contracts.push({name: contract_name, src: c.join("\n")});
                    c = [];
                    contract_name = null;
                }
            } else {
                c.push(line);
            }
        });
        if (c && c.length > 0) { // push after EOF
            contracts.push({name: contract_name, src: c.join("\n")});
        }

        // search each contract definition for our ##mapping macro
        contracts.forEach(function(c) {
            c.mappings = [];
            var matches = c.src.match(/^ *\/\/ *##mapping +(.+?)$/m);
            if (matches) {
                var mapping_var = matches[1];


                matches = c.src.match(new RegExp("mapping *\\((.+?) => (.+?)\\) *.*? " + mapping_var + " *;"));
                if (matches) {
                    var key_type = matches[1],
                        val_type = matches[2];

                    var mapping = {
                        var:      mapping_var,
                        key_type: key_type,
                        val_type: val_type
                    };
                    c.mappings.push(mapping);

                    // now that we have all the mapping info, modify the original source
                    c.modified_src = Contract.expose_mapping(c.src, mapping);
                    // console.log(c);
                }
            }
        });

        return contracts;
    };

    Contract.expose_mapping = function(src, mapping) {

        var counter = mapping.counter = "__" + mapping.var + "_num_ids";
        var keyset  = mapping.keyset  = "__" + mapping.var + "_ids";
        var getter  = mapping.getter  = "__get_" + mapping.var + "_ids";

        // skip if the src has already been modified
        if (src.match(new RegExp(counter))) {
            return src;
        }

        var msrc = "";

        src.split(/\n/).forEach(function(line) {
            var map_set = line.match(new RegExp(mapping.var + "\\[(.*?)\\] *="));
            if (line.match(new RegExp("^ *\\/\\/ *##mapping +" + mapping.var + "$", "m"))) {
                msrc += line + "\n";
                // attach helper vars
                msrc += "uint public " + counter + ";\n";
                msrc += mapping.key_type + "[] public " + keyset + ";\n";
                msrc += "function " + getter + "() public constant returns(" + mapping.key_type + "[] _ids) {\n";
                msrc += "  return " + keyset + ";\n";
                msrc += "}\n";

            } else if (map_set) {
                msrc += line + "\n";
                msrc += keyset + ".length = ++" + counter + ";\n"; // grow array
                msrc += keyset + "[" + counter + "-1] = " + map_set[1] + ";"; // store key

            } else {
                msrc += line + "\n";
            }

        });

        return msrc;
    };


})();
