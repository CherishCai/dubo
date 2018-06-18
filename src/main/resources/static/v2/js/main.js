

var dataKey = ["2_79", "2_135","4_79","4_135","7_24","7_6810","9_24","9_6810",
    "24_79","24_135","79_24","79_6810","135_6810","6810_135",
    "1_4","7_2","7_4","9_4","9_2","2_9","2_7","4_9","4_7","10_3"];

var newestNum = null;

function syncData(){
    var dataKK = $("#dataKK").val();
    var url = "/dubo/data?kk=" + dataKK;
    var result = getAjax(url);

    var needPlayAudio = false;

    if (result.success) {
        var newestNumTmp = result.data.newestNumStr;

        for(var i in dataKey){
            var kk = dataKey[i];

            var data = result.data[dataKK];
            var dd = data[kk];

            $("#data"+kk).empty();
            $.each(dd, function (index, obj) {
                var third = obj.third;
                var second = obj.second;
                var first = obj.first;

                var even = obj.thirdEven;

                var big = obj.thirdBig;

                var curTermNum = obj.termNum;

                var sameTermNumWithPrev = false;
                if (index > 0) {
                    var prev =  dd[index - 1];
                    var prevCurTermNum = prev.termNum;
                    sameTermNumWithPrev = (curTermNum == prevCurTermNum);
                }

                // 计算奇偶数的连续数目
                var evenNum = 0;
                if (index >= 4) {
                    var tmpIndex = index;
                    var prevThirdEven = even;
                    while (tmpIndex >0 && prevThirdEven == even) {
                        evenNum++;
                        var prevD =  dd[--tmpIndex];
                        prevThirdEven = prevD.thirdEven;
                    }
                }

                var bigNum = 0;
                if (index >= 4) {
                    var tmpIndex2 = index;
                    var prevThirdBig = big;
                    while (tmpIndex2 >0 && prevThirdBig == big) {
                        bigNum++;
                        var prevD2 =  dd[--tmpIndex2];
                        prevThirdBig = prevD2.thirdBig;
                    }
                }

                var blue = false;
                if( (9==first || 7==first) && (1==third || 3==third || 5==third)) {
                    blue = true;
                }

                // termNumClass
                var curTermNumClass = "";
                if (curTermNum === newestNumTmp-2) {
                    curTermNumClass = "brown";
                }
                curTermNumClass = (sameTermNumWithPrev ? "red" : curTermNumClass);
                if (kk == "135_6810" || kk == "6810_135"){
                    curTermNumClass = (sameTermNumWithPrev ? "blue" : curTermNumClass);
                }

                // 第二个数值的颜色
                var secClass = "black";
                if ("79_24" == kk) {
                    secClass = "green";
                }

                var thirdClass = (blue ? "blue big-font" : "big-font");
                var evenClass = (evenNum>=5 ? "red big-font" : "big-font");
                var bigClass = (bigNum>=5 ? "red big-font" : "big-font");

                // audio
                if (newestNumTmp !== newestNum && dd.length === (index+1) && third) {
                    console.log("newestNumTmp:" + newestNumTmp);
                    console.log("newestNum:" + newestNum);
                    console.log("curTermNum:" + curTermNum);
                    if (curTermNum === newestNumTmp-2) {
                        console.log("needPlayAudio");
                        needPlayAudio = true;
                    }
                }

                var html = '<div class="datameta">' +
                    '<p class="'+bigClass+'"><b>' + (big ? "大" : "小") + '</b></p>' +
                    '<p class="'+evenClass+'"><b>' + (even ? "双" : "单") + '</b></p>' +
                    '<p class="'+thirdClass+'"><b>' + third + '</b></p>' +
                    '<p class="'+secClass+'">' + second + '</p>' +
                    '<p>' + first + '</p>' +
                    '<p class="'+curTermNumClass+'">' + curTermNum + '</p>' +
                    '</div>';

                if ("79_24" == kk) {
                    html = '<div class="datameta">' +
                        '<p class="'+secClass+'">' + second + '</p>' +
                        '<p>' + first + '</p>' +
                        '<p class="'+curTermNumClass+'">' + curTermNum + '</p>' +
                        '</div>';
                }

                $("#data"+kk).append(html)
            });
            var tmp = "<b class='big-font'>" + kk + "车</br><hr/>";
            $("#data"+kk).append(tmp);

        } //end for

        newestNum = newestNumTmp;
        $("#newestNum").text(newestNum);

    }
    if (needPlayAudio) {
        playMusic();
    }
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
