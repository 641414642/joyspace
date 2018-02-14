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

function validateCompanyInput() {
    clearFormGroupErrMsg("#createCompanyForm");

    var name = $("#createCompanyForm [name='name']").val();
    var username = $("#createCompanyForm [name='username']").val();

    if (name.length === 0 || !name.trim()) {
        showFormGroupErrMsg("name", "投放商名称不能为空!");
        return false;
    }
    else if (username.length === 0 || !username.trim()) {
        showFormGroupErrMsg("username", "管理员登录帐号不能为空!");
        return false;
    }

    return true;
}

//添加编辑店面
function createOrEditCompany(event, create) {
    return showPostFormModal(event, create ? 'createCompanyForm' : 'editCompanyForm', null, true, validateCompanyInput, processCompanyResult);
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