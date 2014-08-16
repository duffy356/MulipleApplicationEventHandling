/**
 * New node file
 */
var http = require('http');
var fs = require('fs');
var utils = require('./js/Utils')

var server = http.createServer(function(request, response) {
	function resolve(url) {
		switch (url) {
		case '/':
			return '/MAEH.html';
			break;
		case '/favicon.ico':
			return null;
			break;
		default:
			if (url.indexOf(".") === -1) {
				url += ".html";
			}
			return url;
			break;
		}
	}

    console.log('%s Received request for %s', utils.getActTime(), request.url);
	var url = resolve(request.url);
	if (url != null) {
	    fs.readFile(__dirname + url, function(err, data) {
			if (err) {
				console.log(err);
				response.writeHead(501);
				return response.end('Error loading page');
			}
			response.writeHead(200);
			response.end(data);
		});
	}
});
 
server.listen(8080, function() {
    console.log('%s Server is listening on port 8080', utils.getActTime());
});
 
