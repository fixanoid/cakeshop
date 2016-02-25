
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var activeContract;

    function showTxView() {
        loadContracts();
    }

    function loadContracts() {
        // show deployed contracts (via registry)
        $("select.contracts").empty();
        $("select.contracts").append("<option value=''></option>");
        Contract.list(function(contracts) {
            contracts.forEach(function(c) {
                var ts = moment.unix(c.get("createdDate")).format("YYYY-MM-DD hh:mm A");
                var name = c.get("name") + " (" + trunc(c.id) + ", " + ts + ")";
                $("select.contracts").append("<option value='" + c.id + "'>" + name + "</option>");
            });
        });

        // Show compiled contracts in dropdown
        if (Sandbox.compiler_output && _.isArray(Sandbox.compiler_output)) {
            $("select.compiled_contracts").empty();
            $("select.compiled_contracts").append("<option value=''></option>");
            Sandbox.compiler_output.forEach(function(c) {
                $("select.compiled_contracts").append("<option value='" + c.get("name") + "'>" + c.get("name") + "</option>");
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
        var s = '<form class="form-inline" data-method="' + method.name + '"><div class="form-group"><label>' + method.name + '</label> ';
        s += wrapInputs(method);
		s += '<button class="btn btn-default send" type="submit">Send</button>';
        s += '</div></form>';
        return s;
    }

    function showTransactForm() {
        $(".transact .send").off("click");
        $(".transact .panel-body").empty();

        var abi = activeContract.abi;
        if (!abi) {
            return;
        }

        abi.forEach(function(method) {
            if (method.type !== "function") {
                return;
            }
            $(".transact .panel-body").append(wrapFunction(method));
        });

        $(".transact .send").click(function(e) {
            e.preventDefault();
            var form = $(e.target).parents("form");
            var methodName = form.attr("data-method");
            var method = activeContract.getMethod(methodName);
            var params = {};
            $(form).find("input").each(function(i, el) {
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
                s += '</td>';
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
        return '<a class="block" target="_explorer" href="index.html#section=explorer&widgetId=block-detail&data=' + blockId + '">#' + blockId + '</a>';
    }

    function wrapTx(txId) {
        return '<a class="tx" target="_explorer" href="index.html#section=explorer&widgetId=txn-detail&data=' + txId + '" title="' + txId + '">' + trunc(txId) + '</a>';
    }

    function wrapAddr(addr) {
        return '<span class="addr">' + addr + '</span>';
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
    $(".select_contract input.address").change(function(e) {
        var addr = $(e.target).val();
        Contract.get(addr).then(setActiveContract);
    });

    // Select already deployed contract
    $(".select_contract .contracts").change(function(e) {
        var addr = $(e.target).val();
        $(".select_contract input.address").val(addr).change();
    });

    // Select contract to deploy
    $(".select_contract .compiled_contracts").change(function(e) {
        var sel = $(e.target).val();
        var con = $(".select_contract .constructor");

        $(".select_contract .deploy").off("click");
        con.empty();

        var contract = _.find(Sandbox.compiler_output, function(c) { return c.get("name") === sel; });

        if (!sel || !contract) {
            con.hide();
            return;
        }

        var conMethod = _.find(contract.abi, function(m) { return m.type === "constructor"; });

        if (!conMethod.inputs || conMethod.inputs.length === 0) {
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
            var sel = $("select.compiled_contracts").val();
            if (!sel) {
                return false;
            }

            var contract = _.find(Sandbox.compiler_output, function(c) { return c.get("name") === sel; });
            // take it and deploy it
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

            Contract.deploy(contract.get("code"), optimize, _params).then(function(addr) {
                addTx("Contract '" + contract.get("name") + "' deployed at " + wrapAddr(addr));
                $(".select_contract input.address").val(addr);

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
                    });
                }
                setTimeout(waitForRegistration, 200);
            });

            return false;
        });

    });


    // Collapse 'select contract' form
    $(".select_contract .shrink").click(function(e) {
        var i = $(e.target);
        if (toggleCollapseIcon(i)) {
            $(".select_contract .panel-heading .title").text("Choose Contract");
        } else {
            var addr = $(".select_contract input.address").val();
            if (addr) {
                $(".select_contract .panel-heading .title").text("Using " + addr);
            }
        }
        $(".select_contract .panel-body").toggle();
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

    $(".papertape .shrink").click(function(e) {
        toggleCollapseIcon($(e.target));
        $(".papertape .panel-body").toggle();
    });

    $(".transact .shrink").click(function(e) {
        toggleCollapseIcon($(e.target));
        $(".transact .panel-body").toggle();
    });


    Sandbox.showTxView = showTxView;

})();
