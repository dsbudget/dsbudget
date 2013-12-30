#!/bin/env node
var http = require('http');
var path = require('path');
var express = require('express');
var mongo = require('mongodb');

//store config on global space
config = require('./config.json');


var app = express();
app.set('view engine', 'ejs');
app.set('views', __dirname + '/views');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use("/public", express.static(__dirname + '/public'));

if(process.env.OPENSHIFT_NODEJS_PORT !== undefined) {
    //on openshift. override port/host
    console.dir(process.env); //https://www.openshift.com/page/openshift-environment-variables
    config.port = process.env.OPENSHIFT_NODEJS_PORT;
    config.host = process.env.OPENSHIFT_NODEJS_IP;
    //https://dl.dropboxusercontent.com/u/61433005/Web%20Socket%20and%20Http%20routing%20on%20OpenShift.png
    config.socket_url = process.env.OPENSHIFT_APP_DNS+":8443";

    config.mongo_db = process.env.OPENSHIFT_APP_NAME;
    config.mongo_host = process.env.OPENSHIFT_MONGODB_DB_HOST;
    config.mongo_port = process.env.OPENSHIFT_MONGODB_DB_PORT;
    config.mongo_user = process.env.OPENSHIFT_MONGODB_DB_USER;
    config.mongo_pass = process.env.OPENSHIFT_MONGODB_DB_PASSWORD;
} else {
    //assume development
    app.use(express.errorHandler());
}

var server = http.createServer(app);
var io = require('socket.io').listen(server);
io.sockets.on('connection', function (socket) {
    console.log('connected');
    setInterval(function() {
        socket.emit('news', { time: new Date().getTime() });
    }, 1000);
    socket.on('my other event', function (data) {
        console.log(data);
    });
});

var mongo_server = new mongo.Server(config.mongo_host, config.mongo_port, {auto_reconnect: true});
var db = new mongo.Db(config.mongo_db, mongo_server);
app.get('/', require('./routes/index')(db));
app.get('/admin', require('./routes/admin')(db));

//store db on global
db.open(function(err, db) {
    if(err) {
        console.log("failed to connect to mongodb");
        throw err;
    }
    server.listen(config.port, config.host, function(){
        console.log('Express server listening on host ' + config.host);
    });
});
