package com.danbramos.ringprototype.screens.ui; // New sub-package for UI helpers

import com.badlogic.gdx.Gdx;
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
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.items.Item;
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

    private Label createLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        } else {
            Gdx.app.log("PartyScreenView", "LabelStyle '" + styleName + "' not found. Using default.");
            return new Label(text, skin);
        }
    }

    public Table buildUI(Stage stage) {
        Table mainLayoutTable = new Table();
        mainLayoutTable.setFillParent(true);

        // Apply a background to the main table if available in skin
        if (skin.has("window-background", Skin.TintedDrawable.class)) {
            mainLayoutTable.setBackground(skin.getTiledDrawable("window-background"));
        }

        // --- HEADER ---
        Table headerTable = new Table();
        Label titleLabel = createLabel("Party Roster", "title");
        headerTable.add(titleLabel).expandX().center().pad(20);
        mainLayoutTable.add(headerTable).expandX().fillX().pad(10).row();

        // --- CONTENT AREA ---
        Table contentTable = new Table();

        // --- Character List (LEFT PANEL) ---
        Table leftPanel = new Table();
        leftPanel.top().pad(10);

        Label charactersLabel = createLabel("Characters", "header");
        leftPanel.add(charactersLabel).padBottom(10).left().row();

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
        characterListScrollPane.setScrollingDisabled(true, false);

        leftPanel.add(characterListScrollPane).expand().fill().minWidth(200);

        // --- Character Details (RIGHT PANEL) ---
        Table rightPanel = new Table();
        rightPanel.top().pad(10);

        // Basic info section
        Table infoSection = new Table();
        infoSection.top().left();

        selectedCharNameLabel = createLabel("", "title");
        selectedCharLevelClassLabel = createLabel("", "default");
        selectedCharHpLabel = createLabel("", "default");
        selectedCharMpLabel = createLabel("", "default");
        selectedCharXpLabel = createLabel("", "default");

        infoSection.add(selectedCharNameLabel).left().padBottom(10).row();
        infoSection.add(selectedCharLevelClassLabel).left().padBottom(5).row();

        // Health and Resources with visual bars
        Table statsTable = new Table();
        statsTable.add(selectedCharHpLabel).left().expandX();
        statsTable.add(selectedCharMpLabel).left().expandX().row();
        statsTable.add(selectedCharXpLabel).left().colspan(2).padTop(5);

        infoSection.add(statsTable).expandX().fillX().padBottom(15).row();

        // Attributes section
        Table attributesSection = new Table();
        attributesSection.top().left();

        Label attributesHeaderLabel = createLabel("Attributes", "header");
        selectedCharAttributesLabel = createLabel("", "default");
        selectedCharAttributesLabel.setWrap(true);

        attributesSection.add(attributesHeaderLabel).left().padBottom(5).row();
        attributesSection.add(selectedCharAttributesLabel).expandX().fillX().left().row();

        // Skills section
        Table skillsSection = new Table();
        skillsSection.top().left();

        Label skillsHeaderLabel = createLabel("Skills", "header");
        skillsTable = new Table(skin);
        ScrollPane skillsScrollPane = new ScrollPane(skillsTable, skin);
        skillsScrollPane.setFadeScrollBars(false);

        skillsSection.add(skillsHeaderLabel).left().padBottom(5).row();
        skillsSection.add(skillsScrollPane).expandX().fillX().height(120).row();

        // Equipment section
        inventoryLayoutTable = new Table();
        equippedItemsTable = new Table(skin);
        carriedItemsTable = new Table(skin);

        Label equippedLabel = createLabel("Equipped Items", "header");
        ScrollPane equippedScrollPane = new ScrollPane(equippedItemsTable, skin);
        equippedScrollPane.setFadeScrollBars(false);

        Label carriedLabel = createLabel("Carried Items", "header");
        ScrollPane carriedScrollPane = new ScrollPane(carriedItemsTable, skin);
        carriedScrollPane.setFadeScrollBars(false);

        inventoryLayoutTable.add(equippedLabel).left().padBottom(5).padTop(15).row();
        inventoryLayoutTable.add(equippedScrollPane).expandX().fillX().height(100).padBottom(10).row();
        inventoryLayoutTable.add(carriedLabel).left().padBottom(5).row();
        inventoryLayoutTable.add(carriedScrollPane).expandX().fillX().height(100).row();

        // No character selected label
        noCharacterSelectedLabel = createLabel("Select a character to view details", "default");

        // Add all sections to right panel
        rightPanel.add(infoSection).expandX().fillX().row();
        rightPanel.add(attributesSection).expandX().fillX().padTop(10).row();
        rightPanel.add(skillsSection).expandX().fillX().padTop(15).row();
        rightPanel.add(inventoryLayoutTable).expandX().fillX().padTop(10).row();
        rightPanel.add(noCharacterSelectedLabel).center().expand().row();

        // Add panels to content area
        contentTable.add(leftPanel).width(200).fillY().padRight(20);
        contentTable.add(rightPanel).expand().fill();

        mainLayoutTable.add(contentTable).expand().fill().pad(10).row();

        // --- FOOTER ---
        Table footerTable = new Table();
        TextButton backButton = new TextButton("Back to Map", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(new MapScreen(game));
            }
        });

        footerTable.add(backButton).expandX().right().pad(10);
        mainLayoutTable.add(footerTable).expandX().fillX().pad(10).row();

        // Initial state
        if (!listItems.isEmpty()) {
            characterListWidget.setSelectedIndex(0);
            updateCharacterDetails(listItems.first().getCharacter());
        } else {
            updateCharacterDetails(null);
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

        // Find parent Tables for section headers
        Actor attributesHeader = selectedCharAttributesLabel.getParent().getParent().getChild(0);
        Actor skillsScrollPane = skillsTable.getParent();
        Actor skillsHeader = skillsScrollPane.getParent().getChild(0);

        // Set visibility for headers
        if (attributesHeader instanceof Label) attributesHeader.setVisible(detailsVisible);
        if (skillsHeader instanceof Label) skillsHeader.setVisible(detailsVisible);
        if (skillsScrollPane instanceof ScrollPane) skillsScrollPane.setVisible(detailsVisible);

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

        // Format HP with colored text if available in the skin
        int hpPercent = (int)(100 * character.getHealthPoints() / (float)character.getMaxHealthPoints());
        selectedCharHpLabel.setText("HP: " + character.getHealthPoints() + "/" + character.getMaxHealthPoints() + " (" + hpPercent + "%)");

        // Format MP with colored text
        int mpPercent = (int)(100 * character.getManaPoints() / (float)character.getMaxManaPoints());
        selectedCharMpLabel.setText("MP: " + character.getManaPoints() + "/" + character.getMaxManaPoints() + " (" + mpPercent + "%)");

        // Format XP with progress
        int xpPercent = (int)(100 * character.getExperiencePoints() / (float)character.getExperienceToNextLevel());
        selectedCharXpLabel.setText("XP: " + character.getExperiencePoints() + "/" + character.getExperienceToNextLevel() + " (" + xpPercent + "%)");

        // Format attributes in a more readable layout
        StringBuilder attrBuilder = new StringBuilder();
        attrBuilder.append("Strength: ").append(character.getStrength()).append("\n");
        attrBuilder.append("Dexterity: ").append(character.getDexterity()).append("\n");
        attrBuilder.append("Intelligence: ").append(character.getIntelligence()).append("\n");
        attrBuilder.append("Constitution: ").append(character.getConstitution()).append("\n");
        attrBuilder.append("Wisdom: ").append(character.getWisdom()).append("\n");
        attrBuilder.append("Charisma: ").append(character.getCharisma());
        selectedCharAttributesLabel.setText(attrBuilder.toString());

        // Clear and rebuild skills table
        skillsTable.clearChildren();
        skillsTable.top().left();
        if (character.getKnownSkills().isEmpty()) {
            skillsTable.add(createLabel("No skills known", "default")).left();
        } else {
            // Create a more informative skill list with descriptions
            Table headerRow = new Table();
            headerRow.add(createLabel("Skill", "default-bold")).expandX().left().padRight(10);
            headerRow.add(createLabel("Type", "default-bold")).expandX().left().padRight(10);
            headerRow.add(createLabel("Cost", "default-bold")).expandX().left();
            skillsTable.add(headerRow).expandX().fillX().padBottom(5).row();

            for (Skill skill : character.getKnownSkills()) {
                Table skillRow = new Table();
                skillRow.add(createLabel(skill.getName(), "default")).expandX().left().padRight(10);
                skillRow.add(createLabel(skill.getType().name(), "default")).expandX().left().padRight(10);
                skillRow.add(createLabel(skill.getDamageRoll(), "default")).expandX().left();
                skillsTable.add(skillRow).expandX().fillX().padBottom(3).row();
            }
        }

        // Clear and rebuild equipped items
        equippedItemsTable.clearChildren();
        equippedItemsTable.top().left();
        if (character.getEquippedItems().isEmpty()) {
            equippedItemsTable.add(createLabel("Nothing equipped", "default")).left();
        } else {
            // Create a more informative equipment list
            Table headerRow = new Table();
            headerRow.add(createLabel("Item", "default-bold")).expandX().left().padRight(10);
            headerRow.add(createLabel("Type", "default-bold")).expandX().left();
            equippedItemsTable.add(headerRow).expandX().fillX().padBottom(5).row();

            for (Item item : character.getEquippedItems()) {
                Table itemRow = new Table();
                itemRow.add(createLabel(item.getName(), "default")).expandX().left().padRight(10);
                itemRow.add(createLabel(item.getType().getDisplayName(), "default")).expandX().left();
                equippedItemsTable.add(itemRow).expandX().fillX().padBottom(3).row();
            }
        }

        // Clear and rebuild carried items
        carriedItemsTable.clearChildren();
        carriedItemsTable.top().left();
        if (character.getInventory().isEmpty()) {
            carriedItemsTable.add(createLabel("Carrying nothing else", "default")).left();
        } else {
            // Create a more informative inventory list
            Table headerRow = new Table();
            headerRow.add(createLabel("Item", "default-bold")).expandX().left().padRight(10);
            headerRow.add(createLabel("Type", "default-bold")).expandX().left();
            carriedItemsTable.add(headerRow).expandX().fillX().padBottom(5).row();

            for (Item item : character.getInventory()) {
                Table itemRow = new Table();
                itemRow.add(createLabel(item.getName(), "default")).expandX().left().padRight(10);
                itemRow.add(createLabel(item.getType().getDisplayName(), "default")).expandX().left();
                carriedItemsTable.add(itemRow).expandX().fillX().padBottom(3).row();
            }
        }
    }
}
