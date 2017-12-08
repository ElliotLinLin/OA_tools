function getMessageOfAndroid(data, lineNum, wOrR) {
	//读0  写1
	//alert("返回数据  ：  " + data);
	if(wOrR == 0) {
		//        console.log("数组"+lineNum  +"  "  +  listArr[lineNum-1]);
		var id = lineNum * 1000;
		var titleLB = document.getElementById(id);
		//        console.log("之前数据  ：  "+titleLB.innerText  +  "  "  +lineNum);
		var str = listArr[lineNum - 1] + ":" + data;
		titleLB.innerText = str;

	}

	if(wOrR == 1) {
		var a = parseInt(data);
		if(a == 1) { //+'第'+（lineNum+1)+'行'
			var inputT = document.getElementById(lineNum);
			inputT.value = "";
			mui.toast("写入成功");
		} else {
			mui.toast("写入失败");
		}
	}

	if(wOrR == 2) {
		var buardTitle = document.getElementById('showRatePicker');
		buardTitle.innerHTML = "波特率：" + lineNum;

		var parent = document.getElementById('list3');
		parent.innerHTML = ""; //清空
		var arr = data.split(";");
		for(var a = 0; a < arr.length - 1; a++) {
			var arr1 = arr[a].split(",");
			var str1 = arr1[0].split("=")[1];
			var str22 = arr1[1].split("=")[1];
			var str33 = arr1[2].split("=")[1].split("#")[0];
			var str2 = '9' + str22;
			var str3 = '9' + str33;
			var inputID = a + 1;
			listArr[a] = str1;

			//插入数据测试
			var div = document.createElement("div");
			div.className = 'controlRow';
			var b = inputID * 1000;
			div.innerHTML = '<a  class="mainTitle"  id="' + b + '">' + str1 + ':</a><br  /><div    class="midLine"></div><br  /><input  type="text"  id="' + inputID + '"  class="controlInput"  placeholder="指令"  style="width:  40%;"><button  class="writeBtn"  onclick="sendOrderOfWrite(' + str3 + ',' + inputID + ')"></button><button  class="readBtn"  onclick="sendOrderOfRead(' + str2 + ',' + inputID + ')"></button>';
			parent.appendChild(div);
		}
	}
}

function sendOrderOfBaudrate(baudrate) {
	//baudrate  int
	//	console.log("波特率"+baudrate);
	android.Connect(baudrate);
}

function sendOrderOfRead(str, inputID) {
	//读0 写1
	//	alert(typeof(str));
	var commmandStr = "pramrd=" + str.toString().substring(1);
	//	console.log("duqu " + commmandStr + inputID);
	android.sendUsbData("" + commmandStr, inputID, 0);

}

function sendOrderOfWrite(str, inputID) {
	//	alert(typeof(str));
	//	console.log("xieru" + str + "输入框id  command" + inputID);
	var inputStr = document.getElementById(inputID).value; //输入框值
	//  	console.log("输入指令 " + inputStr);
	if(inputStr == undefined || inputStr == null) {
		mui.toast("请输入指令");
	} else {
		var inputStr1 = inputStr.replace(/(^\s*)|(\s*$)/g, ""); //去除空格
		if(inputStr1 == "" || inputStr1 == undefined || inputStr1 == null || inputStr1.length == 0) {
			mui.toast("请输入指令");
		} else {
			var commandStr = "pramwt=" + str.toString().substring(1) + "#" + inputStr1; //指令
			console.log("xieru" + commandStr);
			android.sendUsbData("" + commandStr, inputID, 1);
		}

	}

}

function sendMessage() {
	var contentInput = document.getElementById('content');
	var row = document.createElement('p');
	row.innerHTML = contentInput.value;
	row.className = 'selfSend';
	document.getElementById("allData").appendChild(row);
	row.scrollIntoView();

	android.sendData(contentInput.value);
	contentInput.value = '$PWISDOM,';
}

function sendM_1() {
	if(itemValue == '1') {
		var lpl = 'U|V|T|I|E|LC|M|W|7|R|D|N|B|A|i|e|s|S';
		var arr = lpl.split('|');
		jQuery.each(arr, function(index, value) {
			var po = '';
			if(value == 'S') {
				po = '$PWISDOM,QS#';
			} else {
				po = '$PWISDOM,QP' + value + '#';
			}
			android.sendData(po);
		})

	}
}

function isConnect(connect) {
	switch(connect) {
		case '1': //连接中
			{
				document.getElementById('connect').innerText = '连接中';
				isConnectting = false;
			}
			break;
		case '2': //连接上
			{
				document.getElementById('connect').innerText = '已连接';
				isConnectting = true;
				sendM_1();
			}
			break;
		case '3': //连接断开
			{

				document.getElementById('connect').innerText = '连接断开';
				isConnectting = false;
			}
			break;
		case '4': //主动关闭
			{

				document.getElementById('connect').innerText = '主动关闭';
				isConnectting = false;
			}
			break;
		default:
			break;
	}
	sessionStorage.setItem('ca_connect', jQuery('#connect').text());
}

function funFromjs(data) {
	if(document.title == '1') {
		if(data.indexOf('$PWISDOM,QS') > -1) //作息表
		{
			var arr1 = data.split('QS');
			//			var arr = data.split(':');
			//			var timee = timeFormat3(arr[1].substring(0,arr[1].length - 1)) + 
			//			            timeFormat3(arr[2].substring(0,arr[2].length - 1)) +
			//			            timeFormat3(arr[3].substring(0,arr[3].length - 1)) +
			//			            timeFormat3(arr[4].substring(0,arr[4].length - 1)) +
			//			            timeFormat3(arr[5].substring(0,arr[5].length - 1)) +
			//			            timeFormat3(arr[6].substring(0,arr[6].length - 1)) +
			//			            timeFormat3(arr[7].substring(0,arr[7].length - 1))
			jQuery('#S').text(arr1[1].substring(0, arr1[1].length - 1))
			sessionStorage.setItem('ca_S', jQuery('#S').text());
		} else if(data.indexOf('$PWISDOM,QP') > -1) {
			if(data.indexOf('$PWISDOM,QPU') > -1) {
				var arr = data.split(':');
				jQuery('#U').text(timeFormat(arr[1].substring(0, arr[1].length - 1)))
				sessionStorage.setItem('ca_U', jQuery('#U').text());
			} else if(data.indexOf('$PWISDOM,QPV') > -1) {
				var arr = data.split(':');
				jQuery('#V').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_V', jQuery('#V').text());
			} else if(data.indexOf('$PWISDOM,QPT') > -1) {
				var arr = data.split(':');
				jQuery('#T').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_T', jQuery('#T').text());
			} else if(data.indexOf('$PWISDOM,QPI') > -1) {
				var arr = data.split(':');
				jQuery('#I').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_I', jQuery('#I').text());
			} else if(data.indexOf('$PWISDOM,QPE') > -1) {
				var arr = data.split(':');
				jQuery('#E').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_E', jQuery('#E').text());
			} else if(data.indexOf('$PWISDOM,QPL') > -1) {
				var arr = data.split(':');
				var pb = arr[1].substring(0, arr[1].length - 1) + '|' + arr[2].substring(0, arr[2].length - 1);
				jQuery('#LC').text(pb)
				sessionStorage.setItem('ca_LC', jQuery('#LC').text());
			} else if(data.indexOf('$PWISDOM,QPM') > -1) {
				var arr = data.split(':');
				var mContent = arr[1].substring(0, arr[1].length - 1).split(',');
				jQuery('#M_7').text(mContent[6] == '1' ? '在线' : '离线');
				jQuery('#M_6').text(mContent[5]);
				changeBg(mContent[5])
				jQuery('#M_2').text(mContent[1] == '1' ? '基站定位' : 'GPS定位');
				jQuery('#M_1').text(mContent[0]);

				sessionStorage.setItem('ca_M_7', jQuery('#M_7').text());
				sessionStorage.setItem('ca_M_6', jQuery('#M_6').text());
				sessionStorage.setItem('ca_M_2', jQuery('#M_2').text());
				sessionStorage.setItem('ca_M_1', jQuery('#M_1').text());
			} else if(data.indexOf('$PWISDOM,QPW') > -1) {
				var arr = data.split(':');
				jQuery('#W').text(arr[1].substring(0, arr[1].length - 1) == '1' ? '作息表模式' : '正常模式')
				sessionStorage.setItem('ca_W', jQuery('#W').text());
			} else if(data.indexOf('$PWISDOM,QP7') > -1) {
				var arr = data.split(':');
				jQuery('#7').text(timeFormat2(arr[1].substring(0, arr[1].length - 1)))
				sessionStorage.setItem('ca_7', jQuery('#7').text());
			} else if(data.indexOf('$PWISDOM,QPR') > -1) {
				var arr = data.split(':');
				jQuery('#R').text(arr[1].substring(0, arr[1].length - 1) + '秒')
				sessionStorage.setItem('ca_R', jQuery('#R').text());
			} else if(data.indexOf('$PWISDOM,QPD') > -1) {
				var arr = data.split(':');
				jQuery('#D').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_D', jQuery('#D').text());
			} else if(data.indexOf('$PWISDOM,QPN') > -1) {
				var arr = data.split(':');
				jQuery('#N').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_N', jQuery('#N').text());
			} else if(data.indexOf('$PWISDOM,QPB') > -1) {
				var arr = data.split(':');
				jQuery('#B').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_B', jQuery('#B').text());
			} else if(data.indexOf('$PWISDOM,QPA') > -1) {
				var arr = data.split(':');
				jQuery('#A').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_A', jQuery('#A').text());
			} else if(data.indexOf('$PWISDOM,QPi') > -1) {
				var arr = data.split(':');
				jQuery('#i').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_i', jQuery('#i').text());
			} else if(data.indexOf('$PWISDOM,QPe') > -1) {
				var arr = data.split(':');
				jQuery('#e').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_e', jQuery('#e').text());
			} else if(data.indexOf('$PWISDOM,QPs') > -1) {
				var arr = data.split(':');
				jQuery('#s').text(arr[1].substring(0, arr[1].length - 1))
				sessionStorage.setItem('ca_s', jQuery('#s').text());
			}
		}

	} else if(document.title == '2') {
		if((data.indexOf('$PWISDOM,SP') > -1)) {
			if(data.indexOf('$PWISDOM,SP:OK#') > -1) {
				showSuccess();
				jQuery('.name2').each(function() {
					var itemKey = 'ca_' + this.id;
					if(jQuery(this).text() == '工作模式') {
						if(document.getElementById('mo1').className == 'mo1') {
							sessionStorage.setItem(itemKey, '正常模式')
						} else if(document.getElementById('mo1').className == 'mo1p') {
							sessionStorage.setItem(itemKey, '作息表模式')
						}
					} else if(jQuery(this).text() == '基站服务器开启') {
						if(jQuery('#s').next().children().attr('class') == 'open') {
							sessionStorage.setItem(itemKey, '1')
						} else if(jQuery('#s').next().children().attr('class') == 'close') {
							sessionStorage.setItem(itemKey, '0')
						}
					} else if(jQuery(this).text() == '车牌号') {
						if(sessionStorage.getItem('ca_LC') == '') {
							var lome = jQuery(this).next().children().val() + '|';
							sessionStorage.setItem('ca_LC', lome)
						} else {
							var array1 = sessionStorage.getItem('ca_LC').split('|');
							var lo = jQuery(this).next().children().val() + '|' + array1[1];
							sessionStorage.setItem('ca_LC', lo);
						}
					} else if(jQuery(this).text() == '车牌颜色') {
						if(sessionStorage.getItem('ca_LC') == '') {
							var lome = '|' + jQuery(this).next().children().val();
							sessionStorage.setItem('ca_LC', lome)
						} else {
							var array2 = sessionStorage.getItem('ca_LC').split('|');
							var lo = array2[0] + '|' + jQuery(this).next().children().val();
							sessionStorage.setItem('ca_LC', lo);
						}
					} else {
						sessionStorage.setItem(itemKey, jQuery(this).next().children().val())
					}
				})

			} else {
				showFail();
			}
		} else if((data.indexOf('$PWISDOM,LO') > -1)) {
			if((data.indexOf('$PWISDOM,LO:OK#') > -1)) {
				showSuccess();
			} else {
				showFail();
			}
		} else if((data.indexOf('$PWISDOM,RT') > -1)) {
			if((data.indexOf('$PWISDOM,RT:OK#') > -1)) {
				showSuccess();
			} else {
				showSuccess();
			}
		}
	} else if(document.title == '3') {
		var row = document.createElement('p');
		row.innerHTML = data;
		document.getElementById("allData").appendChild(row);
		row.scrollIntoView();
	}
}

function timeFormat(time) {
	var year = time.substring(0, 4);
	var month = time.substring(4, 6);
	var day = time.substring(6, 8);
	var hour = time.substring(9, 11);
	var min = time.substring(11, 13);
	var sec = time.substring(13, 15);

	return year + '-' + month + '-' + day + ' ' + hour + ':' + min + ':' + sec;
}

function timeFormat2(time) {
	var hour = time.substring(0, 2);
	var min = time.substring(2, 4);
	return hour + ':' + min;
}

function timeFormat3(time) {
	var arr = time.split(',');
	return timeFormat2(arr[0]) + '-' + timeFormat2(arr[1]);
}

function changeBg(uo) {
	if(parseInt(uo, 10) == 0) {
		jQuery('#M_6').css({
			'background-image': 'url(images/xh1.png)'
		})
	} else if(parseInt(uo, 10) > 0 && parseInt(uo, 10) <= 6) {
		jQuery('#M_6').css({
			'background-image': 'url(images/xh2.png)'
		})
	} else if(parseInt(uo, 10) > 6 && parseInt(uo, 10) <= 12) {
		jQuery('#M_6').css({
			'background-image': 'url(images/xh3.png)'
		})
	} else if(parseInt(uo, 10) > 12 && parseInt(uo, 10) <= 18) {
		jQuery('#M_6').css({
			'background-image': 'url(images/xh4.png)'
		})
	} else if(parseInt(uo, 10) > 18 && parseInt(uo, 10) <= 24) {
		jQuery('#M_6').css({
			'background-image': 'url(images/xh5.png)'
		})
	} else if(parseInt(uo, 10) > 24) {
		jQuery('#M_6').css({
			'background-image': 'url(images/xh6.png)'
		})
	}
}

function openServer() {
	android.openSocketService();
}

function closeServer() {
	android.closeSocketService();
}