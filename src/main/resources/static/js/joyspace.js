//翻页
function goto_page(event, page) {
    var pager = $(event.target).closest('ul');

    var pageNoField = $('#' + pager.data('page_no_field_id'));
    var curPageNo = pager.data('cur_page_no');
    var totalPageCount = pager.data('total_page_count');

    var targetPageNo = 1;
    if (page == 'first') {
        targetPageNo = 1;
    }
    else if (page == 'prev') {
        targetPageNo = curPageNo-1;
    }
    else if (page == 'next') {
        targetPageNo = curPageNo+1;
    }
    else if (page == 'last') {
        targetPageNo = totalPageCount;
    }
    else {
        targetPageNo = page;
    }

    if (targetPageNo > 0 && targetPageNo <= totalPageCount && targetPageNo != curPageNo) {
        pageNoField.val(targetPageNo);
        pageNoField.closest("form")[0].submit();
    }

    return false;
}

//修改密码
$('#changePassForm').submit(function (ev) {
    var frm = $('#changePassForm');
    var newPass = $('#new-pass');
    var repeatNewPass = $('#repeat-new-pass');

    $('#changePwdModal .form-group').removeClass('has-error');
    $('#changePwdModal .help-block').text('');

    if (newPass.val().length < 6) {
        newPass.closest('.form-group').addClass('has-error');
        newPass.siblings('.help-block').text('密码长度不能小于6个字符');
    }
    else if (newPass.val() != repeatNewPass.val()) {
        repeatNewPass.closest('.form-group').addClass('has-error');
        repeatNewPass.siblings('.help-block').text('两次输入的密码不一致');
    }
    else {
        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: frm.serialize(),
            success: function (data) {
                $('#changePwdModal').modal('hide');
            }
        });
    }

    ev.preventDefault();
});

$('#changePwdModal').on('show.bs.modal', function () {
    $('#changePwdModal .form-group').removeClass('has-error');
    $('#changePwdModal .help-block').text('');
    $('#changePwdModal input').val('');
})