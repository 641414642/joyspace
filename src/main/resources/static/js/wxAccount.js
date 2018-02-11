function showTooltips(msg) {
    var $tooltips = $('.js_tooltips');

    $tooltips.text(msg);

    if ($tooltips.css('display') != 'none') {
        return;
    }

    $tooltips.css('display', 'block');
    setTimeout(function () {
        $tooltips.css('display', 'none');
    }, 2000);
}

$(function(){
    var frm = $("#addWxAccountForm");
    frm.submit(function(e) {
        e.preventDefault();

        var realname = $("input[name='realname']").val();
        var phoneNumber = $("input[name='phone']").val();
        var verifyCode = $("input[name='verifyCode']").val();

        if (realname.length === 0 || !realname.trim()) {
            showTooltips("请输入真实姓名!");
        }
        else if (phoneNumber.length === 0 || !phoneNumber.trim()) {
            showTooltips("请输入手机号!");
        }
        else if (verifyCode.length === 0 || !verifyCode.trim()) {
            showTooltips("请输入验证码!");
        }
        else if (!$("#agree").is(":checked")) {
            showTooltips("请阅读并同意《相关条款》!");
        }
        else {
            $.ajax({
                type: frm.attr('method'),
                url: frm.attr('action'),
                data: frm.serialize(),
                success: function (data) {
                    $(".page__hd").hide();
                    $("#addWxAccountForm").hide();

                    if (data.errcode == 0) {
                        $("#success_msg").show();
                    }
                    else {
                        $("#fail_msg_detail").text(data.errmsg);
                        $("#fail_msg").show();
                    }
                }
            });
        }
    })
})