package com.danbramos.ringprototype.screens.ui; // Or a new package like com.danbramos.ringprototype.battle.ui

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.Enemy;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.screens.BattleScreen; // To call back

import java.util.List;

public class BattleUiManager {
    private final RingPrototypeGame game;
    private final BattleScreen battleScreen; // To call methods like advanceTurn, selectSkill
    private final Skin skin;
    private final Stage stage;

    // UI Components
    public Label turnInfoLabel;
    private TextButton endTurnButton;
    private Table statsPanelTable;
    private Table activeCombatantTable;
    private Label activeCharNameLabel;
    private Label activeCharHealthLabel;
    private ProgressBar activeCharHealthBar;
    private Label activeCharManaLabel;
    private ProgressBar activeCharManaBar;
    private Label actionStateLabel;
    private Label battleLogLabel;
    
    // Character popup menu
    private Window actionPopupMenu;
    private boolean isPopupVisible = false;
    private int tileWidth;
    private int tileHeight;

    public BattleUiManager(RingPrototypeGame game, Stage stage, BattleScreen battleScreen) {
        this.game = game;
        this.skin = game.skin;
        this.stage = stage;
        this.battleScreen = battleScreen;
        this.tileWidth = battleScreen.getTileWidth();
        this.tileHeight = battleScreen.getTileHeight();
        setupUI();
    }

    private void setupUI() {
        // Top right panel for turn info and end turn button
        Table topRightPanel = new Table();
        topRightPanel.setFillParent(true);
        topRightPanel.top().right();
        
        // Style the turn info with a background
        Table turnInfoTable = new Table();
        if (skin.has("panel-background", Skin.TintedDrawable.class)) {
            turnInfoTable.setBackground(skin.getTiledDrawable("panel-background"));
        }

        turnInfoLabel = createLabel("Waiting...", "title");
        turnInfoTable.add(turnInfoLabel).pad(10);

        endTurnButton = new TextButton("End Turn", skin);
        endTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor uiActor) {
                if (battleScreen.getCurrentTurnActor() instanceof BattleCharacter) {
                    battleScreen.advanceTurn();
                    hidePopupMenu();
                }
            }
        });
        
        topRightPanel.add(turnInfoTable).pad(10).fillX().expandX().right().row();
        topRightPanel.add(endTurnButton).width(150).pad(10).right().row();
        
        // Top left panel for active combatant info
        activeCombatantTable = new Table();
        activeCombatantTable.setFillParent(true);
        activeCombatantTable.top().left();
        
        Table combatantInfoTable = new Table();
        if (skin.has("panel-background", Skin.TintedDrawable.class)) {
            combatantInfoTable.setBackground(skin.getTiledDrawable("panel-background"));
        }
        
        activeCharNameLabel = createLabel("No active combatant", "header");
        activeCharHealthLabel = createLabel("HP: 0/0", "default");
        activeCharHealthBar = new ProgressBar(0, 100, 1, false, skin, "curved");
        activeCharHealthBar.setValue(100);
        
        activeCharManaLabel = createLabel("MP: 0/0", "default");
        activeCharManaBar = new ProgressBar(0, 100, 1, false, skin, "curved");
        activeCharManaBar.setValue(100);
        
        combatantInfoTable.add(activeCharNameLabel).colspan(2).left().pad(5).row();
        combatantInfoTable.add(activeCharHealthLabel).left().pad(5, 5, 0, 5);
        combatantInfoTable.add(activeCharHealthBar).width(150).height(15).pad(5, 5, 0, 5).row();
        combatantInfoTable.add(activeCharManaLabel).left().pad(5);
        combatantInfoTable.add(activeCharManaBar).width(150).height(15).pad(5).row();
        
        // Add status info
        actionStateLabel = createLabel("Select an action", "default");
        combatantInfoTable.add(actionStateLabel).colspan(2).left().pad(5).row();
        
        activeCombatantTable.add(combatantInfoTable).pad(10).fillX().expandX().left();

        // Center panel for battle log
        Table centerPanel = new Table();
        centerPanel.setFillParent(true);
        centerPanel.center().bottom();
        
        battleLogLabel = createLabel("Battle started!", "default");
        battleLogLabel.setAlignment(Align.center);
        battleLogLabel.setWrap(true);
        
        Table logTable = new Table();
        if (skin.has("panel-background", Skin.TintedDrawable.class)) {
            logTable.setBackground(skin.getTiledDrawable("panel-background"));
        }
        logTable.add(battleLogLabel).width(300).pad(10);
        
        centerPanel.add(logTable).padBottom(100);
        
        // Create the popup menu (initially hidden)
        createPopupMenu();
        
        // Skill buttons now at the top center
        Table skillButtonTable = new Table();
        skillButtonTable.setFillParent(true);
        skillButtonTable.top().center(); // Position at top center
        
        // Add all UI components to stage
        stage.addActor(topRightPanel);
        stage.addActor(activeCombatantTable);
        stage.addActor(centerPanel);
        stage.addActor(skillButtonTable);
    }

    private void createPopupMenu() {
        // Create the popup but don't add to stage yet
        actionPopupMenu = new Window("", skin);
        actionPopupMenu.setMovable(false); // Don't allow user to move it
        actionPopupMenu.setModal(false);   // Don't block input to other elements
        actionPopupMenu.setVisible(false); // Start hidden
    }
    
    private void updatePopupMenu(BattleCharacter character) {
        actionPopupMenu.clear();
        
        Table content = new Table();
        
        if (skin.has("window-background", Skin.TintedDrawable.class)) {
            content.setBackground(skin.getTiledDrawable("window-background"));
        }
        
        // Movement info
        String movementText = "Movement: " + character.getRemainingMovement() + "/" + character.getMovementRange();
        Label movementLabel = createLabel(movementText, "default");
        content.add(movementLabel).padTop(5).padBottom(5).left().expandX().row();
        
        // Skills button
        TextButton skillsButton = new TextButton("Skills", skin);
        skillsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showSkillsSubmenu(character);
            }
        });
        
        // Items button
        TextButton itemsButton = new TextButton("Items", skin);
        itemsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // For now just show a log message as items aren't implemented yet
                battleLogLabel.setText(character.getName() + " checks inventory...");
            }
        });
        
        // End turn button for the popup
        TextButton popupEndTurnButton = new TextButton("End Turn", skin);
        popupEndTurnButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                battleScreen.advanceTurn();
                hidePopupMenu();
    }
        });
        
        // Disable the appropriate buttons based on character state
        if (character.hasPerformedMajorAction()) {
            skillsButton.setDisabled(true);
        }
        
        // Add the buttons to the content table
        content.add(skillsButton).width(100).pad(5).fillX().row();
        content.add(itemsButton).width(100).pad(5).fillX().row();
        content.add(popupEndTurnButton).width(100).pad(5).fillX().row();
        
        actionPopupMenu.add(content).width(120).pad(5);
        actionPopupMenu.pack();
    }
    
    private void showSkillsSubmenu(BattleCharacter character) {
        // Hide the main popup
        hidePopupMenu();
        
        // Create a skills window
        Window skillsWindow = new Window("Skills", skin);
        skillsWindow.setMovable(false);
        
        Table content = new Table();
        List<Skill> skills = character.getKnownSkills();
        
        if (skills.isEmpty()) {
            content.add(createLabel("No skills available", "default")).pad(10);
        } else {
            for (final Skill skill : skills) {
                TextButton skillButton = new TextButton(skill.getName(), skin);
                skillButton.addListener(new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        battleScreen.selectSkill(skill);
                        skillsWindow.remove(); // Close the skills window
                    }
                });
                content.add(skillButton).width(120).pad(5).fillX().row();
            }
        }
        
        // Back button
        TextButton backButton = new TextButton("Back", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                skillsWindow.remove();
                showPopupMenu(character);
            }
        });
        content.add(backButton).width(120).pad(5).fillX().row();
        
        skillsWindow.add(content).width(130).pad(5);
        skillsWindow.pack();
        
        // Position the skills window above the character
        positionWindowAboveCharacter(skillsWindow, character);
        
        // Add to stage
        stage.addActor(skillsWindow);
    }
    
    public void showPopupMenu(BattleCharacter character) {
        if (character == null) return;
        
        // Update the menu content
        updatePopupMenu(character);
        
        // Position the menu above the character sprite
        positionWindowAboveCharacter(actionPopupMenu, character);
        
        // Make it visible and add to stage
        actionPopupMenu.setVisible(true);
        if (actionPopupMenu.getStage() == null) {
            stage.addActor(actionPopupMenu);
        }
        
        isPopupVisible = true;
    }
    
    private void positionWindowAboveCharacter(Window window, BattleCharacter character) {
        // Get character position in world coordinates
        Vector2 characterPos = character.getBattleMapPosition();
        
        // Convert to screen coordinates
        float worldX = characterPos.x * tileWidth;
        float worldY = characterPos.y * tileHeight;
        
        // Position window above character with an offset
        window.setPosition(
            worldX - window.getWidth() / 2 + tileWidth / 2,
            worldY + tileHeight + 10
        );
    }
    
    public void hidePopupMenu() {
        if (actionPopupMenu != null) {
            actionPopupMenu.setVisible(false);
            if (actionPopupMenu.getStage() != null) {
                actionPopupMenu.remove();
            }
        }
        isPopupVisible = false;
    }

    public void updateTurnInfo(IBattleActor currentTurnActor) {
        if (turnInfoLabel == null) return;
        
        // Hide any popup menu when turn changes
        hidePopupMenu();
        
        if (currentTurnActor == null) {
            turnInfoLabel.setText("Battle Complete");
            activeCharNameLabel.setText("No active combatant");
            activeCharHealthLabel.setText("HP: 0/0");
            activeCharHealthBar.setValue(0);
            activeCharManaLabel.setText("MP: 0/0");
            activeCharManaBar.setValue(0);
            actionStateLabel.setText("");
            endTurnButton.setDisabled(true);
            return;
        }
        
        // Update turn info
        String turnText = "Current Turn: " + currentTurnActor.getName();
        turnInfoLabel.setText(turnText);
        
        // Update active combatant info
        activeCharNameLabel.setText(currentTurnActor.getName());
        
        int hp = currentTurnActor.getCurrentHp();
        int maxHp = currentTurnActor.getMaxHp();
        float hpPercent = (float) hp / maxHp * 100;
        
        activeCharHealthLabel.setText("HP: " + hp + "/" + maxHp);
        activeCharHealthBar.setValue(hpPercent);
        
        // Set health bar color based on health percentage
        if (hpPercent < 25) {
            activeCharHealthBar.setColor(Color.RED);
        } else if (hpPercent < 50) {
            activeCharHealthBar.setColor(Color.ORANGE);
        } else {
            activeCharHealthBar.setColor(Color.GREEN);
        }
        
        // Update mana for player characters or hide for enemies
        if (currentTurnActor instanceof BattleCharacter) {
            BattleCharacter bc = (BattleCharacter) currentTurnActor;
            int mp = bc.getSourceCharacter().getManaPoints();
            int maxMp = bc.getSourceCharacter().getMaxManaPoints();
            float mpPercent = (float) mp / maxMp * 100;
            
            activeCharManaLabel.setText("MP: " + mp + "/" + maxMp);
            activeCharManaBar.setValue(mpPercent);
            activeCharManaLabel.setVisible(true);
            activeCharManaBar.setVisible(true);
            
            // Update action state with more detailed info
            if (bc.hasPerformedMajorAction()) {
                if (bc.getRemainingMovement() > 0) {
                    actionStateLabel.setText("Skill used - Can still move " + bc.getRemainingMovement() + " tiles");
                } else {
                    actionStateLabel.setText("No actions left - End turn");
                }
            } else {
                if (bc.getRemainingMovement() > 0) {
                    actionStateLabel.setText("Select an action or move " + bc.getRemainingMovement() + " tiles");
                } else {
                    actionStateLabel.setText("Select an action (no movement left)");
                }
            }
            
            // Show the popup menu for the active character
            showPopupMenu(bc);
        } else {
            // Hide mana for enemies
            activeCharManaLabel.setVisible(false);
            activeCharManaBar.setVisible(false);
            actionStateLabel.setText("Enemy turn");
        }
        
        // Update end turn button state
        endTurnButton.setDisabled(!(currentTurnActor instanceof BattleCharacter) || battleScreen.isBattleOver());
    }

    public void updateSkillButtons(IBattleActor currentTurnActor) {
        // Skill buttons are now handled by the popup menu
        // This is kept for compatibility with existing code that calls this method
        if (currentTurnActor instanceof BattleCharacter && !battleScreen.isBattleOver()) {
            showPopupMenu((BattleCharacter)currentTurnActor);
        }
    }

    public void setEndTurnButtonDisabled(boolean disabled) {
        if (endTurnButton != null) {
            endTurnButton.setDisabled(disabled);
        }
    }

    public void clearSkillButtons() {
        // Just hide the popup menu
        hidePopupMenu();
        }
    
    public void updateBattleLog(String message) {
        if (battleLogLabel != null) {
            battleLogLabel.setText(message);
        }
    }

    private Label createLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        } else {
            return new Label(text, skin);
        }
    }
}
