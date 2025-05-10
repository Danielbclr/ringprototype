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
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.battle.skills.SkillType;
import com.danbramos.ringprototype.screens.BattleScreen; // To call back

public class BattleInputHandler extends InputAdapter {

    public enum ActionState {
        IDLE, MOVING,
        TARGETING_SKILL_TILE, TARGETING_SKILL_ACTOR, TARGETING_SKILL_AOE_CONFIRM,
        TARGETING_NIMBLE_MOVEMENT // New state for Nimble Movement
    }
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

    private Array<Vector2> nimbleMovementTiles;
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
        this.nimbleMovementTiles = new Array<>();
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

    public Array<Vector2> getNimbleMovementTiles() {
        return nimbleMovementTiles;
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
        nimbleMovementTiles.clear();
    }

    public void calculateMovementReachableTiles(BattleCharacter battleCharacter) {
        movementReachableTiles.clear();
        if (battleCharacter == null) return;

        // Allow movement if the character has remaining movement points
        if (battleCharacter.getRemainingMovement() <= 0) return;

        Vector2 startPos = battleCharacter.getBattleMapPosition();
        int range = battleCharacter.getRemainingMovement(); // Use remaining movement

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
        if (currentBC == null) return;

        // If a major action skill was already used, generally no more skills.
        // However, Nimble Movement is a "free" action, so it might be an exception.
        // For now, let's assume Nimble Movement can be selected unless its own effect is active.
        if (currentBC.hasPerformedMajorAction() && !skill.getId().equals("skill_nimble_movement")) {
            Gdx.app.log("BattleInputHandler", "Major action already performed, cannot select skill: " + skill.getName());
            return;
        }

        this.selectedSkill = skill;
        this.aoeCenterTile = null;
        clearAllHighlights(); // Clear previous highlights

        Gdx.app.log("BattleInputHandler", "Selected skill: " + skill.getName() + " by " + currentBC.getName());

        if (skill.getId().equals("skill_nimble_movement")) {
            if (currentBC.hasStatusEffect("NIMBLE_MOVEMENT_ACTIVE")) {
                Gdx.app.log("BattleInputHandler", "Nimble Movement free action already used this turn.");
                this.selectedSkill = null; // Deselect, as it can't be used again
                setActionState(determineFallbackState(currentBC));
                return;
            }
            currentActionState = ActionState.TARGETING_NIMBLE_MOVEMENT;
            // Assuming the skill's first status effect holds the range value (5)
            int nimbleRange = skill.getStatusEffects().isEmpty() ? 5 : skill.getStatusEffects().get(0).getValue();
            calculateNimbleMovementTiles(currentBC, nimbleRange);
        } else if (skill.getType() == SkillType.MELEE_ATTACK || skill.getType() == SkillType.RANGED_SINGLE_TARGET) {
            currentActionState = ActionState.TARGETING_SKILL_ACTOR;
            calculateSkillRangeTiles(currentBC, skill);
        } else if (skill.getType() == SkillType.RANGED_AOE_CIRCLE) {
            currentActionState = ActionState.TARGETING_SKILL_TILE;
            calculateSkillRangeTiles(currentBC, skill);
        } else if (skill.getType() == SkillType.SUPPORT && skill.getRange() == 0) { // Self-target support
            battleScreen.executeSupportSkill(currentBC, skill);
            this.selectedSkill = null;
            setActionState(determineFallbackState(currentBC));
        } else if (skill.getType() == SkillType.SUPPORT) { // Targeted support
            currentActionState = ActionState.TARGETING_SKILL_ACTOR;
            calculateSkillRangeTiles(currentBC, skill);
        } else {
            setActionState(determineFallbackState(currentBC));
        }
    }

    private ActionState determineFallbackState(BattleCharacter bc) {
        if (bc.getRemainingMovement() > 0 && !bc.hasPerformedMajorAction()) {
            calculateMovementReachableTiles(bc);
            return ActionState.MOVING;
        }
        return ActionState.IDLE;
    }

    public void clearSkillHighlights() {
        skillRangeTiles.clear();
        skillAoeTiles.clear();
        selectedSkill = null;
        aoeCenterTile = null;
    }

    public void calculateNimbleMovementTiles(BattleCharacter caster, int range) {
        nimbleMovementTiles.clear();
        if (caster == null) return;
        Vector2 startPos = caster.getBattleMapPosition();

        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                int dist = (int) (Math.abs(x - startPos.x) + Math.abs(y - startPos.y));
                if (dist > 0 && dist <= range) { // Distance must be > 0 (actual move)
                    if (!battleScreen.isTileOccupied(x, y)) { // Cannot move to occupied tiles
                        nimbleMovementTiles.add(new Vector2(x, y));
                    }
                }
            }
        }
        Gdx.app.log("BattleInputHandler", "Calculated " + nimbleMovementTiles.size + " nimble movement tiles for " + caster.getName());
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        IBattleActor currentTurnActor = battleScreen.getCurrentTurnActor();
        if (currentTurnActor == null || !(currentTurnActor instanceof BattleCharacter)) {
            return false;
        }
        BattleCharacter currentBC = (BattleCharacter) currentTurnActor;

        Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int) (worldCoordinates.x / tileWidth);
        int tileY = (int) (worldCoordinates.y / tileHeight);
        Vector2 clickedTileVec = new Vector2(tileX, tileY);

        if (tileX < 0 || tileX >= mapWidthInTiles || tileY < 0 || tileY >= mapHeightInTiles) {
            Gdx.app.debug("BattleInputHandler", "Clicked outside map bounds.");
            return false;
        }

        switch (currentActionState) {
            case IDLE:
            case MOVING:
                if (currentBC.hasPerformedMajorAction()) {
                    Gdx.app.log("BattleInputHandler", "Major action already performed, cannot move normally.");
                    return false;
                }
                if (movementReachableTiles.contains(clickedTileVec, false)) {
                    int cost = currentBC.calculateMovementCost(tileX, tileY);
                    if (currentBC.getRemainingMovement() >= cost) {
                        battleScreen.handleCharacterMove(currentBC, tileX, tileY, cost);
                        // State transition is handled within handleCharacterMove's aftermath
                        return true;
                    } else {
                        Gdx.app.log("BattleInputHandler", "Not enough movement points for normal move.");
                    }
                }
                break;

            case TARGETING_NIMBLE_MOVEMENT: // Handle new state
                if (selectedSkill != null && selectedSkill.getId().equals("skill_nimble_movement") &&
                    nimbleMovementTiles.contains(clickedTileVec, false)) {
                    battleScreen.handleCharacterFreeMove(currentBC, tileX, tileY, selectedSkill);
                    return true;
                }
                break;

            case TARGETING_SKILL_ACTOR:
                if (selectedSkill != null && skillRangeTiles.contains(clickedTileVec, false)) {
                    IBattleActor targetActor = battleScreen.getActorAtTile(tileX, tileY);
                    boolean isValidTarget = false;
                    if (targetActor != null) {
                        if (selectedSkill.getType() == SkillType.MELEE_ATTACK || selectedSkill.getType() == SkillType.RANGED_SINGLE_TARGET) {
                            isValidTarget = targetActor instanceof Enemy;
                        } else if (selectedSkill.getType() == SkillType.HEAL) {
                            isValidTarget = targetActor instanceof BattleCharacter;
                        } else if (selectedSkill.getType() == SkillType.SUPPORT) {
                            isValidTarget = true; // Or more specific checks
                        }
                    }
                    if (isValidTarget) {
                        battleScreen.executeSingleTargetSkill(currentBC, selectedSkill, targetActor);
                        return true;
                    } else {
                        Gdx.app.log("BattleInputHandler", "Invalid target or no valid actor at " + tileX + "," + tileY + " for skill " + selectedSkill.getName());
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
                    battleScreen.executeAoeSkill(currentBC, selectedSkill, aoeCenterTile);
                    return true;
                }
                break;
        }
        return false;
    }

    public void setActionState(ActionState state) {
        this.currentActionState = state;
        if (state == ActionState.IDLE || state == ActionState.MOVING) {
            this.selectedSkill = null; // Clear selected skill when returning to general movement/idle
            // Highlights for movement should be recalculated if state is MOVING
        }
    }
}
