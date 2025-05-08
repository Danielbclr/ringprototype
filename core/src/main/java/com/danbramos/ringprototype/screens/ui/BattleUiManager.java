package com.danbramos.ringprototype.screens.ui; // Or a new package like com.danbramos.ringprototype.battle.ui

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.screens.BattleScreen; // To call back

import java.util.List;

public class BattleUiManager {
    private final RingPrototypeGame game;
    private final BattleScreen battleScreen; // To call methods like advanceTurn, selectSkill
    private final Skin skin;
    private final Stage stage;

    public Label turnInfoLabel;
    private TextButton endTurnButton;
    private Table skillButtonTable;

    public BattleUiManager(RingPrototypeGame game, Stage stage, BattleScreen battleScreen) {
        this.game = game;
        this.skin = game.skin;
        this.stage = stage;
        this.battleScreen = battleScreen;
        setupUI();
    }

    private void setupUI() {
        Table mainUiTable = new Table();
        mainUiTable.setFillParent(true);
        mainUiTable.top().right(); // Turn info and end turn button

        turnInfoLabel = createLabel("Turn: N/A", "default-title");
        mainUiTable.add(turnInfoLabel).pad(10).align(Align.right).row();

        endTurnButton = new TextButton("End Turn", skin);
        endTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor uiActor) {
                // Delegate to BattleScreen to handle turn advancement logic
                if (battleScreen.getCurrentTurnActor() instanceof BattleCharacter) {
                    battleScreen.advanceTurn();
                }
            }
        });
        mainUiTable.add(endTurnButton).width(150).pad(10).align(Align.right).row();
        stage.addActor(mainUiTable);

        // Skill Button Table (Bottom of screen)
        skillButtonTable = new Table();
        skillButtonTable.setFillParent(true);
        skillButtonTable.bottom().pad(10);
        stage.addActor(skillButtonTable);
    }

    public void updateTurnInfo(IBattleActor currentTurnActor) {
        if (turnInfoLabel == null) return;
        String turnText = "Turn: N/A";
        if (currentTurnActor != null) {
            turnText = "Turn: " + currentTurnActor.getName();
            if (currentTurnActor.hasPerformedMajorAction()) {
                turnText += " (Action Taken)";
            }
        }
        turnInfoLabel.setText(turnText);
        // Disable end turn button if it's not a player's turn or if battle is over
        endTurnButton.setDisabled(!(currentTurnActor instanceof BattleCharacter) || currentTurnActor == null || battleScreen.isBattleOver());
    }

    public void updateSkillButtons(IBattleActor currentTurnActor) {
        skillButtonTable.clearChildren();
        if (currentTurnActor instanceof BattleCharacter) {
            BattleCharacter bc = (BattleCharacter) currentTurnActor;
            if (bc.hasPerformedMajorAction() || battleScreen.isBattleOver()) {
                return;
            }
            List<Skill> skills = bc.getKnownSkills();
            for (final Skill skill : skills) {
                TextButton skillButton = new TextButton(skill.getName(), skin);
                skillButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        // Delegate skill selection to BattleScreen or BattleInputHandler
                        battleScreen.selectSkill(skill);
                    }
                });
                skillButtonTable.add(skillButton).pad(5);
            }
        }
    }

    public void setEndTurnButtonDisabled(boolean disabled) {
        if (endTurnButton != null) {
            endTurnButton.setDisabled(disabled);
        }
    }

    public void clearSkillButtons() {
        if (skillButtonTable != null) {
            skillButtonTable.clearChildren();
        }
    }


    // Helper to create labels
    private Label createLabel(String text, String preferredStyleName) {
        String fallbackStyleName = "default";
        if (skin.has(preferredStyleName, Label.LabelStyle.class)) {
            return new Label(text, skin, preferredStyleName);
        } else if (skin.has(fallbackStyleName, Label.LabelStyle.class)) {
            // Gdx.app.log("BattleUiManager", "LabelStyle '" + preferredStyleName + "' not found. Falling back to '" + fallbackStyleName + "'.");
            return new Label(text, skin, fallbackStyleName);
        } else {
            // Gdx.app.error("BattleUiManager", "LabelStyle '" + preferredStyleName + "' and fallback '" + fallbackStyleName + "' not found. Creating programmatic fallback.");
            Label.LabelStyle programmaticStyle = new Label.LabelStyle();
            programmaticStyle.font = skin.getFont("default-font");
            if (programmaticStyle.font == null) programmaticStyle.font = new BitmapFont();
            programmaticStyle.fontColor = Color.WHITE;
            return new Label(text, programmaticStyle);
        }
    }
}
