function tpriceEnabled(event) {

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


//添加编辑阶梯价格
function createOrEditTPrice(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");

    showModal(event, function() {
        var frm = $('#editTPriceForm');
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

            ev.preventDefault();
        });
    });

    return false;
}