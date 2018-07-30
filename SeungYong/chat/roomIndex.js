const app = require('express')();
const SocketIo = require('socket.io');

const server = app.listen(3000, () => {
    console.log('running...');
});
const io = SocketIo(server);

var queue = [];    // list of sockets waiting for peers
var rooms = {};    // map socket.id => room
var allUsers = {}; // map socket.id => socket

var findPeerForLoneSocket = function(socket) {
    // this is place for possibly some extensive logic
    // which can involve preventing two people pairing multiple times
    console.log(queue.length);
    if (queue.length > 0) {
        // somebody is in queue, pair them!
        var peer = queue.pop();
        var room = socket.id + '#' + peer.id;
        console.log(room);
        // join them both
        peer.join(room);
        socket.join(room);
        // register rooms to their names
        rooms[peer.id] = room;
        rooms[socket.id] = room;
        // exchange names between the two of them and start the chat
        peer.emit('chat start', {'room':room});
        socket.emit('chat start', { 'room':room});
    } else {
        // queue is empty, add our lone socket
        console.log('push queue');
        queue.push(socket);
    }
}

io.on('connection', function (socket) {
    console.log('User '+socket.id + ' connected');
    socket.on('add user', function () {
        allUsers[socket.id] = socket;
        // now check if sb is in queue
        findPeerForLoneSocket(socket);
    });
    socket.on('message', function (data) {
        var room = rooms[socket.id];
        console.log(room, data);
        //socket.broadcast.to(room).emit('message', {'message': data});
        socket.to(room).emit('message', {'message': data});
        socket.emit('message', {'message': data});
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