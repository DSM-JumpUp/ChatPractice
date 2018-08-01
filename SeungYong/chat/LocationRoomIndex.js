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

var locationLat = [];
var locationLng = [];

var isDistatnce = [];

const toDistance = function toDistanceFromCoordinate(lat1, lat2, lng1, lng2) {
    const RAD = 0.000008998719243599958;
    console.log(lat1 + " " + lat2 + " " + lng1 + " " + lng2);
    console.log("result " + Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lng1 - lng2, 2)) / RAD)
    return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lng1 - lng2, 2)) / RAD;
}

var findPeerForLoneSocket = function (socket) {
    if (queue.length > 0) {
        for (var peer in queue) {
            console.log(queue[peer]);
            if (toDistance(locationLat[socket.id], locationLat[queue[peer].id], locationLng[socket.id], locationLng[queue[peer].id]) < 100.0) {
                locationLat.splice(locationLat.indexOf(queue[peer].id), 1);
                locationLng.splice(locationLng.indexOf(queue[peer].id), 1);
                locationLat.splice(locationLat.indexOf(socket.id), 1);
                locationLng.splice(locationLng.indexOf(socket.id), 1);
                var room = socket.id + '#' + queue[peer].id;
                console.log(room);
                // join them both
                queue[peer].join(room);
                socket.join(room);
                // register rooms to their names
                rooms[queue[peer].id] = room;
                rooms[socket.id] = room;
                // exchange names between the two of them and start the chat
                queue[peer].emit('chat start', {
                    'room': room
                });
                socket.emit('chat start', {
                    'room': room
                });
                isDistatnce[socket.id] = true;
                isDistatnce.splice(queue.indexOf(queue[peer].id), 1);
                queue.splice(queue.indexOf(queue[peer].id), 1);
                break;
            }
            if (!isDistatnce[socket.id]) queue.push(socket);
            else isDistatnce.splice(queue.indexOf(socket.id), 1);
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
    /*
    socket.on('disconnect', function () {
        var room = rooms[socket.id];
        //socket.broadcast.to(room).emit('chat end');
        peerID = peerID[0] === socket.id ? peerID[1] : peerID[0];
        queue.splice(queue.indexOf(peer.id), 1);
        queue.splice(queue.indexOf(socket.id), 1);
    });
    */
   socket.on('disconnect', function () {});
});