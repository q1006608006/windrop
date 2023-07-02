$('#confirmConfig').click(function () {
    var vals = $('#ip-select').selectpicker("val")

    $.ajax({
        url: '/test/post',
        type: 'POST',
        data: JSON.stringify({
            ips: vals
        }),
        contentType: 'application/json',
        finish: function () {
            console.log('send ok')
        }
    }).done(function (msg) {
        console.log(msg)
    })
})