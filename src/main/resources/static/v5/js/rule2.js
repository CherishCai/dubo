
const r24 = [2, 4];
const r2479 = [2, 4, 7, 9];
const r135 = [1, 3, 5];
const r680 = [6, 8, 10];

const rules24_680 = [r24, r135, r24, r680];

const rules680 = [r2479, r135, r680, r135, r680];
const rules135 = [r2479, r680, r135, r680, r135];

const rules135_2479 = [r135, r680, r135, r680, r2479];
const rules680_2479 = [r680, r135, r680, r135, r2479];

function testR34_680(arr){
    const rLen = rules24_680.length;
    for (var i=0; i < rLen;i++){
        if (rules24_680[rLen - 1 - i].indexOf(arr[i]) < 0) {
            return false;
        }
    }
    return true;
}


