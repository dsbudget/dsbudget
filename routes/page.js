var express = require('express');
var router = new express.Router();

var PageModel = require('../dsbudget').PageModel;
 
// routes also have the .param method to allow for easier resource loading
// in this case we will make sure all our routes load the post
router.param('page_id', function(req, res, next, page_id) {
    //console.log("page_id param:"+page_id);
    /*
    // simulate loading a post
    setTimeout(function() {
        req.post = { name: 'foo' };
        next();
    }, 1000);
    */
    var pageModel = new PageModel();
    pageModel.findById(page_id, function(err, page) {
        req.page = page;
        next();
    });
});
 
router.get('/:page_id', function(req, res, next) {
    //res.send(req.page.name + '\n');
    var page = req.page;
    res.render('page.jade', {
        title: page.title,
        body: page.body,
    });
});
 
router.post('/:page_id/comment', function(req, res, next) {
    // we could add the comment to req.post here
    res.send('\\o/\n');
});
 
module.exports = router;
