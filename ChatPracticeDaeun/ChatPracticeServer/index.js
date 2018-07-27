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

var findPeerForLoneSocket = function(socket) {
    // this is place for possibly some extensive logic
    // which can involve preventing two people pairing multiple times
    console.log(queue);
    if (queue.length > 0) {
        // somebody is in queue, pair them!
        var peer = queue.pop();
        if(peer == socket) {
            queue.push(peer);
            findPeerForLoneSocket(socket);
        }
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
        // queue is empty, add our lone socket
        console.log('push queue');
        queue.push(socket);
    }
}

io.on('connection', function (socket) {
    console.log('User '+socket.id + ' connected');
    socket.on('login', function () {
        allUsers[socket.id] = socket;
        socket.emit('')
        // now check if sb is in queue
        findPeerForLoneSocket(socket);
    });
    socket.on('pop queue', function() {
        console.log(queue);
        queue.splice(queue.indexOf(socket), 1);
        console.log(queue);
    });
    socket.on('message', function (data) {
        var room = rooms[socket.id];
        socket.broadcast.to(room).emit('message', data);
    });
    socket.on('leave room', function () {
        var room = rooms[socket.id];
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
