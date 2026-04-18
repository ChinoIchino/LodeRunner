package io.github.Chino.LodeRunner.GameInterface.Interface.Other;

import java.io.FileNotFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.video.VideoPlayer;
import com.badlogic.gdx.video.VideoPlayerCreator;

import io.github.Chino.LodeRunner.GameInterface.GDXMain;

public class IntroCinematicScreen implements Screen {
    private GDXMain main;

    private int originalXSize;
    private int originalYSize;

    private SpriteBatch batch;

    private VideoPlayer player;
    private FileHandle videoFile;

    public IntroCinematicScreen(GDXMain main){
        this.main = main;
        this.batch = new SpriteBatch();

        this.originalXSize = Gdx.graphics.getWidth();
        this.originalYSize = Gdx.graphics.getHeight();
    }
    
    @Override
    public void render(float delta){
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        this.player.update();

        Texture videoFrame = this.player.getTexture();
        if(videoFrame != null){
            this.batch.begin();
            this.batch.draw(videoFrame, 0, 0, this.originalXSize, this.originalYSize);
            this.batch.end();
        }

    }

    @Override
    public void dispose(){
        this.batch.dispose();
        this.player.dispose();
    }

    @Override
    public void show() {
        try{
            Gdx.input.setInputProcessor(null);

            this.player = VideoPlayerCreator.createVideoPlayer();
            this.videoFile = Gdx.files.internal("IntroCinematicV4.2.webm");
        
            this.player.setOnCompletionListener(new VideoPlayer.CompletionListener() {
                @Override
                public void onCompletionListener(FileHandle file) {
                    main.setScreen(main.getMenuScreen());
                }
		    });

            this.player.setOnVideoSizeListener(new VideoPlayer.VideoSizeListener() {
                @Override
                public void onVideoSize(float width, float height){
                    player.play();
                }
            });

            this.player.load(this.videoFile);
        }catch(FileNotFoundException e){
            System.out.println("GameInterface/Interface/IntroCinematicScreen.java: constructor catched FileNotFoundException while loading a video");
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
        this.player.pause();
    }

    @Override
    public void resume() {
        this.player.play();
    }

    @Override
    public void hide() {
    }
}