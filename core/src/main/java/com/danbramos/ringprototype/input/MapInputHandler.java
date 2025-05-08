package com.danbramos.ringprototype.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.screens.BattleScreen;
import com.danbramos.ringprototype.screens.MapScreen;
import com.danbramos.ringprototype.screens.PartyScreen;
import com.danbramos.ringprototype.screens.PauseMenuScreen; // Import PauseMenuScreen

public class MapInputHandler implements InputProcessor {

    private final RingPrototypeGame game;
    private final MapScreen mapScreen;


    public MapInputHandler(MapScreen mapScreen, RingPrototypeGame game) {
        this.mapScreen = mapScreen;
        this.game = game;
    }

    @Override
    public boolean keyDown(int keycode) {
        int deltaX = 0;
        int deltaY = 0;

        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A) {
            deltaX = -1;
        }
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) {
            deltaX = 1;
        }
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W) {
            deltaY = 1;
        }
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) {
            deltaY = -1;
        }

        if (deltaX != 0 || deltaY != 0) {
            mapScreen.attemptCharacterMove(deltaX, deltaY);
            return true;
        }

        if (keycode == Input.Keys.P) {
            Gdx.app.log("MapInputHandler", "P key pressed, switching to PartyScreen.");
            // Pass the current mapScreen as the previous screen for PartyScreen if it needs to return
            game.setScreen(new PartyScreen(game));
            return true;
        }

        if (keycode == Input.Keys.B) {
            Gdx.app.log("MapInputHandler", "B key pressed, switching to BattleScreen.");
            game.setScreen(new BattleScreen(game));
            return true;
        }

        // Change ESC to open Pause Menu
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.log("MapInputHandler", "ESC key pressed, switching to PauseMenuScreen.");
            game.setScreen(new PauseMenuScreen(game, mapScreen)); // Pass current mapScreen
            return true;
        }

        return false;
    }

    // ... other InputProcessor methods ...
    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchCancelled(int i, int i1, int i2, int i3) {
        return false; // Added missing method
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
