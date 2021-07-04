function calculate(file, callBack) {
    /*
    * 	file 选取的文件
    * 	callBack 回调函数可以返回获取的MD5
    */
    var fileReader = new FileReader(),
        blobSlice = File.prototype.mozSlice || File.prototype.webkitSlice || File.prototype.slice,
        chunkSize = 2097152,
        // read in chunks of 2MB
        chunks = Math.ceil(file.size / chunkSize),
        currentChunk = 0,
        spark = new SparkMD5();

    fileReader.onload = function (e) {
        spark.appendBinary(e.target.result); // append binary string
        currentChunk++;

        if (currentChunk < chunks) {
            loadNext();
        } else {
            callBack(spark.end());
        }
    };

    function loadNext() {
        var start = currentChunk * chunkSize,
            end = start + chunkSize >= file.size ? file.size : start + chunkSize;

        fileReader.readAsBinaryString(blobSlice.call(file, start, end));
    }
    loadNext();
};

function toShortSize(size) {
    let KB_SIZE = 1024;
    let MB_SIZE = 1024 * KB_SIZE;
    let GB_SIZE = 1024 * MB_SIZE;
    if (size > GB_SIZE) {
        return (size / GB_SIZE).toFixed(2) + "GB";
    } else if (size > MB_SIZE) {
        return (size / MB_SIZE).toFixed(2) + "MB";
    } else if (size > KB_SIZE) {
        return (size / KB_SIZE).toFixed(2) + "KB";
    } else {
        return size + "B";
    }
}

function toShortName(name) {
    if(name.length > 24) {
        return name.substr(0,15) + "..." + name.substr(name.length - 7)
    } else {
        return name;
    }
}

// 当input标签文件被置换
$('.custom-file-input').on('change', function () {
    let file = $(this)[0].files[0];
    // $(this).next('.custom-file-label').html(toShortName(file.name));
    $('.custom-file-label').attr("data-browse", '').text(toShortName(file.name));

    $("#fileName").text(file.name)
    $("#fileSize").text(toShortSize(file.size))
    $("#fileMd5").text('（正在计算，请稍等......）');

    calculate(file, function (md5) {
        $("#fileMd5").text(md5);
    })
    // 恢复提交按钮
    // $('button[type=submit]').prop('disabled', false);
});
// 重选文件，重选按钮被点击后执行
$('.reset').click(function () {
    $(this).parent().prev().children('.custom-file-label').html('点击选择...');
    $('.custom-file-label').attr("data-browse", '选择文件');
    $("#fileName").text('')
    $("#fileSize").text('')
    $("#fileMd5").text('')

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
            $('#alertMsg').text(msg.message);
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