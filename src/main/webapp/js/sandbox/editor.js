
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    Sandbox.initEditor = function() {

        var editor = ace.edit("editor_input");
        var session = editor.getSession();
        var Range = ace.require('ace/range').Range;

        editor.resize(true);
        session.setMode("ace/mode/javascript");
        session.setTabSize(2);
        session.setUseSoftTabs(true);

        var maxWidth;
        var editorHeight;

        function updateSizing() {
            maxWidth = $(".container-fs").width()-400;
            editorHeight = $(".container-fs").height() - $(".filetabs").height() - 5;
            $("#editor_input").height(editorHeight);
        	editor.resize();
            $(".sidebar .tab").height(editorHeight-10);
        }
        updateSizing();

        // set default widths
        var fs = $(".container-fs").width();
        $("#editor").width(fs*0.7);
        $("#sidebar").width(fs*0.3-7);

        $(window).resize(function(e) {
            updateSizing();
          	if (this === e.target) {
        		$('.sidebar').removeAttr('style');
            }
        });

        $("#editor").resizable({
            handles: "e",
        	minWidth: 200,
            maxWidth: maxWidth,

            resize: function(event, ui) {
                var x=ui.element.outerWidth();
                // var y=ui.element.outerHeight();
                var ele=ui.element;
                var factor = $(this).parent().width()-x;
                var f2 = $(this).parent().width() * 0.02999;
                $.each(ele.siblings(), function(idx, item) {
                    //ele.siblings().eq(idx).css('height',y+'px');
                    //ele.siblings().eq(idx).css('width',(factor-41)+'px');
                    ele.siblings().eq(idx).width((factor-f2)+'px');
                });
            }

        });

        return editor;
    };



})();
