module.exports = function(db){
    var express = require('express');
    var app = express();
    console.log("port is "+config.port);

    app.get('/', function(req, res){
        res.render("admin.ejs", {});
    });

    return app;
};
