
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    Sandbox.getActiveSidebarTab = function() {
        return getTabName($("ul.sidenav li.active"));
    };

    function getTabName(tab) {
        var classes = tab.attr("class").split(/ /);
        if (classes.length > 1) {
            classes = _.reject(classes, function(c) { return c === "active"; });
        }
        return classes[0];
    }

    $(".sidenav li a").click(function(e) {
        var tab = getTabName($(e.target).parents("li"));

        $(".sidenav li").removeClass("active");
        $(".sidenav li."+tab).addClass("active");
        $(".sidebar .tab").hide();
        $(".sidebar #"+tab).show();

        if (tab === "txView") {
            Sandbox.showTxView();
        }

    });

})();
