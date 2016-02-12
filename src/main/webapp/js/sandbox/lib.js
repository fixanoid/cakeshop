
(function() {

    var Sandbox = window.Sandbox = window.Sandbox || {};

    Sandbox.loadContract = function(name) {
        var url = "js/sandbox/contracts/" + name;
        return new Promise(function(resolve, reject) {
            $.get(url).done(function(data) {
                resolve(data);
            });
        });
    };

})();
