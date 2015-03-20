var Server = {
    worldId: location.pathname.split('/')[2],
    socket: null,
    init: function() {
        this.socket = new WebSocket("ws://%s:%s/world/%s/socket".format(location.hostname, location.port, this.worldId));
        this.socket.onopen = function(event) {
            Game.init();
        };

        this.socket.onmessage = function(event) {
            var message = JSON.parse(event.data);

            switch(message.$message) {
                case 'UpdateMap':
                    Game.updateMap(message.map);
                    break;
                case 'MovePlayer':
                    Game.player.movePlayer(message.newX, message.newY);
                    break;
                case 'AddEntity':
                    Game.addEntity(message.id);
                    break;
                case 'MoveEntity':
                    Game.moveEntity(message.id, message.newX, message.newY);
                    break;
                case 'RemoveEntity':
                    Game.removeEntity(message.id);
                    break;
                default:
                    console.log(message);
            }
        };
    }
};

Server.send = function(message) {
    this.socket.send(JSON.stringify(message));
};

Server.getMap = function() {
    this.send({
        "$message": "GetMap"
    });
};

Server.movePlayer = function(newX, newY, oldX, oldY) {
    this.send({
        "$message": "MovePlayer",
        "newX": newX,
        "newY": newY,
        "oldX": oldX,
        "oldY": oldY
    });
};