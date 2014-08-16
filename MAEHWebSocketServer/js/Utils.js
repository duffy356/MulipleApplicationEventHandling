/**
 * Utility Methods
 */

(function(exports) {
	function format2String(timeComponent, len) {
		var res = String(timeComponent);
		while (res.length < len) {
			res = '0' + res;
		}
		return res;
	}
	
	exports.getActTime = function() {
		var d = new Date();
		return format2String(d.getHours(),2) + ":" 
					+ format2String(d.getMinutes(),2) + ":" 
					+ format2String(d.getSeconds(),2) + "," 
					+ format2String(d.getMilliseconds(),3);	
	}

    exports.isString = function(value) {
        if (value === undefined
            || value == null
            || typeof (value) !== 'string') {
            return false;
        }
        return true;
    }

    exports.isObject = function(value) {
        if (value === undefined
            || value == null
            || typeof (value) !== 'object') {
            return false;
        }
        return true;
    }

    exports.isArray = function(value) {
        if (value === undefined
            || value == null
            || typeof (value) !== 'object'
            || value.length === undefined
            || value.length == null) {
            return false;
        }
        return true;
    }
}) (typeof exports === 'undefined'? this['Utils']={} : exports);
	

//module.exports.getActTime = getActTime;