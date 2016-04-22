
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

        var fs = $(".container-fs");
        function resizeEditorHeight() {
            var editorHeight = fs.height() - $(".filetabs").height() - 5;
            $("#editor_input").height(editorHeight);
        	editor.resize(true);
        }

        $(window).resize(function(e) {
            if (e.target !== window) {
                return;
            }
            resizeEditorHeight();
        });
        resizeEditorHeight();

        return editor;
    };

})();
