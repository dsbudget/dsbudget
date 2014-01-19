//divrep.js requires jquery lib (for now..). please load it before you load this script

var divrep_processing_id = null;
function divrepClearProcessing() {
	divrep_processing_id = null;
	$(".divrep_processing").removeClass("divrep_processing");
}

function divrep(id, event, value, action) {
	//stop bubble - needs to happen before ignore / queueing events to prevent
	//event such as double clicking to bubble up
	if(!event) var event = window.event;//for IE
	if(event) {
		//event.cancelBubble = true;//IE
		if(event.stopPropagation) event.stopPropagation();//Standard
	}
	
	if(!action) {
		try {
			action = event.type;
		} catch(e) {
			action = "unknown";
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
		setTimeout(function() { divrep(id, event, value,action);}, 100);
		return;
	}
	
	//set class while processing
	$("#"+id).addClass("divrep_processing");
	
	divrep_processing_id = id;
	$.ajax({
		url: "divrep",
		async: true,//now running in async mode to not hose up browser..
		data: { nodeid: id,
			action: action,
			value : value },
		type: "POST",
		contentType: "application/x-www-form-urlencoded; charset=UTF-8",//IE doesn't set charset correctly..
		dataType: "script",//Evaluates the response as JavaScript and returns it as plain text. Disables caching unless option "cache" is used. Note: This will turn POSTs into GETs for remote-domain requests. 
	    success: function(msg) {
		    divrepClearProcessing();
		},
	    error: function (XMLHttpRequest, textStatus, errorThrown) {
		    alert("Sorry! Server is having trouble processing your request.\n\n"+textStatus + ": " + errorThrown);
		    divrepClearProcessing();
	    }
	});
}

function divrep_replace(id, content) 
{
	var node = $("#"+id);
	if(node.length == 0) {
		alert("couldn't find the divrep node - maybe it's not wrapped with div?\n" + id);
	}
	//why am I emptying the content before replacing it? because jQuery's replaceWith adds new content before removing the
	//old content. This causes identical ID to coexist in the dom structure and causes redraw issue
	//node.empty();
	node.replaceWith(Base64.decode(content));
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

/*
Copyright (c) 2008 Fred Palmer fred.palmer_at_gmail.com

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.
*/
function StringBuffer()
{ 
    this.buffer = []; 
} 

StringBuffer.prototype.append = function append(string)
{ 
    this.buffer.push(string); 
    return this; 
}; 

StringBuffer.prototype.toString = function toString()
{ 
    return this.buffer.join(""); 
}; 

var Base64 =
{
    codex : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",

    encode : function (input)
    {
        var output = new StringBuffer();

        var enumerator = new Utf8EncodeEnumerator(input);
        while (enumerator.moveNext())
        {
            var chr1 = enumerator.current;

            enumerator.moveNext();
            var chr2 = enumerator.current;

            enumerator.moveNext();
            var chr3 = enumerator.current;

            var enc1 = chr1 >> 2;
            var enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            var enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            var enc4 = chr3 & 63;

            if (isNaN(chr2))
            {
                enc3 = enc4 = 64;
            }
            else if (isNaN(chr3))
            {
                enc4 = 64;
            }

            output.append(this.codex.charAt(enc1) + this.codex.charAt(enc2) + this.codex.charAt(enc3) + this.codex.charAt(enc4));
        }

        return output.toString();
    },

    decode : function (input)
    {
        var output = new StringBuffer();

        var enumerator = new Base64DecodeEnumerator(input);
        while (enumerator.moveNext())
        {
            var charCode = enumerator.current;

            if (charCode < 128)
                output.append(String.fromCharCode(charCode));
            else if ((charCode > 191) && (charCode < 224))
            {
                enumerator.moveNext();
                var charCode2 = enumerator.current;

                output.append(String.fromCharCode(((charCode & 31) << 6) | (charCode2 & 63)));
            }
            else
            {
                enumerator.moveNext();
                var charCode2 = enumerator.current;

                enumerator.moveNext();
                var charCode3 = enumerator.current;

                output.append(String.fromCharCode(((charCode & 15) << 12) | ((charCode2 & 63) << 6) | (charCode3 & 63)));
            }
        }

        return output.toString();
    }
}


function Utf8EncodeEnumerator(input)
{
    this._input = input;
    this._index = -1;
    this._buffer = [];
}

Utf8EncodeEnumerator.prototype =
{
    current: Number.NaN,

    moveNext: function()
    {
        if (this._buffer.length > 0)
        {
            this.current = this._buffer.shift();
            return true;
        }
        else if (this._index >= (this._input.length - 1))
        {
            this.current = Number.NaN;
            return false;
        }
        else
        {
            var charCode = this._input.charCodeAt(++this._index);

            // "\r\n" -> "\n"
            //
            if ((charCode == 13) && (this._input.charCodeAt(this._index + 1) == 10))
            {
                charCode = 10;
                this._index += 2;
            }

            if (charCode < 128)
            {
                this.current = charCode;
            }
            else if ((charCode > 127) && (charCode < 2048))
            {
                this.current = (charCode >> 6) | 192;
                this._buffer.push((charCode & 63) | 128);
            }
            else
            {
                this.current = (charCode >> 12) | 224;
                this._buffer.push(((charCode >> 6) & 63) | 128);
                this._buffer.push((charCode & 63) | 128);
            }

            return true;
        }
    }
}

function Base64DecodeEnumerator(input)
{
    this._input = input;
    this._index = -1;
    this._buffer = [];
}

Base64DecodeEnumerator.prototype =
{
    current: 64,

    moveNext: function()
    {
        if (this._buffer.length > 0)
        {
            this.current = this._buffer.shift();
            return true;
        }
        else if (this._index >= (this._input.length - 1))
        {
            this.current = 64;
            return false;
        }
        else
        {
            var enc1 = Base64.codex.indexOf(this._input.charAt(++this._index));
            var enc2 = Base64.codex.indexOf(this._input.charAt(++this._index));
            var enc3 = Base64.codex.indexOf(this._input.charAt(++this._index));
            var enc4 = Base64.codex.indexOf(this._input.charAt(++this._index));

            var chr1 = (enc1 << 2) | (enc2 >> 4);
            var chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
            var chr3 = ((enc3 & 3) << 6) | enc4;

            this.current = chr1;

            if (enc3 != 64)
                this._buffer.push(chr2);

            if (enc4 != 64)
                this._buffer.push(chr3);

            return true;
        }
    }
};
