#!/bin/env node
var http = require('http');
var path = require('path');
var assert = require('assert');
var crypto = require('crypto');

var express = require('express');
var mongo = require('mongodb');
var async = require('async');
var sass = require('node-sass');
var passport = require('passport'), 
    GoogleStrategy = require('passport-google').Strategy,
    LocalStrategy = require('passport-local').Strategy;
var flash = require('connect-flash');

/*
var MemoryStore = express.session.MemoryStore,
    sessionStore = new MemoryStore();
*/

console.log("dumping env");
console.dir(process.env);

var now = new Date().getTime();

//store config on global space
config = require('./config.json');

var app = express();
var server = http.createServer(app);
app.configure(function() {
    app.set('view engine', 'ejs');
    app.set('views', __dirname + '/views');
    app.use(sass.middleware({
        src: __dirname, //look for /public/*.scss
        dest: __dirname, //and put compiled in /public/*.css
        debug: true // obvious
    }));
    app.use("/public", express.static(__dirname + '/public'));

    app.use(express.cookieParser());
    app.use(express.favicon());
    app.use(express.bodyParser());
    app.use(express.methodOverride());
    app.use(express.session({ /*store: sessionStore,*/ secret: config.cookie_secret }));

    app.use(passport.initialize());
    app.use(passport.session());
    app.use(flash());

    app.use(app.router);
    app.use(express.errorHandler());
    app.use(express.logger());
});

if(process.env.OPENSHIFT_NODEJS_PORT !== undefined) {
    console.log("seems to be running on openshift");
    //on openshift. override port/host
    config.port = process.env.OPENSHIFT_NODEJS_PORT;
    config.host = process.env.OPENSHIFT_NODEJS_IP;
    //https://dl.dropboxusercontent.com/u/61433005/Web%20Socket%20and%20Http%20routing%20on%20OpenShift.png
    config.socket_url = process.env.OPENSHIFT_APP_DNS+":8443";
    config.mongo_url = process.env.OPENSHIFT_MONGODB_DB_URL + process.env.OPENSHIFT_APP_NAME;
    config.app_url = "https://"+config.host+":"+config.port;
} 

if(process.env.HEROKU) {
    console.log("seems to be running on heroku");
    config.mongo_url = process.env.MONGOLAB_URI;
    config.port = process.env.PORT;
    config.app_url = 'https://dsbudget.herokuapp.com'; 
    config.socket_url = 'dsbudget.herokuapp.com:443';
}

var io = require('socket.io').listen(server);
/*
//share session with express (http://stackoverflow.com/questions/15093018/sessions-with-express-js-passport-js)
io.configure(function (){
    io.set("authorization", passport.authorize({
        key:    'express.sid',       //the cookie where express (or connect) stores its session id.
        secret: config.cookie_secret, //the session secret to parse the cookie
        store:   sessionStore,     //the session store that express uses
        fail: function(data, accept) {
            // console.log("failed");
            // console.log(data);// *optional* callbacks on success or fail
            accept(null, false);             // second param takes boolean on whether or not to allow handshake
        },
        success: function(data, accept) {
          //  console.log("success socket.io auth");
         //   console.log(data);
            accept(null, true);
        }
    }));
});
*/
io.sockets.on('connection', function (socket) {
    console.log('connected');
    setInterval(function() {
        socket.emit('news', { time: new Date().getTime() });
    }, 1000);
    socket.on('my other event', function (data) {
        console.log(data);
    });
});

function hashpassword(pass, salt, callback) {
    crypto.pbkdf2(pass, salt, 10000, 512, function(err, hash) {
        if(err) {
            callback(err);
        } else {
            callback(null, hash.toString());
        }
    });
}

//most of initialization happens after we connect to db.
mongo.MongoClient.connect(config.mongo_url, function(err, db) {
    if(err) throw err;

    var model = require('./model').init(db);

    app.get('/', function(req,res) {
        if(req.user) {
            res.redirect('/page');  //jumpt to where we want to go
        } else {
            res.redirect('/about');  //jumpt to where we want to go
        }
    });
    app.get('/about', function(req,res) {
        res.render('about.ejs', {req: req, menu: "about"});
    });
    app.get('/auth', function(req, res){
        res.render("auth.ejs", {req: req, menu: "login"});
    });
    app.get('/auth/logout', function(req, res){
        req.logout();
        res.redirect('/'); 
    });
    app.get('/auth/error', function(req, res){
        res.render("auth-error.ejs", {req: req, menu: "login"});
    });
    app.get('/signup', function(req, res){
        res.render("signup.ejs", {req: req, menu: "signup"});
    });
    app.get('/setpass', function(req, res){
        if(req.user) {
            res.render("setpass.ejs", {req: req, menu: "signup"});
        }
    });
    app.post('/setpass', function(req, res) {
        if(req.user) {
            if(req.body.password != req.body.password_confirm) {
                req.flash('error', "Password doesn't match. Please enter again.");
                res.redirect('/setpass');
            } else {
                model.User.findByEmail(req.body.email, function(err, rec) {
                    if(rec && rec._id.toString() != req.user._id.toString()) {
                        req.flash('error', "Sorry, the email address is already registered. Please a choose different one.");
                        res.redirect('/setpass');
                    } else {
                        var salt = crypto.randomBytes(128).toString('base64');
                        hashpassword(req.body.password, salt, function(err, hash) {
                            model.User.update(req.user._id, {
                                password_salt: salt,
                                password: hash,
                                email: req.body.email, 
                                name: req.body.name
                            }, function(err) {
                                if(err) {
                                    req.flash('error', "Sorry, failed to update your record. Please contact dsBudget support.");
                                    res.statusCode = 500;
                                    res.redirect('/setpass');
                                } else {
                                    res.statusCode = 200;
                                    res.redirect('/');
                                }
                            });
                        });
                    }
                });
            }
        }
    });

    passport.use(new GoogleStrategy({
        returnURL: config.app_url+'/auth/google/return',
            realm: config.app_url+'/'
        },
        function(openid, profile, done) {
            model.User.findByGoogleID(openid, function(err, user) {
                if(err) {       
                    console.log(err);
                    done(err);
                } else {
                    if(user) {
                        //welcome back
                        //console.dir(user); 
                        done(err, user);
                    } else {
                        //new user - create account (let user reset user/pass later)
                        console.log("creating new user account for "+profile.displayName);
                        model.User.create({
                            admin: false,
                            googleid: openid, 
                            name: profile.displayName, 
                            email: profile.emails[0].value
                        }, function(err, id) {
                            //lookup newly created account
                            model.User.findByID(id, function(err, user) {
                                if(err) throw err; //really?
                                done(null, user);
                            });
                        });
                    }
                }
            });
        }
    ));

    passport.use(new LocalStrategy({
            usernameField: 'email',
            passwordField: 'password'
        },
        function(email, password, done) {
            //console.log("passport-local called");
            model.User.findByEmail(email, function(err, user) {
                if(err) throw err; //really?
                if(user) {
                    hashpassword(password, user.password_salt, function(err, hash) {
                        //console.log("salt:"+user.password_salt);
                        //console.log("hash:"+hash);
                        if(user.password != hash) {
                            return done(null, false, { message: 'Incorrect password.' });
                        } else {
                            return done(null, user);
                        }
                    });
                } else {
                    return done(null, false, { message: 'Incorrect email.' });
                }
            });
        }
    ));
    app.post('/auth/login', passport.authenticate('local', { 
        successRedirect: '/check', failureRedirect: '/auth',
        failureFlash: true 
    }), function(req, res) {
        console.log("local authentication successful");
    });

    passport.serializeUser(function(user, done) {
        //what am I supposed to do here?
        done(null, user._id);
    });

    passport.deserializeUser(function(id, done) {
        model.User.findByID(new mongo.ObjectID(id), function(err, user) {
            //console.log("deserialize user with id: "+id);
            //console.dir(user);
            done(err, user);
        });
    });
    app.get('/auth/google', passport.authenticate('google'));
    app.get('/auth/google/return', passport.authenticate('google', { successRedirect: '/check', failureRedirect: '/auth/error' }));

    //forward to /check when user first login 
    app.get('/check', function(req, res){
        //check to see if user has user/pass reset (if this is the first time user logsin)
        //console.log("checking user object");
        if(req.user) {
            //console.dir(req.user);
            if(req.user.password) {
                model.Doc.findByOwnerID(req.user._id, function(err, docs) {
                    if(err) {
                        console.error(err);
                    } else {
                        if(docs.length > 0) {
                            //all good
                            res.redirect('/'); 
                        } else {
                            //create doc for user  
                            model.Doc.create({
                                "name" : "My Budget",
                                "owners" : [ req.user._id ]
                            }, function(err, docid) {
                                console.log("created first doc for user "+req.user._id);
                                res.redirect('/'); 
                            });
                        }
                    }
                });
            } else {
                res.redirect('/setpass'); 
            }
        }
    });

    app.get('/page', function(req, res){
        if(req.user) {
            res.render("page.ejs", {req: req, menu: "page"});
        } else {
            res.redirect('/'); 
        }
    });
    app.get('/page/list', function(req, res){
        if(req.user) {
            //load all docs
            model.Doc.findByOwnerID(req.user._id, function(err, docs) {
                //load pages for each doc
                async.forEach(docs, function(doc, next) {
                    model.Page.findByDocID(doc._id, function(err, pages) {
                        //add some optional parameters to each page
                        async.forEach(pages, function(page, next_page) {
                            page._pct = page.total_expense/page.total_income*100;
                            if(page.start_date < now && page.end_date > now) {
                                page._active = true; 
                            }
                            next_page();
                        }, function() {
                            doc.pages = pages;
                            next();
                        });
                    });
                }, function() {
                    res.json(docs);
                });
            });
        }
    });
    app.get('/page/detail', function(req, res){
        if(req.user) {
            //load page requested
            model.Page.findByID(new mongo.ObjectID(req.query.id), function(err, page) {
                model.Doc.getAuth(req.user, page.doc_id, function(err, auth) {
                    if(auth.canread) {
                        model.Income.findByPageID(page._id, function(err, incomes) {
                            model.Category.findByPageID(page._id, function(err, categories) {
                                page.incomes = incomes;
                                page.categories = categories;
                                res.json(page);
                            });
                        });
                    }
                });
            });
        }
    });
    app.get('/setting', function(req, res) {
        if(req.user) {
            res.render("setting.ejs", {req: req, menu: "setting"});
        } else {
            res.redirect('/'); 
        }
    });
    app.post('/setting', function(req, res) {
        if(req.user) {
            model.User.update(req.user._id, {name: req.body.name, email: req.body.email}, function(err) {
                if(err) {
                    res.statusCode = 500;
                } else {
                    res.statusCode = 200;
                }
                res.end();
            });
        }
    });
    app.post('/import/dsbudget', function(req, res) {
        if(req.user) {
            var docid = new mongo.ObjectID(req.body.docid);
            var importtype = req.body.importtype;
            var import_opts = {fd: req.body.fd};
            model.Doc.getAuth(req.user, docid, function(err, auth) {
                if(auth.canwrite) {
                    //parse the xml
                    var path = req.files.file.path;
                    var importer = require('./import');
                    switch(importtype) {
                    case "dsbudget":
                        importer.dsbudget(model, docid, path, import_opts, function(err) {
                            if(err) {
                                res.statusCode = 500;
                                res.write(err);
                            } else {
                                //all good
                                res.statusCode = 200;
                            }
                            res.end();
                        }); 
                        break;
                    }
                } else {
                    res.statuCode = 403; //forbidden
                    res.end();
                }
            });
        }
    });

    /*
    app.get('/page/:id', function(req, res) {
        if(req.user) {
            //load the page
            var pageid = req.params.id;
            model.Page.findByID(new mongo.ObjectID(pageid), function(err, page) {
                //load docs user has access to 
                model.Doc.findByOwnerID(req.user._id, function(err, docs) {
                    //make sure page.doc_id is one of user's doc
                    docs.forEach(function(doc) {
                        if(doc._id == page.doc_id) {
                            res.json(doc);
                        }
                    });
                    res.end();
                });
            });
        } else {
            //user only
            res.redirect('/'); 
        }
    });
    */

    server.listen(config.port, config.host, function(){
        console.log('Express server listening on host ' + config.host);
    });
});

