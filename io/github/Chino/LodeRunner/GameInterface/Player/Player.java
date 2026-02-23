package io.github.Chino.LodeRunner.GameInterface.Player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class Player extends InputListener{
    private int posX = 20;
    private int posY = 20;

    private Texture playerSprite;

    public Player() {
        this.playerSprite = new Texture("data/textures/blocks/brick.png");
    }

    public void moveX(int xMovement){
        this.posX += xMovement;
    }

    public void render(SpriteBatch batch){
        batch.begin();
        batch.draw(this.playerSprite, this.posX, this.posY);
        batch.end();
    }

    


}