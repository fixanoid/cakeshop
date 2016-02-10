
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
                $("select.contracts").append("<option value='" + c.get("address") + "'>" + (c.get("name") || c.get("address")) + "</option>");
            });
        });

        // Show compiled contracts in dropdown
        if (Sandbox.compiler_output && _.isArray(Sandbox.compiler_output)) {
            $("select.compiled_contracts").empty();
            Sandbox.compiler_output.forEach(function(c) {
                $("select.compiled_contracts").append("<option value='" + c.get("name") + "'>" + c.get("name") + "</option>");
            });
        }
    }

    function wrapFunction(method) {
        var s = '<form class="form-inline" data-method="' + method.name + '"><div class="form-group"><label>' + method.name + '</label> ';
        method.inputs.forEach(function(input) {
            s += '<input type="text" class="form-control" data-param="' + input.name + '" placeholder="' + input.name + '(' + input.type + ')"> ';
        });
		s += '<button class="btn btn-default send" type="submit">Send</button>';
        s += '</div></form>';
        return s;
    }

    function showTransactForm() {
        $(".transact .panel-body").empty();

        var abi = getActiveAbi();
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
            var form = $(e.target).parents("form");
            var methodName = form.attr("data-method");
            var method = getActiveMethod(methodName);
            var params = {};
            $(form).find("input").each(function(i, el) {
                el = $(el);
                params[el.attr("data-param")] = el.val();
            });
            doMethodCall(activeContract, method, params);
        });

    }

    function doMethodCall(contract, method, params) {
        var _params = _.map(params, function(v, k) { return v; });

        if (method.constant === true) {
            // use read
            activeContract.read(method.name, _params).then(function(res) {
                addTx("Called '" + method.name + "': " + JSON.stringify(res));
            });

        } else {
            // use transact
            activeContract.transact(method.name, _params).then(function(txId) {
                addTx("Called '" + method.name + "': created tx " + wrapTx(txId));
                Transaction.waitForTx(txId).then(function(tx) {
                    addTx("Transaction " + wrapTx(txId) + " was committed in block " + wrapBlock(tx.get("blockNumber")));
                });
            });
        }
    }

    function trunc(addr) {
        var len = addr.startsWith("0x") ? 10 : 8;
        return addr.substring(0, len);
    }

    function wrapBlock(blockId) {
        return '<a class="block" target="_blank" href="index.html#section=explorer&widgetId=block-detail&data=' + blockId + '">#' + blockId + '</a>';
    }

    function wrapTx(txId) {
        return '<a class="tx" target="_blank" href="index.html#section=explorer&widgetId=txn-detail&data=' + txId + '" title="' + txId + '">' + trunc(txId) + '</a>';
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

    // Enter contract address
    $(".select_contract input.address").change(function(e) {
        var addr = $(e.target).val();
        Contract.get(addr).then(function(c) {
            activeContract = c;
            showTransactForm();
            addTx("Using '" + c.get("name") + "' at " + wrapAddr(c.id));
        });
    });

    // Select already deployed contract
    $(".select_contract .contracts").change(function(e) {
        var addr = $(e.target).val();
        $(".select_contract input.address").val(addr).change();
    });

    // Deploy selected contract
    $(".select_contract .deploy").click(function(e) {
        // find contract to deploy
        var sel = $("select.compiled_contracts").val();
        if (!sel) {
            return;
        }

        var contract = _.find(Sandbox.compiler_output, function(c) { return c.get("name") === sel; });
        // take it and deploy it
    	var optimize = document.querySelector('#optimize').checked ? 1 : 0;
        addTx("Deploy Contract '" + contract.get("name") + "'");
        Contract.deploy(contract.get("code"), optimize).then(function(addr) {
            addTx("Contract '" + contract.get("name") + "' deployed at " + wrapAddr(addr));
            $(".select_contract input.address").val(addr).change();
        });
    });

    // Collapse 'select contract' form
    $(".select_contract .shrink").click(function(e) {
        var i = $(e.target);
        if (i.hasClass("fa-minus-square-o")) {
            // collapse
            i.removeClass("fa-minus-square-o").addClass("fa-plus-square-o");
            var addr = $(".select_contract input.address").val();
            if (addr) {
                $(".select_contract .panel-heading .title").text("Using " + addr);
            }
        } else {
            // expand
            i.removeClass("fa-plus-square-o").addClass("fa-minus-square-o");
            $(".select_contract .panel-heading .title").text("Choose Contract");
        }
        $(".select_contract .panel-body").toggle();
    });

    function getActiveAbi() {
        if (!activeContract) {
            return null;
        }

        return JSON.parse(activeContract.get("abi"));
    }

    function getActiveMethod(methodName) {
        var abi = getActiveAbi();
        if (!abi) {
            return;
        }
        return _.find(abi, function(m) { return m.type === "function" && m.name === methodName; });
    }

    Sandbox.showTxView = showTxView;

})();
