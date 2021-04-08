// 当input标签文件被置换
$('.custom-file-input').on('change', function () {
    $(this).next('.custom-file-label').html($(this)[0].files[0].name);
    $('.custom-file-label').attr("data-browse", '');
    // 恢复提交按钮
    // $('button[type=submit]').prop('disabled', false);
});
// 重选文件，重选按钮被点击后执行
$('.reset').click(function () {
    $(this).parent().prev().children('.custom-file-label').html('点击选择...');
    $('.custom-file-label').attr("data-browse", '选择文件');
    // 恢复提交按钮
    // $('button[type=submit]').prop('disabled', false);
});
// 提交按钮被点击后执行
$("#uploadFileBtn").click(function (e) {

    e.preventDefault();
    // 置灰按钮无效，前台防止双重提交
    $(this).prop('disabled', true);

    // 获取文件
    var file = $('#customFile')[0].files[0];
    var hidden = $('#hidden').attr("value");
    var formData = new FormData();
    formData.append("file", file);
    formData.append("hidden", hidden);

    $.ajax({
        url: '/windrop/addition/upload',
        type: 'POST',
        data: formData,
        cache: false,
        contentType: false,
        processData: false,
        xhr: function () {
            //Get XmlHttpRequest object
            var xhr = $.ajaxSettings.xhr();

            // 设置onprogress事件控制器
            xhr.upload.onprogress = function (event) {
                var perc = Math.round((event.loaded / event.total) * 100);
                $('#progressBar').text(perc + '%');
                $('#progressBar').css('width', perc + '%');
            };
            return xhr;
        },
        beforeSend: function (xhr) {
            // 提交前重置提示消息为空，并重置进度条
            $('#alertMsg').text('');
            $('#progressBar').text('');
            $('#progressBar').css('width', '0%');
        }
    })
        .done(function (msg) {
            // 添加提示框显示类
            $('#alertDiv').addClass("show");
            // 设置返回消息
            $('#alertMsg').text(msg);
            // 清空文件
            $('input[type=file]').val('');
            // 恢复提交按钮
            // $('button[type=submit]').prop('disabled', false);
        })
        .fail(function (jqXHR) {
            // 添加提示框显示类
            $('#alertDiv').addClass("show");
            // 设置返回消息
            $('#alertMsg').text("发生错误");
        });
    return false;
});