//翻页
function goto_page(event, page) {
    var pager = $(event.target).closest('ul');

    var pageNoField = $('#' + pager.data('page_no_field_id'));
    var curPageNo = pager.data('cur_page_no');
    var totalPageCount = pager.data('total_page_count');

    var targetPageNo = 1;
    if (page === 'first') {
        targetPageNo = 1;
    }
    else if (page === 'prev') {
        targetPageNo = curPageNo-1;
    }
    else if (page === 'next') {
        targetPageNo = curPageNo+1;
    }
    else if (page === 'last') {
        targetPageNo = totalPageCount;
    }
    else if (page === 'input') {
        var content =
            '<div class="modal fade" id="pager_goto_page_input_modal" tabindex="-1" role="dialog">'+
                '<div class="modal-dialog" role="document">'+
                     '<div class="modal-content">' +
                         '<div class="modal-header">' +
                             '<button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>' +
                             '<h4 class="modal-title">转到</h4>' +
                         '</div>' +
                         '<div class="modal-body">' +
                            '<p">' +
                                '<span>第</span>' +
                                '<input type="text" style="width:50px;margin:0 10px" id="pager_goto_page_input" />' +
                                '<span>页</span>' +
                            '</p>' +
                         '</div>' +
                         '<div class="modal-footer">' +
                            '<button type="submit" id="pager_goto_page_ok_btn" class="btn btn-primary">确定</button>' +
                            '<button type="button" class="btn btn-default" data-dismiss="modal">取消</button>' +
                         '</div>' +
                     '</div>'+
                '</div>'+
            '</div>';

        $('#pager_goto_page_input_modal').remove();
        $("body").append(content)

        $('#pager_goto_page_input_modal').on('shown.bs.modal', function() {
            $("#pager_goto_page_input").focus();
        })
        $('#pager_goto_page_input_modal').modal('show');

        $('#pager_goto_page_ok_btn').on('click', function(e) {
            goto_page(event, parseInt($("#pager_goto_page_input").val()));
        });

        return false;
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
function showModal(event, onloadfunc, options) {
    var largeModal = false;
    if (options && options.largeModal) {
        largeModal = true;
    }

    var source = event.target || event.srcElement;
    $('#modalTemplate .modal-dialog').load($(source).data('url'), function() {
        $('#modalTemplate').modal('show');
        if (typeof onloadfunc === "function") {
            onloadfunc();
        }
    });

    if (largeModal) {
        $('#modalTemplate .modal-dialog').addClass(" modal-lg");
    }
    else {
        $('#modalTemplate .modal-dialog').removeClass(" modal-lg");
    }

    return false;
}

function showPostFormModal(event, formId, modalStyleClass, reload, validateFunc, resultProcessFunc, onloadFunc) {
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

        if (typeof onloadFunc === "function") {
            onloadFunc();
        }
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

function IEObjectAssignPolyfill() {
    //IE Object.assign polyfill
    if (typeof Object.assign != 'function') {
        Object.assign = function(target, varArgs) { // .length of function is 2
            'use strict';
            if (target == null) { // TypeError if undefined or null
                throw new TypeError('Cannot convert undefined or null to object');
            }

            var to = Object(target);

            for (var index = 1; index < arguments.length; index++) {
                var nextSource = arguments[index];

                if (nextSource != null) { // Skip over if undefined or null
                    for (var nextKey in nextSource) {
                        // Avoid bugs when hasOwnProperty is shadowed
                        if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
                            to[nextKey] = nextSource[nextKey];
                        }
                    }
                }
            }
            return to;
        };
    }
}

//显示二维码
function showQrCode(event) {
    var source = event.target || event.srcElement;
    var qrCode = $(source).data('qrcode');
    var dialogTitle = $(source).data('dlgtitle');

    var content =
         '<div class="modal-content">' +
             '<div class="modal-header">' +
                 '<button type="button" class="close" data-dismiss="modal"><span>&times;</span></button>' +
                 '<h4 class="modal-title">' + dialogTitle + '</h4>' +
             '</div>' +
             '<div class="modal-body">' +
                  '<div id="qrCodeDiv" style="text-align:center"></div>' +
             '</div>' +
             '<div class="modal-footer">' +
                  '<button type="button" class="btn btn-default" data-dismiss="modal">关闭</button>' +
             '</div>' +
         '</div>';
    $('#modalTemplate .modal-dialog').empty().append(content)

    IEObjectAssignPolyfill();

    $('#qrCodeDiv').append(kjua({
       render: 'image',
       crisp: true,
       size: 200,
       fill: '#000',
       back: '#fff',
       text: qrCode,
       rounded: 0,
       quiet: 2,
       mode: 'plain',
    }));

    $('#modalTemplate').modal('show');

    return false;
}

function setCookie(name,value,days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days*24*60*60*1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + (value || "")  + expires + "; path=/";
}

function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for(var i=0;i < ca.length;i++) {
        var c = ca[i];
        while (c.charAt(0)==' ') c = c.substring(1,c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
    }
    return null;
}
function eraseCookie(name) {
    document.cookie = name+'=; Max-Age=-99999999;';
}

$(function() {
    $("body").on("collapsed.pushMenu", function() {
        setCookie("sideBarCollapsed", "true", 365);
    })
    $("body").on("expanded.pushMenu", function() {
        setCookie("sideBarCollapsed", "false", 365);
    })
});