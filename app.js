
/**
 * Module dependencies.
 */

var express = require('express');
var http = require('http');
var path = require('path');
var config = require('./config.json');
var passport = require('passport');
var passport_google = require('passport-google').Strategy;

var app = express();

// all environments
app.set('port', process.env.PORT || config.server_port);
app.set('views', __dirname + '/views');
app.set('view engine', 'ejs');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(express.cookieParser(config.cookie_secret));
app.use(express.session());
app.use(app.router);

app.configure('development', function(){
  app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
});

app.configure('production', function(){
  app.use(express.errorHandler());
});

//mount all 
app.use('/admin', require('./routes/admin').middleware);
app.use('/page', require('./routes/page').middleware);

//passport
passport.use(new passport_google({
    returnURL: config.url+'/auth/google/return',
    realm: config.url
  },
  function(identifier, profile, done) {
    /*
    User.findOrCreate({ openId: identifier }, function(err, user) {
      done(err, user);
    });
    */
    console.dir(identifier);
    console.dir(profile);
  }
));
app.get('/auth/google', passport.authenticate('google'));
app.get('/auth/google/return', 
    passport.authenticate('google', { 
        successRedirect: '/',
        failureRedirect: '/login' }
    )
);


/*
app.get('/page/:id', routes.
    articleProvider.findById(req.params.id, function(error, article) {
        res.render('blog_show.jade',
        { locals: {
            title: article.title,
            article:article
        }
        });
    });
});
*/
/*
var routes_admin = require('./routes/admin');
app.get('/admin/page', routes_admin.pagelist);
app.get('/admin/page/new', routes_admin.getnewpage);
app.post('/admin/page/new', routes_admin.postnewpage);
*/

//homepage
app.get('/', function(req, res){
  res.render('index', { title: 'dsBudget' });
});

//this must come at the end..
app.use(express.static(path.join(__dirname, 'static')));

http.createServer(app).listen(app.get('port'), function(){
  console.log('Express server listening on port ' + app.get('port'));
});

