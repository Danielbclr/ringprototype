package com.danbramos.ringprototype.screens.ui; // New sub-package for UI helpers

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.items.Item;
import com.danbramos.ringprototype.party.Character;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.screens.MapScreen; // Needed for back button

public class PartyScreenView {

    private final RingPrototypeGame game;
    private final Skin skin;

    // UI Components that need to be accessed/updated
    private List<CharacterListItem> characterListWidget;
    private Label selectedCharNameLabel;
    private Label selectedCharLevelClassLabel;
    private Label selectedCharHpLabel;
    private Label selectedCharMpLabel;
    private Label selectedCharXpLabel;
    private Label selectedCharAttributesLabel;
    private Table skillsTable;
    private Table inventoryLayoutTable;
    private Table equippedItemsTable;
    private Table carriedItemsTable;
    private Label noCharacterSelectedLabel;

    // Wrapper class for displaying characters in the List widget
    // Made public static so PartyScreen can potentially access it if needed,
    // though primarily used internally by PartyScreenView.
    public static class CharacterListItem {
        public final GameCharacter character;

        public CharacterListItem(GameCharacter character) {
            this.character = character;
        }

        public GameCharacter getCharacter() {
            return character;
        }

        @Override
        public String toString() {
            return character.getName(); // This is what the List widget will display
        }
    }

    public PartyScreenView(RingPrototypeGame game) {
        this.game = game;
        this.skin = game.skin; // Cache the skin
    }

    private Label createLabel(String text, String preferredStyleName, String fallbackStyleName) {
        if (skin.has(preferredStyleName, Label.LabelStyle.class)) {
            return new Label(text, skin, preferredStyleName);
        } else if (skin.has(fallbackStyleName, Label.LabelStyle.class)) {
            Gdx.app.log("PartyScreenView", "LabelStyle '" + preferredStyleName + "' not found. Falling back to '" + fallbackStyleName + "'.");
            return new Label(text, skin, fallbackStyleName);
        } else {
            Gdx.app.error("PartyScreenView", "LabelStyle '" + preferredStyleName + "' and fallback '" + fallbackStyleName + "' not found. Creating programmatic fallback.");
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

    public Table buildUI(Stage stage) {
        Table mainLayoutTable = new Table();
        mainLayoutTable.setFillParent(true);
        // mainLayoutTable.setDebug(true); // For layout debugging

        // --- Title ---
        Label titleLabel = createLabel("Party Roster", "default-title");
        mainLayoutTable.add(titleLabel).colspan(2).padBottom(20).row();

        // --- Character List ---
        characterListWidget = new List<>(skin);
        Array<CharacterListItem> listItems = new Array<>();
        for (GameCharacter member : game.partyManager.getMembers()) {
            listItems.add(new CharacterListItem(member));
        }
        characterListWidget.setItems(listItems);

        characterListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                CharacterListItem selectedItem = characterListWidget.getSelected();
                updateCharacterDetails(selectedItem != null ? selectedItem.getCharacter() : null);
            }
        });

        ScrollPane characterListScrollPane = new ScrollPane(characterListWidget, skin);
        characterListScrollPane.setFadeScrollBars(false);

        // --- Character Details Table ---
        Table characterDetailsTable = new Table(skin);
        characterDetailsTable.top().left();
        // characterDetailsTable.setDebug(true);

        selectedCharNameLabel = createLabel("", "default-title");
        selectedCharLevelClassLabel = createLabel("", "default");
        selectedCharHpLabel = createLabel("", "default");
        selectedCharMpLabel = createLabel("", "default");
        selectedCharXpLabel = createLabel("", "default");
        selectedCharAttributesLabel = createLabel("", "default");
        selectedCharAttributesLabel.setWrap(true);

        skillsTable = new Table(skin);
        inventoryLayoutTable = new Table(skin);
        equippedItemsTable = new Table(skin);
        carriedItemsTable = new Table(skin);
        noCharacterSelectedLabel = createLabel("Select a character to view details.", "default");

        characterDetailsTable.add(selectedCharNameLabel).left().colspan(2).padBottom(10).row();
        characterDetailsTable.add(selectedCharLevelClassLabel).left().colspan(2).padBottom(5).row();
        characterDetailsTable.add(selectedCharHpLabel).left().colspan(2).padBottom(5).row();
        characterDetailsTable.add(selectedCharMpLabel).left().colspan(2).padBottom(5).row();
        characterDetailsTable.add(selectedCharXpLabel).left().colspan(2).padBottom(15).row();

        characterDetailsTable.add(createLabel("Attributes:", "default-bold")).left().colspan(2).padBottom(5).row();
        characterDetailsTable.add(selectedCharAttributesLabel).left().growX().colspan(2).padBottom(15).row();

        characterDetailsTable.add(createLabel("Skills:", "default-bold")).left().colspan(2).padBottom(5).row();
        characterDetailsTable.add(new ScrollPane(skillsTable, skin)).growX().height(80).colspan(2).padBottom(15).row();

        inventoryLayoutTable.add(createLabel("Equipped Items:", "default-bold")).left().row();
        inventoryLayoutTable.add(new ScrollPane(equippedItemsTable, skin)).growX().height(60).padBottom(10).row();
        inventoryLayoutTable.add(createLabel("Carried Items:", "default-bold")).left().row();
        inventoryLayoutTable.add(new ScrollPane(carriedItemsTable, skin)).growX().height(60).row();
        characterDetailsTable.add(inventoryLayoutTable).growX().colspan(2).row();

        characterDetailsTable.add(noCharacterSelectedLabel).center().colspan(2).padTop(20);

        // --- Layout for List and Details ---
        Table contentTable = new Table();
        contentTable.add(characterListScrollPane).width(200).growY().padRight(10);
        contentTable.add(characterDetailsTable).grow();
        mainLayoutTable.add(contentTable).grow().padBottom(20).row();

        // --- Back Button ---
        TextButton backButton = new TextButton("Back to Map", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game)); // Consider a more robust screen management
            }
        });
        mainLayoutTable.add(backButton).colspan(2).padTop(10);

        // Initial state
        if (!listItems.isEmpty()) {
            characterListWidget.setSelectedIndex(0); // Select the first character by default
            updateCharacterDetails(listItems.first().getCharacter());
        } else {
            updateCharacterDetails(null); // Show "no character selected" or empty state
        }
        return mainLayoutTable;
    }

    public void updateCharacterDetails(GameCharacter character) {
        boolean detailsVisible = character != null;

        noCharacterSelectedLabel.setVisible(!detailsVisible);
        selectedCharNameLabel.setVisible(detailsVisible);
        selectedCharLevelClassLabel.setVisible(detailsVisible);
        selectedCharHpLabel.setVisible(detailsVisible);
        selectedCharMpLabel.setVisible(detailsVisible);
        selectedCharXpLabel.setVisible(detailsVisible);
        selectedCharAttributesLabel.setVisible(detailsVisible);
        inventoryLayoutTable.setVisible(detailsVisible);

        // It's safer to check if the parent table (characterDetailsTable) is fully constructed
        // before trying to access cells that might not exist if `character` is null initially.
        // However, the labels themselves are already created, so setting their visibility is fine.

        // Find header labels and set visibility. This is a bit fragile if layout changes.
        // A more robust way would be to store references to these header labels if they also need to be hidden.
        // For now, we assume the section content (like inventoryLayoutTable) handles its own children.
        Table detailsParentTable = (Table) selectedCharAttributesLabel.getParent(); // This is characterDetailsTable
        if (detailsParentTable != null) {
            // Attributes Header (assuming it's the actor in the cell directly above selectedCharAttributesLabel's cell)
            com.badlogic.gdx.scenes.scene2d.ui.Cell<?> attrCell = detailsParentTable.getCell(selectedCharAttributesLabel);
            if (attrCell != null && attrCell.getRow() > 0) {
                Actor attributesHeader = detailsParentTable.getCells().get(attrCell.getRow() -1 ).getActor();
                if (attributesHeader instanceof Label && ((Label)attributesHeader).getText().toString().startsWith("Attributes:")) {
                    attributesHeader.setVisible(detailsVisible);
                }
            }
            // Skills Header
            Actor skillsScrollPane = skillsTable.getParent(); // This is the ScrollPane
            if (skillsScrollPane != null) {
                skillsScrollPane.setVisible(detailsVisible); // Hide/show the scrollpane itself
                com.badlogic.gdx.scenes.scene2d.ui.Cell<?> skillCell = detailsParentTable.getCell(skillsScrollPane);
                if (skillCell != null && skillCell.getRow() > 0) {
                    Actor skillsHeader = detailsParentTable.getCells().get(skillCell.getRow() -1 ).getActor();
                    if (skillsHeader instanceof Label && ((Label)skillsHeader).getText().toString().startsWith("Skills:")) {
                        skillsHeader.setVisible(detailsVisible);
                    }
                }
            }
        }


        if (!detailsVisible) {
            selectedCharNameLabel.setText("");
            selectedCharLevelClassLabel.setText("");
            selectedCharHpLabel.setText("");
            selectedCharMpLabel.setText("");
            selectedCharXpLabel.setText("");
            selectedCharAttributesLabel.setText("");
            skillsTable.clearChildren();
            equippedItemsTable.clearChildren();
            carriedItemsTable.clearChildren();
            return;
        }

        selectedCharNameLabel.setText(character.getName());
        selectedCharLevelClassLabel.setText("Level: " + character.getLevel() + " " + character.getGameClass().getDisplayName());
        selectedCharHpLabel.setText("HP: " + character.getHealthPoints() + " / " + character.getMaxHealthPoints());
        selectedCharMpLabel.setText("MP: " + character.getManaPoints() + " / " + character.getMaxManaPoints());
        selectedCharXpLabel.setText("XP: " + character.getExperiencePoints() + " / " + character.getExperienceToNextLevel());

        StringBuilder attrBuilder = new StringBuilder();
        attrBuilder.append("STR: ").append(character.getStrength()).append(" | ");
        attrBuilder.append("DEX: ").append(character.getDexterity()).append(" | ");
        attrBuilder.append("INT: ").append(character.getIntelligence()).append("\n");
        attrBuilder.append("CON: ").append(character.getConstitution()).append(" | ");
        attrBuilder.append("WIS: ").append(character.getWisdom()).append(" | ");
        attrBuilder.append("CHA: ").append(character.getCharisma());
        selectedCharAttributesLabel.setText(attrBuilder.toString());

        skillsTable.clearChildren();
        skillsTable.top().left();
        if (character.getKnownSkills().isEmpty()) {
            skillsTable.add(createLabel("No skills known.", "default")).left();
        } else {
            for (Skill skill : character.getKnownSkills()) {
                skillsTable.add(createLabel(skill.getName(), "default")).left().row();
            }
        }

        equippedItemsTable.clearChildren();
        equippedItemsTable.top().left();
        if (character.getEquippedItems().isEmpty()) {
            equippedItemsTable.add(createLabel("Nothing equipped.", "default")).left();
        } else {
            for (Item item : character.getEquippedItems()) {
                equippedItemsTable.add(createLabel(item.getName() + " [" + item.getType().getDisplayName() + "]", "default")).left().row();
            }
        }

        carriedItemsTable.clearChildren();
        carriedItemsTable.top().left();
        if (character.getInventory().isEmpty()) {
            carriedItemsTable.add(createLabel("Carrying nothing else.", "default")).left();
        } else {
            for (Item item : character.getInventory()) {
                carriedItemsTable.add(createLabel(item.getName() + " [" + item.getType().getDisplayName() + "]", "default")).left().row();
            }
        }
    }
}
