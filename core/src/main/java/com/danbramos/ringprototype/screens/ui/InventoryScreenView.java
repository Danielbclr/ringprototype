package com.danbramos.ringprototype.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.items.Item;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.screens.PauseMenuScreen;

import java.util.Map;

public class InventoryScreenView {

    private final RingPrototypeGame game;
    private final Skin skin;
    private final PauseMenuScreen parentScreen;

    // UI Elements
    private List<DisplayableCharacter> characterListWidget;
    private GameCharacter selectedCharacter;

    private List<DisplayableItem> equippedItemsListWidget;
    private List<DisplayableItem> partyCarriedItemsListWidget;

    private TextButton equipButton;
    private TextButton unequipButton;
    private Label selectedCharacterNameLabel;
    private Label itemDetailsLabel;
    private Table inventoryViewRootTable;

    // Wrapper for characters in the character selection list
    public static class DisplayableCharacter {
        public final GameCharacter character;

        public DisplayableCharacter(GameCharacter character) {
            this.character = character;
        }

        public GameCharacter getCharacter() {
            return character;
        }

        @Override
        public String toString() {
            return character != null ? character.getName() : "No character";
        }
    }

    // Wrapper for items in UI lists
    public static class DisplayableItem {
        public final Item item;

        public DisplayableItem(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        @Override
        public String toString() {
            return item != null ? item.getName() : "No item";
        }
    }

    public InventoryScreenView(RingPrototypeGame game, PauseMenuScreen parentScreen) {
        this.game = game;
        this.skin = game.skin;
        this.parentScreen = parentScreen;
    }

    private Label createLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        } else {
            Gdx.app.log("InventoryScreenView", "LabelStyle '" + styleName + "' not found. Using default.");
            return new Label(text, skin);
        }
    }

    public Table buildUI() {
        inventoryViewRootTable = new Table(skin);
        inventoryViewRootTable.setFillParent(true);
        
        // Apply a background if available
        if (skin.has("window-background", Skin.TintedDrawable.class)) {
            inventoryViewRootTable.setBackground(skin.getTiledDrawable("window-background"));
        }

        // --- HEADER ---
        Table headerTable = new Table();
        Label titleLabel = createLabel("Inventory Management", "title");
        headerTable.add(titleLabel).expandX().center().pad(15);
        inventoryViewRootTable.add(headerTable).expandX().fillX().pad(10).row();

        // --- CONTENT AREA ---
        Table contentTable = new Table();

        // --- Left Panel - Character Selection ---
        Table leftPanel = new Table();
        leftPanel.top();
        
        Label characterLabel = createLabel("Characters", "header");
        leftPanel.add(characterLabel).left().padBottom(10).row();
        
        characterListWidget = new List<>(skin);
        
        // Populate character list
        Array<DisplayableCharacter> displayableCharacters = new Array<>();
        if (game.partyManager.getPartySize() > 0) {
            for (GameCharacter member : game.partyManager.getMembers()) {
                displayableCharacters.add(new DisplayableCharacter(member));
            }
            characterListWidget.setItems(displayableCharacters);
            if (!displayableCharacters.isEmpty()) {
                characterListWidget.setSelectedIndex(0);
                selectedCharacter = displayableCharacters.first().getCharacter();
            }
        } else {
            characterListWidget.setItems(new DisplayableCharacter(null));
        }

        characterListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableCharacter dc = characterListWidget.getSelected();
                if (dc != null && dc.getCharacter() != null) {
                    selectedCharacter = dc.getCharacter();
                } else {
                    selectedCharacter = null;
                }
                refreshInventoryDisplay();
                updateItemDetailsLabel(null);
            }
        });
        
        ScrollPane characterScrollPane = new ScrollPane(characterListWidget, skin);
        characterScrollPane.setFadeScrollBars(false);
        characterScrollPane.setScrollingDisabled(true, false);
        
        leftPanel.add(characterScrollPane).expand().fill().minWidth(180);

        // --- Right Panel - Inventory Management ---
        Table rightPanel = new Table();
        rightPanel.top();

        // Character info header
        selectedCharacterNameLabel = createLabel("Select Character", "header");
        rightPanel.add(selectedCharacterNameLabel).expandX().center().padBottom(15).row();

        // Equipment and Inventory area with columns
        Table inventoryArea = new Table();
        
        // --- Equipped Items Column ---
        Table equippedColumn = new Table();
        equippedColumn.top();
        
        Label equippedLabel = createLabel("Equipped Items", "subheader");
        equippedColumn.add(equippedLabel).left().padBottom(10).row();
        
        equippedItemsListWidget = new List<>(skin);
        ScrollPane equippedScrollPane = new ScrollPane(equippedItemsListWidget, skin);
        equippedScrollPane.setFadeScrollBars(false);
        equippedScrollPane.setScrollingDisabled(true, false);
        
        equippedColumn.add(equippedScrollPane).expand().fill().minHeight(200).row();

        // --- Center Action Buttons ---
        Table actionButtonsColumn = new Table();
        actionButtonsColumn.center();
        
        // Add arrow indicators for direction
        Label arrowRight = createLabel("→", "header");
        Label arrowLeft = createLabel("←", "header");
        
        equipButton = new TextButton("Equip", skin);
        equipButton.pad(10, 15, 10, 15);
        
        unequipButton = new TextButton("Unequip", skin);
        unequipButton.pad(10, 15, 10, 15);
        
        actionButtonsColumn.add(arrowLeft).padBottom(15).row();
        actionButtonsColumn.add(unequipButton).padBottom(20).row();
        actionButtonsColumn.add(equipButton).padBottom(20).row();
        actionButtonsColumn.add(arrowRight).row();

        // --- Available Items Column ---
        Table availableColumn = new Table();
        availableColumn.top();
        
        Label availableLabel = createLabel("Available Items", "subheader");
        availableColumn.add(availableLabel).left().padBottom(10).row();
        
        partyCarriedItemsListWidget = new List<>(skin);
        ScrollPane availableScrollPane = new ScrollPane(partyCarriedItemsListWidget, skin);
        availableScrollPane.setFadeScrollBars(false);
        availableScrollPane.setScrollingDisabled(true, false);
        
        availableColumn.add(availableScrollPane).expand().fill().minHeight(200).row();

        // Add columns to inventory area
        inventoryArea.add(equippedColumn).expand().fill().pad(5);
        inventoryArea.add(actionButtonsColumn).width(120).pad(5, 20, 5, 20);
        inventoryArea.add(availableColumn).expand().fill().pad(5);
        
        rightPanel.add(inventoryArea).expand().fill().row();

        // --- Item Details Section ---
        Table itemDetailsTable = new Table();
        itemDetailsTable.top().pad(10);
        
        Label detailsHeaderLabel = createLabel("Item Details", "subheader");
        itemDetailsTable.add(detailsHeaderLabel).left().padBottom(5).row();
        
        itemDetailsLabel = createLabel("Select an item to see details", "default");
        itemDetailsLabel.setWrap(true);
        
        ScrollPane itemDetailsScrollPane = new ScrollPane(itemDetailsLabel, skin);
        itemDetailsScrollPane.setFadeScrollBars(false);
        
        itemDetailsTable.add(itemDetailsScrollPane).expandX().fillX().height(80).row();

        rightPanel.add(itemDetailsTable).expandX().fillX().padTop(15).row();

        // Add panels to content area
        contentTable.add(leftPanel).width(180).fillY().padRight(20);
        contentTable.add(rightPanel).expand().fill();
        
        inventoryViewRootTable.add(contentTable).expand().fill().pad(10).row();

        // --- FOOTER ---
        Table footerTable = new Table();
        TextButton backButton = new TextButton("Back to Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parentScreen.showMainPauseOptions();
            }
        });
        
        footerTable.add(backButton).expandX().right().pad(10);
        inventoryViewRootTable.add(footerTable).expandX().fillX().pad(10).row();

        // Register list selection listeners
        equippedItemsListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableItem selectedItem = equippedItemsListWidget.getSelected();
                if (selectedItem != null) {
                    partyCarriedItemsListWidget.getSelection().clear();
                    equipButton.setDisabled(true);
                    unequipButton.setDisabled(selectedCharacter == null);
                    updateItemDetailsLabel(selectedItem.getItem());
                } else {
                    unequipButton.setDisabled(true);
                }
            }
        });

        partyCarriedItemsListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableItem selectedItem = partyCarriedItemsListWidget.getSelected();
                if (selectedItem != null) {
                    equippedItemsListWidget.getSelection().clear();
                    equipButton.setDisabled(selectedCharacter == null);
                    unequipButton.setDisabled(true);
                    updateItemDetailsLabel(selectedItem.getItem());
                } else {
                    equipButton.setDisabled(true);
                }
            }
        });

        // Register action button listeners
        equipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableItem itemToEquipWrapper = partyCarriedItemsListWidget.getSelected();
                if (itemToEquipWrapper != null && selectedCharacter != null) {
                    Item item = itemToEquipWrapper.getItem();
                    GameCharacter originalOwner = findItemOwner(item);
                    if (originalOwner != null) {
                        originalOwner.removeItemFromInventory(item);
                        selectedCharacter.addItemToInventory(item);
                        if (selectedCharacter.equipItem(item)) {
                            Gdx.app.log("InventoryScreenView", selectedCharacter.getName() + " equipped " + item.getName());
                        } else {
                            Gdx.app.log("InventoryScreenView", "Failed to equip " + item.getName());
                            originalOwner.addItemToInventory(item);
                        }
                        refreshInventoryDisplay();
                        updateItemDetailsLabel(item);
                    }
                }
            }
        });

        unequipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableItem itemToUnequipWrapper = equippedItemsListWidget.getSelected();
                if (itemToUnequipWrapper != null && selectedCharacter != null) {
                    Item item = itemToUnequipWrapper.getItem();
                    if (selectedCharacter.unequipItem(item)) {
                        Gdx.app.log("InventoryScreenView", selectedCharacter.getName() + " unequipped " + item.getName());
                        refreshInventoryDisplay();
                        updateItemDetailsLabel(item);
                    }
                }
            }
        });

        refreshInventoryDisplay();
        return inventoryViewRootTable;
    }

    private void updateItemDetailsLabel(Item item) {
        if (item == null) {
            itemDetailsLabel.setText("Select an item to see details");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(item.getName()).append(" (").append(item.getType().getDisplayName()).append(")\n");
        
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            sb.append(item.getDescription()).append("\n");
        }
        
        // Add stats based on stat bonuses
        Map<String, Integer> bonuses = item.getStatBonuses();
        if (!bonuses.isEmpty()) {
            sb.append("\nBonuses:\n");
            
            int str = item.getStatBonus("strength");
            int dex = item.getStatBonus("dexterity");
            int con = item.getStatBonus("constitution");
            int intel = item.getStatBonus("intelligence");
            int wis = item.getStatBonus("wisdom");
            int damage = item.getStatBonus("damage");
            int defense = item.getStatBonus("defense");
            int hp = item.getStatBonus("hp");
            int mp = item.getStatBonus("mp");
            
            // Display only non-zero stats
            if (damage > 0) sb.append("Damage: +").append(damage).append("\n");
            if (defense > 0) sb.append("Defense: +").append(defense).append("\n");
            if (str > 0) sb.append("STR: +").append(str).append("\n");
            if (dex > 0) sb.append("DEX: +").append(dex).append("\n");
            if (con > 0) sb.append("CON: +").append(con).append("\n");
            if (intel > 0) sb.append("INT: +").append(intel).append("\n");
            if (wis > 0) sb.append("WIS: +").append(wis).append("\n");
            if (hp > 0) sb.append("Restores: ").append(hp).append(" HP\n");
            if (mp > 0) sb.append("Restores: ").append(mp).append(" MP\n");
        }
        
        itemDetailsLabel.setText(sb.toString());
    }

    private GameCharacter findItemOwner(Item itemToFind) {
        if (itemToFind == null) return null;
        for (GameCharacter member : game.partyManager.getMembers()) {
            if (member.getInventory().contains(itemToFind)) {
                return member;
            }
        }
        return null;
    }

    public void refreshInventoryDisplay() {
        // Update character name label
        if (selectedCharacter != null) {
            selectedCharacterNameLabel.setText(selectedCharacter.getName() + "'s Equipment");
        } else {
            selectedCharacterNameLabel.setText("Select a Character");
        }

        // Update equipped items list
        Array<DisplayableItem> equippedItems = new Array<>();
        if (selectedCharacter != null) {
            for (Item item : selectedCharacter.getEquippedItems()) {
                equippedItems.add(new DisplayableItem(item));
            }
        }
        equippedItemsListWidget.setItems(equippedItems);

        // Update available items list
        Array<DisplayableItem> availableItems = new Array<>();
        for (GameCharacter member : game.partyManager.getMembers()) {
            for (Item item : member.getInventory()) {
                availableItems.add(new DisplayableItem(item));
            }
        }
        partyCarriedItemsListWidget.setItems(availableItems);

        // Reset selection state
        equippedItemsListWidget.getSelection().clear();
        partyCarriedItemsListWidget.getSelection().clear();
        equipButton.setDisabled(true);
        unequipButton.setDisabled(true);
    }
}
