function unpack(Value){
	if(typeof Value != "string" || Value.length < 1)return null;
	var len = Value.length;
	if(len < 2)	return null;
	var pos = 0;
	var ret = [];
	var segLen = 0;
	var startPos = 0;
	while(pos < len - 1){
		// try to get the next index
		segLen = Value.indexOf(":",pos);
		if(segLen <= pos)break;
		startPos = segLen + 1;
		segLen = parseInt(Value.substr(pos, segLen - pos));
		if(segLen >= 0 && startPos + segLen <= len){
			ret.push(Value.substr(startPos,segLen));
			pos = startPos + segLen;
		}
		else
			break;
	}
	if(pos != len || ret.length < 1)return null;
	return ret;
}

function pack(arr){
	if(!arr || !(arr.length > 0))return "";
	var arr2 = [];
	for(var i = 0; i < arr.length;i++)
		arr2.push((arr[i] ? arr[i].length : "0") + ":" + (arr[i] || ""));
	return arr2.join("");
}

function _on(){}
function call(data,callBack,udata){
	if(_action){
		if(new Date().getTime() - _actionTime > 40000){
			_action.onreadystatechange = _on;
			_action.abort();
			_action = null;
		}
		else
			return false;
	}
    if(!typeof callBack == "function")
        return false;
    var httpRequest = null;
    try{
        httpRequest = new ActiveXObject("msxml2.xmlhttp");
    }
    catch(z){
        try{
            httpRequest = new XMLHttpRequest();
        }
        catch(z){
            return false;
        }
    }
    httpRequest.open("POST","/html/service",true);
    httpRequest.onreadystatechange = _onre;
    if(typeof data == "string" || data.length > 0)
        httpRequest.send(data);
    else
        httpRequest.send();
	_action = httpRequest;
	_actionTime = new Date().getTime();
    return true;   
    
	function _onre()
    {
        if(httpRequest.readyState == 4)
        {
            httpRequest.onreadystatechange = _on;
			_action = null;
			_actionTime = 0;
			
            callBack(httpRequest,udata);
            httpRequest = null;
			
        }
    }
}

var _action = null;
var _actionTime = 0;
var _config = [];
var $ = function(d){return document.getElementById(d)};

function callAPI(name,paras,uobj){
	if(!paras){
		var s = _config[name];
		if(s){
			var arr = s.split(",");
			for(var i = 0; i < arr.length;i++){
				if(arr[i].substr(0,1) == "+"){
					s = arr[i].substr(1);
					if($(s).type.toLowerCase() == "checkbox")
						arr[i] = $(s).checked ? "true" :"false";
					else
						arr[i] = $(s).value;
					if(arr[i].length < 1){
						info("Please Input Value For " + s);
						return;
					}
				}else{
					if($(arr[i]).type.toLowerCase() == "checkbox")
						arr[i] = $(arr[i]).checked ? "true" :"false";
					else
						arr[i] = $(arr[i]).value;
				}
			}
			paras = arr;
		}
	}
	var old = $("st").innerHTML;
	
	$("st").innerHTML = "Working In Progress...<img src='/html/loading.gif' />";
	if(typeof uobj == "undefined")
		uobj = name
	if(!call(pack([name,pack(paras)]),processAPI,uobj)){
		$("st").innerHTML = old;
		return false;
	}else
		return true;
}
function getParameterFromUrl(ParameterName) {
	return decodeURI((RegExp(ParameterName + '=' + '(.+?)(&|$)').exec(location.search) || [, ""])[1]);
}

