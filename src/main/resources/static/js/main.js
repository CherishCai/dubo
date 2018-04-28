

var dataKey = ["2_79", "2_135","4_79","4_135","7_24","7_6810","9_24","9_6810",
    "24_79","24_135","79_24","79_6810"];

function syncData(){
    var dataKK = $("#dataKK").val();
    var url = "/dubo/data?kk=" + dataKK;
    var result = getAjax(url);

    if (result.success) {
        for(var k in dataKey){
            var kk = dataKey[k];

            var data = result.data[dataKK];
            var dd = data[kk];

            $("#data"+kk).empty();
            $.each(dd, function (index, obj) {
                var third = obj.third;

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

                var html = '<div class="datameta">' +
                    '<p class="'+(bigNum>=5 ? "red,big-font" : "big-font")+'"><b>' + (big ? "大" : "小") + '</b></p>' +
                    '<p class="'+(evenNum>=5 ? "red,big-font" : "big-font")+'"><b>' + (even ? "双" : "单") + '</b></p>' +
                    '<p class="big-font"><b>' + third + '</b></p>' +
                    '<p>' + obj.second + '</p>' +
                    '<p>' + obj.first + '</p>' +
                    '<p class="'+(sameTermNumWithPrev ? "red" : "")+'">' + curTermNum + '</p>' +
                    '</div>';

                $("#data"+kk).append(html)
            });
        }
    }
}

$(function () {
    syncData();
    setInterval("syncData()", 666);

});
