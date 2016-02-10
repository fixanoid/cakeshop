
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var activeContract;

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

    function showTransactForm() {

    }

    function showTxView() {
        console.log("showTxView");
        loadContracts();
        showTransactForm();
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


    $(".select_contract input.address").change(function(e) {
        var addr = $(e.target).val();
        console.log("got new addr", addr);
        Contract.get(addr).then(function(c) {
            console.log("got contract", c);
        });
    });

    // Select already deployed contract
    $(".select_contract .contracts").change(function(e) {
        var addr = $(e.target).val();
        console.log("selected ", addr);
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

    Sandbox.showTxView = showTxView;

})();
