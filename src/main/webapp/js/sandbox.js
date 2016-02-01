
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var loadingFromGist = Sandbox.loadFromGist();

    // ----------------- editor ----------------------

    var SOL_CACHE_FILE_PREFIX = 'sol-cache-file-';
    var SOL_CACHE_UNTITLED = SOL_CACHE_FILE_PREFIX + 'Untitled';
    var SOL_CACHE_FILE = null;

    var editor = ace.edit("input");
    var session = editor.getSession();
    var Range = ace.require('ace/range').Range;
    var errMarkerId = null;

    var untitledCount = '';
    if (!getFiles().length || window.localStorage['sol-cache']) {
    	if(loadingFromGist) return;
    	// Backwards-compatibility
    	while (window.localStorage[SOL_CACHE_UNTITLED + untitledCount])
    		untitledCount = (untitledCount - 0) + 1;
    	SOL_CACHE_FILE = SOL_CACHE_UNTITLED + untitledCount;
    	window.localStorage[SOL_CACHE_FILE] = window.localStorage['sol-cache'] || BALLOT_EXAMPLE;
    	window.localStorage.removeItem('sol-cache');
    }

    SOL_CACHE_FILE = getFiles()[0];

    editor.setValue( window.localStorage[SOL_CACHE_FILE], -1);
    editor.resize(true);
    session.setMode("ace/mode/javascript");
    session.setTabSize(4);
    session.setUseSoftTabs(true);



    // ----------------- tabbed menu -------------------

    $('#options li').click(function(ev){
    	var $el = $(this);
    	var cls = /[a-z]+View/.exec( $el.get(0).className )[0];
    	if (!$el.hasClass('active')) {
    		$el.parent().find('li').removeClass('active');
    		$('#optionViews').attr('class', '').addClass(cls);
    		$el.addClass('active');
    	} else {
    		$el.removeClass('active');
    		$('#optionViews').removeClass(cls);
    	}
    });

    // ----------------- execution context -------------

    var $vmToggle = $('#vm');
    var $web3Toggle = $('#web3');
    var $web3endpoint = $('#web3Endpoint');

    if (typeof web3 !== 'undefined')
    {
    	if (web3.providers && web3.currentProvider instanceof web3.providers.IpcProvider)
    		$web3endpoint.val('ipc');
    	web3 = new Web3(web3.currentProvider);
    } else
    	web3 = new Web3(new Web3.providers.HttpProvider("http://localhost:8545"));

    var executionContext = 'vm';
    $vmToggle.get(0).checked = true;

    $vmToggle.on('change', executionContextChange );
    $web3Toggle.on('change', executionContextChange );
    $web3endpoint.on('change', function() {
    	var endpoint = $web3endpoint.val();
    	if (endpoint == 'ipc')
    		web3.setProvider(new Web3.providers.IpcProvider());
    	else
    		web3.setProvider(new Web3.providers.HttpProvider(endpoint));
    	compile();
    });

    function executionContextChange (ev) {
    	if (ev.target.value == 'web3' && !confirm("Are you sure you want to connect to a local ethereum node?") ) {
    		$vmToggle.get(0).checked = true;
    		executionContext = 'vm';
    	} else executionContext = ev.target.value;
    	compile();
    }



    // ------------------ gist publish --------------

    $('#gist').click(function(){
    	if (confirm("Are you sure you want to publish all your files anonymously as a public gist on github.com?")) {

    		var files = {};
    		var filesArr = getFiles();
    		var description = "Created using soleditor: Realtime Ethereum Contract Compiler and Runtime. Load this file by pasting this gists URL or ID at https://chriseth.github.io/browser-solidity/?gist=";

    		for(var f in filesArr) {
    			files[fileNameFromKey(filesArr[f])] = {
    				content: localStorage[filesArr[f]]
    			};
    		}

    		$.ajax({
    			url: 'https://api.github.com/gists',
    			type: 'POST',
    			data: JSON.stringify({
    				description: description,
    				public: true,
    				files: files
    			})
    		}).done(function(response) {
    			if (response.html_url && confirm("Created a gist at " + response.html_url + " Would you like to open it in a new window?")) {
    				window.open( response.html_url, '_blank' );
    			}
    		});
    	}
    });


    // ----------------- file selector-------------
    var $filesEl = $('#files');
    $filesEl.on('click','.newFile', function() {
    	while (window.localStorage[SOL_CACHE_UNTITLED + untitledCount])
    		untitledCount = (untitledCount - 0) + 1;
    	SOL_CACHE_FILE = SOL_CACHE_UNTITLED + untitledCount;
    	window.localStorage[SOL_CACHE_FILE] = '';
    	updateFiles();
    });

    $filesEl.on('click', '.file:not(.active)', showFileHandler);

    $filesEl.on('click', '.file.active', function(ev) {
    	var $fileTabEl = $(this);
    	var originalName = $fileTabEl.find('.name').text();
    	ev.preventDefault();
    	if ($(this).find('input').length > 0) return false;
    	var $fileNameInputEl = $('<input value="'+originalName+'"/>');
    	$fileTabEl.html($fileNameInputEl);
    	$fileNameInputEl.focus();
    	$fileNameInputEl.select();
    	$fileNameInputEl.on('blur', handleRename);
    	$fileNameInputEl.keyup(handleRename);

    	function handleRename(ev) {
    		ev.preventDefault();
    		if (ev.which && ev.which !== 13) return false;
    		var newName = ev.target.value;
    		$fileNameInputEl.off('blur');
    		$fileNameInputEl.off('keyup');

    		if (newName !== originalName && confirm("Are you sure you want to rename: " + originalName + " to " + newName + '?')) {
    			var content = window.localStorage.getItem( fileKey(originalName) );
    			window.localStorage[fileKey( newName )] = content;
    			window.localStorage.removeItem( fileKey( originalName) );
    			SOL_CACHE_FILE = fileKey( newName );
    		}

    		updateFiles();
    		return false;
    	}

    	return false;
    });

    $filesEl.on('click', '.file .remove', function(ev) {
    	ev.preventDefault();
    	var name = $(this).parent().find('.name').text();
    	var index = getFiles().indexOf( fileKey(name) );

    	if (confirm("Are you sure you want to remove: " + name + " from local storage?")) {
    		window.localStorage.removeItem( fileKey( name ) );
    		SOL_CACHE_FILE = getFiles()[ Math.max(0, index - 1)];
    		updateFiles();
    	}
    	return false;
    });

    function showFileHandler(ev) {
    	ev.preventDefault();
    	SOL_CACHE_FILE = fileKey( $(this).find('.name').text() );
    	updateFiles();
    	return false;
    }

    function fileTabFromKey(key) {
    	var name = fileNameFromKey(key);
    	return $('#files .file').filter(function(){ return $(this).find('.name').text() == name; });
    }


    function updateFiles() {
    	var $filesEl = $('#files');
    	var files = getFiles();

    	$filesEl.find('.file').remove();

    	for (var f in files) {
    		$filesEl.append(fileTabTemplate(files[f]));
    	}

    	if (SOL_CACHE_FILE) {
    		var active = fileTabFromKey(SOL_CACHE_FILE);
    		active.addClass('active');
    		editor.setValue( window.localStorage[SOL_CACHE_FILE] || '', -1);
    		editor.focus();
    	}
    	$('#input').toggle( !!SOL_CACHE_FILE );
    	$('#output').toggle( !!SOL_CACHE_FILE );
    }

    function fileTabTemplate(key) {
    	var name = fileNameFromKey(key);
    	return $('<span class="file"><span class="name">'+name+'</span><span class="remove"><i class="fa fa-close"></i></span></span>');
    }

    function fileKey( name ) {
    	return SOL_CACHE_FILE_PREFIX + name;
    }

    function fileNameFromKey(key) {
    	return key.replace( SOL_CACHE_FILE_PREFIX, '' );
    }

    function getFiles() {
    	var files = [];
    	for (var f in localStorage ) {
    		if (f.indexOf( SOL_CACHE_FILE_PREFIX, 0 ) === 0) {
    			files.push(f);
    		}
    	}
    	return files;
    }

    updateFiles();

    // export methods
    Sandbox.fileKey = fileKey;
    Sandbox.updateFiles = updateFiles;
    Sandbox.getFiles = getFiles;

    // ----------------- resizeable ui ---------------

    var EDITOR_SIZE_CACHE_KEY = "editor-size-cache";
    var dragging = false;
    $('#dragbar').mousedown(function(e){
    	e.preventDefault();
    	dragging = true;
    	var main = $('#righthand-panel');
    	var ghostbar = $('<div id="ghostbar">', {
    		css: {
    			top: main.offset().top,
    			left: main.offset().left
    		}
    	}).prependTo('body');

    	$(document).mousemove(function(e){
    		ghostbar.css("left",e.pageX+2);
    	});
    });

    var $body = $('body');

    function setEditorSize (delta) {
    	$('#righthand-panel').css("width", delta);
    	$('#editor').css("right", delta);
    	onResize();
    }

    function getEditorSize(){
    	window.localStorage[EDITOR_SIZE_CACHE_KEY] = $('#righthand-panel').width();
    }

    $(document).mouseup(function(e){
    	if (dragging) {
    		var delta = $body.width() - e.pageX+2;
    		$('#ghostbar').remove();
    		$(document).unbind('mousemove');
    		dragging = false;
    		setEditorSize(delta);
    		window.localStorage.setItem(EDITOR_SIZE_CACHE_KEY, delta);
    	}
    });

    // set cached defaults
    var cachedSize = window.localStorage.getItem(EDITOR_SIZE_CACHE_KEY);
    if (cachedSize) setEditorSize(cachedSize);
    else getEditorSize();


    // ----------------- toggle right hand panel -----------------

    var hidingRHP = false;
    $('.toggleRHP').click(function(){
       hidingRHP = !hidingRHP;
       setEditorSize( hidingRHP ? 0 : window.localStorage[EDITOR_SIZE_CACHE_KEY] );
       $('.toggleRHP').toggleClass('hiding', hidingRHP);
       if (!hidingRHP) compile();
    });


    // ----------------- editor resize ---------------

    function onResize() {
    	editor.resize();
    	session.setUseWrapMode(document.querySelector('#editorWrap').checked);
    	if(session.getUseWrapMode()) {
    		var characterWidth = editor.renderer.characterWidth;
    		var contentWidth = editor.container.ownerDocument.getElementsByClassName("ace_scroller")[0].clientWidth;

    		if(contentWidth > 0) {
    			session.setWrapLimit(parseInt(contentWidth / characterWidth, 10));
    		}
    	}
    }
    window.onresize = onResize;
    onResize();

    document.querySelector('#editor').addEventListener('change', onResize);
    document.querySelector('#editorWrap').addEventListener('change', onResize);


    // ----------------- compiler ----------------------
    var compileJSON;
    var compilerAcceptsMultipleFiles;

    var previousInput = '';
    var sourceAnnotations = [];
    var compile = function() {
    	editor.getSession().clearAnnotations();
    	sourceAnnotations = [];
    	editor.getSession().removeMarker(errMarkerId);
    	$('#output').empty(); // clear output window

    	var editorSource = editor.getValue();
    	window.localStorage.setItem(SOL_CACHE_FILE, editorSource);

    	var files = {};
    	files[fileNameFromKey(SOL_CACHE_FILE)] = editorSource;
    	var input = gatherImports(files, compile);
    	if (!input) {
            return;
        }
    	var optimize = document.querySelector('#optimize').checked ? 1 : 0;

        Contract.compile(input, optimize, compilationFinished);
    };

    var compilationFinished = function(data) {
    	var noFatalErrors = true; // ie warnings are ok

        if (data.urlRoot === undefined) {
        	if (data.error !== undefined) {
        		renderError(data.error);
        		if (errortype(data.error) !== 'warning') noFatalErrors = false;
        	}
        	if (data.errors !== undefined) {
        		$.each(data.errors, function(i, err) {
        			renderError(err);
        			if (errortype(err) !== 'warning') noFatalErrors = false;
        		});
        	}
        }

    	if (noFatalErrors && !hidingRHP) {
            renderContracts(data, editor.getValue());
        }

    };

    var compileTimeout = null;
    var onChange = function() {
    	var input = editor.getValue();
    	if (input === "") {
    		window.localStorage.setItem(SOL_CACHE_FILE, '');
    		return;
    	}
    	if (input === previousInput)
    		return;
    	previousInput = input;
    	if (compileTimeout) window.clearTimeout(compileTimeout);
    	compileTimeout = window.setTimeout(compile, 300);
    };

    var cachedRemoteFiles = {};
    function gatherImports(files, asyncCallback, needAsync) {
    	if (!compilerAcceptsMultipleFiles)
    		return files[fileNameFromKey(SOL_CACHE_FILE)];
    	var importRegex = /import\s[\'\"]([^\'\"]+)[\'\"];/g;
    	var reloop = false;
    	do {
    		reloop = false;
    		for (var fileName in files) {
    			var match;
    			while (match = importRegex.exec(files[fileName])) {
    				var m = match[1];
    				if (m in files) continue;
    				if (getFiles().indexOf(fileKey(m)) !== -1) {
    					files[m] = window.localStorage[fileKey(match[1])];
    					reloop = true;
    				} else if (m in cachedRemoteFiles) {
    					files[m] = cachedRemoteFiles[m];
    					reloop = true;
    				} else if (githubMatch = /^(https?:\/\/)?(www.)?github.com\/([^\/]*\/[^\/]*)\/(.*)/.exec(m)) {
    					$.getJSON('https://api.github.com/repos/' + githubMatch[3] + '/contents/' + githubMatch[4], function(result) {
    						var content;
    						if ('content' in result)
    							content = Base64.decode(result.content);
    						else
    							content = "\"" + m + "\" NOT FOUND"; //@TODO handle this better
    						cachedRemoteFiles[m] = content;
    						files[m] = content;
    						gatherImports(files, asyncCallback, true);
    					}).fail(function(){
    						var content = "\"" + m + "\" NOT FOUND"; //@TODO handle this better
    						cachedRemoteFiles[m] = content;
    						files[m] = content;
    						gatherImports(files, asyncCallback, true);
    					});
    					return null;
    				}
    			}
    		}
    	} while (reloop);
    	var input = JSON.stringify({'sources':files});
    	if (needAsync)
    		asyncCallback(input);
    	return input;
    }

    editor.getSession().on('change', onChange);

    document.querySelector('#optimize').addEventListener('change', compile);

    // ----------------- compiler output renderer ----------------------
    var detailsOpen = {};

    function errortype(message) {
    	return message.match(/^.*:[0-9]*:[0-9]* Warning: /) ? 'warning' : 'error';
    }

    var renderError = function(message) {
    	var type = errortype(message);
    	var $pre = $("<pre />").text(message);
    	var $error = $('<div class="sol ' + type + '"><div class="close"><i class="fa fa-close"></i></div></div>').prepend($pre);
    	$('#output').append( $error );
    	var err = message.match(/^([^:]*):([0-9]*):(([0-9]*):)? /);
    	if (err) {
    		var errFile = err[1];
    		var errLine = parseInt(err[2], 10) - 1;
    		var errCol = err[4] ? parseInt(err[4], 10) : 0;
    		if (errFile === '' || errFile === fileNameFromKey(SOL_CACHE_FILE)) {
    			sourceAnnotations[sourceAnnotations.length] = {
    				row: errLine,
    				column: errCol,
    				text: message,
    				type: type
    			};
    			editor.getSession().setAnnotations(sourceAnnotations);
    		}
    		$error.click(function(ev){
    			if (errFile !== '' && errFile !== fileNameFromKey(SOL_CACHE_FILE) && getFiles().indexOf(fileKey(errFile)) !== -1) {
    				// Switch to file
    				SOL_CACHE_FILE = fileKey(errFile);
    				updateFiles();
    				//@TODO could show some error icon in files with errors
    			}
    			editor.focus();
    			editor.gotoLine(errLine + 1, errCol - 1, true);
    		});
    		$error.find('.close').click(function(ev){
    			ev.preventDefault();
    			$error.remove();
    			return false;
    		});
    	}
    };

    var gethDeploy = function(contractName, interface, bytecode){
        var abi = _.isString(interface) ? JSON.parse(interface) : interface;
    	var funABI = getConstructorInterface(interface);

    	var code = "";
    	$.each(funABI.inputs, function(i, inp) {
    		code += "var " + inp.name + " = /* var of type " + inp.type + " here */ ;\n";
    	});

    	code += "var " + contractName + "Contract = web3.eth.contract(" + interface.replace("\n","") + ");" +
            "\nvar " + contractName + " = " + contractName + "Contract.new(";

    	$.each(funABI.inputs, function(i, inp) {
    		code += "\n   " + inp.name + ",";
    	});

    	code += "\n   {"+
    	"\n     from: web3.eth.accounts[0], "+
    	"\n     data: '"+bytecode+"', "+
    	"\n     gas: 3000000"+
    	"\n   }, function(e, contract){"+
    	"\n    console.log(e, contract);"+
    	"\n    if (typeof contract.address != 'undefined') {"+
    	"\n         console.log('Contract mined! address: ' + contract.address + ' transactionHash: ' + contract.transactionHash);" +
    	"\n    }" +
    	"\n })";


    	return code;
    };

    var combined = function(contractName, interface, bytecode){
    	return JSON.stringify([{name: contractName, interface: interface, bytecode: bytecode}]);
    };

    var renderContracts = function(contracts, source) {
    	var udappContracts = [];
        contracts.forEach(function(contract) {
    		udappContracts.push({
                name:      contract.get("name"),
                interface: contract.get("abi"),
                bytecode:  contract.get("binary")
    		});

        });

    	var dapp = new UniversalDApp(udappContracts, {
    		vm: executionContext === 'vm',
    		removable: false,
    		getAddress: function(){ return $('#txorigin').val(); },
    		removable_instances: true,
    		renderOutputModifier: function(contractName, $contractOutput) {
    			var contract = _.find(contracts, function(c) { return c.get("name").toLowerCase() === contractName.toLowerCase(); });
    			return $contractOutput
    				.append(textRow('Bytecode', contract.get("binary")))
    				.append(textRow('Interface', contract.get("abi")))
    				.append(textRow('Web3 deploy', gethDeploy(contractName.toLowerCase(), contract.get("abi"), contract.get("binary")), 'deploy'))
    				.append(textRow('uDApp', combined(contractName, contract.get("abi"), contract.get("binary")), 'deploy'))
    				.append(getDetails(contract, source, contractName));
    		}});
    	var $contractOutput = dapp.render();


    	$txOrigin = $('#txorigin');
    	if (executionContext === 'vm') {
    		$txOrigin.empty();
    		var addr = '0x' + dapp.address.toString('hex');
    		$txOrigin.val(addr);
    		$txOrigin.append($('<option />').val(addr).text(addr));
    	} else web3.eth.getAccounts(function(err, accounts) {
    		if (err)
    			renderError(err.message);
    		if (accounts && accounts[0]){
    			$txOrigin.empty();
    			for( var a in accounts) { $txOrigin.append($('<option />').val(accounts[a]).text(accounts[a])); }
    			$txOrigin.val(accounts[0]);
    		} else $txOrigin.val('unknown');
    	});

    	$contractOutput.find('.title').click(function(ev){ $(this).closest('.contract').toggleClass('hide'); });
    	$('#output').append( $contractOutput );
    	$('.col2 input,textarea').click(function() { this.select(); });

    }; // renderContracts

    var tableRowItems = function(first, second, cls) {
    	return $('<div class="row"/>')
    		.addClass(cls)
    		.append($('<div class="col1">').append(first))
    		.append($('<div class="col2">').append(second));
    };
    var tableRow = function(description, data) {
    	return tableRowItems(
    		$('<span/>').text(description),
    		$('<input readonly="readonly"/>').val(data));
    };
    var textRow = function(description, data, cls) {
    	return tableRowItems(
    		$('<strong/>').text(description),
    		$('<textarea readonly="readonly" class="gethDeployText"/>').val(data),
    		cls);
    };
    var getDetails = function(contract, source, contractName) {
    	var button = $('<button>Toggle Details</button>');
    	var details = $('<div style="display: none;"/>')
    		.append(tableRow('Solidity Interface', contract.solidity_interface))
    		.append(tableRow('Opcodes', contract.opcodes));
    	var funHashes = '';
    	for (var fun in contract.functionHashes)
    		funHashes += contract.functionHashes[fun] + ' ' + fun + '\n';
    	details.append($('<span class="col1">Functions</span>'));
    	details.append($('<pre/>').text(funHashes));
    	details.append($('<span class="col1">Gas Estimates</span>'));
    	details.append($('<pre/>').text(formatGasEstimates(contract.gasEstimates)));
    	if (contract.runtimeBytecode && contract.runtimeBytecode.length > 0)
    		details.append(tableRow('Runtime Bytecode', contract.runtimeBytecode));
    	if (contract.assembly !== null)
    	{
    		details.append($('<span class="col1">Assembly</span>'));
    		var assembly = $('<pre/>').text(formatAssemblyText(contract.assembly, '', source));
    		details.append(assembly);
    	}
    	button.click(function() { detailsOpen[contractName] = !detailsOpen[contractName]; details.toggle(); });
    	if (detailsOpen[contractName])
    		details.show();
    	return $('<div class="contractDetails"/>').append(button).append(details);
    };
    var formatGasEstimates = function(data) {
        if (_.isNull(data) || _.isUndefined(data)) {
            return "";
        }
    	var gasToText = function(g) { return g === null ? 'unknown' : g; };
    	var text = '';
    	if ('creation' in data)
    		text += 'Creation: ' + gasToText(data.creation[0]) + ' + ' + gasToText(data.creation[1]) + '\n';
    	text += 'External:\n';
    	for (var fun in data.external) {
    		text += '  ' + fun + ': ' + gasToText(data.external[fun]) + '\n';
        }
    	text += 'Internal:\n';
    	for (fun in data.internal) {
    		text += '  ' + fun + ': ' + gasToText(data.internal[fun]) + '\n';
        }
    	return text;
    };
    var formatAssemblyText = function(asm, prefix, source) {
    	if (typeof(asm) == typeof('') || asm === null || asm === undefined)
    		return prefix + asm + '\n';
    	var text = prefix + '.code\n';
    	$.each(asm['.code'], function(i, item) {
    		var v = item.value === undefined ? '' : item.value;
    		var src = '';
    		if (item.begin !== undefined && item.end !== undefined)
    			src = source.slice(item.begin, item.end).replace('\n', '\\n', 'g');
    		if (src.length > 30)
    			src = src.slice(0, 30) + '...';
    		if (item.name != 'tag')
    			text += '  ';
    		text += prefix + item.name + ' ' + v + '\t\t\t' + src +  '\n';
    	});
    	text += prefix + '.data\n';
    	if (asm['.data'])
    		$.each(asm['.data'], function(i, item) {
    			text += '  ' + prefix + '' + i + ':\n';
    			text += formatAssemblyText(item, prefix + '    ', source);
    		});

    	return text;
    };

    $('.asmOutput button').click(function() {$(this).parent().find('pre').toggle(); });

    var getConstructorInterface = function(abi) {
    	var funABI = {'name':'','inputs':[],'type':'constructor','outputs':[]};
    	for (var i = 0; i < abi.length; i++)
    		if (abi[i].type == 'constructor') {
    			funABI.inputs = abi[i].inputs || [];
    			break;
    		}
    	return funABI;
    };

    // compile on page load (whatever is in buffer)
    $(function() {
        onChange();
    });

})();
