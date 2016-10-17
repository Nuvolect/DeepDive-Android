/*
 *
 * Script by http://www.yvoschaap.com/weblog/ajax_inline_instant_update_text_20
 * 
 */
/*jslint white: true */
/*jslint nomen: true */
/*jslint plusplus: true */

/* request channel */
function xhr() {}
xhr.prototype.init = function() {
	try {
		this._xh = new XMLHttpRequest();
	} catch (e) {
		var _ie = new Array(
		'MSXML2.XMLHTTP.5.0',
		'MSXML2.XMLHTTP.4.0',
		'MSXML2.XMLHTTP.3.0',
		'MSXML2.XMLHTTP',
		'Microsoft.XMLHTTP'
		);
		var success = false;
		for (var i=0;i < _ie.length && !success; i++) {
			try {
				this._xh = new ActiveXObject(_ie[i]);
				success = true;
			} catch (e) {
				
			}
		}
		if ( !success ) {
			return false;
		}
		return true;
	}
}

xhr.prototype.wait = function() {
	state = this._xh.readyState;
	return (state && (state < 4));
}

xhr.prototype.process = function() {
	if (this._xh.readyState == 4 && this._xh.status == 200) {
		this.processed = true;
	}
}

xhr.prototype.send = function(urlpost,data) {
	if (!this._xh) {
		this.init();
	}
	if (!this.wait()) {
		this._xh.open("POST",urlpost,false);// Matt changed, was GET
		this._xh.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");//Matt added
		this._xh.send(data);

	console.log( 'xhr.prototype.send data: '+data);

		if (this._xh.readyState == 4 && this._xh.status == 200) {
		    //Next line was returning entire page HTML, caller inserted this into field (entire page in input field)
			//return this._xh.responseText;

			changeLink(data);

			return "";
		}
	}

	return false;
}

var urlBase = ""; /* "/update.php"; */
var formVars = "";
var changing = false;

function changeLink(data) {
	var el = $('#' + data.id);
	// get link in same row to update with new data
	var a = el.closest('tr').find('a');
	// if found, we have a link to update
	if (a.length > 0) {
		// let's see what kind of link we need to update
		// email
		if (a.attr('href').indexOf('mailto:') != -1) {
			a.attr('href', 'mailto:' + data.value);
		}
		// address
		else if (a.attr('href').indexOf('http://maps.google.com/?q=') != -1) {
			a.attr('href', 'http://maps.google.com/?q=' + escape(data.value));
		}
		// url
		else {
			var url = data.value;

			if (url.indexOf('http://') == -1 && url.indexOf('https://') == -1) {
				url = 'http://' + url;
			}

			a.attr('href', url);
		}
	}
}

function fieldEnter(campo, e, id) {
	var el = $('#' + id);
	campo = $(campo);

	if (el.is('textarea')) {
		return true;
	}

	e = (e) ? e : window.event;

	if (e.keyCode == 13) {
		var remote = new xhr;
		remote.send(urlBase + '?update=' + encodeURIComponent(id) + '&content=' + encodeURIComponent(campo.val()) + '&' + formVars, '');

		noLight(el[0]);

		el.html(campo.val());

		changing = false;
		return false;
	}
	else {
		return true;
	}
}

function fieldBlur(campo, id) {

	campo = $(campo);
	var el = $('#' + id);
	var remote = new xhr;
	remote.send(urlBase
		+ '?update=' +encodeURIComponent(id)
		+ '&content='+encodeURIComponent(campo.val())
		+ '&' + formVars, {
			id: id,
			value: campo.val()
		});
	el.html(campo.val());	
	
	// revert back to placeholder if value was empty
	if (campo.val() == '') {		
		el.html(el.data('placeholder'));
		changeLink({
			id: id,
			value: el.data('placeholder')
		});
		el.addClass('placeholder');
	}
	
	changing = false;
	return false;
}

//edit field created
function editBox(actual) {

	if(!changing){
		width = widthEl(actual.id) + 20;
		height = heightEl(actual.id) + 2;

		if(height < 40){
			if(width < 100)	width = 250;
		}
		else{
			if(width < 70) width = 90;
			if(height < 50) height = 50;
		}

		if ($(actual).hasClass('multiline')) {
			actual.innerHTML = "<textarea name=\"textarea\" id=\""+ actual.id +"_field\" onfocus=\"highLight(this);\" onblur=\"noLight(this); return fieldBlur(this,'" + actual.id + "');\">" + actual.innerHTML + "</textarea>";
		}
		else {
			actual.innerHTML = "<input id=\""+ actual.id +"_field\" type=\"text\" value=\"" + actual.innerHTML + "\" onkeypress=\"return fieldEnter(this,event,'" + actual.id + "')\" onfocus=\"highLight(this);\" onblur=\"noLight(this); return fieldBlur(this,'" + actual.id + "');\" />";
		}

		changing = true;
	}

	var el = $(actual.firstChild);

	console.log( 'el.val(): '+el.val());

	switch (el.val()) {
		case 'Enter address':
		case 'Enter date':
		case 'Enter email':
		case 'Enter phone':
		case 'Enter relationship':
		case 'Enter website':
		case full_name_placeholder:
		case nickname_placeholder:
		case note_placeholder:
		case organization_placeholder:
		case password_placeholder:
		case phonetic_given_placeholder:
		case phonetic_middle_placeholder:
		case phonetic_family_placeholder:
		case title_placeholder:
		case username_placeholder:
			console.log( 'match: '+el.val());
			el.val('');
			break;
	}

	$(actual).removeClass('placeholder');

	actual.firstChild.focus();
}



//find all span tags with class editText and id as key:update parsed to update script. add onclick function
function editbox_init(){
	if (!document.getElementsByTagName){ return; }
	var spans = document.getElementsByTagName("span");

	// loop through all span tags
	for (var i=0; i<spans.length; i++){
		var spn = spans[i];

        	if (((' '+spn.className+' ').indexOf("editText") != -1) && (spn.id)) {
			spn.onclick = function () { editBox(this); }
			spn.style.cursor = "pointer";
			spn.title = "Click to edit!";	
       		}

	}

	$('.editText').each(function(){
		if ($(this).data('placeholder') == $(this).html()) {
			$(this).addClass('placeholder');
		}
	});
}

//crossbrowser load function

function addEvent(elm, evType, fn, useCapture)
{
	if (elm.addEventListener){
		elm.addEventListener(evType, fn, useCapture);
		return true;
	} else if (elm.attachEvent){
		var r = elm.attachEvent("on"+evType, fn);
		return r;
	} else {
		//alert("Please upgrade your browser to use full functionality on this page");
	}
}

//get width of text element
function widthEl(span){
	if(document.layers){
		w=document.layers[span].clip.width;
	} else if (document.all && !document.getElementById){
		w=document.all[span].offsetWidth;
	} else if(document.getElementById){
		w=document.getElementById(span).offsetWidth;
	}
	return w;
}

//get height of text element
function heightEl(span){

	if(document.layers){
		h=document.layers[span].clip.height;
	} else if (document.all && !document.getElementById){
		h=document.all[span].offsetHeight;
	} else if(document.getElementById){
		h=document.getElementById(span).offsetHeight;
	}
	return h;
}

function highLight(span){
	//span.parentNode.style.border = "2px solid #D1FDCD";
	//span.parentNode.style.padding = "0";
	span.style.border = "1px solid #54CE43";          
}

function noLight(span){
	//span.parentNode.style.border = "0px";
	//span.parentNode.style.padding = "2px";
	span.style.border = "0px";   
}

//sets post/get vars for update
function setVarsForm(vars){
	formVars  = vars;
}

addEvent(window, "load", editbox_init);
