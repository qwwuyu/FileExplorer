//打印
function L(str) {
    console.log(str);
}

//设置ajax默认配置
$.ajaxSetup({
    timeout: 5000,
    type: 'POST',
    cache: false,
    dataType: "json"
});

// 处理请求失败
function handErr(textStatus) {
    if ("timeout" == textStatus) {
        showErr("请求超时");
    } else if ("error" == textStatus) {
        showErr("请求失败");
    } else if ("abort" == textStatus) {
        showErr("请求取消");
    } else if ("parsererror" == textStatus) {
        showErr("解析数据出错");
    } else {
        showErr("请求失败");
    }
}

// 获取url without parameters
function getUrl() {
    return location.host + location.pathname;
}

// 获取url所有参数
function getRequest() {
    var url = location.search;
    var theRequest = {};
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        var strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            theRequest[strs[i].split("=")[0]] = (strs[i].split("=")[1]);
        }
    }
    return theRequest;
}

// 获取url某个参数
function getParam(key) {
    var url = location.search;
    if (url.indexOf("?") != -1) {
        var str = url.substr(1);
        var strs = str.split("&");
        for (var i = 0; i < strs.length; i++) {
            var split = strs[i].split("=");
            if (key == split[0]) {
                return (split[1]) ? split[1] : "";
            }
        }
    }
    return "";
}

Date.prototype.format = function (format) {
    var date = {
        "M+": this.getMonth() + 1,
        "d+": this.getDate(),
        "h+": this.getHours(),
        "m+": this.getMinutes(),
        "s+": this.getSeconds(),
        "q+": Math.floor((this.getMonth() + 3) / 3),
        "S+": this.getMilliseconds()
    };
    if (/(y+)/i.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear() + '')
            .substr(4 - RegExp.$1.length));
    }
    for (var k in date) {
        if (new RegExp("(" + k + ")").test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? date[k]
                : ("00" + date[k]).substr(("" + date[k]).length));
        }
    }
    return format;
};

function showTime(sysTime, time) {
    var now = new Date(sysTime);
    var date = new Date(time);
    var diff = sysTime - time;
    if (diff < 180000) {
        return "刚刚";
    } else if (diff < 3600000) {
        return Math.ceil(diff / 60000) + "分钟前";
    } else if (diff < 86400000) {
        return Math.ceil(diff / 3600000) + "小时前";
    } else if (now.getFullYear() == date.getFullYear()) {
        return date.format('MM-dd hh:mm:ss');
    } else {
        return date.format('yyyy-MM-dd hh:mm:ss');
    }
}

// 提醒
function showErr(err, time) {
    var alertTxt = $('<div class="alert"></div>').text(err);
    $('.alert-list').append(alertTxt);
    setTimeout(function () {
        alertTxt.remove();
    }, 'number' == typeof (time) ? time : 3000);
}

// 提醒
function showSucc(err, time) {
    var alertTxt = $('<div class="alert"></div>').text(err);
    $('.alert-list-succ').append(alertTxt);
    setTimeout(function () {
        alertTxt.remove();
    }, 'number' == typeof (time) ? time : 3000);
}

// 获取认证信息
function tokenInfo() {
    var token = Cookies.get('token');
    if ('string' == typeof (token)) {
        return JSON.parse(BASE64.decode(token));
    }
    return JSON.parse("{}");
}