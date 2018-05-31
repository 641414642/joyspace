//添加编辑产品
function createOrEditProduct(event) {
    return showPostFormModal(event, 'editProductForm', null, true);
}

//预览广告图片
function previewImage(input) {
  if (input.files && input.files[0]) {
    var reader = new FileReader();

    reader.onload = function(e) {
      $(input).parent()
        .css('background-image', 'url(' + e.target.result + ')')
        .css('background-size', 'cover')
    }

    reader.readAsDataURL(input.files[0]);
  }
}

//添加编辑广告
function createOrEditAdSet(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");
    showModal(event, function() {
         $('.ad_img_chooser').change(function() {
            previewImage(this)
            $(this).closest('tr').next().show()
         });
     });
    return false;
}

//上传产品图片文件
function manageProductImageFiles(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");
    showModal(event, function() {
        $('#productPrevImgFileInput').change(function() {
          $('#uploadProductPrevImgFileForm').submit();
        });

        $('#productThumbImgFileInput').change(function() {
          $('#uploadProductThumbImgFileForm').submit();
        });
    });
    return false;
}

//删除产品图片
function deleteProductImage(event) {
    if (confirm("您确定要删除此图片吗?")) {
        var source = event.target || event.srcElement;
        $(source).closest('form').submit();
    }
}

//上传店面图片文件
function managePositionImageFiles(event) {
    $('#modalTemplate').removeClass().addClass("modal fade");
    showModal(event, function() {
        $('#positionImgFileInput').change(function() {
          $('#uploadPositionImgFileForm').submit();
        });
    });
    return false;
}

//删除店面图片
function deletePositionImage(event) {
    if (confirm("您确定要删除此图片吗?")) {
        var source = event.target || event.srcElement;
        $(source).closest('form').submit();
    }
}
//查看价目表
function viewPriceList(event) {
    return showPostFormModal(event, 'editPriceListForm', 'modal-info', true);
}

//添加编辑价目表
function createOrEditPriceList(event) {
    return showPostFormModal(event, 'editPriceListForm', null, true);
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
            //if (typeof validateFunc !== "function" || validateFunc()) {
                $.ajax({
                    type: frm.attr('method'),
                    url: frm.attr('action'),
                    data: frm.serialize(),
                    success: function (data) {
                        $('#modalTemplate').modal('hide');
                        window.location.reload();
                    }
                });
            //}

            ev.preventDefault();
        });
    });

    return false;

//    return showPostFormModal(event, 'editCouponForm', null, true);
}

function moveProduct(event, up) {
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

function couponEnabled(event) {

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

//确认删除产品
function deleteProduct(event) {
    var source = event.target || event.srcElement;
    $("#productId").val($(source).data("id"));
    $("#productName").text($(source).data("name"));
    $("#confirmDeleteDialog").modal("show");
}

$(function() {
    var frm = $('#deleteProductForm');
    frm.submit(function (ev) {
        $.ajax({
            type: frm.attr('method'),
            url: frm.attr('action'),
            data: frm.serialize(),
            success: function (data) {
                if (data) {
                    window.location.reload();
                }
            }
        });

        ev.preventDefault();
    });
})
