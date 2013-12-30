
var express = require('express');
var dsbudget = require('../dsbudget');

module.exports = function(db){
    var app = express();
    app.get('/', function(req, res){
        var pagemodel = new dsbudget.PageModel(db);
        pagemodel.findAll(function(err, pages) {
            res.render("index.ejs", {pages: pages});
        });
    });
    return app;
};
