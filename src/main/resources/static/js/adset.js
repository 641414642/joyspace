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

        $('#adImgFileInput').change(function() {
            $('#uploadAdImgForm').submit();
        });

        $("#adImageTable .up, #adImageTable .down").click(function(){
            var row = $(this).parents("tr:first");
            if ($(this).is(".up")) {
                if (!row.prev().hasClass("header_row")) {
                    row.insertBefore(row.prev());
                }
            } else {
                if (row.next().length > 0 && row.next().is(":visible")) {
                   row.insertAfter(row.next());
                }
            }
        });

        $("#adImageTable .delete").click(function(){
            $(this).parents("tr:first").remove();
        });
     });
    return false;
}

function displayNewImgRow(tempAdSetImgFileName, thumbUrl) {
    var tr = $("#adImageTable tr:hidden:first");
    tr.find(".ad_img_display").css("background-image", "url(" + thumbUrl + ")");
    tr.find("input[name^=uploadFileName]").val(tempAdSetImgFileName);
    tr.show();
}

function submitAdSetImgEditForm() {
    $("#adImageTable input[name^=sequence]").each(function(index) {
        $(this).val(index);
    });

    $('#editAdSetForm').submit();
    $('#modalTemplate').modal('hide');
}