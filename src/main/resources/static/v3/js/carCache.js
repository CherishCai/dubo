

var newestNum = null;

function syncData(){
    var url = "/dubo/cars/term/cache";
    var result = getAjax(url);

    var needPlayAudio = false;
    var needSendSMS = false;

    if (result.success) {
        const newestNumTmp = result.data.newestNumStr;
        const list = result.data.records;
        const len = list.length;
        const wid = list[0].termDataArr.length;

        for(var k = 0; k<wid; k++) {
            $("#row" + k).empty();
        }

        for(var i = 0; i< wid; i++) {

            var arr = new Array(len);
            for(var j = 0; j < len;j++) {
                const termI = list[j];
                const termDataArr = termI.termDataArr;
                const termIJ = termDataArr[i];
                arr[j] = new Array(3);

                const odd = termIJ%2===0 ? 0 : 1;
                const small = termIJ > 5 ? 0 : 1;
                arr[j][0] = termIJ;
                arr[j][1] = odd;
                arr[j][2] = small;
            }

            var serialOdd = 0;
            var o = true;
            var serialSmall = 0;
            var s = true;

            for(var k = 0; k<len; k++) {
                const termVal = arr[k][0];
                const odd = arr[k][1];
                const small = arr[k][2];

                if (small && s) {
                    serialSmall++;
                } else if(!small && s) {
                    serialSmall = 1;
                    s = false;
                } else if (!small && !s){
                    serialSmall++;
                } else {
                    // small && !s
                    serialSmall = 1;
                    s = true;
                }

                if (odd && o) {
                    serialOdd++;
                    o = true;
                } else if(!odd && o) {
                    serialOdd = 1;
                    o = false;
                } else if (!odd && !o){
                    serialOdd++;
                    o = false;
                } else if(odd && !o){
                    serialOdd = 1;
                    o = true;
                } else {
                    console.log("??????????" + odd + o)
                }


                var oddHTML = oddEvenHtml(odd);
                if (!small) {
                    const oddBlue = serialOdd >= 5;
                    if (oddBlue) {
                        console.log("newestNum:" + newestNum);
                        console.log("newestNumTmp:" + newestNumTmp);
                        if(4===k && newestNumTmp!= newestNum) {
                            needPlayAudio = true;
                            console.log("needPlayAudio");
                            if(newestNum !== null){
                                needSendSMS = true;
                            }
                        }
                        oddHTML = "<span class='blue'>"+oddHTML+"</span>"
                    }
                }

                var bigHTML = smallBigHtml(small);
                if (odd) {
                    const smallGreen = serialSmall >= 5;
                    if (smallGreen) {
                        console.log("newestNum:" + newestNum);
                        console.log("newestNumTmp:" + newestNumTmp);
                        if(4===k && newestNumTmp!= newestNum) {
                            needPlayAudio = true;
                            console.log("needPlayAudio");
                            if(newestNum !== null){
                                needSendSMS = true;
                            }
                        }
                        bigHTML = "<span class='violet'>"+bigHTML+"</span>"
                    }
                }

                // console.log(i+"|"+k + ": 单双" + serialOdd);
                // console.log(i+"|"+k + s+": 大小" + serialSmall);

                var html = "<p> " + termVal + oddHTML + bigHTML + "</p>";

                $("#row" + i).append(html);

            }

        }

        newestNum = newestNumTmp;
        $("#newestNum").text(newestNum);
    }
    if (needPlayAudio) {
        playMusic();
    }
    if (needSendSMS) {
        sendSMS();
    }
}

function smallBigHtml(smallBig) {
    if (!smallBig) {
        return "「大」"
    }
    return "「小」"
}

function oddEvenHtml(oddEven) {
    if (!oddEven) {
        return "「双」"
    }
    return "「单」"
}

function sendSMS() {
    var url = "/dubo/sendSMS?kk=car单双";
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
