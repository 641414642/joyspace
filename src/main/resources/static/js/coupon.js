function validateCouponInput() {
    clearFormGroupErrMsg("#editCouponForm");

    var discount = $("#editCouponForm [name='discount']").val();
    var minExpense = $("#editCouponForm [name='minExpense']").val();

    if (Number(discount) > minExpense) {
        showFormGroupErrMsg("discount", "折扣金额不能大于最低消费");
        return false;
    }

    return true;
}

//添加编辑优惠券
function createOrEditCoupon(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");

    showModal(event, function() {
        var frm = $('#editCouponForm');
        frm
            .find('.date_picker')
            .datepicker({
                format: 'yyyy-mm-dd',
                startView: 0,
                todayHighlight: true,
                weekStart: 1,
                language: 'zh-CN',
                autoclose: true,
                todayBtn: true
            });
        frm.submit(function (ev) {
            if (validateCouponInput()) {
                $.ajax({
                    type: frm.attr('method'),
                    url: frm.attr('action'),
                    data: frm.serialize(),
                    success: function (data) {
                        if (data.errcode == 12138) {
                            showFormGroupErrMsg("code", data.errmsg)
                        } else {
                            $('#modalTemplate').modal('hide');
                            window.location.reload();
                        }
                    }
                });
            }

            ev.preventDefault();
        });
    });

    return false;

//    return showPostFormModal(event, 'editCouponForm', null, true);
}