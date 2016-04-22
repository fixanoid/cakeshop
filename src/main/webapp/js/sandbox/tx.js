
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var activeContract;
    var compiler_output;

    function showTxView() {
        if ($(".select_contract .contracts select option").length <= 1) {
            loadContracts();
        }
        loadAccounts();
        // showCompiledContracts();
        $(".col3").scrollToFixed();
    }

    var events_enabled = true;

    Sandbox.on("compile", function() {
        if (!events_enabled) {
            return;
        }
        $(".select_contract .compiled_contracts select").empty();
        $(".select_contract .constructor").empty();
        $(".compiled_contracts .refresh").show();
    });

    Sandbox.on("compiled", function(contracts) {
        if (!events_enabled) {
            return;
        }
        showCompiledContracts(contracts);
    });

    function loadAccounts() {
        Account.list().then(function(accounts) {
            var s = '<table class="table">';
            accounts.forEach(function(a) {
                s += '<tr>';
                s += '<td>' + a.get("address") + '</td>';
                s += '<td class="text-right">' + a.humanBalance() + '</td>';
                s += '</tr>';
            });
            s += '</table>';

            $(".panel.accounts .table").remove();
            $(".panel.accounts").append(s);
        });
    }

    function loadContracts() {
        // show deployed contracts (via registry)
        var sel = $(".select_contract .contracts select")
            .empty()
            .append("<option value=''></option>");

        // $("div.contracts .refresh").show();
        Contract.list(function(contracts) {
            contracts.forEach(function(c) {
                var ts = moment.unix(c.get("createdDate")).format("YYYY-MM-DD hh:mm A");
                var name = c.get("name") + " (" + trunc(c.id) + ", " + ts + ")";
                sel.append("<option value='" + c.id + "'>" + name + "</option>");
            });
            // $("div.contracts .refresh").hide();
        });
    }

    function showCompiledContracts(_output) {
        // Show compiled contracts in dropdown

        // if (_output === -1) {
        //     return; //
        // }

        compiler_output = _output;
        $(".compiled_contracts .refresh").hide();

        var sel = $(".select_contract .compiled_contracts select")
            .empty()
            .append("<option value=''></option>");

        if (compiler_output && _.isArray(compiler_output)) {
            compiler_output.forEach(function(c) {
                sel.append("<option value='" + c.get("name") + "'>" + c.get("name") + "</option>");
            });
        }
    }

    function wrapInputs(method) {
        var s = "";
        method.inputs.forEach(function(input) {
            s += '<input type="text" class="form-control" data-param="' + input.name + '" placeholder="' + input.name + '(' + input.type + ')"> ';
        });
        return s;
    }

    function wrapFunction(method) {
        var s = '<tr>';
        s += '<td>' + method.name + '<br/>' + wrapInputs(method) + '</td>';
        s += '<td class="send"><button class="btn btn-default send" type="submit" data-method="' + method.name + '">';
        s += (method.constant === true ? "Read" : "Transact");
        s += '</button></td>';
        s += '</tr>';
        return s;
    }

    function showTransactForm() {
        $(".transact .send").off("click");
        $(".transact .panel-body").empty();

        var abi = activeContract.abi;
        if (!abi) {
            return;
        }

        abi = _.sortBy(abi, "name");

        var s = '<table class="table">';

        abi.forEach(function(method) {
            if (method.type !== "function") {
                return;
            }
            s += wrapFunction(method);
        });
        s += '</table>';
        $(".transact .panel-body").append(s);

        $(".transact .send").click(function(e) {
            e.preventDefault();
            var tr = $(e.target).parents("tr");
            var methodName = $(e.target).attr("data-method");
            var method = activeContract.getMethod(methodName);
            var params = {};
            $(tr).find("input").each(function(i, el) {
                el = $(el);
                params[el.attr("data-param")] = el.val();
            });
            doMethodCall(activeContract, method, params);
            return false;
        });

    }

    function doMethodCall(contract, method, params) {
        var _params = _.map(params, function(v, k) { return v; });
        var _sig_params = _.map(params, function(v, k) { return JSON.stringify(v); }).join(", ");
        var method_sig = method.name + "(" + _sig_params + ")";

        if (method.constant === true) {
            activeContract.proxy[method.name].apply(null, _params).then(function(res) {
                addTx("[read] " + method_sig + " => " + JSON.stringify(res), null);
            }, function(err) {
                addTx("[read] " + method_sig + " => [ERROR]" + err);
            });

        } else {
            activeContract.proxy[method.name].apply(null, _params).then(function(txId) {
                addTx("[txn] " + method_sig + " => created tx " + wrapTx(txId));
                Transaction.waitForTx(txId).then(function(tx) {
                    addTx("[txn] " + wrapTx(txId) + " was committed in block " + wrapBlock(tx.get("blockNumber")));
                    showCurrentState();
                });
            });
        }
    }

    function showCurrentState() {
        if (!activeContract) {
            return;
        }

        var contract_mappings = _.find(
            parseContracts(activeContract.get("code")),
            function(c) { return c.name === activeContract.get("name"); }
        );

        activeContract.readState().then(function(results) {

            // modify results if we have mappings
            var state = results;
            if (contract_mappings && contract_mappings.mappings.length > 0) {
                state = _.reject(results, function(r) {
                    var matches = _.find(contract_mappings.mappings, function(m) {
                        return (r.method.name === m.counter || r.method.name === m.keyset || r.method.name === m.getter); });
                    if (matches) {
                        return true;
                    } else {
                        return false;
                    }
                });

                // now that we filtered our special vars out, add back in a mapping var/table
                contract_mappings.mappings.forEach(function(mapping) {
                    var data = { method: { name: mapping.var } };
                    state.push(data);

                    var res = {};
                    var getter_results = _.find(results, function(r) { return r.method.name === mapping.getter; });
                    var promises = [];
                    getter_results.result.forEach(function(gr) {
                        promises.push(new Promise(function(resolve, reject) {
                            activeContract.read(mapping.var, [gr]).then(function(mapping_val) {
                                var d = {};
                                d[gr] = mapping_val;
                                resolve(d);
                            });
                        }));
                    });
                    Promise.all(promises).then(function(mapping_results) {
                        // convert mapping_results array back into single object
                        data.result = _.reduce(mapping_results, function(memo, r) { return _.extend(memo, r); }, {});
                        displayStateTable(state);
                    });

                });
            } else {
                displayStateTable(results);
            }


        });
    }

    function displayStateTable(results) {
        var s = '<table class="table">';
        results.forEach(function(r) {
            s += '<tr>';
            s += '<td>' + r.method.name + '</td>';
            s += '<td>';
            // console.log(r);
            if (r.result && _.isArray(r.result)) {
                s += '<ol start="0">';
                r.result.forEach(function(v) {
                    s += "<li>" + JSON.stringify(v) + "</li>";
                });
            } else if (r.result && _.isObject(r.result)) {
                s += '<table class="table table-bordered table-condensed">';
                _.keys(r.result).forEach(function(key) {
                    s += '<tr>';
                    s += '<td>' + key + '</td>';
                    s += '<td>' + r.result[key] + '</td>';
                    s += '</tr>';
                });
                s += '</table>';
            } else if (r.result && _.isString(r.result)) {
                if (r.result.length > 20) {
                    s += '<div class="form-group"><textarea class="form-control" rows="3">' + r.result + '</textarea></div>';
                } else {
                    s += r.result;
                }
            } else {
                s += r.result;
            }
            s += '</td></tr>';
        });
        s += '</table>';
        try {
            $(".panel.state .table").remove();
            $(".panel.state").append(s);
        } catch (e) {
            console.log(e);
        }
    }

    function trunc(addr) {
        var len = addr.startsWith("0x") ? 10 : 8;
        return addr.substring(0, len);
    }

    function wrapBlock(blockId) {
        return '<a class="block" target="_explorer" href="index.html#section=explorer&widgetId=block-detail&data=' + encodeURIComponent(blockId) + '">#' + blockId + '</a>';
    }

    function wrapTx(txId) {
        return '<a class="tx" target="_explorer" href="index.html#section=explorer&widgetId=txn-detail&data=' + encodeURIComponent(txId) + '" title="' + txId + '">' + trunc(txId) + '</a>';
    }

    function wrapAddr(addr) {
        return '<span class="addr" title="' + addr + '">' + trunc(addr) + '</span>';
    }

    function addTx(message, date) {
        date = date ? moment(date) : moment();
        var timestamp = '<span class="time pull-right">' + date.format("hh:mm:ss A") + '</span>';
        var div = '<div class="tx">' + timestamp + message + '</div>';
		$(".papertape .panel-body").append(div);
    }

    function setActiveContract(c) {
        activeContract = c;
        showTransactForm();
        showCurrentState();
        addTx("using '" + c.get("name") + "' at " + wrapAddr(c.id));
    }

    // Enter contract address
    $(".select_contract .address input").change(function(e) {
        var addr = $(e.target).val();
        Contract.get(addr).then(setActiveContract);
    });

    // Select already deployed contract
    $(".select_contract .contracts select").change(function(e) {
        var addr = $(e.target).val();
        $(".select_contract .address input").val(addr).change();
    });


    var expose_mapping = function(src, mapping) {

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

    var parseContracts = function(src) {
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
                    c.modified_src = expose_mapping(c.src, mapping);
                    // console.log(c);
                }
            }
        });

        return contracts;
    };

    var preprocess = function(src) {
        var contracts = parseContracts(src);
        return _.map(contracts, function(c) { return (c.modified_src ? c.modified_src : c.src); }).join("\n");
    };



    // Select contract to deploy
    $(".select_contract .compiled_contracts select").change(function(e) {
        var sel = $(e.target).val();
        var con = $(".select_contract .constructor");

        $(".select_contract .deploy").off("click");
        con.empty();

        var contract = _.find(compiler_output, function(c) { return c.get("name") === sel; });

        if (!sel || !contract) {
            con.hide();
            return;
        }

        var conMethod = _.find(contract.abi, function(m) { return m.type === "constructor"; });

        if (!conMethod || !conMethod.inputs || conMethod.inputs.length === 0) {
            con.append("(no constructor arguments)");
        } else {
            con.append(wrapInputs(conMethod));
        }

		con.append('<br/><button class="btn btn-default deploy" type="submit">Deploy</button>');
        con.show();

        // Deploy selected contract
        $(".select_contract .deploy").click(function(e) {
            e.preventDefault();
            // find contract to deploy
            var sel = $(".select_contract .compiled_contracts select").val();
            if (!sel) {
                return false;
            }

        	var editorSource = preprocess(Sandbox.getEditorSource());
        	var optimize = document.querySelector('#optimize').checked;
            Contract.compile(editorSource, optimize).then(function(compiler_output) {
                var contract = _.find(compiler_output, function(c) { return c.get("name") === sel; });

                var params = {};
                $(".select_contract .constructor").find("input").each(function(i, el) {
                    el = $(el);
                    params[el.attr("data-param")] = el.val();
                });
                var _params = _.map(params, function(v, k) { return v; });

                var _args = "";
                if (_params.length > 0) {
                    _args = " (" + _params.join(", ") + ")";
                }
                addTx("[deploy] Contract '" + contract.get("name") + "'" + _args);

                Contract.deploy(contract.get("code"), optimize, _params, contract.get("binary")).then(function(addr) {
                    addTx("Contract '" + contract.get("name") + "' deployed at " + wrapAddr(addr));
                    $(".select_contract .address input").val(addr);

                    addTx("Waiting for contract to be registered");
                    var registered = false;
                    function waitForRegistration() {
                        // TODO use contract event topic for registry ??
                        Contract.get(addr).then(function(c) {
                            if (c === null || c.get("name") === null) {
                                setTimeout(waitForRegistration, 1000); // poll every 1s til done
                                return;
                            }
                            registered = true;
                            setActiveContract(c);
                            loadContracts(); // refresh contract list
                        });
                    }
                    setTimeout(waitForRegistration, 200);
                });

            });


            return false;
        });

    });

    /**
     * Toggle the expand/collapse icon.
     *
     * @return [Boolean] true if panel is expanded
     */
    function toggleCollapseIcon(i) {
        if (i.hasClass("fa-minus-square-o")) {
            // collapse
            i.removeClass("fa-minus-square-o").addClass("fa-plus-square-o");
            return false;
        } else {
            // expand
            i.removeClass("fa-plus-square-o").addClass("fa-minus-square-o");
            return true;
        }
    }

    function shrinkify(panel_class) {
        $(panel_class + " .shrink").click(function(e) {
            toggleCollapseIcon($(e.target));
            $(panel_class + " .panel-body").toggle();
        });
    }

    shrinkify(".select_contract");
    shrinkify(".state");
    shrinkify(".papertape");
    shrinkify(".transact");

    Sandbox.showTxView = showTxView;

    $(function() {
        showTxView(); // default view
    });

})();
