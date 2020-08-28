var isHistoryApi = !!(window.history && history.pushState);
// temp_file模版
template(
    'temp_file',
    '<ul> {{each datas}} <li> {{if $value.dir == true}} <a class="file-folder flex-center" href="{{dirPath}}{{$value.path}}" data-name="{{$value.name}}"> <i class="ion-icon ion-folder"></i> <span class="file-text file-text-folder">{{$value.name}}</span> </a> {{if $value.date != null || $value.info != null}} <div class="flex-center file-ctrl"> {{if $value.date != null}} <span class="file-date ml12">{{$value.date}}</span> {{/if}} {{if $value.date != null && $value.info != null}} <span class="file-info ml24">{{$value.info}}</span> {{else $value.info != null}} <span class="file-info ml12">{{$value.info}}</span> {{/if}} </div> {{/if}} {{else if $value.dir != true}} <div class="flex-center"> <i class="ion-icon ion-document"></i> <span class="file-text file-text-file">{{$value.name}}</span> </div> <div class="flex-center file-ctrl"> <a class="file-open ml12" href="{{openPath}}{{$value.path}}" target="_blank">打开</a> <a class="file-download ml24" href="{{downloadPath}}{{$value.path}}">下载</a> <a class="file-delete ml24" href="javascript:;" data-name="{{$value.name}}">删除</a> {{if $value.apk == true}} <a class="file-apk ml24" href="javascript:;" data-name="{{$value.name}}">安装</a> {{/if}} {{if $value.date != null}} <span class="file-date ml24">{{$value.date}}</span> {{/if}} {{if $value.info != null}} <span class="file-info ml24">{{$value.info}}</span> {{/if}} </div> {{/if}} </li> {{/each}}</ul>');

$(document).ready(function () {
    initUpload(getParam("path"));
    $("#progress").hide();
    $("#result").hide();
    if (isHistoryApi) {
        $(window).on("popstate", function (event) {
            var path = getParam("path");
            initUpload(path);
        });
    }
    $('body').on('click', '.file-folder', function (e) {
        if (!isHistoryApi) {
            return true;
        }
        var oldPath = getParam("path");
        var name = $(this).data("name");
        var path = oldPath + ("" != oldPath ? "/" : "") + name;
        var url = location.pathname + "?path=" + path;
        history.pushState(null, name, url);
        initUpload(path);
        return false;
    }).on('click', '.file-delete', function (e) {
        var name = $(this).data("name");
        if (confirm("确认要删除文件：" + name + "?")) {
            var oldPath = getParam("path");
            var path = oldPath + ("" != oldPath ? "/" : "") + name;
            deleteFile(path, $(this))
        }
    }).on('click', '.file-apk', function (e) {
        var name = $(this).data("name");
        if (confirm("确认要安装：" + name + "?确认后在手机进行下一步操作.")) {
            var oldPath = getParam("path");
            var path = oldPath + ("" != oldPath ? "/" : "") + name;
            apkFile(path, $(this))
        }
    }).on('click', '#back', function (e) {
        var oldPath = getParam("path");
        goBack(oldPath)
    }).on('click', '#deleteDir', function (e) {
        var oldPath = getParam("path");
        if ("" != oldPath && confirm("确认要删除目录：" + decodeURI(oldPath) + "?")) {
            deleteDir(oldPath)
        }
    }).on('click', '#newDir', function (e) {
        var word = prompt("输入文件夹名称");
        if (word && "" != word) {
            var path = getParam("path");
            newDir(word, path)
        }
    });
});

function requestFile(path) {
    var request = $.ajax({
        url: location.pathname + "i/query?path=" + path
    });
    request.then(function (data) {
        if (1 == data.state) {
            handFileData(data.data);
        } else {
            handFileData({});
        }
        if (1 != data.state && data.info) {
            showErr(data.info);
        }
    }, function (jqXHR, textStatus, errorThrown) {
        handErr(textStatus);
        handFileData({});
    });
}

// 处理列表数据
function handFileData(list) {
    var oldPath = getParam("path");
    oldPath = oldPath + ("" != oldPath ? "/" : "");
    for (let i = 0; i < list.length; i++) {
        list[i].path = list[i].name
    }
    var temp_file = template('temp_file', {
        datas: list,
        dirPath: location.pathname + "?path=" + oldPath,
        downloadPath: location.pathname + "i/download?path=" + oldPath,
        openPath: location.pathname + "i/open?path=" + oldPath
    });
    $('.content').html(temp_file);
    setDeleteDir();
}

function setDeleteDir() {
    if ($('.content').find("li").length != 0) {
        $("#deleteDir").hide();
    } else {
        $("#deleteDir").show();
    }
}

function initUpload(path) {
    $("#deleteDir").hide();
    if (path == "") {
        $("#back").hide();
        $("#dir").text("全部文件");
    } else {
        $("#back").show();
        $("#dir").text(decodeURI(path));
    }
    requestFile(path);
    //初始化上传插件
    $('#fileupload').fileupload({
        url: location.pathname + "i/upload?path=" + getParam("path"),
        dataType: 'text',
        progressall: function (e, data) {
            $('#progress').text(data.loaded + "/" + data.total);
        },
        start: function (e) {
            $("#result").text("");
            $("#progress").show();
            $("#result").show();
        },
        done: function (e, data) {
            $('#result').text($('#result').text() + "done>>" + data.result + "\n");
        },
        fail: function (e, data) {
            $('#result').text($('#result').text() + "fail>>" + data.result + "\n");
        },
        stop: function (e) {
            var path = getParam("path");
            requestFile(path);
        },
    });
}

function deleteFile(path, obj) {
    var params = getRequest();
    var request = $.ajax({
        url: location.pathname + "i/del?path=" + path
    });
    request.then(function (data) {
        if (1 == data.state) {
            showSucc("删除成功");
            obj.parent().parent().remove();
            setDeleteDir();
        } else if (data.info) {
            showErr(data.info);
        }
    }, function (jqXHR, textStatus, errorThrown) {
        handErr(textStatus);
    });
}

function apkFile(path, obj) {
    var params = getRequest();
    var request = $.ajax({
        url: location.pathname + "i/apk?path=" + path
    });
    request.then(function (data) {
        if (1 == data.state) {
            showSucc("请求成功");
        } else if (data.info) {
            showErr(data.info);
        }
    }, function (jqXHR, textStatus, errorThrown) {
        handErr(textStatus);
    });
}

function deleteDir(path) {
    var params = getRequest();
    var request = $.ajax({
        url: location.pathname + "i/delDir?path=" + path
    });
    request.then(function (data) {
        if (1 == data.state) {
            showSucc("删除成功");
            goBack(path)
        } else if (data.info) {
            showErr(data.info);
        }
    }, function (jqXHR, textStatus, errorThrown) {
        handErr(textStatus);
    });
}

function newDir(dirName, path) {
    var params = getRequest();
    var request = $.ajax({
        url: location.pathname + "i/createDir?path=" + path + "&dirName=" + dirName,
    });
    request.then(function (data) {
        if (1 == data.state) {
            showSucc("创建成功");
            var path = getParam("path");
            requestFile(path);
        } else if (data.info) {
            showErr(data.info);
        }
    }, function (jqXHR, textStatus, errorThrown) {
        handErr(textStatus);
    });
}

function goBack(oldPath) {
    if (oldPath != "") {
        var path;
        var url;
        if (oldPath.lastIndexOf("/") != -1) {
            path = oldPath.substring(0, oldPath.lastIndexOf("/"));
            url = location.pathname + "?path=" + path;
        } else {
            path = "";
            url = location.pathname;
        }
        history.pushState(null, name, url);
        initUpload(path);
    }
}