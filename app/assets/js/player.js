var Player = function(x, y) {
    this._x = x;
    this._y = y;
    this._draw();
};

Player.prototype.movePlayer = function(x, y) {
    Game.display.draw(this._x, this._y, Game.map[this._x][this._y]);
    this._x = x;
    this._y = y;
    this._draw();
};

Player.prototype._draw = function() {
    Game.display.draw(this._x, this._y, "@", "#ff0");
};

Player.prototype.act = function() {
    Game.engine.lock();
    /* wait for user input; do stuff when user hits a key */
    window.addEventListener("keydown", this);
};

Player.prototype.handleEvent = function(e) {
    /* process user input */
    var keyMap = {};
    keyMap[38] = 0;
    keyMap[33] = 1;
    keyMap[39] = 2;
    keyMap[34] = 3;
    keyMap[40] = 4;
    keyMap[35] = 5;
    keyMap[37] = 6;
    keyMap[36] = 7;

    var code = e.keyCode;

    if (!(code in keyMap)) { return; }

    var diff = ROT.DIRS[8][keyMap[code]];
    var newX = this._x + diff[0];
    var newY = this._y + diff[1];

    //if (!(newKey in Game.map)) { return; } /* cannot move in this direction */
    Server.movePlayer(newX, newY, this._x, this._y);
    this.movePlayer(newX, newY);
    window.removeEventListener("keydown", this);
    Game.engine.unlock();
};


