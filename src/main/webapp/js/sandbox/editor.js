
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    Sandbox.initEditor = function() {

        var editor = ace.edit("editor_input");
        var session = editor.getSession();
        var Range = ace.require('ace/range').Range;

        editor.$blockScrolling = Infinity;
        session.setMode("ace/mode/javascript");
        session.setTabSize(2);
        session.setUseSoftTabs(true);

        var fs = $(".container-fs"),
            ed = $("#editor"),
            sb = $("#sidebar");

        var SIDEBAR_MIN_WIDTH = 330;

        function resizeEditor(width) {
            // set editor div width
            width = width || (fs.width() - sb.width() - 30);
            ed.width(width);
            ed.resizable("option", { maxWidth: fs.width() - SIDEBAR_MIN_WIDTH - 10 });

            // set Ace editor height
            var editorHeight = fs.height() - $(".filetabs").height() - 5;
            $("#editor_input").height(editorHeight);
        	editor.resize(true);
            $(".sidebar .tab").height(editorHeight - 10);
        }

        function resizeSidebar() {
            var newWidth = fs.width() - ed.width();
            if (newWidth < SIDEBAR_MIN_WIDTH) {
                newWidth = SIDEBAR_MIN_WIDTH;
            }
            sb.width(newWidth - 30); // 30px room for paddings
        }

        $(window).resize(function(e) {
            if (e.target !== window) {
                return;
            }
            resizeEditor();
            resizeSidebar();
        });

        ed.resizable({
            handles: "e",
        	minWidth: 500,
            resize: resizeSidebar, // expand sidebar based on new editor width
        });

        // set default widths
        resizeEditor(fs.width() * 0.7); // set a default width to 70%
        resizeSidebar();

        return editor;
    };



})();
