package com.danbramos.ringprototype.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.screens.ui.PartyScreenView; // Import the new view class

public class PartyScreen implements Screen {

    private final RingPrototypeGame game;
    private Stage stage;
    private PartyScreenView view; // Reference to our view/UI builder

    public PartyScreen(RingPrototypeGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        this.view = new PartyScreenView(game); // Initialize the view
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear(); // Clear previous actors from stage

        Table mainLayoutTable = view.buildUI(stage); // Build the UI using the view
        stage.addActor(mainLayoutTable); // Add the constructed UI to the stage

        Gdx.app.log("PartyScreen", "PartyScreen shown.");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.25f, 1f); // Dark background

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MapScreen(game));
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null); // Clear input processor when screen is hidden
        Gdx.app.log("PartyScreen", "PartyScreen hidden.");
    }

    @Override
    public void dispose() {
        stage.dispose(); // Dispose the stage
        Gdx.app.log("PartyScreen", "PartyScreen disposed.");
        // Skin is disposed by RingPrototypeGame
        // The view itself doesn't hold disposable LibGDX resources directly,
        // its components are part of the stage.
    }
}
