$(document).ready(function () {
	var userName=$('.username:visible'),pwd=$('.password:visible');
    var COOKIE_NAME = 'qrtraceName';
    var COOKIE_PW = 'qrtracePw';
    if($.trim($('#errorMsg').text())!='') $('#errorMsg').css('visibility','visible');   
    if($.cookie(COOKIE_NAME)){
    	userName.val($.cookie(COOKIE_NAME));
        if($.cookie(COOKIE_PW)){
        	pwd.val($.cookie(COOKIE_PW));
        }
    }
    if(userName.val()!=''){userName.siblings('span').addClass('hide');}
    if(pwd.val()!=''){pwd.siblings('span').addClass('hide');}   
    userName.focus(function(){    	
    	$(this).siblings('span').addClass('hide');    		
    }).blur(function(){
        if($.trim(userName.val()) == ""){
        	$(this).siblings('span').removeClass('hide'); 
            $("#errorMsg").html("用户名不能为空！").css('visibility','visible');
        }
        else
        	{
        		$("#errorMsg").css('visibility','hidden');
        	}
    });

    pwd.focus(function(){    	
    	$(this).siblings('span').addClass('hide');   
    }).blur(function(){
        $("#errorMsg").css('visibility','hidden');
    });
      
    $("form").submit(function(){
    	var name=$.trim(userName.val()), pwdTxt=$.trim(pwd.val());
        if( name== ""){
            $("#errorMsg").html("用户名不能为空！").css('visibility','visible');
            return false;
        }else{
        	$("#errorMsg").css('visibility','hidden');
        	return true;
        }
    });

    userName.focus();
});