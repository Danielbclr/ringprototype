package com.danbramos.ringprototype.input; // Or com.danbramos.ringprototype.input

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.Enemy;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.battle.SkillType;
import com.danbramos.ringprototype.screens.BattleScreen; // To call back

public class BattleInputHandler extends InputAdapter {

    public enum ActionState {IDLE, MOVING, TARGETING_SKILL_TILE, TARGETING_SKILL_ACTOR, TARGETING_SKILL_AOE_CONFIRM}

    private final BattleScreen battleScreen;
    private final OrthographicCamera camera;
    private final int tileWidth;
    private final int tileHeight;
    private final int mapWidthInTiles;
    private final int mapHeightInTiles;


    private ActionState currentActionState = ActionState.IDLE;
    private Skill selectedSkill;
    private Vector2 aoeCenterTile;

    // Highlighting data - managed here, rendered by BattleScreen
    private Array<Vector2> movementReachableTiles;
    private Array<Vector2> skillRangeTiles;
    private Array<Vector2> skillAoeTiles;

    public BattleInputHandler(BattleScreen battleScreen, OrthographicCamera camera,
                              int tileWidth, int tileHeight, int mapWidthInTiles, int mapHeightInTiles) {
        this.battleScreen = battleScreen;
        this.camera = camera;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.mapWidthInTiles = mapWidthInTiles;
        this.mapHeightInTiles = mapHeightInTiles;

        this.movementReachableTiles = new Array<>();
        this.skillRangeTiles = new Array<>();
        this.skillAoeTiles = new Array<>();
    }

    public ActionState getCurrentActionState() {
        return currentActionState;
    }

    public Skill getSelectedSkill() {
        return selectedSkill;
    }

    public Vector2 getAoeCenterTile() {
        return aoeCenterTile;
    }

    public Array<Vector2> getMovementReachableTiles() {
        return movementReachableTiles;
    }

    public Array<Vector2> getSkillRangeTiles() {
        return skillRangeTiles;
    }

    public Array<Vector2> getSkillAoeTiles() {
        return skillAoeTiles;
    }

    public void resetState() {
        currentActionState = ActionState.IDLE;
        selectedSkill = null;
        aoeCenterTile = null;
        clearAllHighlights();
    }

    public void clearAllHighlights() {
        movementReachableTiles.clear();
        skillRangeTiles.clear();
        skillAoeTiles.clear();
    }

    public void calculateMovementReachableTiles(BattleCharacter battleCharacter) {
        movementReachableTiles.clear();
        if (battleCharacter == null || battleCharacter.hasPerformedMajorAction()) return;

        Vector2 startPos = battleCharacter.getBattleMapPosition();
        int range = battleCharacter.getMovementRange();

        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                int dist = (int) (Math.abs(x - startPos.x) + Math.abs(y - startPos.y));
                if (dist > 0 && dist <= range) {
                    if (!battleScreen.isTileOccupied(x, y)) {
                        movementReachableTiles.add(new Vector2(x, y));
                    }
                }
            }
        }
    }

    public void calculateSkillRangeTiles(BattleCharacter caster, Skill skill) {
        skillRangeTiles.clear();
        Vector2 casterPos = caster.getBattleMapPosition();
        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                int dist = (int) (Math.abs(x - casterPos.x) + Math.abs(y - casterPos.y));
                if (skill.getType() == SkillType.MELEE_ATTACK) {
                    if (dist == skill.getRange()) { // Adjacent for melee
                        skillRangeTiles.add(new Vector2(x, y));
                    }
                } else { // Ranged
                    if (dist <= skill.getRange()) {
                        skillRangeTiles.add(new Vector2(x, y));
                    }
                }
            }
        }
    }

    public void calculateSkillAoeTiles(Vector2 centerTile, int radius) {
        skillAoeTiles.clear();
        if (centerTile == null) return;
        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                double distSq = Math.pow(x - centerTile.x, 2) + Math.pow(y - centerTile.y, 2);
                if (distSq <= Math.pow(radius, 2)) {
                    skillAoeTiles.add(new Vector2(x, y));
                }
            }
        }
    }

    public void selectSkill(Skill skill, BattleCharacter currentBC) {
        if (currentBC == null || currentBC.hasPerformedMajorAction()) {
            return;
        }
        this.selectedSkill = skill;
        this.aoeCenterTile = null;
        clearAllHighlights(); // Clear movement highlights
        Gdx.app.log("BattleInputHandler", "Selected skill: " + skill.getName());

        calculateSkillRangeTiles(currentBC, skill);

        if (skill.getType() == SkillType.MELEE_ATTACK || skill.getType() == SkillType.RANGED_SINGLE_TARGET) {
            currentActionState = ActionState.TARGETING_SKILL_ACTOR;
        } else if (skill.getType() == SkillType.RANGED_AOE_CIRCLE) {
            currentActionState = ActionState.TARGETING_SKILL_TILE;
        }
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        IBattleActor currentTurnActor = battleScreen.getCurrentTurnActor();
        if (currentTurnActor == null || !(currentTurnActor instanceof BattleCharacter)) {
            return false;
        }
        BattleCharacter currentBC = (BattleCharacter) currentTurnActor;

        if (currentBC.hasPerformedMajorAction() && currentActionState != ActionState.TARGETING_SKILL_AOE_CONFIRM) {
            Gdx.app.log("BattleInputHandler", "Major action already performed this turn.");
            return false;
        }

        Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) (worldCoordinates.x / tileWidth);
        int tileY = (int) (worldCoordinates.y / tileHeight);
        Vector2 clickedTileVec = new Vector2(tileX, tileY);

        // Boundary check for clicked tile
        if (tileX < 0 || tileX >= mapWidthInTiles || tileY < 0 || tileY >= mapHeightInTiles) {
            Gdx.app.debug("BattleInputHandler", "Clicked outside map bounds.");
            return false;
        }

        switch (currentActionState) {
            case IDLE:
            case MOVING: // In IDLE state, a click attempts movement
                if (movementReachableTiles.contains(clickedTileVec, false)) {
                    battleScreen.handleCharacterMove(currentBC, tileX, tileY);
                    // State change to IDLE and clearing highlights will be done by BattleScreen or startTurnFor
                    return true;
                }
                break;

            case TARGETING_SKILL_ACTOR:
                if (selectedSkill != null && skillRangeTiles.contains(clickedTileVec, false)) {
                    IBattleActor targetActor = battleScreen.getActorAtTile(tileX, tileY);
                    if (targetActor != null && targetActor instanceof Enemy) {
                        battleScreen.executeSingleTargetSkill(currentBC, selectedSkill, targetActor);
                        return true;
                    } else {
                        Gdx.app.log("BattleInputHandler", "Invalid target or no enemy at " + tileX + "," + tileY);
                    }
                }
                break;

            case TARGETING_SKILL_TILE:
                if (selectedSkill != null && skillRangeTiles.contains(clickedTileVec, false)) {
                    aoeCenterTile = clickedTileVec;
                    calculateSkillAoeTiles(aoeCenterTile, selectedSkill.getAoeRadius());
                    currentActionState = ActionState.TARGETING_SKILL_AOE_CONFIRM;
                    Gdx.app.log("BattleInputHandler", "AoE center selected at " + tileX + "," + tileY + ". Confirm or cancel.");
                    return true;
                }
                break;

            case TARGETING_SKILL_AOE_CONFIRM:
                if (selectedSkill != null && aoeCenterTile != null) {
                    // Check if the click is within the AoE area to confirm, or implement a separate confirm button
                    // For now, any click while in this state confirms.
                    battleScreen.executeAoeSkill(currentBC, selectedSkill, aoeCenterTile);
                    return true;
                }
                break;
        }
        return false;
    }
}
