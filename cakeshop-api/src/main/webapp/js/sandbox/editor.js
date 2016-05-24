
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
        var nav = $("ul.nav");
        function resizeEditorHeight() {

            // resize editor pane
            var editorHeight = fs.height() - $(".filetabs").height() - 5;
            $("#editor_input").height(editorHeight);
        	editor.resize(true);

            var wh = window.innerHeight;
            var heading = $(".accounts .panel-heading").outerHeight(true);

            // resize col2 panels
            var c2 = wh - $(".col2").position().top - $(".panel.transact").position().top - 60 - 1; // 1px extra buffer (round down)
            $(".panel.transact .panel-overflow").css("max-height", c2);

            // resize col3 panels
            var h = wh - $(".col3").position().top - heading*3 - 40 ; // 40px padding
            // h = real usable height for panels
            var pcts = h < 800 ? [0.3, 0.3, 0.3] : [0.2, 0.4, 0.4];
            $(".accounts .panel-overflow").css("max-height", h*pcts[0]);
            $(".state .panel-overflow").css("max-height", h*pcts[1]);
            $(".papertape .panel-body").css("max-height", h*pcts[2]);
        }

        $(window).resize(function(e) {
            if (e.target !== window) {
                return;
            }
            resizeEditorHeight();
        });

        Sandbox.on("col3-reflow", resizeEditorHeight);

        $(function() {
            resizeEditorHeight();
        });

        return editor;
    };

})();
