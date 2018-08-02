const app = require('express')();
const SocketIo = require('socket.io');

const server =
    app.get('/', (req, res) => res.status(200).end('<h1>socket-chatting</h1>'))
    .listen(process.env.PORT, () => {
        console.log('running...');
    });
const io = SocketIo(server);

var queue = [];
var rooms = [];
var allUsers = {};

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

                queue[peer].join(room);
                socket.join(room);

                rooms[queue[peer].id] = room;
                rooms[socket.id] = room;

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

        findPeerForLoneSocket(socket);
    });
    socket.on('message', function (data) {
        var room = rooms[socket.id];
        console.log(room, data);

        socket.to(room).emit('message', {
            'message': data,
            'id': socket.id
        });
        socket.emit('message', {
            'message': data,
            'id': socket.id
        });
    });
   socket.on('disconnect', function () {
       console.log('disconnect Event');
       var room = rooms[socket.id];
       socket.to(room).emit('chat end');
       queue.splice(queue.indexOf(socket.id), 1);
       rooms.splice(rooms.indexOf(socket.id), 1); 
   });
});