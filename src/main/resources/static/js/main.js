

var dataKey = ["2_79", "2_135","4_79","4_135","7_24","7_6810","9_24","9_6810",
    "24_79","24_135","79_24","79_6810","135_6810","6810_135"];

function syncData(){
    var dataKK = $("#dataKK").val();
    var url = "/dubo/data?kk=" + dataKK;
    var result = getAjax(url);

    if (result.success) {
        $("#newestNum").text(result.data.newestNumStr);

        for(var k in dataKey){
            var kk = dataKey[k];

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

                var html = '<div class="datameta">' +
                    '<p class="'+(bigNum>=5 ? "red big-font" : "big-font")+'"><b>' + (big ? "大" : "小") + '</b></p>' +
                    '<p class="'+(evenNum>=5 ? "red big-font" : "big-font")+'"><b>' + (even ? "双" : "单") + '</b></p>' +
                    '<p class="'+(blue ? "blue big-font" : "big-font")+'"><b>' + third + '</b></p>' +
                    '<p>' + second + '</p>' +
                    '<p>' + first + '</p>' +
                    '<p class="'+(sameTermNumWithPrev ? "red" : "")+'">' + curTermNum + '</p>' +
                    '</div>';

                $("#data"+kk).append(html)
            });
            var tmp = "<b class='big-font'>" + kk +
                "车&nbsp;&nbsp;最新一期号码：" + result.data.newestNumStr + "</br>";
            $("#data" + kk).append(tmp);
            $("#data"+kk).append("<hr/>")

        } //end for

        // playMusic();
    }
}

var music = "http://gddx.sc.chinaz.com/Files/DownLoad/sound1/201803/9821.mp3";
function playMusic(){
    //IE9+,Firefox,Chrome均支持<audio/>
    $('#newMessageDIV').html('<audio autoplay="autoplay"><source src="'+music
        + '" type="audio/wav"/><source src="'+music+ '" type="audio/mpeg"/></audio>');

    var mm = '<bgsound loop="1" src="'+music+'" />';
    $('#newMessageDIV').html(mm);
}

$(function () {
    syncData();
    setInterval("syncData()", 6666);

});
