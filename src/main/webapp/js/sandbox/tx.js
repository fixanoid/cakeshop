
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var activeContract;
    var compiler_output;

    function showTxView() {
        if ($(".select_contract .contracts select option").length <= 1) {
            loadContracts();
        }
        // showCompiledContracts();
    }

    Sandbox.on("compile", function() {
        $(".select_contract .compiled_contracts select").empty();
        $(".select_contract .constructor").empty();
        $(".compiled_contracts .refresh").show();
    });

    Sandbox.on("compiled", showCompiledContracts);

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

        if (method.constant === true) {
            // use read
            activeContract.read(method.name, _params).then(function(res) {
                addTx("Called '" + method.name + "': " + JSON.stringify(res));
            }, function(err) {
                addTx("Called '" + method.name + "': [ERROR]" + err);
            });

        } else {
            // use transact
            activeContract.transact(method.name, _params).then(function(txId) {
                addTx("Called '" + method.name + "': created tx " + wrapTx(txId));
                Transaction.waitForTx(txId).then(function(tx) {
                    addTx("Transaction " + wrapTx(txId) + " was committed in block " + wrapBlock(tx.get("blockNumber")));
                    showCurrentState();
                });
            });
        }
    }

    function showCurrentState() {
        if (!activeContract) {
            return;
        }
        activeContract.readState().then(function(results) {
            var s = '<table class="table">';
            results.forEach(function(r) {
                s += '<tr>';
                s += '<td>' + r.method + '</td>';
                s += '<td>' + r.result + '</td>';
                s += '</tr>';
            });
            s += '</table>';
            try {
                $(".panel.state .body").empty().append(s);
            } catch (e) {
                console.log(e);
            }
        });
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
        addTx("Using '" + c.get("name") + "' at " + wrapAddr(c.id));
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

            var contract = _.find(compiler_output, function(c) { return c.get("name") === sel; });
        	var optimize = document.querySelector('#optimize').checked;

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
            addTx("Deploying Contract '" + contract.get("name") + "'" + _args);

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
