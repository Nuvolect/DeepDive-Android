/**
 * Reload the page.  This function is inside the global scope. The location.reload() method
 * cannot be called directly from within a function.
 */
function reloadPage() {

    location.reload();
}

function get( url) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.open("GET", url, false);// Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send();
}

/**
 * Send the payload to the web server using POST method
 */
function post( url, payload ) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.open("POST", url, false);// Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( payload );
  //window.location=""

  reloadPage();
}
/**
 * Send the payload to the web server using POST method, stay on same page
 */
function postSamePage( url, payload ) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.open("POST", url, true);// Operates properly when true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( payload );
}

// Present the download modal and post a message to build the export.vcf file
function downloadFile( url, payload ) {

    $( "#my_modal" ).load( '/files/file_download_modal.htm' );

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.open("POST", url, false);// Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( payload );
}

function sendKeyElementId( key, id ) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.open("POST", "", false); // Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( key+"="+ document.getElementById( id ).value );

  reloadPage();
}

function validateLogin( emailId, passwordId, keepInId ){

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  var kv1 = "email="+ document.getElementById( emailId ).value;
  var kv2 = "password="+ document.getElementById( passwordId ).value;

  xmlhttp.open("POST", "", false); // Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( kv1 + "&" + kv2 );

  reloadPage();
}

function groupCb( url, cbId ){

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  var kv1 = "cb_group=" + cbId;
  var kv2 = "cb_state=" + document.getElementById( "cb_"+cbId ).value;

//alert(kv1 + "&" + kv2);

  xmlhttp.open("POST", "", false); // async is false, fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( kv1 + "&" + kv2 );
}

/**
 * Send a post method update and dynamically update the star element without a full refresh.
 */
function starUpdate( contact_id, star_state, payload ) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.onreadystatechange=function() {

    if (xmlhttp.readyState==4 && xmlhttp.status==200) {
      var s;
      if( star_state == "0"){
      s = "<span class=\"glyphicon glyphicon-star\" style=\"font-size:150%;\" \
              onclick=\"starUpdate(" +contact_id+ ",1,\'item_star="+contact_id+"\');\"/>";
      } else {
      s = "<span class=\"glyphicon glyphicon-star-empty\" style=\"font-size:150%;\" \
              onclick=\"starUpdate(" +contact_id+ ",0,\'item_star="+contact_id+"\');\"/>";
      }
      document.getElementById("item_"+contact_id).innerHTML= s;
    }
  };

  xmlhttp.open("POST", "", false); // Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( payload );
}
/**
 * Send a post method update and dynamically update the checkbox element without a full refresh.
 */
function cbUpdate( contact_id, cb_state, payload ) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.onreadystatechange=function() {

    if (xmlhttp.readyState==4 && xmlhttp.status==200) {
      var s;
      if( cb_state == "0"){
          s = "<span class=\"glyphicon glyphicon-check\" style=\"font-size:125%;\" \
              onclick=\"cbUpdate(" +contact_id+ ",1,\'item_cb="+contact_id+"\');\"/>";
      } else {
          s = "<span class=\"glyphicon glyphicon-unchecked\" style=\"font-size:125%;\" \
              onclick=\"cbUpdate(" +contact_id+ ",0,\'item_cb="+contact_id+"\');\"/>";
      }
      document.getElementById("cb_"+contact_id).innerHTML= s;
    }
  };

  xmlhttp.open("POST", "", false); // Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send( payload );
}

function deleteItem( id, tVar,cat, value ) {

var xmlhttp;
if (window.XMLHttpRequest)
  {// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
  }
else
  {// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }

  xmlhttp.open("POST", "", false); // Fails when set to true
  xmlhttp.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
  xmlhttp.send(  "contact_id="+id+ "&type="+tVar+  "&cat="+cat+  "&value="+value  );

  reloadPage();
}

/*
 * Create a new empty group.
 */
function newGroup( url ){

    var group = prompt("Please enter a new group name", "");

    if (group !== null) {

        post( url, "new_group="+group);
    }
}

/**
 * Rename group. Confirm that the group being renamed is not a protected group and that
 * the new group name is not a protected group name
 */
function renameGroup( url ){

    var group = prompt("Please enter a new group name", "");

    if (group !== null) {

        post( url, "rename_group="+group);
    }
}


/**
 * When the new group is created, assign currently selected contacts to it
 * function newGroupContacts( url ){
 */
function newGroupContacts( url ){

    var group = prompt("Please enter a new group name", "");

    if (group !== null) {

        post( url, "new_group_contacts="+group);
    }
}

function printPage(){

    window.print();
}


function notifyJs( message, style ){

    $.notify( message, style);
}

function injectModal( htmTemplate){

    // document.getElementById("my_modal").innerHTML= htmTemplate;
//debugger;
    $( "#my_modal" ).load( htmTemplate );
}

function clearModal(){

    document.getElementById("my_modal").innerHTML= "";
}

function clearFileInputList(){

    $("#js-upload-files").replaceWith($("#js-upload-files").clone(true));
}

function clearFileInput(id) {

    var oldInput = document.getElementById(id);

    var newInput = document.createElement("input");

    newInput.type = "file";
    newInput.id = oldInput.id;
    newInput.name = oldInput.name;
    newInput.className = oldInput.className;
    newInput.style.cssText = oldInput.style.cssText;

    oldInput.parentNode.replaceChild(newInput, oldInput);
}
function resetFormElement(e) {
  e.wrap('<form>').closest('form').get(0).reset();
  e.unwrap();
}

// Collect the password parameters and create a POST event to generate the password
function genPassword(){

    var mode = 0;
    var characters = "";

    if( document.getElementById( 'password_caps' ).value == 1){
        mode = 1;
        characters += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    }
    if( document.getElementById( 'password_lc' ).value  == 1){
        mode = mode | 2;
        characters += "abcdefghijklmnopqrstuvwxyz";
    }
    if( document.getElementById( 'password_09' ).value  == 1){
        mode = mode | 4;
        characters += "0123456789";
    }
    if( document.getElementById( 'password_spec' ).value  == 1){
        mode = mode | 8;
       characters += "!$%@#";
    }
    if( document.getElementById( 'password_hex' ).value  == 1){
        mode = mode | 16;
        characters += "0123456789abcdef";
    }
    if( characters.length === 0)
        characters = "0123456789";

    var charactersLength = characters.length;
    var buffer = "";
    //var password_length = document.getElementById( 'password_length' ).innerText;
    var password_length = $('#password_length').text();
    var i;

    for (i = 0; i < password_length; i++) {
        var index = Math.floor( Math.random() * charactersLength);
        buffer += characters.charAt( index );
    }
    // Update the password history spinner label
    setHistorySelectLabel( buffer );

    // Update the password history spinner content
    var html = "<li onclick=\"setHistorySelectLabel('"+buffer+"')\">"+buffer+"</li>";
    $("#password_history ul").prepend( html );

    // Encode the password with UTF-8.  The server will automatically decode it to store in params.
    var encoded = encodeURI( buffer);

    postSamePage("", "password_mode="+mode +"&password_length="+password_length+"&password="+encoded);
}

// The user has selected a new length, update the spinner label
function passwordLengthSelect( len ){

    var html = len+" <span class='caret'></span>";
    document.getElementById('password_length').innerHTML = html;
}

function setHistorySelectLabel( password_select ){

    // Update the password history spinner label
    var html = password_select+" <span class='caret'></span>";
    document.getElementById('newest_password').innerHTML= html;
}

// this variable will keep track of the password field the user is trying to upload via the modal
var passwordHolder;

function openPasswordModal(el) {
	passwordHolder = $(el).closest('td').next('td').find('span').first();
	injectModal('/files/password_modal_apply_filled.htm');
}

function usePassword(){
    var password = $.trim($('#newest_password').text());
    passwordHolder.html(password);
	passwordHolder.removeClass('placeholder');
	if (password == '') {
		passwordHolder.html(passwordHolder.data('placeholder'));
		passwordHolder.addClass('placeholder');
	}
    postSamePage('', 'password_update=' + password);
}

// Copy the text of a specific ID to the users paste buffer
function copyToClipboard( id ) {

      var text = document.getElementById(id).innerText;
      window.prompt("Copy to clipboard: Ctrl+C, Enter", text);
}

function showFileUploadModal(){

  $(document).ready(function(){
    $("#fileUploadModal").modal('show');
  });
}

$(function() {
	// preload background images
	$('a.themes').each(function() {
		$('<img/>').attr('src', '/css/themes/' + $(this).data('theme') + '-bg.png');
	});

	// change theme by changing the path to the css stylesheet in the link element with id theme in the head
	$('a.theme').click(function(e) {
		e.preventDefault();

        var theme = $(this).data('theme');

		$('head #theme').attr('href', '/css/themes/' + theme + '.css');
		$(this).closest('ul').find('li').removeClass('active');
		$(this).closest('li').addClass('active');

		postSamePage(baseUrl, 'theme=' + theme);
	});

	// change spacing by adding a class to the body (actual spacing is handled in the css)
	$('a.spacing').click(function(e) {
		e.preventDefault();

		var spacings = ['comfortable', 'cozy', 'compact'];
		for (var i=0; i < spacings.length; i++) {
			$(document.body).removeClass(spacings[i]);
		}

        var spacing = $(this).data('spacing');

		$(document.body).addClass(spacing);
		$(this).closest('ul').find('li').removeClass('active');
		$(this).closest('li').addClass('active');

		postSamePage(baseUrl, 'spacing=' + spacing);
	});

	// autocomplete search - ***** future feature
	$('#search_field').autocomplete({
      	minLength: 3,
      	source: 'json.php',
      	focus: function(event, ui) {
        	$('#search_field').val(ui.item.name);
        	return false;
      	},
      	select: function(event, ui) {
      	    // set the value of the search box the name that was selected
        	$('#search_field').val(ui.item.name);
        	// do something with the value returned (ie: redirect to that user's details)
        	//postSamePage('details.htm', 'id=' + ui.item.id);
			return false;
      	}
    })
    .autocomplete('instance')._renderItem = function(ul, item) {
      	var re = new RegExp('(' + this.term + ')', 'gi');
		var template = '<strong>$1</strong>';
		var html = item.name.replace(re, template);

		return $('<li>')
        	.append('<a>' + html + '</a>')
        	.appendTo(ul);
    };

    // clear the search box when the x is clicked
	$('#clear-search').click(function() {
		$('#search_field').val('');
		$('#clear-search').css({
            'z-index': -1,
            opacity: 0
        });
	});

    // check if the user has typed anything and if so show the x. otherwise, hide it
	$('#search_field').keyup(function() {
		$('#clear-search').css({
		    'z-index': 2,
		    opacity: 1
		});

		if ($(this).val() === '') {
			$('#clear-search').css({
			    'z-index': -1,
			    opacity: 0
			});
		}
	});

    // see if the search field is already populated and if so display the x
    if ($('#search_field').val() !== '') {
        $('#clear-search').css({
            'z-index': 2,
            opacity: 1
        });
    }
});
