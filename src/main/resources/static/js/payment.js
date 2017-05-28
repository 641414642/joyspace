//修改密码
function editWxPay(event) {
    showModal(event, function() {
        $('#editWxPayForm').submit(function (ev) {
            var frm = $('#editWxPayForm');
//            var newPass = $('#new-pass');
//            var repeatNewPass = $('#repeat-new-pass');
//
//            $('#changePwdModal .form-group').removeClass('has-error');
//            $('#changePwdModal .help-block').text('');
//
//            if (newPass.val().length < 6) {
//                newPass.closest('.form-group').addClass('has-error');
//                newPass.siblings('.help-block').text('密码长度不能小于6个字符');
//            }
//            else if (newPass.val() != repeatNewPass.val()) {
//                repeatNewPass.closest('.form-group').addClass('has-error');
//                repeatNewPass.siblings('.help-block').text('两次输入的密码不一致');
//            }
//            else {
                $.ajax({
                    type: frm.attr('method'),
                    url: frm.attr('action'),
                    data: frm.serialize(),
                    success: function (data) {
                        $('#modalTemplate').modal('hide');
                        window.location.reload();
                    }
                });
//            }

            ev.preventDefault();
        });
    });

    return false;
}