package com.danbramos.ringprototype.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
// import com.badlogic.gdx.utils.Align; // No longer directly used here
// import com.badlogic.gdx.utils.Array; // No longer directly used here
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danbramos.ringprototype.RingPrototypeGame;
// import com.danbramos.ringprototype.items.Item; // No longer directly used here
import com.danbramos.ringprototype.party.Character;
import com.danbramos.ringprototype.screens.ui.InventoryScreenView;

public class PauseMenuScreen implements Screen {

    private final RingPrototypeGame game;
    private final Screen previousScreen;
    private Stage stage;
    private Skin skin;

    private Table mainPauseOptionsTable;
    // private Table inventoryManagementTable; // Moved to InventoryView

    private boolean showingInventoryView = false;
    private boolean justOpened = false;

    // Inventory UI Elements - Moved to InventoryView
    // private Character currentCharacterForInventory;
    // private List<InventoryView.DisplayableItem> equippedItemsListWidget; // Use InventoryView's inner class
    // private List<InventoryView.DisplayableItem> carriedItemsListWidget;
    // private TextButton equipButton;
    // private TextButton unequipButton;
    // private Label inventoryCharacterNameLabel;

    private InventoryScreenView inventoryView; // Instance of our new view


    // DisplayableItem inner class moved to InventoryView

    public PauseMenuScreen(RingPrototypeGame game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        this.stage = new Stage(new ScreenViewport());
        this.skin = game.skin;
        this.inventoryView = new InventoryScreenView(game, this); // Initialize InventoryView
    }

    private Label createLabel(String text, String preferredStyleName, String fallbackStyleName) {
        if (skin.has(preferredStyleName, Label.LabelStyle.class)) {
            return new Label(text, skin, preferredStyleName);
        } else if (skin.has(fallbackStyleName, Label.LabelStyle.class)) {
            Gdx.app.log("PauseMenuScreen", "LabelStyle '" + preferredStyleName + "' not found. Falling back to '" + fallbackStyleName + "'.");
            return new Label(text, skin, fallbackStyleName);
        } else {
            Gdx.app.error("PauseMenuScreen", "LabelStyle '" + preferredStyleName + "' and fallback '" + fallbackStyleName + "' not found. Creating programmatic fallback.");
            Label.LabelStyle programmaticStyle = new Label.LabelStyle();
            programmaticStyle.font = skin.getFont("default-font");
            if (programmaticStyle.font == null) programmaticStyle.font = new BitmapFont();
            programmaticStyle.fontColor = Color.WHITE;
            return new Label(text, programmaticStyle);
        }
    }

    private Label createLabel(String text, String preferredStyleName) {
        return createLabel(text, preferredStyleName, "default");
    }


    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        stage.clear();
        justOpened = true;

        if (showingInventoryView) {
            // Character selection is now handled within InventoryScreenView
            Table inventoryTable = inventoryView.buildUI(); // No longer pass characterToDisplay
            stage.addActor(inventoryTable);
        } else {
            buildMainPauseOptionsUI();
        }
    }

    private void buildMainPauseOptionsUI() {
        stage.clear();
        mainPauseOptionsTable = new Table(skin);
        mainPauseOptionsTable.setFillParent(true);
        mainPauseOptionsTable.center();

        Label title = createLabel("Paused", "default-title");
        mainPauseOptionsTable.add(title).padBottom(30).row();

        TextButton resumeButton = new TextButton("Resume", skin);
        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(previousScreen);
            }
        });
        mainPauseOptionsTable.add(resumeButton).width(200).pad(10).row();

        TextButton partyButton = new TextButton("Party", skin);
        partyButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new PartyScreen(game));
            }
        });
        mainPauseOptionsTable.add(partyButton).width(200).pad(10).row();

        TextButton inventoryButton = new TextButton("Inventory", skin);
        inventoryButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showingInventoryView = true;
                show(); // Re-build the UI for inventory view
            }
        });
        mainPauseOptionsTable.add(inventoryButton).width(200).pad(10).row();

        TextButton quitButton = new TextButton("Quit Game", skin);
        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
        mainPauseOptionsTable.add(quitButton).width(200).pad(10).row();

        stage.addActor(mainPauseOptionsTable);
    }

    // This method is called by InventoryView when its "Back" button is pressed
    public void showMainPauseOptions() {
        showingInventoryView = false;
        show(); // Rebuild the main pause options UI
    }

    // buildInventoryManagementUI() and populateInventoryLists() are now in InventoryView

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 0.8f);

        if (justOpened) {
            justOpened = false;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (showingInventoryView) {
                showMainPauseOptions(); // Use the new method to switch view
            } else {
                game.setScreen(previousScreen);
            }
        }

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        Gdx.app.log("PauseMenuScreen", "PauseMenuScreen disposed.");
    }
}
