


var newestNum = null;
const SEQUENTIAL = 10;

function syncData(){
    var dataKK = $("#dataKK").val();
    var url = "/dubo/flys/term/cache";
    var result = getAjax(url);

    var needPlayAudio = false;

    if (result.success) {
        var newestNumTmp = result.data.newestNumStr;
        if (newestNumTmp === newestNum) {
            console.log("not need to flush");
            return;
        }

        var list = result.data.records;
        var len = list.length;

        var wid = list[0].termDataArr.length;
        var w = wid / 3;

        for(var r =0; r < w; r++){
            $("#dataRow"+r).empty();

            var count = 0;
            var lastStage = 0;
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
                            '<p>' + termVal + '</p>' +
                            '<p>' + curTermNum + '</p>' +
                            '</div>';

                        $("#dataRow"+r).append(html)
                    }else{

                        var term1 =list[c+1];

                        var termDataArr1 = term1.termDataArr;
                        var termVal1 = termDataArr1[r*3];
                        var odd1 = termDataArr1[r*3+1];// 单双： 0 双 1单
                        var big1 = termDataArr1[r*3+2];// 大小： 0 小 1大

                        if (lastStage === odd1) {
                            count++;
                        } else {
                            count = 1;
                        }
                        lastStage = odd1;
                        var bigClass = count >= SEQUENTIAL ? 'big-font red' : 'big-font';
                        var oddClass = count >= SEQUENTIAL ? 'big-font red' : 'big-font';

                        // needPlayAudio
                        if ((count === SEQUENTIAL || count === 12)
                            && parseInt(newestNumTmp) === parseInt(curTermNum) + 1
                        ) {
                            needPlayAudio = true;
                        }

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
            var tmp = "<b class='big-font'>" + (r+1) + "列艇</br><hr/>";
            $("#dataRow"+r).append(tmp);
            if (r === 0) {
                var tmp12 = "<b class='big-font'>一二列和</br><hr/>";
                $("#dataRow12").append(tmp12);
            }

        } //end for

        newestNum = newestNumTmp;
        $("#newestNum").text(newestNum);

    }
    console.log("needPlayAudio:" + needPlayAudio);
    if (needPlayAudio) {
        playMusic();
    }
}

function sendSMS() {
    var url = "/dubo/sendSMS?kk=fly";
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
