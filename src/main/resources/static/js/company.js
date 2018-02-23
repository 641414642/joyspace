function processCompanyResult(data) {
    if (data.errcode == 0) {
        return true;
    }
    else {
        if (data.errcode == 20) {
            showFormGroupErrMsg("name", data.errmsg);
        }
        else if (data.errcode == 21) {
            showFormGroupErrMsg("username", data.errmsg);
        }
        else {
            alert(data.errmsg);
        }

        return false;
    }
}

function validateCompanyInput(formId) {
    clearFormGroupErrMsg("#" + formId);

    var name = $("#" + formId + " [name='name']").val();
    var username = $("#" + formId + " [name='username']").val();
    var password = $("#" + formId + " [name='password']").val();
    var fullname = $("#" + formId + " [name='fullname']").val();
    var phone = $("#" + formId + " [name='phone']").val();
    var email = $("#" + formId + " [name='email']").val();

    if (name.length === 0 || !name.trim()) {
        showFormGroupErrMsg("name", "投放商名称不能为空!");
        return false;
    }
    else if (username.length === 0 || !username.trim()) {
        showFormGroupErrMsg("username", "管理员登录帐号不能为空!");
        return false;
    }
    else if (formId == "createCompanyForm" && (password.length === 0 || !password.trim())) {
        showFormGroupErrMsg("password", "管理员登录密码不能为空!");
        return false;
    }
    else if (fullname.length === 0 || !fullname.trim()) {
        showFormGroupErrMsg("fullname", "管理员姓名不能为空!");
        return false;
    }
    else if (phone.length === 0 || !phone.trim()) {
        showFormGroupErrMsg("phone", "管理员手机号码不能为空!");
        return false;
    }
    else if (phone.trim().length != 11) {
        showFormGroupErrMsg("phone", "管理员手机号码格式错误!");
        return false;
    }
    else if (email.length === 0 || !email.trim()) {
        showFormGroupErrMsg("email", "电子邮件地址不能为空!");
        return false;
    }

    return true;
}

//添加编辑店面
function createOrEditCompany(event, create) {
    var formId = create ? 'createCompanyForm' : 'editCompanyForm';
    return showPostFormModal(event, formId, null, true,
        function() {
            return validateCompanyInput(formId);
        },
        processCompanyResult);
}

//添加微信收款账户
function startAddCompanyWxAccount(event) {
    return showModal(event, function() {
        $('#wxmpQrCodeDiv').append(kjua({
           render: 'image',
           crisp: true,
           size: 200,
           fill: '#000',
           back: '#fff',
           text: $("#wxmpQrCodeDiv").data("qrcode"),
           rounded: 0,
           quiet: 2,
           mode: 'plain',
        }));

        $('#qrCodeDiv').append(kjua({
           render: 'image',
           crisp: true,
           size: 200,
           fill: '#000',
           back: '#fff',
           text: $("#qrCodeDiv").data("qrcode"),
           rounded: 0,
           quiet: 2,
           mode: 'plain',
        }));

        $('#modalTemplate').on('hidden.bs.modal', function () {
            window.location.reload();
        });
    });
}

//确认删除收款账户
function deleteCompanyWxAccount(event) {
    var source = event.target || event.srcElement;
    $("#accountId").val($(source).data("id"));
    $("#accountName").text($(source).data("name"));
    $("#confirmDeleteDialog").modal("show");
}

$(function() {
    var frm = $('#deleteAccountForm');
    frm.submit(function (ev) {
        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: frm.serialize(),
            success: function (data) {
                if (data.errcode == 0) {
                    window.location.reload();
                }
                else {
                    alert(data.errmsg);
                }
            }
        });

        ev.preventDefault();
    });
})

function moveWxAccount(event, up) {
    var source = event.target || event.srcElement;
    var actionUrl = $(source).data('url');

    $.ajax({
        type: 'post',
        url: actionUrl,
        success: function (data) {
            if (data) {
                window.location.reload();
            }
        }
    });
}
function toggleWxAccount(event, up) {
    var source = event.target || event.srcElement;
    var actionUrl = $(source).data('url');

    $.ajax({
        type: 'post',
        url: actionUrl,
        success: function (data) {
            if (data) {
                window.location.reload();
            }
        }
    });
}