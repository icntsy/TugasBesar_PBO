package com.fish.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.Screen;

public class FishLevel implements Screen{
    private Stage mainStage;
    private Stage uiStage;
    private AnimatedActor fish;
    private BaseActor people;
    private BaseActor floor;
    private BaseActor winText;
    private boolean win;
    private float timeElapsed;
    private Label timeLabel;

    // game world dimensions
    final int mapWidth = 800;
    final int mapHeight = 500;

    // window dimensions
    final int viewWidth = 640;
    final int viewHeight = 480;

    public Game game;
    public FishLevel(Game g) //aggregation
    {
        game = g;
        create();
    }

    public void create()
    {
        mainStage = new Stage();
        uiStage = new Stage();
        timeElapsed = 0;

        floor = new BaseActor();
        floor.setTexture( new
                Texture(Gdx.files.internal("tiles-800-800.jpg")) );
        floor.setPosition( 0, 0 );
        mainStage.addActor( floor );

        people = new BaseActor();
        people.setTexture( new
                Texture(Gdx.files.internal("cheese.png")) );
        people.setPosition( 400, 300 );
        people.setOrigin( people.getWidth()/2,
                people.getHeight()/2 );

        mainStage.addActor( people );
        fish = new AnimatedActor(); //composisition
        TextureRegion[] frames = new TextureRegion[4];
        for (int n = 0; n < 4; n++)
        {
            String fileName = "mouse" + n + ".png";
            Texture tex = new
                    Texture(Gdx.files.internal(fileName));
            tex.setFilter(TextureFilter.Linear,
                    TextureFilter.Linear);
            frames[n] = new TextureRegion( tex );
        }
        Array<TextureRegion> framesArray = new Array<TextureRegion>(frames);

        Animation anim = new Animation(0.1f, framesArray,
                Animation.PlayMode.LOOP_PINGPONG);

        fish.setAnimation( anim );
        fish.setOrigin( fish.getWidth()/2, fish.getHeight()/2 );
        fish.setPosition( 20, 20 );
        mainStage.addActor(fish);

        winText = new BaseActor();
        winText.setTexture( new Texture(Gdx.files.internal("you-win.png")) );
        winText.setPosition( 170, 60 );
        winText.setVisible( false );
        uiStage.addActor( winText );

        BitmapFont font = new BitmapFont();
        String text = "Time: 0";
        LabelStyle style = new LabelStyle( font, Color.NAVY );
        timeLabel = new Label( text, style );
        timeLabel.setFontScale(2);
        timeLabel.setPosition(500,440); // sets bottom left(baseline) corner?
        uiStage.addActor( timeLabel );

        win = false;
    }

    public void render(float dt){
        // process input
        fish.velocityX = 0;
        fish.velocityY = 0;

        if (Gdx.input.isKeyPressed(Keys.LEFT))
            fish.velocityX -= 300;
        else if (Gdx.input.isKeyPressed(Keys.RIGHT))
            fish.velocityX += 300;
        else if (Gdx.input.isKeyPressed(Keys.UP))
            fish.velocityY += 300;
        else if (Gdx.input.isKeyPressed(Keys.DOWN))
            fish.velocityY -= 300;
        else if (Gdx.input.isKeyPressed(Keys.M))
            game.setScreen( new FishMenu(game) );

        // update
        mainStage.act(dt);
        uiStage.act(dt);

        // bound mouse to the rectangle defined by mapWidth, mapHeight
        fish.setX( MathUtils.clamp( fish.getX(),
                0, mapWidth - fish.getWidth() ));
        fish.setY( MathUtils.clamp( fish.getY(),
                0, mapHeight - fish.getHeight() ));

        // check win condition: mouse must be overlapping cheese
        Rectangle cheeseRectangle = people.getBoundingRectangle();
        Rectangle mouseRectangle = fish.getBoundingRectangle();

        if ( !win && cheeseRectangle.overlaps( mouseRectangle))
        {
            win = true;
            winText.addAction( Actions.sequence(
                    Actions.alpha(0),
                    Actions.show(),
                    Actions.fadeIn(2),
                    Actions.forever( Actions.sequence(
                            Actions.color( new Color(1,0,0,1), 1 ),
                            Actions.color( new Color(0,0,1,1), 1 )
                    ))
            ));

            people.addAction( Actions.parallel(
                    Actions.alpha(1),
                    Actions.rotateBy(360f, 1),
                    Actions.scaleTo(0,0, 2), // xAmt, yAmt,duration
                    Actions.fadeOut(1)));
        }
        if (!win) {
            timeElapsed += dt;
            timeLabel.setText( "Time: " + (int)timeElapsed );
        }

        // draw graphics
        Gdx.gl.glClearColor(0.8f, 0.8f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // camera adjustment
        Camera cam = mainStage.getCamera();

        // center camera on player
        cam.position.set( fish.getX() + fish.getOriginX(),
                fish.getY() + fish.getOriginY(), 0 );

        // bound camera to layout
        cam.position.x = MathUtils.clamp(cam.position.x,
                viewWidth/2, mapWidth - viewWidth/2);
        cam.position.y = MathUtils.clamp(cam.position.y,
                viewHeight/2, mapHeight - viewHeight/2);
        cam.update();

        mainStage.draw();
        uiStage.draw();
    }

    public void resize(int width, int height) { }
    public void pause() { }
    public void resume() { }
    public void dispose() { }
    public void show() { }
    public void hide() { }
}

