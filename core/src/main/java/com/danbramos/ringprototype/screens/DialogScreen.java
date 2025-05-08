package com.danbramos.ringprototype.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.quests.Quest;
import com.danbramos.ringprototype.quests.QuestManager;
import com.danbramos.ringprototype.resources.ResourceType;

import java.util.List;

/**
 * Screen for displaying dialogue with NPCs and quest givers
 */
public class DialogScreen implements Screen {
    private final RingPrototypeGame game;
    private final Quest currentQuest;
    private final Stage stage;
    private final TextureRegion npcSprite;
    private int currentDialogIndex;
    private List<Quest.DialogueLine> currentDialogue;
    private Window dialogWindow;
    private Label speakerLabel;
    private Label dialogTextLabel;
    private Table choicesTable;
    
    /**
     * Constructor for the dialog screen
     * 
     * @param game The game instance
     * @param questId The ID of the quest to display dialog for
     */
    public DialogScreen(RingPrototypeGame game, String questId) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
        
        // Get the quest from the quest manager
        this.currentQuest = QuestManager.getInstance().getQuest(questId);
        if (this.currentQuest == null) {
            Gdx.app.error("DialogScreen", "Quest not found: " + questId);
            this.npcSprite = null;
            return;
        }
        
        // Get the NPC sprite
        this.npcSprite = QuestManager.getInstance().createQuestGiverSprite(currentQuest, game.characterSheet);
        
        // Initialize dialog
        this.currentDialogue = currentQuest.getCurrentDialogue();
        this.currentDialogIndex = 0;
        
        Gdx.input.setInputProcessor(stage);
        createUI();
        updateDialogText();
    }
    
    /**
     * Create the UI elements for the dialog screen
     */
    private void createUI() {
        float padding = 20f;
        
        try {
            // Create the dialog window
            dialogWindow = new Window("", game.skin);
            dialogWindow.setSize(Gdx.graphics.getWidth() * 0.8f, Gdx.graphics.getHeight() * 0.4f);
            dialogWindow.setPosition(
                Gdx.graphics.getWidth() / 2f - dialogWindow.getWidth() / 2f,
                Gdx.graphics.getHeight() * 0.1f
            );
            dialogWindow.setMovable(false);
            
            // Set up the content table
            Table contentTable = new Table();
            contentTable.pad(padding);
            contentTable.setFillParent(true);
            
            // Create NPC sprite image (if available)
            if (npcSprite != null) {
                // Create a scaled version of the sprite for display
                TextureRegionDrawable npcDrawable = new TextureRegionDrawable(npcSprite);
                com.badlogic.gdx.scenes.scene2d.ui.Image npcImage = 
                    new com.badlogic.gdx.scenes.scene2d.ui.Image(npcDrawable);
                float scale = 3.0f; // Scale up the sprite for better visibility
                npcImage.setSize(npcSprite.getRegionWidth() * scale, npcSprite.getRegionHeight() * scale);
                contentTable.add(npcImage).size(npcImage.getWidth(), npcImage.getHeight()).padRight(padding);
            }
            
            // Create dialog content area
            Table dialogContent = new Table();
            
            // Speaker name
            speakerLabel = new Label("", game.skin);
            speakerLabel.setColor(Color.YELLOW);
            dialogContent.add(speakerLabel).left().padBottom(padding/2).row();
            
            // Dialog text
            dialogTextLabel = new Label("", game.skin);
            dialogTextLabel.setWrap(true);
            dialogContent.add(dialogTextLabel).width(dialogWindow.getWidth() * 0.7f).left().row();
            
            // Add choices table
            choicesTable = new Table();
            dialogContent.add(choicesTable).width(dialogWindow.getWidth() * 0.7f).left().padTop(padding).row();
            
            // Add a continue button
            TextButton continueButton = new TextButton("Continue", game.skin);
            continueButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    advanceDialog();
                }
            });
            dialogContent.add(continueButton).right().padTop(padding);
            
            // Add dialog content to main content table
            contentTable.add(dialogContent).expand().fill();
            
            // Add content table to dialog window
            dialogWindow.add(contentTable).expand().fill();
            
            // Add dialog window to stage
            stage.addActor(dialogWindow);
        } catch (Exception e) {
            // If we encounter any issues with the UI, log them and provide a fallback
            Gdx.app.error("DialogScreen", "Error creating UI: " + e.getMessage());
            
            // Create a simple fallback UI
            createFallbackUI(padding);
        }
    }
    
    /**
     * Create a minimal fallback UI in case the skin has issues
     */
    private void createFallbackUI(float padding) {
        // Create a simple table without a Window
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.pad(padding);
        
        // Create basic labels
        BitmapFont font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        
        // Create our UI elements
        speakerLabel = new Label("", labelStyle);
        speakerLabel.setColor(Color.YELLOW);
        
        dialogTextLabel = new Label("", labelStyle);
        dialogTextLabel.setWrap(true);
        
        choicesTable = new Table();
        
        // Add elements to the root table
        rootTable.add(speakerLabel).left().padBottom(padding/2).row();
        rootTable.add(dialogTextLabel).width(Gdx.graphics.getWidth() * 0.7f).left().row();
        rootTable.add(choicesTable).left().padTop(padding).row();
        
        // Create a simple text button
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        
        TextButton continueButton = new TextButton("Continue", buttonStyle);
        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advanceDialog();
            }
        });
        
        rootTable.add(continueButton).right().padTop(padding);
        
        // Add to stage
        stage.addActor(rootTable);
    }
    
    /**
     * Update the dialog text and choices
     */
    private void updateDialogText() {
        if (currentDialogue == null || currentDialogue.isEmpty() || currentDialogIndex >= currentDialogue.size()) {
            // No more dialog, return to map
            returnToMap();
            return;
        }
        
        try {
            // Get the current dialog line
            Quest.DialogueLine dialogLine = currentDialogue.get(currentDialogIndex);
            
            // Update speaker and text
            if (speakerLabel != null) {
                speakerLabel.setText(dialogLine.getSpeaker());
            }
            
            if (dialogTextLabel != null) {
                dialogTextLabel.setText(dialogLine.getText());
            }
            
            // Clear and update choices
            if (choicesTable != null) {
                choicesTable.clear();
                
                // If there are choices, show them instead of the continue button
                if (dialogLine.getChoices() != null && !dialogLine.getChoices().isEmpty()) {
                    for (Quest.DialogueChoice choice : dialogLine.getChoices()) {
                        try {
                            TextButton choiceButton = new TextButton(choice.getText(), game.skin);
                            choiceButton.addListener(new ClickListener() {
                                @Override
                                public void clicked(InputEvent event, float x, float y) {
                                    handleChoice(choice);
                                }
                            });
                            choicesTable.add(choiceButton).left().padTop(10).row();
                        } catch (Exception e) {
                            Gdx.app.error("DialogScreen", "Error creating choice button: " + e.getMessage());
                            // Continue with other choices if possible
                        }
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("DialogScreen", "Error updating dialog: " + e.getMessage());
            // In case of error, try to recover by advancing
            advanceDialog();
        }
    }
    
    /**
     * Handle a player choice
     * 
     * @param choice The chosen option
     */
    private void handleChoice(Quest.DialogueChoice choice) {
        // For this simple implementation, just accept the quest and advance dialog
        if (currentQuest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            QuestManager.getInstance().acceptQuest(currentQuest.getId());
            Gdx.app.log("DialogScreen", "Quest accepted: " + currentQuest.getTitle());
        }
        
        advanceDialog();
    }
    
    /**
     * Advance to the next dialog line
     */
    private void advanceDialog() {
        currentDialogIndex++;
        
        // Check if we've reached the end of the dialog
        if (currentDialogIndex >= currentDialogue.size()) {
            // If quest is completed, give rewards
            if (currentQuest.getStatus() == Quest.QuestStatus.COMPLETED) {
                awardQuestRewards();
            }
            
            // Return to map
            returnToMap();
            return;
        }
        
        // Update the dialog with the next line
        updateDialogText();
    }
    
    /**
     * Award quest rewards to the player
     */
    private void awardQuestRewards() {
        if (currentQuest.getRewards() == null) {
            return;
        }
        
        // Add gold
        if (currentQuest.getRewards().getGold() > 0) {
            game.resourceManager.addResource(ResourceType.GOLD, currentQuest.getRewards().getGold());
            Gdx.app.log("DialogScreen", "Awarded " + currentQuest.getRewards().getGold() + " gold");
        }
        
        // TODO: Add experience and items when those systems are implemented
    }
    
    /**
     * Return to the map screen
     */
    private void returnToMap() {
        game.setScreen(new MapScreen(game));
    }

    @Override
    public void show() {
        Gdx.app.log("DialogScreen", "Showing DialogScreen");
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.2f, 1f);
        
        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        
        // Reposition the dialog window
        if (dialogWindow != null) {
            dialogWindow.setSize(width * 0.8f, height * 0.4f);
            dialogWindow.setPosition(
                width / 2f - dialogWindow.getWidth() / 2f,
                height * 0.1f
            );
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
} 