'use strict';

//contrib
var express = require('express');
var router = express.Router();
var jwt = require('express-jwt');

//mine
var config = require('./config');

/**
 * @apiGroup System
 * @api {get} /health Get API status
 * @apiDescription Get current API status
 * @apiName GetHealth
 *
 * @apiSuccess {String} status 'ok' or 'failed'
 */
router.get('/health', function(req, res) {
    res.json({status: 'ok'});
});

module.exports = router;

///////////////////////////////////////////////////////////////////////////////////////////////////

function hashpassword(pass, salt, callback) {
    crypto.pbkdf2(pass, salt, 10000, 512, function(err, hash) {
        if(err) {
            callback(err);
        } else {
            callback(null, hash.toString());
        }
    });
}

function oldstuff() {

    //most of initialization happens after we connect to db.
    mongo.MongoClient.connect(config.mongo_url, function(err, db) {
        if(err) throw err;

        var model = require('./model').init(db);

        app.get('/', function(req,res) {
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
                                res.redirect('/page'); 
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
                    req.flash('info', "Looks like this is your first time using dsBudget! Please specify a password so you can login with your email and password.");
                    res.redirect('/setpass'); 
                }
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
                                model.User.update(req.user._id, {$set: {
                                    password_salt: salt,
                                    password: hash,
                                    email: req.body.email, 
                                    name: req.body.name
                                }}, function(err) {
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
            successRedirect: '/', failureRedirect: '/auth',
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
        app.get('/auth/google/return', passport.authenticate('google', 
            { successRedirect: '/', failureRedirect: '/auth/error' }
        ));

        //forward to /check when user first login 
        app.get('/', function(req, res){
        });

        app.get('/page', function(req, res){
            if(req.user) {
                res.render("page.ejs", {req: req, menu: "page"});
            } else {
                res.redirect('/'); 
            }
        });
        app.get('/page/balance/:id', function(req, res) {
            if(req.user) {
                model.Page.getBalance(new mongo.ObjectID(req.params.id), function(err, balance) {
                    if(err) {
                        res.statusCode = 500;
                        res.write(err);
                    } else {
                        //all good
                        res.statusCode = 200;
                        res.write(balance);
                    }
                    res.end();
                });
            }
        });
        app.get('/docs', function(req, res){
            var now = new Date().getTime();
            if(req.user) {
                //load all docs
                model.Doc.findByOwnerID(req.user._id, function(err, docs) {
                    //load pages for each doc
                    async.forEach(docs, function(doc, next) {
                        model.Page.findByDocID(doc._id, function(err, pages) {
                            //add some optional parameters to each page
                            async.forEach(pages, function(page, next_page) {
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
        app.get('/page/detail', function(req, res) {
            if(req.user) {
                //load page requested
                model.Page.findByID(new mongo.ObjectID(req.query.id), function(err, page) {
                    if(err) {
                        console.error(err);
                        res.statusCode = 404;
                        res.end();
                        return;
                    }
                    model.Doc.getAuth(req.user, page.doc_id, function(err, auth) {
                        if(auth.canread) {
                            model.Income.findByPageID(page._id, function(err, incomes) {
                                /*
                                //console.dir(incomes);
                                //for balance income, lookup the real page name & balance
                                async.forEach(incomes, function(income, next_income) {
                                    if(income.balance_from) {
                                        //lookup page name
                                        model.Page.findByID(income.balance_from, function(err, ipage) {
                                            income.page_name = ipage.name;
                                            //get the actual balance for the page
                                            model.Page.getBalance(income.balance_from, function(amount) {
                                                income.amount = amount;
                                                next_income();
                                            });
                                        });
                                    } else {
                                        next_income();
                                    }
                                }, function() {
                                    //finally load the categories and emit
                                    model.Category.findByPageID(page._id, function(err, categories) {
                                        page.incomes = incomes;
                                        page.categories = categories;
                                        res.json(page);
                                    });
                                });
                                */
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
                var update = {name: req.body.name, email: req.body.email};
                /*
                if(req.body.password) {
                    if(req.body.password != req.body.password2) {
                        res.write("Password doesn't match. Please enter it again.");
                        res.statusCode = 500;
                        res.end();
                        return;
                    }
                    update.password = req.body.password;
                }
                */
                model.User.update(req.user._id, {$set: update}, function(err) {
                    if(err) {
                        res.write(err);
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
                        //console.log(path); // like ... /tmp/29315-1n858ly.jpg
                        switch(importtype) {
                        case "dsbudget":
                            importer.dsbudget(model, docid, path, import_opts, function(err) {
                                if(err) {
                                    console.log("returning error:"+err);
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
        app.post('/expense', function(req, res) {
            if(req.user) {
                var catid = new mongo.ObjectID(req.body.catid);
                model.Category.findByID(catid, function(err, cat) {
                    var page_id = cat.page_id;
                    model.Page.findByID(page_id, function(err, page) {
                        var docid = page.doc_id;
                        model.Doc.getAuth(req.user, docid, function(err, auth) {
                            if(auth.canwrite) {
                                var expense = req.body.expense;
                                var clean_expense = {
                                    time: parseInt(expense.time),
                                    amount: parseFloat(expense.amount),
                                    where: expense.where, //make sure it's string?
                                    name: expense.name, //make sure it's string?
                                    tentative: expense.tentative //make sure it's bool?
                                }
                                if(req.body.eid != undefined) {
                                    cat.expenses[req.body.eid] = clean_expense;
                                } else {
                                    cat.expenses.push(clean_expense);
                                }
                                model.Category.update(page_id, cat._id, {$set: {expenses: cat.expenses}}, function(err, id) {
                                    if(err) {
                                        console.error(err);
                                        res.statusCode = 500;
                                        res.write('update failed');
                                    } else {
                                        res.statusCode = 200;
                                        res.write(id.toString());
                                    }
                                    res.end();
                                });
                            }
                        });
                    });
                });
            }
        });
        app.delete('/expense/:cid/:eid', function(req, res) {
            if(req.user) {
                var category_id = req.params.cid;
                var eid = req.params.eid;
                model.Category.findByID(new mongo.ObjectID(category_id), function(err, cat) {
                    //make sure user has write access
                    var page_id = cat.page_id;
                    model.Page.findByID(page_id, function(err, page) {
                        var docid = page.doc_id;
                        model.Doc.getAuth(req.user, docid, function(err, auth) {
                            if(auth.canwrite) {
                                cat.expenses.splice(eid, 1);
                                model.Category.update(page_id, cat._id, {$set: {expenses: cat.expenses}}, function(err, id) {
                                    if(err) {
                                        console.error(err);
                                        res.statusCode = 500;
                                        res.write('update failed');
                                    } else {
                                        res.statusCode = 200;
                                        res.write(id.toString());
                                    }
                                    res.end();
                                });
                            }
                        }); 
                    });
                });
            }
        });
        app.post('/income', function(req, res) {
            function upsert(id, income) {
                if(id) {
                    var iid = new mongo.ObjectID(id);
                    model.Income.update(iid, {$set: income}, function(err) {
                        if(err) {
                            console.error(err);
                            res.statusCode = 500;
                            res.write('update failed');
                        } else {
                            res.statusCode = 200;
                            res.write('ok');
                        }
                        res.end();
                    });
                } else {
                    model.Income.create(income, function(err, newid) {
                        if(err) {
                            console.error(err);
                            res.statusCode = 500;
                            res.write('insert failed');
                        } else {
                            res.statusCode = 200;
                            res.write(newid.toString());
                        }
                        res.end();
                    });
                }
            }
            if(req.user) {
                var income = req.body.income;
                var page_id = new mongo.ObjectID(income.page_id);
                model.Page.findByID(page_id, function(err, page) {
                    model.Doc.getAuth(req.user, page.doc_id, function(err, auth) {
                        if(auth.canwrite) {
                            var clean_income = {
                                page_id: page_id,
                                name: income.name //TODO..make sure it's string?
                            }
                            if(income.balance_from) {
                                //convert to mongo id
                                clean_income.balance_from = new mongo.ObjectID(income.balance_from);
                                //make sure the page belongs to the same doc
                                model.Page.findByID(clean_income.balance_from, function(err, balance_page) {
                                    if(balance_page.doc_id.equals(page.doc_id)) {
                                        upsert(income._id, clean_income);
                                    } else {
                                        console.dir("can't use page from other doc.. for security reason");
                                        console.dir(page);
                                        console.dir(balance_page);
                                    }
                                });
                            } else {
                                clean_income.amount = parseFloat(income.amount);
                                upsert(income._id, clean_income);
                            }
                         }
                    });
                });
            }
        });
        app.post('/category', function(req, res) {
            if(req.user) {
                var dirty_category = req.body.category;
                var category = dirty_category; //TODO - not sure how to validate data structure

                if(category._id) {
                    //update
                    var category_id = new mongo.ObjectID(category._id);
                    model.Category.findByID(category_id, function(err, cat) {
                        //make sure user can edit this category
                        model.Page.findByID(cat.page_id, function(err, page) {
                            var docid = page.doc_id;
                            model.Doc.getAuth(req.user, docid, function(err, auth) {
                                if(auth.canwrite) {
                                    //ok proceed...
                                    delete category._id; //can't update _id
                                    category.page_id = cat.page_id; //replace string to ObjectID
                                    model.Category.update(cat.page_id, cat._id, {$set: category}, 
                                    function(err, id) {
                                        if(err) {
                                            console.error(err);
                                            res.statusCode = 500;
                                            res.write('update failed');
                                        } else {
                                            res.statusCode = 200;
                                            res.write(id.toString());
                                        }
                                        res.end();
                                    });
                                }
                            });
                        });
                    });
                } else {
                    //insert
                    var page_id = new mongo.ObjectID(category.page_id);
                    model.Page.findByID(page_id, function(err, page) {
                        var docid = page.doc_id;
                        model.Doc.getAuth(req.user, docid, function(err, auth) {
                            if(auth.canwrite) {
                                //ok proceed...
                                category.page_id = page_id; //replace string to ObjectID (necessary?)
                                console.dir(category);
                                model.Category.create(category, function(err, id) {
                                    if(err) {
                                        console.error(err);
                                        res.statusCode = 500;
                                        res.write('insert failed');
                                    } else {
                                        res.statusCode = 200;
                                        console.log("created category with id:"+id);
                                        res.write(id.toString());
                                    }
                                    res.end();
                                });
                            }
                        });
                    });
                }
            }
        });
        app.delete('/category/:id', function(req, res) {
            if(req.user) {
                var category_id = new mongo.ObjectID(req.params.id);
                console.log("removing category :"+category_id);
                model.Category.findByID(category_id, function(err, category) {
                    //make sure user has write access
                    var page_id = category.page_id;
                    model.Page.findByID(page_id, function(err, page) {
                        var docid = page.doc_id;
                        model.Doc.getAuth(req.user, docid, function(err, auth) {
                            if(auth.canwrite) {
                                //go ahead with removal
                                model.Category.remove(page_id, category._id, function(err) {
                                    if(err) {
                                        console.error(err);
                                        res.statusCode = 500;
                                        res.write('removal failed');
                                    } else {
                                        res.statusCode = 200;
                                        res.write('ok');
                                    }
                                    res.end();
                                });
                            }
                        }); 
                    });
                });
            }
        });
        app.delete('/income/:id', function(req, res) {
            if(req.user) {
                var income_id = req.params.id;
                //console.dir(income_id);
                model.Income.findByID(new mongo.ObjectID(income_id), function(err, income) {
                    //make sure user has write access
                    var page_id = income.page_id;
                    model.Page.findByID(page_id, function(err, page) {
                        var docid = page.doc_id;
                        model.Doc.getAuth(req.user, docid, function(err, auth) {
                            if(auth.canwrite) {
                                //go ahead with removal
                                model.Income.remove(page_id, income._id, function(err) {
                                    if(err) {
                                        console.error(err);
                                        res.statusCode = 500;
                                        res.write('removal failed');
                                    } else {
                                        res.statusCode = 200;
                                        res.write('ok');
                                    }
                                    res.end();
                                });
                            }
                        }); 
                    });
                });
            }
        });
        app.delete('/page/:id', function(req, res) {
            if(req.user) {
                var page_id = new mongo.ObjectID(req.params.id);
                model.Page.findByID(page_id, function(err, page) {
                    var docid = page.doc_id;
                    model.Doc.getAuth(req.user, docid, function(err, auth) {
                        if(auth.canwrite) {
                            model.Page.remove(page_id, function(err) {
                                if(err) {
                                    console.error(err);
                                    res.statusCode = 500;
                                    res.write('removal failed');
                                } else {
                                    res.statusCode = 200;
                                    res.write('ok');
                                }
                                res.end();
                            });
                        }
                    });
                });
            }
        });

        app.post('/page', function(req, res) {
            function createpage(newpage, next) {
                model.Page.create(newpage, function(err, id) {
                    if(err) {
                        res.statusCode = 500;
                        res.write("Failed to create page");
                    } else {
                        next(id); 
                        res.statusCode = 200;
                        res.write(id.toString());
                    }
                    res.end();
                });
            }
            function updatepage(id, page) {
                model.Page.update(id, {$set: page}, function(err, id) {
                    if(err) {
                        //console.error(err);
                        res.statusCode = 500;
                        res.write('update failed');
                    } else {
                        res.statusCode = 200;
                        res.write(id.toString());
                    }
                    res.end();
                });
            }
            function copyincomes(from_pageid, to_pageid) {
                model.Income.findByPageID(from_pageid, function(err, incomes) {
                    incomes.forEach(function(income) {
                        //don't copy balance income
                        if(!income.balance_from) {
                            income.page_id = to_pageid;
                            delete income._id; //necessary?
                            model.Income.create(income);
                        }
                    });
                });
            }
            function copycategories(from_pageid, to_pageid, start_time) {
                model.Category.findByPageID(from_pageid, function(err, categories) {
                    categories.forEach(function(category) {
                        category.page_id = to_pageid;
                        delete category._id; //necessary?
                        if(category.recurring) {
                            //reset expense date to the same month as start_time by keeping the date itself
                            var start_date = new Date(start_time);
                            category.expenses.forEach(function(expense) {
                                var d = new Date(expense.time);
                                d.setFullYear(start_date.getFullYear());
                                d.setMonth(start_date.getMonth()); 
                                expense.time = d.getTime();
                            });
                        } else {
                            //reset all expenses
                            category.expenses = [];
                            category._remaining = category.budget; 
                        }
                        model.Category.create(category);
                    });
                });
            }

            if(req.user && req.body.page) {
                var dirty_page = req.body.page;
                var clean_page = {
                    //TODO - I am not sure who is really responsible for validating field types.. model?
                    doc_id: new mongo.ObjectID(dirty_page.doc_id),
                    name: dirty_page.name.toString(),
                    desc: (dirty_page.desc ? dirty_page.desc.toString() : ""),
                    start_date: parseInt(dirty_page.start_date),
                    end_date: parseInt(dirty_page.end_date)
                };

                if(dirty_page._id) {
                    //updating existing page
                    var page_id = new mongo.ObjectID(dirty_page._id);
                    model.Page.findByID(page_id, function(err, page) {
                        var docid = page.doc_id;
                        model.Doc.getAuth(req.user, docid, function(err, auth) {
                            if(auth.canwrite) {
                                updatepage(page_id, clean_page);
                            }
                        });
                    });
                } else {
                    //adding new page
                    model.Doc.getAuth(req.user, dirty_page.doc_id, function(err, auth) {
                        if(auth.canwrite) {
                            createpage(clean_page, function(page_id) {
                                //TODO - if parent page is specified, copy income and recurring expenses..
                                if(req.body.parent != null) {
                                    var parentid = new mongo.ObjectID(req.body.parent._id);
                                    //make sure user really has read access to this parent
                                    model.Page.findByID(parentid, function(err, parent) {
                                        if(!err) {
                                            model.Doc.getAuth(req.user, parent.doc_id, function(err, auth) {
                                                if(auth.canread) {
                                                    copyincomes(parentid, page_id);
                                                    copycategories(parentid, page_id, clean_page.start_date);
                                                    //TODO - add balance income using parent?
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }
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
            console.log('Express server listening on host ' + config.host+":"+config.port);
        });

        process.on('uncaughtException', function(err) {
            console.error('Caught exception: ' + err);
        });
    });
}
