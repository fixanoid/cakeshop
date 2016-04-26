
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
            Sandbox.accounts = accounts;
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
        var s = '<tr class="method" data-method="' + method.name + '">';
        s += '<td>' + method.name + '<br/>' + wrapInputs(method) + '</td>';
        s += '<td class="send"><button class="btn btn-default send" type="submit">';
        s += (method.constant === true ? "Read" : "Transact");
        s += '</button></td>';
        s += '</tr>';
        return s;
    }

    function accountsDropDown() {
        var s = '<tr class="from_address">';
        s += '<td colspan="2" class="from_address">FROM ADDRESS<br/>';
        s += '<select class="accounts">';
        Sandbox.accounts.forEach(function(a) {
            s += "<option>" + a.get("address") + "</option>";
        });
        s += '</select>';
        s += '</td>';
        s += '</tr>';
        return s;
    }

    function showTransactForm() {
        $(".transact .method").off("click");
        $(".transact .send").off("click");

        var abi = activeContract.abi;
        if (!abi) {
            return;
        }

        abi = _.sortBy(abi, "name");

        var s = '<table class="table">';

        s += accountsDropDown();

        abi.forEach(function(method) {
            if (method.type !== "function") {
                return;
            }
            s += wrapFunction(method);
        });
        s += '</table>';

        $(".transact table").remove();
        $(".transact").append(s);

        $(".transact .send button").click(function(e) {
            e.preventDefault();
            var tr = $(e.target).parents("tr");
            var fromAddr = $(".transact select.accounts").val();
            var method = activeContract.getMethod(tr.attr("data-method"));
            highlightMethod(method);

            var params = {};
            $(tr).find("input").each(function(i, el) {
                el = $(el);
                params[el.attr("data-param")] = el.val();
            });
            doMethodCall(activeContract, fromAddr, method, params);
            return false;
        });

        $(".transact .method").click(function(e) {
            var el = e.target.tagName === "TR" ? $(e.target) : $(e.target).parents("tr");
            var methodName = el.attr("data-method");
            var method = activeContract.getMethod(methodName);
            highlightMethod(method);
        });

    }

    function highlightMethod(method) {
        var lines = Sandbox.getEditorSource().split("\n");
        for (var i = 0; i < lines.length; i++) {
            var highlight = false;
            if (lines[i].match(new RegExp("function\\s+" + method.name + "\\s*\\("))) {
                highlight = true;
            } else if (method.constant === true &&
                    lines[i].match(new RegExp("^\\s*[a-z\\d\\[\\]]+\\s+public\\b.*?" + method.name + "\\s*;"))) {

                highlight = true;
            }
            if (highlight) {
                Sandbox.editor.selection.moveCursorToPosition({row: i, column: 0});
                Sandbox.editor.selection.selectLine();
                Sandbox.editor.scrollToLine(i, true, false);
            }
        }
    }

    function doMethodCall(contract, from, method, params) {
        var _params = _.map(params, function(v, k) { return v; });
        var _sig_params = _.map(params, function(v, k) { return JSON.stringify(v); }).join(", ");
        var method_sig = method.name + "(" + _sig_params + ")";

        if (method.constant === true) {
            activeContract.proxy[method.name]({from: from, args: _params}).then(function(res) {
                addTx("[read] " + method_sig + " => " + JSON.stringify(res), null);
            }, function(err) {
                addTx("[read] " + method_sig + " => [ERROR]" + err);
            });

        } else {
            activeContract.proxy[method.name]({from: from, args: _params}).then(function(txId) {
                addTx("[txn] " + method_sig + " => created tx " + wrapTx(txId));
                Transaction.waitForTx(txId).then(function(tx) {
                    addTx("[txn] " + wrapTx(txId) + " was committed in block " + wrapBlock(tx.get("blockNumber")));
                    showCurrentState(activeContract._current_state);
                });
            });
        }
    }

    function showCurrentState(previousState) {
        if (!activeContract) {
            return;
        }
        activeContract.readState().then(function(results) {
            displayStateTable(results);

            if (!previousState) {
                return;
            }

            // diff states and show changes in paper tape
            var resultHash = {},
                previousHash = {};

            results.forEach(function(r) { resultHash[r.method.name] = r.result; });
            previousState.forEach(function(r) { previousHash[r.method.name] = r.result; });

            _.each(resultHash, function(val, key) {

                var pVal = previousHash[key];
                if (val !== pVal) {
                    if (_.isUndefined(pVal) || pVal === null) {
                        addTx("[state] " + key + " = " + JSON.stringify(val));
                    } else {
                        addTx("[state] " + key + ": " + JSON.stringify(pVal) + " => " + JSON.stringify(val));
                    }
                }

            });
        });
    }

    function displayStateTable(results) {
        var s = '<table class="table">';
        results.sort(function(a, b) {
            return a.method.name.localeCompare(b.method.name);
        });
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
        showSourceCode(c);
        showTransactForm();
        showCurrentState();
        addTx("using '" + c.get("name") + "' at " + wrapAddr(c.id));
    }

    function showSourceCode(c) {
        var tabName = c.get("name") + " " + trunc(c.id);

        if (Sandbox.Filer.get(tabName)) {
            return Sandbox.activateTab(tabName);
        }

        Sandbox.addFileTab(tabName, c.get("code"), true);
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

        	var editorSource = Contract.preprocess(Sandbox.getEditorSource());
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

    $(".trash").click(function(e) {
		$(".papertape .panel-body").empty();
    });

    shrinkify(".select_contract");
    shrinkify(".state");
    shrinkify(".papertape");
    shrinkify(".transact");

    Sandbox.showTxView = showTxView;
    Sandbox.accounts = [];

    $(function() {
        showTxView(); // default view
    });

})();
