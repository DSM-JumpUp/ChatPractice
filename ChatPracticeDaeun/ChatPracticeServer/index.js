const app = require('express')();
const SocketIo = require('socket.io');

const server = app.listen(3000, () => {
    console.log('running...');
});
const io = SocketIo(server);

var queue = [];    // list of sockets waiting for peers
var rooms = {};    // map socket.id => room
var names = {};    // map socket.id => name
var allUsers = {}; // map socket.id => socket
var locations = {};
var lat1 = 0;
var lng1 = 0;
var lat2 = 0;
var lng2 = 0;

var findPeerForLoneSocket = function(socket, distance) {
    // this is place for possibly some extensive logic
    // which can involve preventing two people pairing multiple times
    console.log("queue.length : "+queue.length);
    if (queue.length > 0) {
        // somebody is in queue, pair them!
        var peer = queue.pop();
        if(peer == socket) {
            findPeerForLoneSocket(socket, distance);
            // console.log("return");
            return;
        }

        lat2 = locations[peer.id].latitude;
        lng2 = locations[peer.id].longitude;
        console.log("peer location"+lat2, lng2);

        console.log("calculateDistance : "+calculateDistance(lat1,lng1,lat2,lng2));
        if(calculateDistance(lat1,lng1,lat2,lng2) <= distance) {
            console.log("success");
            var room = socket.id + '#' + peer.id;
            console.log(room);
            // join them both
            peer.join(room);
            socket.join(room);
            // register rooms to their names
            rooms[peer.id] = room;
            rooms[socket.id] = room;
            // exchange names between the two of them and start the chat
            peer.emit('chat start', {'name': names[socket.id], 'room':room});
            socket.emit('chat start', {'name': names[peer.id], 'room':room});
        } else {
            queue.push(peer);
            socket.emit('connect fail', {'distance': distance});
        }

    } else {
        // queue is empty, add our lone socket
        console.log('push queue');
        queue.push(socket);
        console.log("queue" + queue);
        socket.emit('connect fail', {'distance': distance});
    }
}

function calculateDistance(lat1, lon1, lat2, lon2) {
    console.log("calculatedistance("+lat1,lon1,lat2,lon2+")");
    var R = 6371; // km
    if(lat2>=lat1) {
        var dLat = (lat2-lat1).toRad();
    } else {
        var dLat = (lat1-lat2).toRad();
    }
    if(lon2>=lon1) {
        var dLon = (lon2-lon1).toRad();
    }else {
        var dLon = (lon1-lon2).toRad();
    }     
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1.toRad()) * Math.cos(lat2.toRad()) * 
            Math.sin(dLon/2) * Math.sin(dLon/2); 
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    var d = R*c;
    d = 0.15;
    console.log(d);
    return d;
  }

Number.prototype.toRad = function() {
    return this * Math.PI / 180;
  }

io.on('connection', function (socket) {
    console.log('User '+socket.id + ' connected');
    socket.on('login', function (data) {
        allUsers[socket.id] = socket;
        locations[socket.id] = data;
        //my location
        lat1 = data.latitude;
        lng1 = data.longitude;
        console.log("my location : "+lat1, lng1);
        // now check if sb is in queue
    });
    socket.on('connect 100', function() {
        console.log("connect 100");
        findPeerForLoneSocket(socket,0.1);
    });

    socket.on('connect 200', function() {
        console.log("connect 200");        
        findPeerForLoneSocket(socket,0.2);
    });

    socket.on('connect 400', function() {
        console.log("connect 400");        
        findPeerForLoneSocket(socket,0.4);
    });

    socket.on('pop queue', function() {
        queue.splice(queue.indexOf(socket), 1);
    });
    socket.on('message', function (data) {
        var room = rooms[socket.id];
        console.log(data);
        socket.broadcast.to(room).emit('message', data);
    });
    socket.on('leave room', function () {
        var room = rooms[socket.id];
        console.log('leave room');
        socket.broadcast.to(room).emit('chat end');
        // var peerID = room.split('#');
        // peerID = peerID[0] === socket.id ? peerID[1] : peerID[0];
        // // add both current and peer to the queue
        // findPeerForLoneSocket(allUsers[peerID]);
        // findPeerForLoneSocket(socket);
    });
    socket.on('disconnect', function () {
        var room = rooms[socket.id];
        socket.broadcast.to(room).emit('chat end');
        // var peerID = room.split('#');
        // peerID = peerID[0] === socket.id ? peerID[1] : peerID[0];
        // // current socket left, add the other one to the queue
        // findPeerForLoneSocket(allUsers[peerID]);
        queue.splice(queue.indexOf(socket), 1);
    });
});