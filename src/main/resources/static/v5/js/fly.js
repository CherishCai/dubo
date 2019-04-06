

var newestNum = null;

function syncData(){
    var dataKK = "all";
    var url = "/dubo/flys/term/cache?kk=" + dataKK;
    var result = getAjax(url);

    if (!result.success) {
        console.error("!result.success");
        return;
    }

    const newestNumTmp = result.data.newestNumStr;
    if (newestNumTmp === newestNum){
        console.log("newestNumTmp:"+newestNumTmp+" newestNum:"+newestNum);
        return;
    }

    const list = result.data.records;
    const len = list.length;

    for(var i=0; i<=10 ;i++){
        $("#dataRow"+i).empty();

        const i_3 = i*3;
        const ruleLen = rules24_680.length;

        // 防止溢出index, 不够ruleLen
        for(var j=0; j< len-ruleLen ;j++){

            var obj = list[j];

            var curTermNum = obj.termNum;
            var termDataArr = obj.termDataArr;
            var term = termDataArr[i_3];

            var tmpArr = [];
            for (var tr=0; tr <ruleLen;tr++){
                tmpArr[tr] = list[j + tr].termDataArr[i_3];
            }

            const testRule = testR34_680(tmpArr);
            console.log("curTermNum:" + curTermNum + ",testRule:" + testRule);
            if (!testRule) {
                continue;
            }

            var tmpHTML = "";
            for(var ti=0; ti < ruleLen; ti++){
                tmpHTML += "<p>" + tmpArr[ruleLen - 1 - ti] + "</p>";
            }

            const html = '<div class="datameta">' +
                "<p class='big-font red'>" + list[j + ruleLen+ 1].termDataArr[i_3] + "</p>" +
                "<p class='big-font red'>" + list[j + ruleLen+ 0].termDataArr[i_3] + "</p>" +
                tmpHTML +
                "<p class='blue'>" + curTermNum + "</p>" +
                '</div>';

            $("#dataRow"+i).append(html);
        }
        $("#dataRow" + i).append("第" + (i + 1) + "列");
    } //end for

    newestNum = newestNumTmp;
    $("#newestNum").text(newestNum);
}


$(function () {
    syncData();
    setInterval("syncData()", 6666);
});
