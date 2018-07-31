const app = require('express')();
const SocketIo = require('socket.io');

const server =
    app.get('/', (req, res) => res.status(200).end('<h1>socket-chatting</h1>'))
    .listen(process.env.PORT, () => {
        console.log('running...');
    });
const io = SocketIo(server);

var queue = []; // list of sockets waiting for peers
var rooms = {}; // map socket.id => room
var allUsers = {}; // map socket.id => socket

var locationLat = {};
var locationLng = {};

var isDistatnce = [];

const toDistance = function toDistanceFromCoordinate(lat1, lat2, lng1, lng2) {
    const RAD = 0.000008998719243599958;
    console.log(Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lng1 - lng2, 2)) / RAD);
    return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lng1 - lng2, 2)) / RAD;
}


var findPeerForLoneSocket = function (socket) {
    // this is place for possibly some extensive logic
    // which can involve preventing two people pairing multiple times
    console.log(queue.length);
    if (queue.length > 0) {
        // somebody is in queue, pair them!
        for (var peer in queue) {
            if (toDistance(locationLat[socket.id], locationLng[socket.id], locationLat[peer.id], locationLng[peer.id]) < 100.0) {
                queue.splice(queue.indexOf(peer.id), 1);
                locationLat.splice(locationLat.indexOf(peer.id), 1);
                locationLng.splice(locationLng.indexOf(peer.id), 1);
                locationLat.splice(locationLat.indexOf(socket.id), 1);
                locationLng.splice(locationLng.indexOf(socket.id), 1);
                var room = socket.id + '#' + peer.id;
                console.log(room);
                // join them both
                peer.join(room);
                socket.join(room);
                // register rooms to their names
                rooms[peer.id] = room;
                rooms[socket.id] = room;
                // exchange names between the two of them and start the chat
                peer.emit('chat start', {
                    'room': room
                });
                socket.emit('chat start', {
                    'room': room
                });
                isDistatnce[socket.id] = true;
                isDistatnce[peer.id] = true
                break;
            }
            if (!isDistatnce) queue.push(socket)
        }
    } else {
        // queue is empty, add our lone socket
        console.log('push queue');
        queue.push(socket);
    }
}

io.on('connection', function (socket) {
    console.log('User ' + socket.id + ' connected');
    socket.on('add user', function (lat, lng) {
        allUsers[socket.id] = socket;
        locationLat[socket.id] = lat;
        locationLng[socket.id] = lng;
        console.log(lat + ' ' + lng);
        // now check if sb is in queue
        findPeerForLoneSocket(socket);
    });
    socket.on('message', function (data) {
        var room = rooms[socket.id];
        console.log(room, data);
        //socket.broadcast.to(room).emit('message', {'message': data});
        socket.to(room).emit('message', {
            'message': data
        });
        socket.emit('message', {
            'message': data
        });
    });
    socket.on('leave room', function () {
        var room = rooms[socket.id];
        socket.broadcast.to(room).emit('chat end');
        var peerID = room.split('#');
        peerID = peerID[0] === socket.id ? peerID[1] : peerID[0];
        // add both current and peer to the queue
        findPeerForLoneSocket(allUsers[peerID]);
        findPeerForLoneSocket(socket);
    });
    socket.on('disconnect', function () {
        var room = rooms[socket.id];
        socket.broadcast.to(room).emit('chat end');
        var peerID = room.split('#');
        peerID = peerID[0] === socket.id ? peerID[1] : peerID[0];
        // current socket left, add the other one to the queue
        //findPeerForLoneSocket(allUsers[peerID]);
    });
});