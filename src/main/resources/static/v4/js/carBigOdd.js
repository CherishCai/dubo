


var newestNum = null;

function syncData(){
    var dataKK = $("#dataKK").val();
    var url = "/dubo/cars/term/cache";
    var result = getAjax(url);

    var needPlayAudio = false;

    if (result.success) {
        var newestNumTmp = result.data.newestNumStr;

        var list = result.data.records;
        var len = list.length;

        var wid = list[0].termDataArr.length;
        var w = wid / 3;

        for(var r =0; r < w; r++){
            $("#dataRow"+r).empty();

            for (var c=0; c < len; c++) {

                var term = list[c];
                var curTermNum = term.termNum;

                var termDataArr = term.termDataArr;
                var termVal = termDataArr[r*3];
                var odd = termDataArr[r*3+1];// 单双： 0 双 1单
                var big = termDataArr[r*3+2];// 大小： 0 小 1大

                // 处理单数
                if (big){
                    if(c+1===len){
                        var html = '<div class="datameta">' +
                            '<p><b>null</b></p>' +
                            '<p><b>null</b></p>' +
                            '<p>' + termVal + '</p>' +
                            '<p>' + curTermNum + '</p>' +
                            '</div>';

                        $("#dataRow" + r).append(html);
                    }else{

                        var term1 =list[c+1];

                        var termDataArr1 = term1.termDataArr;
                        var termVal1 = termDataArr1[r*3];
                        var odd1 = termDataArr1[r*3+1];// 单双： 0 双 1单
                        var big1 = termDataArr1[r*3+2];// 大小： 0 小 1大

                        var bigClass = 'big-font';
                        var oddClass = 'big-font';

                        var html = '<div class="datameta">' +
                            '<p class="'+oddClass+'"><b>' + (odd1 ? "单" : "双") + '</b></p>' +
                            // '<p class="'+bigClass+'"><b>' + (big ? "大" : "小") + '</b></p>' +
                            '<p>' + termVal + '</p>' +
                            '<p>' + curTermNum + '</p>' +
                            '</div>';

                        $("#dataRow" + r).append(html);
                    }//end else
                }

            }
            var tmp = "<b class='big-font'>" + (r+1) + "列车</br><hr/>";
            $("#dataRow"+r).append(tmp);

        } //end for

        newestNum = newestNumTmp;
        $("#newestNum").text(newestNum);

    }
    if (needPlayAudio) {
        playMusic();
        sendSMS();
    }
}

function sendSMS() {
    var url = "/dubo/sendSMS?kk=car";
    var result = getAjax(url);
}


var music = "http://gddx.sc.chinaz.com/Files/DownLoad/sound1/201803/9821.mp3";
function playMusic(){
    //IE9+,Firefox,Chrome均支持<audio/>
    $('body').append('<audio style="display:none" autoplay="autoplay"><source src="'+music
        + '" type="audio/wav"/><source src="'+music+ '" type="audio/mpeg"/></audio>');
}

$(function () {
    syncData();
    setInterval("syncData()", 6666);

});
