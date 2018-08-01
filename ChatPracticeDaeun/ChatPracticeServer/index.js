const app = require('express')();
const SocketIo = require('socket.io');

const server = app.listen(3000, () => {
    console.log('running...');
});
const io = SocketIo(server);

var queue = [];    //상대방과의 연결을 위한 대기열
var rooms = {};    //방 목록
var allUsers = {}; //모든 socket의 id 목록
var locations = {}; //모든 socket의 위치 목록
var lat1 = 0; //나의 위도
var lng1 = 0; //나의 경도
var lat2 = 0; //상대방의 위도
var lng2 = 0; //상대방의 경도
var i=0;

io.on('connection', function (socket) { //socket 연결을 기다리는 함수

    console.log('User '+socket.id + ' connected');
    
    socket.on('login', function (data) {
        allUsers[socket.id] = socket;
        locations[socket.id] = data;
        //내 위치 받아오기
        lat1 = data.latitude;
        lng1 = data.longitude;
        console.log("my location : "+lat1, lng1);
    });
    socket.on('connect 100', function() { //100m 이내 사람들 연결
        console.log("connect 100");
        findPeerForLoneSocket(socket,0.1);
    });

    socket.on('connect 200', function() { //200m 이내 사람들 연결
        console.log("connect 200");        
        findPeerForLoneSocket(socket,0.2);
    });

    socket.on('connect 400', function() { //400m 이내 사람들 연결
        console.log("connect 400");        
        findPeerForLoneSocket(socket,0.4);
    });

    socket.on('pop queue', function() { //대기열에서 자기 자신을 pop
        queue.splice(queue.indexOf(socket), 1);
    });
    socket.on('message', function (data) { //message를 받았을 경우
        var room = rooms[socket.id];
        console.log(data);
        socket.broadcast.to(room).emit('message', data); //자신을 제외한 나머지 유저에게 message전송
    });
    socket.on('leave room', function () { //방을 나갔을 경우
        var room = rooms[socket.id];
        console.log('leave room');
        socket.broadcast.to(room).emit('chat end'); //자신을 제외한 나머지 유저에게 chat end전송
    });
    socket.on('disconnect', function () { //socket연결이 끊겼을 경우
        var room = rooms[socket.id];
        socket.broadcast.to(room).emit('chat end'); //자신을 제외한 나머지 유저에게 chat end전송
        queue.splice(queue.indexOf(socket), 1); //자신을 대기열에서 pop
    });
});

//상대방을 찾는 함수
var findPeerForLoneSocket = function(socket, distance) {
    console.log("queue.length : "+queue.length);

    if (queue.length > 0) { //대기열의 크기가 0보다 클 때
        for(i=0; i<10;i++) { //상대방을 10번 찾음
            var peer = queue.pop(); //상대방을 대기열에서 빼기
            if(peer == socket) { //대기열에서 빼낸 상대방이 자기 자신일 경우
                findPeerForLoneSocket(socket, distance); //다시 함수 호출
            }
            lat2 = locations[peer.id].latitude; //상대방의 위도
            lng2 = locations[peer.id].longitude; //상대방의 경
            도
            console.log("peer location"+lat2, lng2);
            console.log("calculateDistance : "+calculateDistance(lat1,lng1,lat2,lng2));

            if(calculateDistance(lat1,lng1,lat2,lng2) <= distance) { //상대방과의 거리가 자신이 찾고자 하는 거리보다 작을 경우
                console.log("success");
                var room = socket.id + '#' + peer.id; //방 만들기
                console.log(room);
                //방에 입장
                peer.join(room); 
                socket.join(room);
                // rooms에 방 저장
                rooms[peer.id] = room;
                rooms[socket.id] = room;
                // 상대방과 자신에게 chat start전송
                peer.emit('chat start', {'room':room});
                socket.emit('chat start', {'room':room});
                break;
            } else { //상대방과의 거리가 자신이 찾고자 하는 거리보다 클 경우
                console.log("push queue");
                queue.push(peer); //상대방을 다시 대기열에 push
            }
        }
        socket.emit('connect fail', {'distance': distance}); //10번동안 상대방을 찾지 못할 경우 connect fail전송
    } else { //대기열의 크기가 1보다 작을 경우
        console.log('push queue');
        queue.push(socket); //대기열에 자기 자신을 넣음
        console.log("queue" + queue);
        socket.emit('connect fail', {'distance': distance}); //connect fail전송
    }
}

//두 좌표 간의 거리 구하는 함수(단위:km)
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
    console.log(d);
    return d;
  }

Number.prototype.toRad = function() {
    return this * Math.PI / 180;
  }