var fs = require('fs');
var xml2js = require('xml2js');
var async = require('async');
var extend = require('extend');
var mongo = require('mongodb');
var decimal = require('decimal');

String.prototype.insert = function (index, string) {
  if (index > 0)
    return this.substring(0, index) + string + this.substring(index, this.length);
  else
    return string + this;
};

function parse_page(model, doc_id, opts, page, next_page) {
    //skip "New Page"
    if(page.$.ctime == 0) {
        next_page();
        return;
    }

    //convert "1234" to "12.34"
    function parse_amount(amount) {
        //convert 1 to 01 so we can add decimal point
        while(amount.length < opts.fd) {
            amount = '0'+amount; 
        }
        var a = amount.insert(amount.length-opts.fd, ".");
        //console.log("amount parsed "+amount +" to "+a);
        return a;
    }

    var db_page = {
        doc_id: doc_id,
        name: page.$.name,
        desc: page.$.description,
        start_date: parseInt(page.$.ctime)*1000,
        end_date: parseInt(page.$.ctime)*1000 + 3600*1000*24*30 //TODO - set it to end of month? 
        /*
        _total_income: null, //will be reset when first accessed
        _total_expense: null, // will be reset when first accessed
        balance_to: null //income id where the balance goes to
        */
        /*
        show_views: {
            balance: (page.$.hide_balance == "yes" ? true : false),
            budget: (page.$.hide_budget == "yes" ? true : false),
            expense: (page.$.hide_expense == "yes" ? true : false),
            income: (page.$.hide_income == "yes" ? true : false)
        }
        */
    };

    model.Page.create(db_page, function(err, page_id) {
        function parse_deductions(name, deductions, next) {
            var db_deduction_category = {
                page_id: page_id,
                name: name, 
                desc: "",
                //budget: '0',
                //is_amount_per: false,
                color: "#909090", //client color parse can't parse #ffe (yet)
                show_view: {balance_graph: false, pie_graph: false},
                sort_by: "name",
                sort_asc: true,
                recurring: true,
                expenses: [],
            };
            async.forEach(deductions, function(deduction, next_deduction) {
                var db_expense = {
                    where: deduction.$.desc,
                    amount: parse_amount(deduction.$.amount),
                    tentative: false,
                    name: "",
                    time: parseInt(page.$.ctime)*1000 //use page's ctime as expense time
                };
                //db_deduction_category.budget = decimal(db_expense.amount).add(db_expense.amount).toString();
                db_deduction_category.expenses.push(db_expense);
                next_deduction();
            }, function() {
                model.Category.create(db_deduction_category, function(err, category_id) {
                    if(err) throw err;
                    next();
                });
            });
        }

        function parse_income(income, next) {
            var db_income = {
                page_id: page_id,
            }
            if(income.$.balance == "yes") {
                //will be converted to balance_from with proper page id later
                db_income._balance_from = income.$.balance_from;
            } else {
                db_income.name = income.$.desc;
                db_income.amount = parse_amount(income.$.amount);
            }
            model.Income.create(db_income, function(err, income_id) {
                //console.log("creating income");
                //console.dir(db_income);
                if(err) throw err;
                if(income.Deduction) {
                    parse_deductions("Deductions for "+income.$.desc, income.Deduction, next);
                } else {
                    next();
                }
            });
        }

        function parse_color(v) {
            var v = parseInt(v);
            var r = (v>>0)&0xff;
            var g = (v>>8)&0xff;
            var b = (v>>16)&0xff;
            return "#"+decimalToHex(r,2)+decimalToHex(g,2)+decimalToHex(b,2);
        }

        function decimalToHex(d, padding) {
            var hex = Number(d).toString(16);
            padding = typeof (padding) === "undefined" || padding === null ? padding = 2 : padding;

            while (hex.length < padding) {
                hex = '0' + hex;
            }

            return hex;
        }

        function parse_category(category, next) {
            var non_recurring_expenses = [];
            var recurring_expenses = [];
            var db_category = {
                page_id: page_id,
                name: category.$.name, 
                desc: category.$.desc,
                color: parse_color(category.$.color),
                /*
                show_view: {
                    balance_graph: (category.$.hide_balance_graph == "yes" ? false : true),
                    pie_graph: (category.$.hide_pie_graph == "yes" ? false : true)
                },
                */
                sort_by: category.$.sort_by, //TODO - I need to map to the real field name
                sort_asc: (category.$.sort_reverse == "yes" ? false : true),
                expenses: [],
                recurring: false
            };

            if(!category.Spent) {
                category.Spent = [];
            }
            async.forEach(category.Spent, function(spent, next_spent) {
                var recurring = (spent.$.recurring == "yes" ? true : false);
                var expense = {
                    name: spent.$.desc,
                    amount: parse_amount(spent.$.amount),
                    where: spent.$.where,
                    time: parseInt(spent.$.time)*1000,
                    tentative: (spent.$.tentative == "yes" ? true : false)
                };
                if(recurring) {
                    recurring_expenses.push(expense);
                } else {
                    non_recurring_expenses.push(expense);
                }
                next_spent();
            }, function() {
                async.series([
                    //optionally create recurring category
                    function(next_type) {
                        if(recurring_expenses.length > 0) {
                            var db_cat = extend({}, db_category, {
                                recurring: true,
                                color: "#909090", //client color parse can't parse #ffe (yet)
                                name: db_category.name + " (Recurring)",
                                expenses: recurring_expenses
                            });
                            //console.dir(db_cat);
                            model.Category.create(db_cat, function(err, category_id) {
                                if(err) throw err;
                                next_type();
                            });
                        } else {
                            next_type();
                        }
                    },
                    //create category
                    function(next_type) {
                        var db_cat = extend({}, db_category, {
                            recurring: false,
                            budget: parse_amount(category.$.budget),
                            is_budget_per: (category.$.amount_is_percentage == "true" ? true : false),
                            expenses: non_recurring_expenses
                        });
                        model.Category.create(db_cat, function(err, category_id) {
                            if(err) throw err;
                            next_type();
                        });
                    },
                ], function() {
                    next();
                });
            });
        }

        async.series([
            function(next) {
                var incomes = page.Income;
                if(incomes) {
                    async.forEach(incomes, parse_income, function() {
                        next(null, 'incomes'); 
                    });
                } else {
                    //console.log(db_page.name + " doesn't have any income");
                    next(null, 'no_incomes');
                }
            },
            function(next) {
                var categories = page.Category;
                if(categories) {
                    async.forEach(categories, parse_category, function() {
                        next(null, 'categories'); 
                    });
                } else {
                    //console.log(db_page.name + " doesn't have any categories");
                    next(null, 'no_categoryes');
                }
            }
        ], next_page);
    });
}

exports.dsbudget = function(model, docid, path, opts, callback) {
    fs.readFile(path, function(err, data) {
        if(err) {
            callback(err);
        } else {
            var parser = new xml2js.Parser();
            parser.parseString(data, function(err, doc) {
                if(err) {
                    callback(err);
                } else {
                    var docversion = doc.Budget.$.docversion;
                    var openpage = doc.Budget.$.openpage;
                    var pages = doc.Budget.Page;
                    async.forEach(pages, function(page, next) {
                        parse_page(model, docid, opts, page, next);
                    }, function(err) {
                        //reset _balance_from reference with actual page_id
                        model.Page.findByDocID(docid, function(err, doc_pages) {
                            function findPageByName(name) {
                                for(var i in doc_pages) {
                                    var searching_doc_page = doc_pages[i];
                                    if(searching_doc_page.name == name) return searching_doc_page;
                                };
                                console.error("failed to find page with name: [" + name+ "]");
                                return null;
                            }

                            function reset_balance_from(doc_income, next) {
                                if(doc_income._balance_from) {
                                    var balance_page = findPageByName(doc_income._balance_from);
                                    if(balance_page.balance_to) {
                                        console.log("balance_to on page "+balance_page._id+" is already set to "+balance_page.balance_to);
                                        //turn it into real balance with amount:0
                                        model.Income.update(doc_income._id, {
                                            $set: {
                                                amount: '0',
                                                name: "Invalid balance originally from "+doc_income._balance_from+" (only 1 child page allowed)"
                                            },
                                            $unset: {_balance_from: 1}
                                        }, next); 
                                    } else {
                                        model.Income.update(doc_income._id, {
                                            $set: {
                                                balance_from: balance_page._id
                                            },
                                            $unset: {_balance_from: 1}
                                        }, function() {
                                            //update balance_to on page also
                                            console.log("setting balance_to on "+balance_page._id+" to be "+doc_income._id);
                                            model.Page.update(balance_page._id, {
                                                $set: { balance_to: doc_income._id }
                                            }, next);
                                        });
                                    }
                                } else {
                                    next();
                                };
                            }

                            async.forEach(doc_pages, function(doc_page, next_doc_page) {
                                model.Income.findByPageID(doc_page._id, function(err, doc_incomes) {
                                    if(err) {
                                        consooe.log("failed to find incomes by page id " + doc_page._id);
                                    } else {
                                        async.forEach(doc_incomes, reset_balance_from, next_doc_page);
                                    } 
                                });
                            }, callback);
                        });
                    });
                }
            });
        }
    });
};
