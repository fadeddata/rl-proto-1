
var Game = {
    display: null,
    player: null,
    engine: null,
    worldId: location.pathname.split('/')[2],
    map: null,
    entities: {},

    init: function() {
        this.display = new ROT.Display();
        document.body.appendChild(this.display.getContainer());
        Server.getMap();

        this.createPlayer(1,1);

        var scheduler = new ROT.Scheduler.Simple();
        scheduler.add(this.player, true);
        this.engine = new ROT.Engine(scheduler);
        this.engine.start();
    }
};

Game.addEntity = function(id) {
    Game.entities[id] = {x: 0, y:0};
};

Game.moveEntity = function(id, x, y) {
    var entity = this.entities[id];
    this.display.draw(entity.x, entity.y, this.map[entity.x][entity.y]);
    this.entities[id] = {x: x, y: y};
    this.display.draw(x, y, "@");
};

Game.removeEntity = function(id) {
    var entity = this.entities[id];
    this.display.draw(entity.x, entity.y, this.map[entity.x][entity.y]);
    delete this.entities[id];
};

Game.createPlayer = function(x, y) {
    this.player = new Player(x, y);
};

Game.updateMap = function(map) {
    this.map = map;
    this.drawWholeMap();
};

Game.drawWholeMap = function() {
    for (var x = 0; x < this.map.length; x++) {
        var ys = this.map[x];
        for (var y = 0; y < ys.length; y++) {
            this.display.draw(x, y, this.map[x][y]);
        }
    }
};
