
(function() {

    $(".sidenav li a").click(function(e) {

        var classes = $(e.target).parents("li").attr("class").split(/ /);
        var tab;
        if (classes.length > 1) {
            classes = _.reject(classes, function(c) { return c === "active"; });
        }
        tab = classes[0];

        // console.log("click tab", tab);
        $(".sidenav li").removeClass("active");
        $(".sidenav li."+tab).addClass("active");
        $(".sidebar .tab").hide();
        $(".sidebar #"+tab).show();

    });

})();
