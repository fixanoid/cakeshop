(function() {
    var Client = window.Client = {};

    Client.post = function(url, data) {
        if (!_.isString(data)) {
            data = JSON.stringify(data);
        }

        return $.ajax({
            url: url,
            method: "POST",
            dataType: "json",
            contentType: "application/json",
            data: data,
        });
    };

})();
