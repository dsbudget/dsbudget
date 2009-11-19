//divrep.js requires jquery lib (for now..). please load it before you load this script

var divrep_processing_id = null;
function divrepClearProcessing() {
	divrep_processing_id = null;
	$(".divrep_processing").removeClass("divrep_processing");
}

function divrep(id, event, value, action) {
	//stop bubble - needs to happen before ignore / queueing events to prevent
	//event such as double clicking to bubble up
	if(!event) var event = window.event;//IE
	if(event) {
		event.cancelBubble = true;//IE
		if(event.stopPropagation) event.stopPropagation();//Standard
	}
	if(!action)  {
		if(event) {
			var action = event.type;
		} else {
			var action = "unknown";
		}
	}
	//make sure there is only one request at the same time (prevent double clicking of submit button)
	if(divrep_processing_id == id) {
		//previous request on same target still running - ignore;
		//console.log('event ignore on ' + id);
		return;
	}
	
	//weird thing about the browser's event handling is that, although javascript is single threaded,
	//for some reason browser start running event handler while another handler is still executing.
	//we need to make sure that this doesn't happen.
	if(divrep_processing_id != null) {
		//wait until the previous processing ends
		//console.log('queusing event on ' + id);
		setTimeout(function() { divrep(id, event, value);}, 100);
		return;
	}
	
	divrep_processing_id = id;
	
	jQuery.ajax({
		url: "divrep",
		async: true,//now running in async mode to not hose up browser..
		data: { nodeid: id,
			action: action,
			value : value },
		type: "POST",
		contentType: "application/x-www-form-urlencoded; charset=UTF-8",//IE doesn't set charset correctly..
		dataType: "script",//Evaluates the response as JavaScript and returns it as plain text. Disables caching unless option "cache" is used. Note: This will turn POSTs into GETs for remote-domain requests. 
	    success: function(msg){
		    divrepClearProcessing();
		},
	    error: function (XMLHttpRequest, textStatus, errorThrown) {
		   alert(textStatus.errorThrown);
		   divrepClearProcessing();
	    }
	});
}

//this is basically the same thing as jquery.load, but instead of replacing the content 
//of the div, it replace the whole div using replaceWith().
var divrep_replace_counter = 0;
function divrep_replace(node, url) 
{
	//count how many requests are there
	divrep_replace_counter++;
	
	if(node.length == 0) {
		alert("couldn't find the divrep node - maybe it's not wrapped with div?\n" + url);
	}
	var self = node;
	// Request the remote document
	jQuery.ajax({
		url: url,
		type: "GET",
		cache: false,
		dataType: "html", //Returns HTML as plain text; included script tags are evaluated when inserted in the DOM. 
		complete: function(res, status){
			// If successful, inject the HTML into all the matched elements
			if ( status == "success" || status == "notmodified" ) {
				//why am I emptying the content before replacing it? because jQuery's replaceWith adds new content before removing the
				//old content. This causes identical ID to coexist in the dom structure and causes redraw issue
				node.empty();
				node.replaceWith(res.responseText);
			}
			--divrep_replace_counter;
		}
	});
	return this;
}

var divrep_jscallback = null;
function divrep_runjs()
{
	if(divrep_replace_counter == 0) {
		divrep_jscallback();
	} else {
		//retry later..
		setTimeout(divrep_runjs, 100);
	}
}

//Firefox 3.0.10 (and may be others) has a bug where windows.location based redirect directly
//from the returned javascript causes the browser history to incorrectly enter entry and hitting
//back button will make the browser skip previous page and render previous - previous page.
//timeout will prevent this issue from happening.
//we now alow divrep event to be processed asynchlonously, so the only chance we got to 
//prevent user from navigating away without saving is to let the event that is kicked off by 
//onblur to finish processing the update. This still doesn't catch if someone edit the text box
//and imediatly close the tab without causing onblur.. but I think it's okay because user should
//know what they are doing - we have to catch the case where user edit something, browser around in 
//the page and forget that she has changed something.
var divrep_redirect_url = null;
function divrep_redirect(url)
{
	divrep_redirect_url = url;
	setTimeout(divrep_redirect_wait, 0);
}
function divrep_redirect_wait()
{
	//wait for all divrep processing completes
	if(divrep_processing_id != null) {
		setTimeout(divrep_redirect_wait, 100);
	} else {
		divrep_doRedirect();
	}
}
function divrep_modified(mod)
{
	if(mod) {
		window.onbeforeunload = divrep_confirm_close;
	} else {
		window.onbeforeunload = null;
	}
}
function divrep_confirm_close()
{
    return "You have not submitted the changes you made on this form.";
}
function divrep_doRedirect()
{
	try {
		window.location.href = divrep_redirect_url;
	} catch(error) {
		//IE7 blows up if user cancel the onbeforeunload confirmation invoked by window redirect
		//this block silences IE7
	}
}

//following is for datepicker used inside dialog (for now)
//http://www.west-wind.com/weblog/posts/891992.aspx
$.maxZIndex = $.fn.maxZIndex = function(opt) {
    /// <summary>
    /// Returns the max zOrder in the document (no parameter)
    /// Sets max zOrder by passing a non-zero number
    /// which gets added to the highest zOrder.
    /// </summary>    
    /// <param name="opt" type="object">
    /// inc: increment value, 
    /// group: selector for zIndex elements to find max for
    /// </param>
    /// <returns type="jQuery" />
    var def = { inc: 10, group: "*" };
    $.extend(def, opt);
    var zmax = 0;
    $(def.group).each(function() {
        var cur = parseInt($(this).css('z-index'));
        zmax = cur > zmax ? cur : zmax;
    });
    if (!this.jquery)
        return zmax;

    return this.each(function() {
        zmax += def.inc;
        $(this).css("z-index", zmax);
    });
}

function colorpicker_hexFromRGB (r, g, b) {	
	var hex = [r.toString(16),	g.toString(16),	b.toString(16)];
	$.each(hex, function (nr, val) {
		if (val.length == 1) {hex[nr] = '0' + val;}
	});
	return hex.join('').toUpperCase();
}
