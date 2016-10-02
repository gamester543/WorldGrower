/*******************************************************************************
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.worldgrower.gui;

import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.worldgrower.Args;
import org.worldgrower.Constants;
import org.worldgrower.DungeonMaster;
import org.worldgrower.ManagedOperation;
import org.worldgrower.World;
import org.worldgrower.WorldObject;
import org.worldgrower.actions.Actions;
import org.worldgrower.actions.BuildAction;
import org.worldgrower.actions.magic.IllusionSpell;
import org.worldgrower.actions.magic.MagicSpell;
import org.worldgrower.actions.magic.ResearchSpellAction;
import org.worldgrower.actions.magic.ScribeMagicSpellAction;
import org.worldgrower.attribute.SkillProperty;
import org.worldgrower.conversation.Conversations;
import org.worldgrower.gui.chooseworldobject.ChooseWorldObjectAction;
import org.worldgrower.gui.chooseworldobject.GuiDisguiseAction;
import org.worldgrower.gui.chooseworldobject.GuiVoteAction;
import org.worldgrower.gui.conversation.GuiAskQuestionAction;
import org.worldgrower.gui.cursor.Cursors;
import org.worldgrower.gui.debug.GuiShowBuildingsOverviewAction;
import org.worldgrower.gui.debug.GuiShowCommonersOverviewAction;
import org.worldgrower.gui.debug.GuiShowEconomicOverviewAction;
import org.worldgrower.gui.debug.GuiShowElectionResultsAction;
import org.worldgrower.gui.debug.GuiShowPerformedActionsAction;
import org.worldgrower.gui.debug.GuiShowPersonalitiesOverviewAction;
import org.worldgrower.gui.debug.GuiShowPropertiesAction;
import org.worldgrower.gui.debug.GuiShowReasonsOverviewAction;
import org.worldgrower.gui.debug.GuiShowSkillOverviewAction;
import org.worldgrower.gui.debug.GuiShowThrownOutOfGroupEventsAction;
import org.worldgrower.gui.debug.ShowPerformedActionsAction;
import org.worldgrower.gui.inventory.GuiBarterAction;
import org.worldgrower.gui.inventory.ShowInventoryAction;
import org.worldgrower.gui.knowledge.GuiCreateNewsPaperAction;
import org.worldgrower.gui.music.SoundIdReader;
import org.worldgrower.gui.start.Game;
import org.worldgrower.gui.start.GuiAction;
import org.worldgrower.gui.start.KeyBindings;
import org.worldgrower.gui.util.IconUtils;
import org.worldgrower.gui.util.MenuFactory;
import org.worldgrower.gui.util.ShowTextDialog;

public class GuiMouseListener extends MouseAdapter {
	private final SkillImageIds skillImageIds = new SkillImageIds();
	private WorldPanel container;
	private WorldObject playerCharacter;
	private World world;
	private DungeonMaster dungeonMaster;
	private ImageInfoReader imageInfoReader;
	private SoundIdReader soundIdReader;
	private KeyBindings keyBindings;
	private JFrame parentFrame;
	
	private final CharacterSheetAction characterSheetAction;
	private final ShowInventoryAction inventoryAction;
	private final MagicOverviewAction magicOverviewAction;
	private final RestAction restAction;
	private final GuiAssignActionToLeftMouseAction assignActionToLeftMouseAction;
	private final ShowStatusMessagesAction showStatusMessagesAction;
	private ManagedOperation leftMouseClickAction;
	private final ShowCharacterActionsAction showCharacterActionsAction;
	private final CommunityOverviewAction communityOverviewAction;
	private final GuiShowGovernanceAction showGovernanceAction;
	
    public GuiMouseListener(WorldPanel container, WorldObject playerCharacter, World world, DungeonMaster dungeonMaster, ImageInfoReader imageInfoReader, SoundIdReader soundIdReader, KeyBindings keyBindings, JFrame parentFrame) {
		super();
		this.container = container;
		this.playerCharacter = playerCharacter;
		this.world = world;
		this.dungeonMaster = dungeonMaster;
		this.imageInfoReader = imageInfoReader;
		this.soundIdReader = soundIdReader;
		this.keyBindings = keyBindings;
		this.parentFrame = parentFrame;
		
		characterSheetAction = new CharacterSheetAction(playerCharacter, imageInfoReader, soundIdReader, world, parentFrame);
		inventoryAction = new ShowInventoryAction(playerCharacter, imageInfoReader, soundIdReader, world, dungeonMaster, container, parentFrame);
		magicOverviewAction = new MagicOverviewAction(playerCharacter, imageInfoReader, soundIdReader, parentFrame);
		restAction = new RestAction(playerCharacter, imageInfoReader, soundIdReader, world, (WorldPanel)container, dungeonMaster, parentFrame);
		showStatusMessagesAction = new ShowStatusMessagesAction(container);
		assignActionToLeftMouseAction = getGuiAssignActionToLeftMouseAction();
		showCharacterActionsAction = new ShowCharacterActionsAction();
		communityOverviewAction = new CommunityOverviewAction(playerCharacter, imageInfoReader, world, parentFrame);
		showGovernanceAction = new GuiShowGovernanceAction(playerCharacter, dungeonMaster, world, container, soundIdReader, parentFrame);
		addKeyBindings(keyBindings);
	}

    public void initializeKeyBindings() {
    	addKeyBindings(keyBindings);
    }
    
	private void addKeyBindings(KeyBindings keyBindings) {
		addKeyBindingsFor(characterSheetAction, keyBindings.getValue(GuiAction.SHOW_CHARACTER_SHEET));
		addKeyBindingsFor(inventoryAction, keyBindings.getValue(GuiAction.SHOW_INVENTORY));
		addKeyBindingsFor(magicOverviewAction, keyBindings.getValue(GuiAction.SHOW_MAGIC_OVERVIEW));
		addKeyBindingsFor(restAction, keyBindings.getValue(GuiAction.REST_ACTION));
		addKeyBindingsFor(showStatusMessagesAction, keyBindings.getValue(GuiAction.SHOW_STATUS_MESSAGES));
		addKeyBindingsFor(assignActionToLeftMouseAction, keyBindings.getValue(GuiAction.ASSIGN_ACTION_TO_LEFT_MOUSE));
		addKeyBindingsFor(showCharacterActionsAction, keyBindings.getValue(GuiAction.SHOW_CHARACTER_ACTIONS));
		addKeyBindingsFor(communityOverviewAction, keyBindings.getValue(GuiAction.COMMUNITY_OVERVIEW));
		addKeyBindingsFor(showGovernanceAction, keyBindings.getValue(GuiAction.SHOW_GOVERNANCE));
	}
	
	private void addKeyBindingsFor(Action action, char binding) {
		String bindingValue = Character.toString(binding);
		container.getInputMap().put(KeyStroke.getKeyStroke(bindingValue), action.getClass().getSimpleName());
		container.getActionMap().put(action.getClass().getSimpleName(), action);
		action.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(bindingValue));
	}

	@Override
	public void mousePressed(MouseEvent e){
		mouseAction(e);
    }
 
	private boolean isDefaultLeftMouseButton(MouseEvent e) {
		if (keyBindings.leftMouseClickCentersMap()) {
			return SwingUtilities.isLeftMouseButton(e);
		} else {
			return SwingUtilities.isRightMouseButton(e);
		}
	}
	
	private boolean isDefaultRightMouseButton(MouseEvent e) {
		if (keyBindings.leftMouseClickCentersMap()) {
			return SwingUtilities.isRightMouseButton(e);
		} else {
			return SwingUtilities.isLeftMouseButton(e);
		}
	}
	
    private void mouseAction(MouseEvent e) {
        int x = (int) e.getPoint().getX() / 48;
        int y = (int) e.getPoint().getY() / 48;

		WorldObject worldObject = ((WorldPanel)container).findWorldObject(x, y);
		
    	if (((WorldPanel)container).inBuildMode()) {
			((WorldPanel)container).endBuildMode(true);
		} else {
	        if (SwingUtilities.isLeftMouseButton(e) && isCtrlPressed(e) && worldObject != null) {
	        	performTalkAction(worldObject);
	        } else if (SwingUtilities.isLeftMouseButton(e) && leftMouseClickAction != null && worldObject != null) {
	        	performLeftMouseAction(worldObject);
	        } else if (isDefaultRightMouseButton(e)) {
		            doPop(e);
	        } else {
	        	centerOnScreen(e);
	        }
		}
    }

	private boolean isCtrlPressed(MouseEvent evt) {
		return (evt.getModifiers() & InputEvent.CTRL_MASK) != 0;
	}

    private void performLeftMouseAction(WorldObject worldObject) {
    	if (Game.canActionExecute(playerCharacter, leftMouseClickAction, Args.EMPTY, world, worldObject)) {
    		Game.executeActionAndMoveIntelligentWorldObjects(playerCharacter, leftMouseClickAction, Args.EMPTY, world, dungeonMaster, worldObject, container, soundIdReader);
    	} else {
    		new ShowTextDialog("Cannot execute action '" + leftMouseClickAction.getSimpleDescription() + "' on " + worldObject.getProperty(Constants.NAME), soundIdReader, parentFrame).showMe();
    	}
	}
    
    private void performTalkAction(WorldObject worldObject) {
    	if (canPlayerCharacterPerformTalkAction(worldObject, Actions.TALK_ACTION)) {
    		GuiAskQuestionAction guiAskQuestionAction = new GuiAskQuestionAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader, soundIdReader, parentFrame);
    		guiAskQuestionAction.actionPerformed(null);
		}
    }

	private void centerOnScreen(MouseEvent e) {
    	int x = (int) e.getPoint().getX() / 48;
        int y = (int) e.getPoint().getY() / 48;
        
        ((WorldPanel)container).centerOffsetsOn(x, y);
        container.repaint();
	}

	private void doPop(MouseEvent e){
        int x = (int) e.getPoint().getX() / 48;
        int y = (int) e.getPoint().getY() / 48;

		WorldPanel worldPanel = (WorldPanel)container;
		WorldObject worldObject = worldPanel.findWorldObject(x, y);
		
        if (worldObject != null) {
            if (worldObject.getProperty(Constants.ID) == 0) {
            	showPlayerCharacterMenu(e.getX(), e.getY());
            } else {
            	JPopupMenu menu = MenuFactory.createJPopupMenu();
            	if (worldObject.hasIntelligence()) {
            		addCommunicationActions(menu, worldObject);
            	} else {
            		addVoteActions(menu, worldObject);
            		addResearchActions(menu, worldObject);
            		addRestActions(menu, worldObject);
            		addScribeMagicSpells(menu, worldObject);
            	}
            	addBarterAction(menu, worldObject);
            	addAccessContainerAction(menu, worldObject);
            	addPropertiesMenu(menu, worldObject);
            	addPerformedActionsMenu(menu, worldObject);
            	addAllActions(menu, worldObject);
            	
            	menu.show(e.getComponent(), e.getX(), e.getY());
            }
        } else {
        	JPopupMenu menu = MenuFactory.createJPopupMenu();
        	addGotoMenu(menu, worldPanel.getRealX(x), worldPanel.getRealY(y));
        	menu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

	private void addGotoMenu(JPopupMenu menu, int x, int y) {
		JMenuItem gotoMenuItem = MenuFactory.createJMenuItem(new GuiGotoAction(playerCharacter, imageInfoReader, soundIdReader, world, (WorldPanel)container, dungeonMaster, parentFrame, x, y), soundIdReader);
		gotoMenuItem.setText("Go to");
		setMenuIcon(gotoMenuItem, ImageIds.MOVING_CHARACTER);
		menu.add(gotoMenuItem);
		gotoMenuItem.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				clearPath();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				clearPath();
			}

			void clearPath() {
				container.setGotoPath(new ArrayList<>());
				container.repaint();
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				container.setGotoPath(GuiGotoAction.calculatePath(playerCharacter, x, y, world));
				container.repaint();
			}
		});
	}

	private void showPlayerCharacterMenu(int x, int y) {
		JPopupMenu menu = MenuFactory.createJPopupMenu();
		addPlayerCharacterInformationMenus(menu);
		
		JMenu organizationMenu = MenuFactory.createJMenu("Organization", soundIdReader);
		setMenuIcon(organizationMenu, ImageIds.BLACK_CROSS);
		JMenu miscMenu = MenuFactory.createJMenu("Miscellaneous", soundIdReader);
		setMenuIcon(miscMenu, ImageIds.INVESTIGATE);
		
		addDisguiseMenu(miscMenu);
		
		addPropertiesMenu(menu, playerCharacter);
		addBuildActions(menu);
		addBuildProductionActions(menu);
		addPlantActions(menu);
		addIllusionActions(menu);
		addRestorationActions(menu);
		addTransmutationActions(menu);
		addEvocationActions(menu);
		addNecromancyActions(menu);
		addRestMenu(menu);
		menu.add(organizationMenu);
		addCreateOrganizationMenu(organizationMenu);
		addShowGovernanceMenu(organizationMenu);
		addShowCommunityActionMenu(organizationMenu);
		addChooseDeityMenu(miscMenu);
		addCreateHumanMeatMenu(miscMenu);
		addInvestigateMenu(miscMenu);
		addNewsPaperAction(miscMenu);
		menu.add(miscMenu);
		addAssignActionsToLeftMouse(menu);
		
		menu.show(container, x, y);
	}

	private void addDisguiseMenu(JMenu menu) {
		JMenuItem disguiseMenuItem = MenuFactory.createJMenuItem(new GuiDisguiseAction(playerCharacter, imageInfoReader, soundIdReader, world, (WorldPanel)container, dungeonMaster, Actions.DISGUISE_ACTION, parentFrame), soundIdReader);
		disguiseMenuItem.setText("Disguise...");
		setMenuIcon(disguiseMenuItem, Actions.DISGUISE_ACTION.getImageIds());
		menu.add(disguiseMenuItem);
	}

	private void addChooseDeityMenu(JMenu menu) {
		JMenuItem chooseDeityMenuItem = MenuFactory.createJMenuItem(new ChooseDeityAction(playerCharacter, imageInfoReader, soundIdReader, world, (WorldPanel)container, dungeonMaster, parentFrame), soundIdReader);
		chooseDeityMenuItem.setText("Choose Deity...");
		setMenuIcon(chooseDeityMenuItem, Actions.CHOOSE_DEITY_ACTION.getImageIds());
		menu.add(chooseDeityMenuItem);
	}

	private void addCreateHumanMeatMenu(JMenu menu) {
		ManagedOperation[] actions = { Actions.CREATE_HUMAN_MEAT_ACTION };
		addActions(menu, actions);
	}
	
	private void addInvestigateMenu(JMenu menu) {
		ManagedOperation[] actions = { Actions.INVESTIGATE_ACTION };
		addActions(menu, actions);
	}
	
	private void addShowGovernanceMenu(JMenu menu) {
		JMenuItem showGovernanceMenuItem = MenuFactory.createJMenuItem(showGovernanceAction, soundIdReader);
		showGovernanceMenuItem.setText("Show governance overview...");
		setMenuIcon(showGovernanceMenuItem, Actions.SET_GOVERNANCE_ACTION.getImageIds());
		addToolTips(Actions.SET_GOVERNANCE_ACTION, showGovernanceMenuItem);
		menu.add(showGovernanceMenuItem);
	}

	private void addShowCommunityActionMenu(JMenu menu) {
		JMenuItem showCommunityMenuItem = MenuFactory.createJMenuItem(communityOverviewAction, soundIdReader);
		showCommunityMenuItem.setText("Community Overview");
		setMenuIcon(showCommunityMenuItem, ImageIds.BLACK_CROSS);
		showCommunityMenuItem.setToolTipText("show family members, acquaintances and organizations");
		menu.add(showCommunityMenuItem);
	}
	
	private void addCreateOrganizationMenu(JMenu menu) {
		JMenuItem createOrganizationMenuItem = MenuFactory.createJMenuItem(new GuiCreateOrganizationAction(playerCharacter, imageInfoReader, soundIdReader, world, (WorldPanel)container, dungeonMaster, parentFrame), soundIdReader);
		createOrganizationMenuItem.setText("Create Organization...");
		setMenuIcon(createOrganizationMenuItem, Actions.CREATE_PROFESSION_ORGANIZATION_ACTION.getImageIds());
		createOrganizationMenuItem.setToolTipText("create an organization based on a deity or profession");
		menu.add(createOrganizationMenuItem);
	}

	private void addRestMenu(JPopupMenu menu) {
		JMenuItem restMenuItem = MenuFactory.createJMenuItem(restAction, soundIdReader);
		setMenuIcon(restMenuItem, ImageIds.SLEEPING_INDICATOR);
		restMenuItem.setText("Rest...");
		restMenuItem.setToolTipText(Actions.REST_ACTION.getDescription());
		menu.add(restMenuItem);
	}

	private void setMenuIcon(JMenuItem menuItem, ImageIds imageIds) {
		Image image = imageInfoReader.getImage(imageIds, null);
		int imageWidth = image.getWidth(null);
		int imageHeight = image.getHeight(null);
		if (imageWidth > 96 || imageHeight > 96) {
			image = cropImage((BufferedImage)image, Math.min(imageWidth, 96), Math.min(imageHeight, 96));
		}
		menuItem.setIcon(new ImageIcon(image));
	}
	
	private BufferedImage cropImage(BufferedImage src, int width, int height) {
	      BufferedImage dest = src.getSubimage(0, 0, width, height);
	      return dest; 
	   }

	private void addCommunicationActions(JPopupMenu menu, WorldObject worldObject) {
		if (canPlayerCharacterPerformTalkAction(worldObject, Actions.TALK_ACTION)) {
			JMenuItem guiTalkMenuItem = MenuFactory.createJMenuItem(new GuiAskQuestionAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader, soundIdReader, parentFrame), soundIdReader);
			guiTalkMenuItem.setText("Talk...");
			setMenuIcon(guiTalkMenuItem, ImageIds.GOLD_AMULET);
			addToolTips(Actions.TALK_ACTION, guiTalkMenuItem);
			menu.add(guiTalkMenuItem);
		}
	}

	private void addBarterAction(JPopupMenu menu, WorldObject worldObject) {
		if (canTrade(worldObject) && !worldObject.hasProperty(Constants.ILLUSION_CREATOR_ID)) {
			JMenuItem guiBarterItem = MenuFactory.createJMenuItem(new GuiBarterAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader, soundIdReader, parentFrame), soundIdReader);
			guiBarterItem.setText("Barter...");
			setMenuIcon(guiBarterItem, Actions.SELL_ACTION.getImageIds());
			guiBarterItem.setToolTipText("buy or sell items for gold");
			menu.add(guiBarterItem);
		}
	}

	private boolean canTrade(WorldObject worldObject) {
		return Actions.BUY_ACTION.isValidTarget(playerCharacter, worldObject, world) && Actions.BUY_ACTION.distance(playerCharacter, worldObject, Args.EMPTY, world) == 0;
	}
	
	private void addAccessContainerAction(JPopupMenu menu, WorldObject worldObject) {
		if (Actions.GET_ITEM_FROM_INVENTORY_ACTION.canExecute(playerCharacter, worldObject, Args.EMPTY, world) && !worldObject.hasProperty(Constants.ILLUSION_CREATOR_ID)) {
			JMenuItem guiBarterItem = MenuFactory.createJMenuItem(new GuiBarterAction(playerCharacter, world, dungeonMaster, container, worldObject, imageInfoReader, soundIdReader, parentFrame), soundIdReader);
			guiBarterItem.setText("Access container...");
			setMenuIcon(guiBarterItem, Actions.SELL_ACTION.getImageIds());
			guiBarterItem.setToolTipText("store or retrieve items from a container");
			menu.add(guiBarterItem);
		}
	}
	
	private void addVoteActions(JPopupMenu menu, WorldObject worldObject) {
		if (canPlayerCharacterPerformAction(worldObject, Actions.VOTE_FOR_LEADER_ACTION)) {
			JMenuItem guiVoteMenuItem = MenuFactory.createJMenuItem(new GuiVoteAction(playerCharacter, imageInfoReader, soundIdReader, world, container, dungeonMaster, worldObject, parentFrame), soundIdReader);
			guiVoteMenuItem.setText("Vote...");
			setMenuIcon(guiVoteMenuItem, Actions.VOTE_FOR_LEADER_ACTION.getImageIds());
			guiVoteMenuItem.setToolTipText(Actions.VOTE_FOR_LEADER_ACTION.getDescription());
			menu.add(guiVoteMenuItem);
		}
	}
	
	private void addResearchActions(JPopupMenu menu, WorldObject worldObject) {
		if (ResearchSpellAction.isValidTarget(worldObject) && Actions.getMagicSpellsToResearch(playerCharacter).size() > 0) {
			JMenuItem guiResearchMagicSpellMenuItem = MenuFactory.createJMenuItem(new GuiResearchMagicSpellAction(playerCharacter, imageInfoReader, soundIdReader, world, container, dungeonMaster, worldObject, parentFrame), soundIdReader);
			guiResearchMagicSpellMenuItem.setText("Research...");
			setMenuIcon(guiResearchMagicSpellMenuItem, ImageIds.SPELL_BOOK);
			menu.add(guiResearchMagicSpellMenuItem);
		}
	}
	
	private void addRestActions(JPopupMenu menu, WorldObject worldObject) {
		if (Game.canActionExecute(playerCharacter, Actions.SLEEP_ACTION, Args.EMPTY, world, worldObject)) {
			JMenuItem restMultipleTurnsMenuItem = MenuFactory.createJMenuItem(new GuiRestMultipleTurnsAction(playerCharacter, imageInfoReader, soundIdReader, world, container, dungeonMaster, worldObject, parentFrame), soundIdReader);
			restMultipleTurnsMenuItem.setText("Sleep multiple turns...");
			setMenuIcon(restMultipleTurnsMenuItem, ImageIds.SLEEPING_INDICATOR);
			restMultipleTurnsMenuItem.setToolTipText(Actions.SLEEP_ACTION.getDescription());
			menu.add(restMultipleTurnsMenuItem);
		}
	}

	private void addPlayerCharacterInformationMenus(JPopupMenu menu) {
		JMenuItem characterSheetMenuItem = MenuFactory.createJMenuItem(characterSheetAction, soundIdReader);
		characterSheetMenuItem.setText("Character Sheet");
		setMenuIcon(characterSheetMenuItem, ImageIds.WOODEN_SHIELD);
		characterSheetMenuItem.setToolTipText("character sheet shows character attributes, skills and equipment");
		menu.add(characterSheetMenuItem);
		
		JMenuItem inventoryMenuItem = MenuFactory.createJMenuItem(inventoryAction, soundIdReader);
		setMenuIcon(inventoryMenuItem, ImageIds.CHEST);
		inventoryMenuItem.setToolTipText("inventory screen shows contents of inventory");
		inventoryMenuItem.setText("Inventory");
		menu.add(inventoryMenuItem);
		
		JMenuItem magicOverviewMenuItem = MenuFactory.createJMenuItem(magicOverviewAction, soundIdReader);
		setMenuIcon(magicOverviewMenuItem, ImageIds.MAGIC_ICON);
		magicOverviewMenuItem.setToolTipText("magic overview screen shows magic spells information");
		magicOverviewMenuItem.setText("Magic Overview");
		menu.add(magicOverviewMenuItem);
	}

	private void addBuildActions(JPopupMenu menu) {
		BuildAction[] buildActions = { Actions.BUILD_SHACK_ACTION, Actions.BUILD_HOUSE_ACTION, Actions.BUILD_SHRINE_ACTION, Actions.BUILD_WELL_ACTION, Actions.BUILD_LIBRARY_ACTION, Actions.CREATE_GRAVE_ACTION, Actions.CONSTRUCT_TRAINING_DUMMY_ACTION, Actions.BUILD_JAIL_ACTION, Actions.BUILD_SACRIFICAL_ALTAR_ACTION, Actions.BUILD_ARENA_ACTION, Actions.CONSTRUCT_CHEST_ACTION };
		addBuildActions(menu, ImageIds.HAMMER, "Build", buildActions);
	}
	
	private void addBuildProductionActions(JPopupMenu menu) {
		BuildAction[] buildActions = { Actions.BUILD_SMITH_ACTION, Actions.BUILD_PAPER_MILL_ACTION, Actions.BUILD_WEAVERY_ACTION, Actions.BUILD_WORKBENCH_ACTION, Actions.BUILD_BREWERY_ACTION, Actions.BUILD_APOTHECARY_ACTION };
		addBuildActions(menu, ImageIds.HAMMER, "Build production buildings", buildActions);
	}
	
	private void addPlantActions(JPopupMenu menu) {
		BuildAction[] buildActions = { Actions.PLANT_BERRY_BUSH_ACTION, Actions.PLANT_GRAPE_VINE_ACTION, Actions.PLANT_TREE_ACTION, Actions.PLANT_COTTON_PLANT_ACTION, Actions.PLANT_NIGHT_SHADE_ACTION };
		addBuildActions(menu, ImageIds.BUSH, "Plant", buildActions);
	}
	
	private void addIllusionActions(JPopupMenu menu) {
		IllusionSpell[] buildActions = { Actions.MINOR_ILLUSION_ACTION, Actions.MAJOR_ILLUSION_ACTION };
		MagicSpell[] illusionActions = { Actions.INVISIBILITY_ACTION };
		JMenu illusionMenu = addBuildActions(menu, ImageIds.MINOR_ILLUSION_MAGIC_SPELL, "Illusions", buildActions, buildAction -> new ChooseWorldObjectAction((IllusionSpell) buildAction, playerCharacter, imageInfoReader, soundIdReader, world, ((WorldPanel)container), dungeonMaster, new StartBuildModeAction(playerCharacter, imageInfoReader, ((WorldPanel)container), buildAction), parentFrame));
		addActions(illusionMenu, illusionActions);
		
    	JMenuItem disguiseMenuItem = MenuFactory.createJMenuItem(new GuiDisguiseAction(playerCharacter, imageInfoReader, soundIdReader, world, (WorldPanel)container, dungeonMaster, Actions.DISGUISE_MAGIC_SPELL_ACTION, parentFrame), soundIdReader);
    	disguiseMenuItem.setText("Disguise self");
    	disguiseMenuItem.setEnabled(canPlayerCharacterPerformBuildAction(Actions.DISGUISE_MAGIC_SPELL_ACTION));
    	addToolTips(Actions.DISGUISE_MAGIC_SPELL_ACTION, disguiseMenuItem);
    	setMenuIcon(disguiseMenuItem, skillImageIds.getImageFor(Constants.ILLUSION_SKILL));
    	illusionMenu.add(disguiseMenuItem);
	}
	
	private void addRestorationActions(JPopupMenu menu) {
		MagicSpell[] restorationActions = { Actions.MINOR_HEAL_ACTION, Actions.CURE_DISEASE_ACTION, Actions.CURE_POISON_ACTION };
		JMenu restorationMenu = addActions(menu, skillImageIds.getImageFor(Constants.RESTORATION_SKILL), "Restoration", restorationActions);
		addBuildAction(restorationMenu, Actions.TURN_UNDEAD_ACTION, startBuildMode());
	}
	
	private void addTransmutationActions(JPopupMenu menu) {
		MagicSpell[] transmutationActions = { Actions.ENLARGE_ACTION, Actions.REDUCE_ACTION, Actions.SLEEP_MAGIC_SPELL_ACTION, Actions.WATER_WALK_ACTION, Actions.BURDEN_ACTION, Actions.FEATHER_ACTION, Actions.DARK_VISION_SPELL_ACTION };
		addActions(menu, skillImageIds.getImageFor(Constants.TRANSMUTATION_SKILL), "Transmute", transmutationActions);
	}
	
	private void addEvocationActions(JPopupMenu menu) {
		MagicSpell[] actions = { Actions.DETECT_MAGIC_ACTION, Actions.DETECT_POISON_AND_DISEASE_ACTION, Actions.DISPEL_MAGIC_ACTION, Actions.SILENCE_MAGIC_ACTION };
		JMenu evocationMenu = addActions(menu, skillImageIds.getImageFor(Constants.EVOCATION_SKILL), "Evocation", actions);
		addBuildAction(evocationMenu, Actions.FIRE_TRAP_ACTION, startBuildMode());
		addBuildAction(evocationMenu, Actions.ENTANGLE_ACTION, startBuildMode());
		addBuildAction(evocationMenu, Actions.DIMENSION_DOOR_ACTION, startBuildMode());
		addBuildAction(evocationMenu, Actions.FIRE_BALL_ATTACK_ACTION, startBuildMode());
	}
	
	private void addNecromancyActions(JPopupMenu menu) {
		MagicSpell[] actions = { Actions.LICH_TRANSFORMATION_ACTION };
		addActions(menu, skillImageIds.getImageFor(Constants.NECROMANCY_SKILL), "Necromancy", actions);
	}
	
	private void addScribeMagicSpells(JPopupMenu menu, WorldObject worldObject) {
		if (Actions.getScribeMagicSpellActionFor(Actions.FIRE_BOLT_ATTACK_ACTION).isValidTarget(playerCharacter, worldObject, world)) {
			JMenu scribeMenu = MenuFactory.createJMenu("Scribe spells", soundIdReader);
			setMenuIcon(scribeMenu, ImageIds.SPELL_BOOK);
			menu.add(scribeMenu);
			Map<SkillProperty, List<ManagedOperation>> scribeActionsMap = Actions.getScribeMagicSpellActions();
			List<SkillProperty> skillsList = Actions.getSortedSkillProperties(scribeActionsMap);
			for(SkillProperty skillProperty : skillsList) {
				JMenu skillMenuItem = createSkillMenu(skillProperty);
				scribeMenu.add(skillMenuItem);
				addActions(skillMenuItem, scribeActionsMap.get(skillProperty).toArray(new ManagedOperation[0]));
			}
		}
	}
	
	private void addBuildActions(JPopupMenu menu, ImageIds imageId, String menuTitle, BuildAction[] buildActions) {
		addBuildActions(menu, imageId, menuTitle, buildActions, startBuildMode());
	}

	private Function<BuildAction, Action> startBuildMode() {
		return buildAction -> new StartBuildModeAction(playerCharacter, imageInfoReader, ((WorldPanel)container), buildAction);
	}
	
	private JMenu addBuildActions(JPopupMenu menu, ImageIds imageId, String menuTitle, BuildAction[] buildActions, Function<BuildAction, Action> guiActionBuilder) {
		JMenu parentMenuItem = MenuFactory.createJMenu(menuTitle, soundIdReader);
		menu.add(parentMenuItem);
		setMenuIcon(parentMenuItem, imageId);
		
		for(BuildAction buildAction : buildActions) {
			addBuildAction(parentMenuItem, buildAction, guiActionBuilder);
		}
		return parentMenuItem;
	}

	private void addBuildAction(JMenu parentMenuItem, BuildAction buildAction, Function<BuildAction, Action> guiActionBuilder) {
		final JMenuItem buildMenuItem;
		if (canPlayerCharacterPerformBuildAction(buildAction)) {
			buildMenuItem = MenuFactory.createJMenuItem(guiActionBuilder.apply(buildAction), soundIdReader);
			buildMenuItem.setText(buildAction.getSimpleDescription() + "...");
			parentMenuItem.add(buildMenuItem);
		} else {
			buildMenuItem = createDisabledActionMenuItem(parentMenuItem, buildAction);
		}
		String requirementsDescription = buildAction.getRequirementsDescription();
		List<ManagedOperation> allowedCraftActions = buildAction.getAllowedCraftActions(playerCharacter, world);
		String allowedCraftActionsDescription = createAllowedCraftActionsDescription(allowedCraftActions);
		
		String tooltip = "<html>" + requirementsDescription;
		if (buildAction.getDescription().length() > 0) {
			tooltip += "<br>" + buildAction.getDescription();
		}
		if (allowedCraftActions.size() > 0) {
			tooltip += "<br>" + allowedCraftActionsDescription;
		}
		tooltip += "</html>";
		
		buildMenuItem.setToolTipText(tooltip);
		addImageIcon(buildAction, buildMenuItem);
	}

	private String createAllowedCraftActionsDescription(List<ManagedOperation> allowedCraftActions) {
		StringBuilder allowedCraftActionsDescription = new StringBuilder("Allows actions:<br>");
		for(ManagedOperation allowedCraftAction : allowedCraftActions) {
			allowedCraftActionsDescription.append("&nbsp;&nbsp;").append(allowedCraftAction.getSimpleDescription()).append("<br>");
		}
		return allowedCraftActionsDescription.toString();
	}
	
	private void addNewsPaperAction(JMenu menu) {
		JMenuItem guiCreateNewsPaperMenuItem = MenuFactory.createJMenuItem(new GuiCreateNewsPaperAction(playerCharacter, imageInfoReader, soundIdReader, world, container, dungeonMaster, parentFrame), soundIdReader);
		guiCreateNewsPaperMenuItem.setText("Create newspaper...");
		boolean enabled = (Game.canActionExecute(playerCharacter, Actions.CREATE_NEWS_PAPER_ACTION, Args.EMPTY, world, playerCharacter));
		guiCreateNewsPaperMenuItem.setEnabled(enabled);
		addToolTips(Actions.CREATE_NEWS_PAPER_ACTION, guiCreateNewsPaperMenuItem);
		addImageIcon(Actions.CREATE_NEWS_PAPER_ACTION, guiCreateNewsPaperMenuItem);
		menu.add(guiCreateNewsPaperMenuItem);
	}
	
	private JMenu addActions(JPopupMenu menu, ImageIds imageId, String menuTitle, ManagedOperation[] actions) {
		JMenu parentMenuItem = MenuFactory.createJMenu(menuTitle, soundIdReader);
		menu.add(parentMenuItem);
		setMenuIcon(parentMenuItem, imageId);
		
		addActions(parentMenuItem, actions);
		return parentMenuItem;
	}

	private void addActions(JMenu parentMenuItem, ManagedOperation[] actions) {
		for(ManagedOperation action : actions) {
			final JMenuItem menuItem;
			if (canPlayerCharacterPerformBuildAction(action)) {
				PlayerCharacterAction guiAction = new PlayerCharacterAction(playerCharacter, world, container, dungeonMaster, action, playerCharacter, soundIdReader);
				menuItem = MenuFactory.createJMenuItem(guiAction, soundIdReader);
				menuItem.setText(action.getDescription(playerCharacter, playerCharacter, null, world) + "...");
				parentMenuItem.add(menuItem);
			} else {
				menuItem = createDisabledActionMenuItem(parentMenuItem, action);
			}
			addToolTips(action, menuItem);
			addImageIcon(action, menuItem);
		}
	}

	private void addImageIcon(ManagedOperation action, JMenuItem menuItem) {
		if (menuItem != null) {
			setMenuIcon(menuItem, action.getImageIds());
		}
	}

	private void addToolTips(ManagedOperation action, final JMenuItem menuItem) {
		if (menuItem != null) {
			String tooltip = "<html>" + action.getRequirementsDescription() + "<br>" + action.getDescription() + "</html>";
			menuItem.setToolTipText(tooltip);
		}
	}

	private JMenuItem createDisabledActionMenuItem(JMenuItem menu, ManagedOperation action) {
		JMenuItem menuItem = MenuFactory.createJMenuItem(action.getSimpleDescription() + "...", soundIdReader);
		menuItem.setEnabled(false);
		addToolTips(action, menuItem);
		menu.add(menuItem);
		
		//bugfix to make disabled components have the correct cursor:
		//https://bugs.openjdk.java.net/browse/JDK-4380700
		menuItem.getParent().setCursor(Cursors.CURSOR);
		
		return menuItem;
	}

	private void addAllActions(JPopupMenu menu, WorldObject worldObject) {
		Map<SkillProperty, JMenu> existingSkillMenus = new HashMap<>();
		for(ManagedOperation action : playerCharacter.getOperations()) {
			if (!action.requiresArguments() && (!(action instanceof ScribeMagicSpellAction)) && (!(action instanceof ResearchSpellAction))) {
				JComponent parentMenu = menu;
				if (action instanceof MagicSpell) {
					MagicSpell magicSpell = (MagicSpell) action;
					SkillProperty skillProperty = magicSpell.getSkill();
					JMenu skillMenu = existingSkillMenus.get(skillProperty);
					if (skillMenu == null) {
						skillMenu = createSkillMenu(skillProperty);
						existingSkillMenus.put(skillProperty, skillMenu);
					}
					parentMenu = skillMenu;
				}
				
				final JMenuItem menuItem;
				if (canPlayerCharacterPerformAction(worldObject, action)) {
					menuItem = createEnabledMenuItem(worldObject, action);
					parentMenu.add(menuItem);
				} else if (canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action)) {
					menuItem = createDisabledMenuItem(action);
					parentMenu.add(menuItem);
					
					//bugfix to make disabled components have the correct cursor:
					//https://bugs.openjdk.java.net/browse/JDK-4380700
					menuItem.getParent().setCursor(Cursors.CURSOR);
				} else {
					menuItem = null;
				}
				addToolTips(action, menuItem);
				addImageIcon(action, menuItem);
			}
			
			addObfuscateAction(menu, worldObject, action);
		}
		
		List<SkillProperty> skillProperties = Actions.sortSkillProperties(existingSkillMenus.keySet());
		for(SkillProperty skillProperty : skillProperties) {
			JMenu skillMenu = existingSkillMenus.get(skillProperty);
			if (skillMenu.getItemCount() > 0) {
				menu.add(skillMenu);
			}
		}
	}

	private JMenu createSkillMenu(SkillProperty skillProperty) {
		JMenu skillMenu;
		String skillName = skillProperty.getName();
		skillName = Character.toUpperCase(skillName.charAt(0)) + skillName.substring(1);
		skillMenu = MenuFactory.createJMenu(skillName, soundIdReader);
		setMenuIcon(skillMenu, skillImageIds.getImageFor(skillProperty));
		return skillMenu;
	}

	private JMenuItem createDisabledMenuItem(ManagedOperation action) {
		final JMenuItem menuItem;
		menuItem = MenuFactory.createJMenuItem(action.getSimpleDescription(), soundIdReader);
		menuItem.setEnabled(false);
		return menuItem;
	}

	private JMenuItem createEnabledMenuItem(WorldObject worldObject, ManagedOperation action) {
		final JMenuItem menuItem;
		PlayerCharacterAction guiAction = new PlayerCharacterAction(playerCharacter, world, container, dungeonMaster, action, worldObject, soundIdReader);
		menuItem = MenuFactory.createJMenuItem(guiAction, soundIdReader);
		menuItem.setText(action.getSimpleDescription());
		return menuItem;
	}

	private void addObfuscateAction(JPopupMenu menu, WorldObject worldObject, ManagedOperation action) {
		if (action == Actions.OBFUSCATE_DEATH_REASON_ACTION) {
			final JMenuItem menuItem;
			if (canPlayerCharacterPerformAction(worldObject, action)) {
				ChooseDeathReasonAction guiAction = new ChooseDeathReasonAction(playerCharacter, imageInfoReader, soundIdReader, world, container, dungeonMaster, worldObject, parentFrame);
				menuItem = MenuFactory.createJMenuItem(guiAction, soundIdReader);
				menuItem.setText(action.getSimpleDescription());
				menu.add(menuItem);
			} else if (canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action)) {
				menuItem = createDisabledMenuItem(action);
				menu.add(menuItem);
			} else {
				menuItem = null;
			}
			addToolTips(action, menuItem);
			addImageIcon(action, menuItem);
		}
	}
	
	private List<ManagedOperation> getActions() {
		List<ManagedOperation> actions = new ArrayList<>();
		for(ManagedOperation action : playerCharacter.getOperations()) {
			if (!action.requiresArguments() && !(action instanceof BuildAction) && !(Actions.getInventoryActions().contains(action))) {
				actions.add(action);
			}
		}
		Actions.sortActionsByDescription(actions);
		return actions;
	}
	
	private void addAssignActionsToLeftMouse(JPopupMenu menu) {
		JMenuItem guiAssignActionsToLeftMouseItem = MenuFactory.createJMenuItem(assignActionToLeftMouseAction, soundIdReader);
		guiAssignActionsToLeftMouseItem.setText("Assign action to left mouse click...");
		guiAssignActionsToLeftMouseItem.setIcon(IconUtils.getMouseIcon());
		menu.add(guiAssignActionsToLeftMouseItem);
	}

	private GuiAssignActionToLeftMouseAction getGuiAssignActionToLeftMouseAction() {
		return new GuiAssignActionToLeftMouseAction(getActions(), container, this, soundIdReader, parentFrame);
	}

	private void addPropertiesMenu(JPopupMenu menu, WorldObject worldObject) {
		if (Boolean.getBoolean("DEBUG")) {
			JMenuItem guiPropertiesItem = MenuFactory.createJMenuItem(new GuiShowPropertiesAction(worldObject), soundIdReader);
			guiPropertiesItem.setText("Properties...");
			menu.add(guiPropertiesItem);
			
			JMenuItem guiShowCommonersOverviewItem = MenuFactory.createJMenuItem(new GuiShowCommonersOverviewAction(world), soundIdReader);
			guiShowCommonersOverviewItem.setText("Show Commoners Overview...");
			menu.add(guiShowCommonersOverviewItem);
			
			JMenuItem guiShowEconomicOverviewItem = MenuFactory.createJMenuItem(new GuiShowEconomicOverviewAction(world), soundIdReader);
			guiShowEconomicOverviewItem.setText("Show Economic Overview...");
			menu.add(guiShowEconomicOverviewItem);
			
			JMenuItem guiShowSkillOverviewItem = MenuFactory.createJMenuItem(new GuiShowSkillOverviewAction(world), soundIdReader);
			guiShowSkillOverviewItem.setText("Show Skill Overview...");
			menu.add(guiShowSkillOverviewItem);
			
			JMenuItem guiShowPersonalityOverviewItem = MenuFactory.createJMenuItem(new GuiShowPersonalitiesOverviewAction(world), soundIdReader);
			guiShowPersonalityOverviewItem.setText("Show Personality Overview...");
			menu.add(guiShowPersonalityOverviewItem);
			
			JMenuItem guiShowReasonsOverviewItem = MenuFactory.createJMenuItem(new GuiShowReasonsOverviewAction(world), soundIdReader);
			guiShowReasonsOverviewItem.setText("Show Reasons Overview...");
			menu.add(guiShowReasonsOverviewItem);
			
			JMenuItem guiShowPerformedActionsItem = MenuFactory.createJMenuItem(new GuiShowPerformedActionsAction(world), soundIdReader);
			guiShowPerformedActionsItem.setText("Show history items...");
			menu.add(guiShowPerformedActionsItem);
			
			JMenuItem guiShowBuildingsItem = MenuFactory.createJMenuItem(new GuiShowBuildingsOverviewAction(world), soundIdReader);
			guiShowBuildingsItem.setText("Show buildings...");
			menu.add(guiShowBuildingsItem);

			JMenuItem guiShowThrownOutOfGroupEventsAction = MenuFactory.createJMenuItem(new GuiShowThrownOutOfGroupEventsAction(), soundIdReader);
			guiShowThrownOutOfGroupEventsAction.setText("Show thrown out of group events...");
			menu.add(guiShowThrownOutOfGroupEventsAction);
			
			JMenuItem guiShowElectionEventsAction = MenuFactory.createJMenuItem(new GuiShowElectionResultsAction(), soundIdReader);
			guiShowElectionEventsAction.setText("Show election events...");
			menu.add(guiShowElectionEventsAction);
		}
	}
	
	private void addPerformedActionsMenu(JPopupMenu menu, WorldObject worldObject) {
		if (Boolean.getBoolean("DEBUG")) {
			JMenuItem showPerformedActionsItem = MenuFactory.createJMenuItem(new ShowPerformedActionsAction(worldObject, world), soundIdReader);
			showPerformedActionsItem.setText("Show performed actions...");
			menu.add(showPerformedActionsItem);
		}
	}

	private boolean canPlayerCharacterPerformAction(WorldObject worldObject, ManagedOperation action) {
		return canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action) 
				&& action.isActionPossible(playerCharacter, worldObject, Args.EMPTY, world)
				&& action.distance(playerCharacter, worldObject, Args.EMPTY, world) == 0;
	}
	
	private boolean canPlayerCharacterPerformTalkAction(WorldObject worldObject, ManagedOperation action) {
		return canPlayerCharacterPerformActionUnderCorrectCircumstances(worldObject, action) 
				&& action.isActionPossible(playerCharacter, worldObject, Conversations.createArgs(Conversations.NAME_CONVERSATION), world)
				&& action.distance(playerCharacter, worldObject, Args.EMPTY, world) == 0;
	}
	
	private boolean canPlayerCharacterPerformActionUnderCorrectCircumstances(WorldObject worldObject, ManagedOperation action) {
		return action.isValidTarget(playerCharacter, worldObject, world) && playerCharacter.canWorldObjectPerformAction(action);
	}
	
	private boolean canPlayerCharacterPerformBuildAction(ManagedOperation action) {
		return action.isActionPossible(playerCharacter, playerCharacter, Args.EMPTY, world)
				&& playerCharacter.canWorldObjectPerformAction(action);
	}

	public void executeBuildAction(ManagedOperation buildAction, WorldObject buildLocation, int[] args) {
		Game.executeActionAndMoveIntelligentWorldObjects(playerCharacter, buildAction, args, world, dungeonMaster, buildLocation, container, soundIdReader);
	}

	public void setLeftMouseClickAction(ManagedOperation action) {
		leftMouseClickAction = action;
	}
	
	private class ShowCharacterActionsAction extends AbstractAction {

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			Point location = MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(location, container);
			int x = location.x;
			int y = location.y;
			showPlayerCharacterMenu(x, y);
		}
	}
}