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
import com.danbramos.ringprototype.party.Character;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.screens.PauseMenuScreen;

public class InventoryScreenView {

    private final RingPrototypeGame game;
    private final Skin skin;
    private final PauseMenuScreen parentScreen;

    // UI Elements
    private List<DisplayableCharacter> characterListWidget; // For selecting active character
    private GameCharacter selectedCharacter; // The character whose inventory is being managed

    private List<DisplayableItem> equippedItemsListWidget; // For selected character's equipped items
    private List<DisplayableItem> partyCarriedItemsListWidget; // All unequipped items in the party

    private TextButton equipButton;
    private TextButton unequipButton;
    private Label selectedCharacterNameLabel; // Displays name of selected character for inventory
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
        // Optional: could store original owner if needed for complex scenarios,
        // but for now, we'll find owner on-the-fly for equipping.

        public DisplayableItem(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }

        @Override
        public String toString() {
            return item != null ? item.getName() + " [" + item.getType().getDisplayName() + "]" : "No item";
        }
    }

    public InventoryScreenView(RingPrototypeGame game, PauseMenuScreen parentScreen) {
        this.game = game;
        this.skin = game.skin;
        this.parentScreen = parentScreen;
    }

    private Label createLabel(String text, String preferredStyleName, String fallbackStyleName) {
        if (skin.has(preferredStyleName, Label.LabelStyle.class)) {
            return new Label(text, skin, preferredStyleName);
        } else if (skin.has(fallbackStyleName, Label.LabelStyle.class)) {
            Gdx.app.log("InventoryScreenView", "LabelStyle '" + preferredStyleName + "' not found. Falling back to '" + fallbackStyleName + "'.");
            return new Label(text, skin, fallbackStyleName);
        } else {
            Gdx.app.error("InventoryScreenView", "LabelStyle '" + preferredStyleName + "' and fallback '" + fallbackStyleName + "' not found. Creating programmatic fallback.");
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

    public Table buildUI() { // Removed Character parameter, will be handled by internal selection
        inventoryViewRootTable = new Table(skin);
        inventoryViewRootTable.setFillParent(true);
        inventoryViewRootTable.pad(15);

        // --- Character Selection List (Left Panel) ---
        Table leftPanel = new Table(skin);
        leftPanel.top();
        characterListWidget = new List<>(skin);
        Array<DisplayableCharacter> displayableCharacters = new Array<>();
        if (game.partyManager.getPartySize() > 0) {
            for (GameCharacter member : game.partyManager.getMembers()) {
                displayableCharacters.add(new DisplayableCharacter(member));
            }
            characterListWidget.setItems(displayableCharacters);
            // Select the first character by default
            if (!displayableCharacters.isEmpty()) {
                characterListWidget.setSelectedIndex(0);
                selectedCharacter = displayableCharacters.first().getCharacter();
            }
        } else {
            characterListWidget.setItems(new DisplayableCharacter(null)); // Show "No Character"
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
            }
        });
        ScrollPane characterScrollPane = new ScrollPane(characterListWidget, skin);
        characterScrollPane.setFadeScrollBars(false);
        leftPanel.add(createLabel("Characters", "default-bold")).padBottom(5).row();
        leftPanel.add(characterScrollPane).growY().width(180);

        // --- Inventory Management Area (Right Panel) ---
        Table rightPanel = new Table(skin);
        rightPanel.top();

        selectedCharacterNameLabel = createLabel("Inventory", "default-title"); // Will be updated
        rightPanel.add(selectedCharacterNameLabel).colspan(3).center().padBottom(10).row();

        // Selected Character's Equipped Items
        equippedItemsListWidget = new List<>(skin);
        ScrollPane equippedScrollPane = new ScrollPane(equippedItemsListWidget, skin);
        equippedScrollPane.setFadeScrollBars(false);

        // Party's Carried (Unequipped) Items
        partyCarriedItemsListWidget = new List<>(skin);
        ScrollPane partyCarriedScrollPane = new ScrollPane(partyCarriedItemsListWidget, skin);
        partyCarriedScrollPane.setFadeScrollBars(false);

        // Action Buttons
        equipButton = new TextButton("Equip", skin);
        unequipButton = new TextButton("Unequip", skin);
        Table actionButtonTable = new Table();
        actionButtonTable.add(equipButton).pad(5).width(100).row();
        actionButtonTable.add(unequipButton).pad(5).width(100);


        // Layout for right panel (Equipped, Buttons, Party Carried)
        rightPanel.add(createLabel("Equipped by Selected", "default-bold")).colspan(1).center().padBottom(5);
        rightPanel.add(); // Spacer for buttons
        rightPanel.add(createLabel("Party Available Items", "default-bold")).colspan(1).center().padBottom(5).row();

        rightPanel.add(equippedScrollPane).growY().minWidth(220).prefHeight(150);
        rightPanel.add(actionButtonTable).center().pad(0,10,0,10);
        rightPanel.add(partyCarriedScrollPane).growY().minWidth(220).prefHeight(150).row();


        // Listeners for item lists and buttons
        equippedItemsListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (equippedItemsListWidget.getSelected() != null) {
                    partyCarriedItemsListWidget.getSelection().clear();
                    equipButton.setDisabled(true);
                    unequipButton.setDisabled(selectedCharacter == null);
                } else {
                    unequipButton.setDisabled(true);
                }
            }
        });

        partyCarriedItemsListWidget.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (partyCarriedItemsListWidget.getSelected() != null) {
                    equippedItemsListWidget.getSelection().clear();
                    equipButton.setDisabled(selectedCharacter == null);
                    unequipButton.setDisabled(true);
                } else {
                    equipButton.setDisabled(true);
                }
            }
        });

        equipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableItem itemToEquipWrapper = partyCarriedItemsListWidget.getSelected();
                if (itemToEquipWrapper != null && selectedCharacter != null) {
                    Item item = itemToEquipWrapper.getItem();
                    // Find original owner and remove from their inventory
                    GameCharacter originalOwner = findItemOwner(item);
                    if (originalOwner != null) {
                        originalOwner.removeItemFromInventory(item); // Assumes item is in inventory, not equipped
                        selectedCharacter.addItemToInventory(item); // Add to target's inventory first
                        if (selectedCharacter.equipItem(item)) { // Then equip from target's inventory
                            Gdx.app.log("InventoryScreenView", selectedCharacter.getName() + " equipped " + item.getName());
                        } else {
                            Gdx.app.log("InventoryScreenView", "Failed to equip " + item.getName() + " to " + selectedCharacter.getName());
                            // Re-add to original owner if equip failed (optional, or handle error)
                            originalOwner.addItemToInventory(item);
                        }
                        refreshInventoryDisplay();
                    }
                }
            }
        });

        unequipButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                DisplayableItem itemToUnequipWrapper = equippedItemsListWidget.getSelected();
                if (itemToUnequipWrapper != null && selectedCharacter != null) {
                    if (selectedCharacter.unequipItem(itemToUnequipWrapper.getItem())) {
                        Gdx.app.log("InventoryScreenView", selectedCharacter.getName() + " unequipped " + itemToUnequipWrapper.getItem().getName());
                        refreshInventoryDisplay();
                    }
                }
            }
        });


        // Main layout: Character List | Inventory Details
        inventoryViewRootTable.add(leftPanel).growY().padRight(10);
        inventoryViewRootTable.add(rightPanel).grow();
        inventoryViewRootTable.row(); // New row for the back button

        TextButton backButton = new TextButton("Back to Pause Menu", skin);
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                parentScreen.showMainPauseOptions();
            }
        });
        // Add back button at the bottom, spanning across
        inventoryViewRootTable.add(backButton).colspan(2).padTop(20).center();


        refreshInventoryDisplay(); // Initial population
        return inventoryViewRootTable;
    }

    private GameCharacter findItemOwner(Item itemToFind) {
        if (itemToFind == null) return null;
        for (GameCharacter member : game.partyManager.getMembers()) {
            if (member.getInventory().contains(itemToFind)) {
                return member;
            }
        }
        return null; // Should not happen if item is from partyCarriedItemsListWidget
    }

    public void refreshInventoryDisplay() {
        if (selectedCharacter != null) {
            selectedCharacterNameLabel.setText(selectedCharacter.getName() + "'s Gear");
        } else {
            selectedCharacterNameLabel.setText("Select Character");
        }

        // Populate selected character's equipped items
        Array<DisplayableItem> equippedItems = new Array<>();
        if (selectedCharacter != null) {
            for (Item item : selectedCharacter.getEquippedItems()) {
                equippedItems.add(new DisplayableItem(item));
            }
        }
        equippedItemsListWidget.setItems(equippedItems);

        // Populate all party's carried (unequipped) items
        Array<DisplayableItem> partyCarriedItems = new Array<>();
        for (GameCharacter member : game.partyManager.getMembers()) {
            for (Item item : member.getInventory()) { // Iterate actual inventory (carried items)
                partyCarriedItems.add(new DisplayableItem(item));
            }
        }
        partyCarriedItemsListWidget.setItems(partyCarriedItems);

        // Clear selections and manage button states
        equippedItemsListWidget.getSelection().clear();
        partyCarriedItemsListWidget.getSelection().clear();
        equipButton.setDisabled(true);
        unequipButton.setDisabled(true);
    }
}
