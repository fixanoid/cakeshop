
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    var SOL_CACHE_FILE_PREFIX = 'sol-cache-file-';
    var SOL_CACHE_UNTITLED = SOL_CACHE_FILE_PREFIX + 'Untitled';
    Sandbox.SOL_CACHE_FILE = null;

    var editor;
    var untitledCount = '';

    function initFileTabs() {
        editor = Sandbox.editor;

        var loadingFromGist = Sandbox.loadFromGist();

        // ------ load default file ------------------------
        if (!getFiles().length || window.localStorage['sol-cache']) {
        	if(loadingFromGist) return;
        	// Backwards-compatibility
        	while (window.localStorage[SOL_CACHE_UNTITLED + untitledCount])
        		untitledCount = (untitledCount - 0) + 1;
        	Sandbox.SOL_CACHE_FILE = SOL_CACHE_UNTITLED + untitledCount;
        	window.localStorage[Sandbox.SOL_CACHE_FILE] = window.localStorage['sol-cache'] || BALLOT_EXAMPLE;
        	window.localStorage.removeItem('sol-cache');
        }

        Sandbox.SOL_CACHE_FILE = getFiles()[0];

        editor.setValue( window.localStorage[Sandbox.SOL_CACHE_FILE], -1);


        // ----------------- file selector-------------
        var $filesEl = $('ul.filetabs');
        $filesEl.on('click','.new_file', function() {
        	while (window.localStorage[SOL_CACHE_UNTITLED + untitledCount])
        		untitledCount = (untitledCount - 0) + 1;
        	Sandbox.SOL_CACHE_FILE = SOL_CACHE_UNTITLED + untitledCount;
        	window.localStorage[Sandbox.SOL_CACHE_FILE] = '';
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
        			Sandbox.SOL_CACHE_FILE = fileKey( newName );
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
        		Sandbox.SOL_CACHE_FILE = getFiles()[ Math.max(0, index - 1)];
        		updateFiles();
        	}
        	return false;
        });

        updateFiles();
    }

    function showFileHandler(ev) {
    	ev.preventDefault();
    	Sandbox.SOL_CACHE_FILE = fileKey( $(this).find('.name').text() );
    	updateFiles();
    	return false;
    }

    function fileTabFromKey(key) {
    	var name = fileNameFromKey(key);
    	return $('.filetabs li').filter(function(){ return $(this).find('.name').text() === name; });
    }

    function updateFiles() {
    	var files = getFiles();

    	$(".filetabs li.file").remove();
    	for (var f in files) {
    		$(".filetabs").append(fileTabTemplate(files[f]));
    	}

    	if (Sandbox.SOL_CACHE_FILE) {
    		var active = fileTabFromKey(Sandbox.SOL_CACHE_FILE);
    		active.addClass('active');
    		editor.setValue( window.localStorage[Sandbox.SOL_CACHE_FILE] || '', -1);
    		editor.focus();
    	}
    	$('#editor_input').toggle( !!Sandbox.SOL_CACHE_FILE );
    	$('#output').toggle( !!Sandbox.SOL_CACHE_FILE );
    }

    function fileTabTemplate(key) {
    	var name = fileNameFromKey(key);
        var t = '<li role="presentation" class="file"><a role="tab" data-toggle="tab"><span class="name">' + name + '</span> <span class="remove"><i class="fa fa-close red"></i></span></a></li>';
    	return $(t);
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

    // updateFiles();

    // export methods
    Sandbox.fileNameFromKey = fileNameFromKey;
    Sandbox.fileKey = fileKey;
    Sandbox.updateFiles = updateFiles;
    Sandbox.getFiles = getFiles;
    Sandbox.initFileTabs = initFileTabs;

})();
