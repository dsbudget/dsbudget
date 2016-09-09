'use strict';

//contrib
const mongoose = require('mongoose');
const winston = require('winston');

//mine
const config = require('../config');
const logger = new winston.Logger(config.logger.winston);
//const events = require('../events');

//use native promise for mongoose
//without this, I will get Mongoose: mpromise (mongoose's default promise library) is deprecated
mongoose.Promise = global.Promise; 

exports.init = function(cb) {
    mongoose.connect(config.mongodb, {}, function(err) {
        if(err) return cb(err);
        logger.info("connected to mongo");
        cb();
    });
}
exports.disconnect = function(cb) {
    mongoose.disconnect(cb);
}

/*
exports.Doc = {
    findByOwnerID: function(id, callback) {
        db.collection('doc', function(err, col) {
            if(err) callback(err)
            else {
                col.find({owners: id}).toArray(callback);
            }
        });
    },
    getAuth: function(user, docid, callback) {
        var auth = {canread: false, canwrite: false, canadmin: false};
        exports.Doc.findByOwnerID(user._id, function(err, docs) {
            async.forEach(docs, function(doc, next) {
                if(doc._id.equals(docid)) {
                    auth.canread = true;
                    auth.canwrite = true;
                }
                next();
            }, function() {
                callback(null, auth);
            });
        }); 
    },
    create: function(doc, callback) {
        db.collection('doc', function(err, col) {
            if(err) callback(err)
            else {
                col.insert(doc, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
};

exports.Page = {
    findByID: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id:id}, {}, callback);
            }
        });
    },
    findByDocID: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.find({doc_id:id}).toArray(callback);
            }
        });
    },

    create: function(page, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("inserting page");
                //console.dir(page);
                col.insert(page, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
    update: function(id, data, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.update({_id: id}, data, {w:1}, callback);
            }
        });
    },

    //if any income entries change, we need to invalidate it
    invalidateIncome: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.update({_id: id}, {$set: {_total_income: null}}, {w:1}, callback);
            }
        });
    },
    
    //if any category entries change, we need to invalidate it
    invalidateExpense: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.update({_id: id}, {$set: {_total_expense: null}}, {w:1}, callback);
            }
        });
    },

    //get balance from page cache, or calculate if it's not set
    getBalance: function(id, callback) {
        exports.Page.findByID(id, function(err, page) {
            async.parallel({
                total_income: function(next) {
                    if(page._total_income) {
                        next(null, decimal(page._total_income));
                    } else {
                        //compute total income and cache
                        var total = decimal('0');
                        exports.Income.findByPageID(id, function(err, incomes) {
                            async.forEach(incomes, function(income, next_income) {
                                if(income.balance_from) {
                                    //recurse if it's balance income
                                    exports.Page.getBalance(income.balance_from, function(err, amount) {
                                        total = total.add(amount);
                                        next_income();
                                    });
                                } else {
                                    total = total.add(income.amount);
                                    next_income();
                                }
                            }, function() {
                                //cache total
                                exports.Page.update(id, {$set: {_total_income: total.toString()}}, function() {
                                    next(null, total);
                                });
                            });
                        });
                    }
                },
                total_expense: function(next) {
                    if(page._total_expense) {
                        next(null, decimal(page._total_expense));
                    } else {
                        var total = decimal('0');
                        exports.Category.findByPageID(id, function(err, categories) {
                            categories.forEach(function(category) {
                                category.expenses.forEach(function(expense) {
                                    if(!expense.tentative) {
                                        total = total.add(expense.amount);
                                        //console.log("adding " + expense.amount);
                                        //console.log("total " + total);
                                    }
                                });
                            });
                            //cache total
                            exports.Page.update(id, {$set: {_total_expense: total.toString()}}, function() {
                                next(null, total);
                            });
                        });
                    }
                }
            }, function(err, ret){
                var balance = ret.total_income.sub(ret.total_expense);
                callback(err, balance.toString());
            });
        });
    },
    remove: function(id, callback) {
        db.collection('page', function(err, col) {
            if(err) callback(err)
            else {
                col.remove({_id: id}, {w:1}, callback);
            }
        });
    }
};

exports.User = {
    findByID: function(id, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id: id}, {}, callback);
            }
        });
    },
    findByGoogleID: function(openid, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({googleid: openid}, {}, callback);
            }
        });
    },
    findByEmail: function(email, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({email: email}, {}, callback);
            }
        });
    },
    create: function(user, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("inserting user");
                //console.dir(user);
                col.insert(user, {safe:true}, function(err, recs) {
                    if(err) callback(err);
                    console.dir(recs[0]._id);
                    callback(null, recs[0]._id);
                });
            }
        });
    },
    update: function(id, data, callback) {
        db.collection('user', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("updating: "+id);
                //console.dir(data);
                col.update({_id: id}, data, {w:1}, callback);
            }
        });
    }
};

exports.Income = {
    findByID: function(id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id: id}, {}, callback);
            }
        });
    },
    findByPageID: function(id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                //col.find({page_id:id}).toArray(callback);
                col.find({page_id:id}).toArray(function(err, incomes) {
                    //set amount for balance incomes
                    async.forEach(incomes, function(income, next) {
                        if(income.balance_from) {
                            exports.Page.findByID(income.balance_from, function(err, page) {
                                income.page_name = page.name; 
                                exports.Page.getBalance(income.balance_from, function(err, amount) {
                                    income.amount = amount;
                                    next();
                                });
                            });
                        } else {
                            next();
                        }
                    }, function() {
                        callback(err, incomes);
                    });
                });
            }
        });
    },
    create: function(income, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                //console.log("inserting income");
                //console.dir(page);
                exports.Page.invalidateIncome(income.page_id, function(err) {
                    if(err) callback(err);
                    else col.insert(income, {safe:true}, function(err, recs) {
                        if(err) callback(err);
                        callback(null, recs[0]._id);
                    });
                });
            }
        });
    },
    update: function(id, data, callback) {
        //console.log(id);
        //console.dir(data);
        delete data._id;
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                exports.Page.invalidateIncome(data.page_id, function(err) {
                    if(err) callback(err);
                    else col.update({_id: id}, data, {w:1}, callback);
                });
            }
        });
    },
    remove: function(page_id, id, callback) {
        db.collection('income', function(err, col) {
            if(err) callback(err)
            else {
                exports.Page.invalidateIncome(page_id, function(err) {
                    if(err) callback(err);
                    else col.remove({_id: id}, {w:1}, callback);
                });
            }
        });
    }
};

exports.Category = {
    findByID: function(id, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                col.findOne({_id: id}, {}, callback);
            }
        });
    },
    findByPageID: function(id, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                col.find({page_id:id}).toArray(callback);
            }
        });
    },
    create: function(category, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                //let's go ahead and invalidate expense.. although new category shouldn't have any expense, import will call create with
                //expenses already populated, and I just feel it's more consistent
                exports.Page.invalidateExpense(category.page_id, function(err) {
                    if(err) callback(err);
                    else {
                        col.insert(category, {safe:true}, function(err, recs) {
                            if(err) callback(err);
                            callback(null, recs[0]._id);
                        });
                    }
                });
            }
        });
    },
    update: function(page_id, id, data, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                exports.Page.invalidateExpense(page_id, function(err) {
                    if(err) callback(err);
                    else {
                        delete data._id;
                        col.update({_id: id}, data, {w:1}, callback);
                    }
                });
            }
        });
    },
    remove: function(page_id, id, callback) {
        db.collection('category', function(err, col) {
            if(err) callback(err)
            else {
                exports.Page.invalidateExpense(page_id, function(err) {
                    if(err) callback(err);
                    else col.remove({_id: id}, {w:1}, callback);
                });
                //TODO - shouldn't I remove the orphaned expenses?
            }
        });
    }
};
*/
