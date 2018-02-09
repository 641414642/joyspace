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

$('#changePwdModal').on('show.bs.modal', function () {
    $('#changePwdModal .form-group').removeClass('has-error');
    $('#changePwdModal .help-block').text('');
    $('#changePwdModal input').val('');
})

//显示form错误信息
function showFormGroupErrMsg(elementName, errMsg) {
    var ele = $("[name='" + elementName + "']");
    var grp = ele.closest(".form-group");
    var helpBlock = grp.find(".help-block");

    if (errMsg === null || errMsg === "") {
        grp.removeClass("has-error");
        helpBlock.text("");
    }
    else {
        grp.addClass("has-error");
        helpBlock.text(errMsg);
    }
}

//清除错误信息
function clearFormGroupErrMsg(ele) {
    $(ele).find(".form-group").removeClass("has-error");
}

//显示对话框
function showModal(event, onloadfunc) {
    var source = event.target || event.srcElement;
    $('#modalTemplate .modal-dialog').load($(source).data('url'), function() {
        $('#modalTemplate').modal('show');
        if (onloadfunc) {
            onloadfunc();
        }
    });
    return false;
}

function showPostFormModal(event, formId, modalStyleClass, reload, validateFunc, resultProcessFunc) {
    $('#modalTemplate').removeClass().addClass("modal fade");

    if (modalStyleClass) {
        $('#modalTemplate').addClass(modalStyleClass);
    }

    showModal(event, function() {
        var frm = $('#' + formId);
        frm.submit(function (ev) {
            if (typeof validateFunc !== "function" || validateFunc()) {
                $.ajax({
                    type: frm.attr('method'),
                    url: frm.attr('action'),
                    data: frm.serialize(),
                    success: function (data) {
                        if (typeof resultProcessFunc === "function") {
                            if (resultProcessFunc(data)) {
                                $('#modalTemplate').modal('hide');
                                if (reload) {
                                    window.location.reload();
                                }
                            }
                        }
                        else {
                            $('#modalTemplate').modal('hide');
                            if (reload) {
                                window.location.reload();
                            }
                        }
                    }
                });
            }

            ev.preventDefault();
        });
    });

    return false;
}

function changePassword(event) {
    return showPostFormModal(event, 'changePassForm', null, false, function() {
        var newPass = $('#new-pass');
        var repeatNewPass = $('#repeat-new-pass');

        $('#changePassForm .form-group').removeClass('has-error');
        $('#changePassForm .help-block').text('');

        if (newPass.val().length < 6) {
            newPass.closest('.form-group').addClass('has-error');
            newPass.siblings('.help-block').text('密码长度不能小于6个字符');
            return false;
        }
        else if (newPass.val() != repeatNewPass.val()) {
            repeatNewPass.closest('.form-group').addClass('has-error');
            repeatNewPass.siblings('.help-block').text('两次输入的密码不一致');
            return false;
        }
        else {
            return true;
        }
    });
}
