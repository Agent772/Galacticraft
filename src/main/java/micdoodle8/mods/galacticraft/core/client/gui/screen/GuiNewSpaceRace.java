package micdoodle8.mods.galacticraft.core.client.gui.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import micdoodle8.mods.galacticraft.api.vector.Vector2;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementCheckbox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementCheckbox.ICheckBoxCallback;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementGradientButton;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementGradientList;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementGradientList.ListElement;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementSlider;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox;
import micdoodle8.mods.galacticraft.core.client.gui.element.GuiElementTextBox.ITextBoxCallback;
import micdoodle8.mods.galacticraft.core.client.model.ModelFlag;
import micdoodle8.mods.galacticraft.core.dimension.SpaceRace;
import micdoodle8.mods.galacticraft.core.dimension.SpaceRaceManager;
import micdoodle8.mods.galacticraft.core.entities.EntityFlag;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.wrappers.FlagData;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class GuiNewSpaceRace extends GuiScreen implements ICheckBoxCallback, ITextBoxCallback
{
	protected static final ResourceLocation texture = new ResourceLocation(GalacticraftCore.ASSET_DOMAIN, "textures/gui/gui.png");
	
	public static enum EnumSpaceRaceGui
	{
		MAIN,
		ADD_PLAYER,
		REMOVE_PLAYER,
		DESIGN_FLAG
	}
	
    private int ticksPassed;
    private EntityPlayer thePlayer;
	private GuiElementCheckbox checkboxPaintbrush;
	private GuiElementCheckbox checkboxShowGrid;
	private GuiElementCheckbox checkboxEraser;
	private GuiElementCheckbox checkboxSelector;
	private GuiElementCheckbox checkboxColorSelector;
	private GuiElementTextBox textBoxRename;
	private boolean initialized;
	private GuiElementSlider sliderColorR;
	private GuiElementSlider sliderColorG;
	private GuiElementSlider sliderColorB;
	private GuiElementSlider sliderBrushSize;
	private GuiElementSlider sliderEraserSize;
	private GuiElementGradientList gradientListAddPlayers;
	private GuiElementGradientList gradientListRemovePlayers;
	private EnumSpaceRaceGui currentState = EnumSpaceRaceGui.MAIN;
    
    private int buttonFlag_width;
    private int buttonFlag_height;
    private int buttonFlag_xPosition;
    private int buttonFlag_yPosition;
    private boolean buttonFlag_hover;

    private Vector2 flagDesignerScale = new Vector2();
    private float flagDesignerMinX;
    private float flagDesignerMinY;
    private float flagDesignerWidth;
    private float flagDesignerHeight;
    
    private int selectionMinX;
    private int selectionMaxX;
    private int selectionMinY;
    private int selectionMaxY;

	private EntityFlag dummyFlag = new EntityFlag(FMLClientHandler.instance().getClient().theWorld);
	private ModelFlag dummyModel = new ModelFlag();
	
	private SpaceRace spaceRaceData;
	public Map<String, Integer> recentlyInvited = new HashMap<String, Integer>();
	
	private boolean showGrid = false;
	private boolean lastMousePressed = false;
	private boolean hasExitedMain = false;
	private boolean hasDataChanged = true;
    
	public GuiNewSpaceRace(EntityPlayer player)
	{
		this.thePlayer = player;
		
		SpaceRace race = SpaceRaceManager.getSpaceRaceFromPlayer(player.getGameProfile().getName());
		
		if (race != null)
		{
			this.spaceRaceData = race;
		}
		else
		{
			List<String> playerList = new ArrayList<String>();
			playerList.add(player.getGameProfile().getName());
			this.spaceRaceData = new SpaceRace(playerList, "Unnamed Team", new FlagData(48, 32));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		float sliderR = 0;
		float sliderG = 0;
		float sliderB = 0;
		
		if (this.sliderColorR != null && this.sliderColorG != null && this.sliderColorR != null)
		{
			sliderR = this.sliderColorR.getSliderPos() / (float)this.sliderColorR.getButtonHeight();
			sliderG = this.sliderColorG.getSliderPos() / (float)this.sliderColorG.getButtonHeight();
			sliderB = this.sliderColorB.getSliderPos() / (float)this.sliderColorB.getButtonHeight();
		}
		
		super.initGui();
		this.buttonList.clear();
		
		if (this.initialized)
		{
			final int var5 = (this.width - this.width / 4) / 2;
			final int var6 = (this.height - this.height / 4) / 2;

			this.buttonFlag_width = 81;
			this.buttonFlag_height = 58;
			this.buttonFlag_xPosition = this.width / 2 - buttonFlag_width / 2;
			this.buttonFlag_yPosition = this.height / 2 - this.height / 3 + 10;
			
			this.buttonList.add(new GuiElementGradientButton(0, this.width / 2 - this.width / 3 + 15, this.height / 2 - this.height / 4 - 15, 50, 15, this.currentState == EnumSpaceRaceGui.MAIN ? "Close" : "Back"));
			
			switch (this.currentState)
			{
			case MAIN:
				this.textBoxRename = new GuiElementTextBox(1, this, this.width / 2 - 75, this.buttonFlag_yPosition + this.buttonFlag_height + 10, 150, 16, "Rename Team", false, 25, true);
				this.buttonList.add(this.textBoxRename);
				this.buttonList.add(new GuiElementGradientButton(2, this.width / 2 - 120, this.textBoxRename.yPosition + this.height / 10, 100, this.height / 10, "Add Player(s)"));
				this.buttonList.add(new GuiElementGradientButton(3, this.width / 2 - 120, this.textBoxRename.yPosition + this.height / 10 + this.height / 10 + this.height / 50, 100, this.height / 10, "Remove Player(s)"));
				this.buttonList.add(new GuiElementGradientButton(4, this.width / 2 + 20, this.textBoxRename.yPosition + this.height / 10, 100, this.height / 10, "View Server Stats"));
				this.buttonList.add(new GuiElementGradientButton(5, this.width / 2 + 20, this.textBoxRename.yPosition + this.height / 10 + this.height / 10 + this.height / 50, 100, this.height / 10, "View Global Stats"));
				break;
			case ADD_PLAYER:
				this.buttonList.add(new GuiElementGradientButton(2, this.width / 2 - this.width / 3 + 7, this.height / 2 + this.height / 4 - 15, 64, 15, "Send Invite"));
				int xPos0 = ((GuiElementGradientButton) this.buttonList.get(0)).xPosition + ((GuiElementGradientButton) this.buttonList.get(0)).getButtonWidth() + 10;
				int xPos1 = this.width / 2 + this.width / 3 - 10;
				int yPos0 = this.height / 2 - this.height / 3 + 10;
				int yPos1 = this.height / 2 + this.height / 3 - 10;
				this.gradientListAddPlayers = new GuiElementGradientList(xPos0, yPos0, xPos1 - xPos0, yPos1 - yPos0);
				break;
			case REMOVE_PLAYER:
				this.buttonList.add(new GuiElementGradientButton(2, this.width / 2 - this.width / 3 + 7, this.height / 2 + this.height / 4 - 15, 64, 15, "Remove Player"));
				int xPos0b = ((GuiElementGradientButton) this.buttonList.get(0)).xPosition + ((GuiElementGradientButton) this.buttonList.get(0)).getButtonWidth() + 10;
				int xPos1b = this.width / 2 + this.width / 3 - 10;
				int yPos0b = this.height / 2 - this.height / 3 + 10;
				int yPos1b = this.height / 2 + this.height / 3 - 10;
				this.gradientListRemovePlayers = new GuiElementGradientList(xPos0b, yPos0b, xPos1b - xPos0b, yPos1b - yPos0b);
				break;
			case DESIGN_FLAG:
				int guiBottom = this.height / 2 + this.height / 4;
				int guiTop = this.height / 2 - this.height / 4;
				int guiLeft = this.width / 2 - this.width / 3;
	        	int guiRight = this.width / 2 + this.width / 3;
				this.flagDesignerScale = new Vector2(this.width / 130.0F, this.height / 70.0F);
	        	this.flagDesignerMinX = this.width / 2 - (this.spaceRaceData.getFlagData().getWidth() * (float)this.flagDesignerScale.x) / 2;
	        	this.flagDesignerMinY = this.height / 2 - (this.spaceRaceData.getFlagData().getHeight() * (float)this.flagDesignerScale.y) / 2;
	        	this.flagDesignerWidth = this.spaceRaceData.getFlagData().getWidth() * (float)this.flagDesignerScale.x;
	        	this.flagDesignerHeight = this.spaceRaceData.getFlagData().getHeight() * (float)this.flagDesignerScale.y;
	        	int flagDesignerRight = (int) (this.flagDesignerMinX + this.flagDesignerWidth);
	        	int availWidth = (int) (((guiRight - 10) - (this.flagDesignerMinX + this.flagDesignerWidth + 10)) / 3);
		        float x1 = flagDesignerRight + 10;
		        float x2 = guiRight - 10;
		        float y1 = guiBottom - 10 - (x2 - x1);
	        	int height = (int) ((y1 - 10) - (guiTop + 10));
				this.sliderColorR = new GuiElementSlider(1, flagDesignerRight + 10, guiTop + 10, availWidth, height, true, new Vector3(0, 0, 0), new Vector3(1, 0, 0));
				this.sliderColorG = new GuiElementSlider(2, flagDesignerRight + 11 + availWidth, guiTop + 10, availWidth, height, true, new Vector3(0, 0, 0), new Vector3(0, 1, 0));
				this.sliderColorB = new GuiElementSlider(3, flagDesignerRight + 12 + availWidth * 2, guiTop + 10, availWidth, height, true, new Vector3(0, 0, 0), new Vector3(0, 0, 1));
				this.sliderColorR.setSliderPos(sliderR);
				this.sliderColorG.setSliderPos(sliderG);
				this.sliderColorB.setSliderPos(sliderB);
				this.buttonList.add(this.sliderColorR);
				this.buttonList.add(this.sliderColorG);
				this.buttonList.add(this.sliderColorB);
				this.checkboxPaintbrush = new GuiElementCheckbox(5, this, (int) (this.flagDesignerMinX - 15), this.height / 2 - this.height / 4 + 10, 13, 13, 26, 26, 133, 0, "", 4210752, false);
				this.checkboxEraser = new GuiElementCheckbox(6, this, (int) (this.flagDesignerMinX - 15), this.height / 2 - this.height / 4 + 25, 13, 13, 26, 26, 133, 52, "", 4210752, false);
				this.checkboxSelector = new GuiElementCheckbox(7, this, (int) (this.flagDesignerMinX - 15), this.height / 2 - this.height / 4 + 40, 13, 13, 26, 26, 133, 78, "", 4210752, false);
				this.checkboxColorSelector = new GuiElementCheckbox(8, this, (int) (this.flagDesignerMinX - 15), this.height / 2 - this.height / 4 + 55, 13, 13, 26, 26, 133, 104, "", 4210752, false);
				this.checkboxShowGrid = new GuiElementCheckbox(9, this, (int) (this.flagDesignerMinX - 15), this.height / 2 - this.height / 4 + 90, 13, 13, 26, 26, 133, 26, "", 4210752, false);
				this.sliderBrushSize = new GuiElementSlider(10, checkboxPaintbrush.xPosition - 40, checkboxPaintbrush.yPosition, 35, 13, false, new Vector3(0.34, 0.34, 0.34), new Vector3(0.34, 0.34, 0.34), "Brush Size");
				this.sliderEraserSize = new GuiElementSlider(11, checkboxEraser.xPosition - 40, checkboxEraser.yPosition, 35, 13, false, new Vector3(0.34, 0.34, 0.34), new Vector3(0.34, 0.34, 0.34), "Eraser Size");
				this.sliderEraserSize.visible = false;
				this.buttonList.add(this.checkboxPaintbrush);
				this.buttonList.add(this.checkboxShowGrid);
				this.buttonList.add(this.checkboxEraser);
				this.buttonList.add(this.checkboxSelector);
				this.buttonList.add(this.checkboxColorSelector);
				this.buttonList.add(this.sliderBrushSize);
				this.buttonList.add(this.sliderEraserSize);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
    public void onGuiClosed() 
    {
		this.exitCurrentScreen(false);
    }

	@Override
	protected void keyTyped(char keyChar, int keyID)
	{
		if (this.textBoxRename != null && this.textBoxRename.keyTyped(keyChar, keyID))
		{
			return;
		}
		
		super.keyTyped(keyChar, keyID);
	}
	
	private void exitCurrentScreen(boolean close)
	{
		if (this.currentState == EnumSpaceRaceGui.MAIN)
		{
			if (this.hasDataChanged)
			{
				this.sendSpaceRaceData();
			}

    		if (close)
    		{
    			thePlayer.closeScreen();
    		}
		}
		else
		{
    		this.currentState = EnumSpaceRaceGui.MAIN;
    		this.initGui();
		}
	}
	
    protected void actionPerformed(GuiButton buttonClicked) 
    {
    	switch (buttonClicked.id)
    	{
    	case 0:
    		this.exitCurrentScreen(true);
    		break;
    	case 1:
    		break;
    	case 2:
    		if (this.currentState == EnumSpaceRaceGui.MAIN)
    		{
    			this.currentState = EnumSpaceRaceGui.ADD_PLAYER;
    			this.initGui();
    		}
    		else if (this.currentState == EnumSpaceRaceGui.ADD_PLAYER)
    		{
        		ListElement playerToInvite = this.gradientListAddPlayers.getSelectedElement();
        		if (playerToInvite != null && !this.recentlyInvited.containsKey(playerToInvite.value))
        		{
            		SpaceRace race = SpaceRaceManager.getSpaceRaceFromPlayer(thePlayer.getGameProfile().getName());
            		if (race != null)
            		{
                		GalacticraftCore.packetPipeline.sendToServer(new PacketSimple(EnumSimplePacket.S_INVITE_RACE_PLAYER, new Object[] { playerToInvite.value, race.getSpaceRaceID() }));
                		this.recentlyInvited.put(playerToInvite.value, 20 * 60);
            		}
        		}
    		}
    		break;
    	case 3:
    		if (this.currentState == EnumSpaceRaceGui.MAIN)
    		{
    			this.currentState = EnumSpaceRaceGui.REMOVE_PLAYER;
    			this.initGui();
    		}
    		break;
		default:
			break;
    	}
    }

    protected void mouseClicked(int x, int y, int clickIndex)
    {
    	super.mouseClicked(x, y, clickIndex);
    }

    public void updateScreen()
    {
        super.updateScreen();
        
        if (this.ticksPassed % 100 == 0)
        {
        	if (this.hasDataChanged || this.ticksPassed == 0)
        	{
            	this.sendSpaceRaceData();
            	this.hasDataChanged = false;
        	}
        	else
        	{
        		SpaceRaceManager.addSpaceRace(this.spaceRaceData);
        	}
        }
        
        ++this.ticksPassed;
        
        for (Entry<String, Integer> e : new HashSet<Entry<String, Integer>>(this.recentlyInvited.entrySet()))
        {
        	int timeLeft = e.getValue();
        	if (--timeLeft < 0)
        	{
        		this.recentlyInvited.remove(e.getKey());
        	}
        	else
        	{
        		this.recentlyInvited.put(e.getKey(), timeLeft);
        	}
        }
        
        if (this.gradientListAddPlayers != null && this.ticksPassed % 20 == 0)
        {
        	List<ListElement> playerNames = new ArrayList<ListElement>();
        	for (int i = 0; i < thePlayer.worldObj.playerEntities.size(); i++)
        	{
        		EntityPlayer player = (EntityPlayer) thePlayer.worldObj.playerEntities.get(i);
        		
        		if (player.getDistanceSqToEntity(thePlayer) <= 25 * 25)
        		{
            		String username = player.getGameProfile().getName();
            		
            		if (!this.spaceRaceData.getPlayerNames().contains(username))
            		{
                		playerNames.add(new ListElement(username, this.recentlyInvited.containsKey(username) ? GCCoreUtil.to32BitColor(255, 250, 120, 0) : GCCoreUtil.to32BitColor(255, 190, 190, 190)));
            		}
        		}
        	}
        	
        	this.gradientListAddPlayers.updateListContents(playerNames);
        }
        
        if (this.currentState == EnumSpaceRaceGui.ADD_PLAYER && this.gradientListAddPlayers != null)
        {
        	this.gradientListAddPlayers.update();
        }
        
        if (!this.initialized)
        {
        	return;
        }

        if (this.currentState == EnumSpaceRaceGui.DESIGN_FLAG)
        {
            int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
            
            if (x >= this.flagDesignerMinX && y >= this.flagDesignerMinY && x <= this.flagDesignerMinX + this.flagDesignerWidth && y <= this.flagDesignerMinY + this.flagDesignerHeight)
        	{
        		int unScaledX = (int)Math.floor((x - this.flagDesignerMinX) / this.flagDesignerScale.x);
        		int unScaledY = (int)Math.floor((y - this.flagDesignerMinY) / this.flagDesignerScale.y);
        		
        		if (Mouse.isButtonDown(0))
        		{
            		if (this.checkboxEraser.isSelected != null && this.checkboxEraser.isSelected)
            		{
                		this.setColorWithBrushSize(unScaledX, unScaledY, new Vector3(255, 255, 255), (int)Math.floor(this.sliderEraserSize.getNormalizedValue() * 10) + 1);
            		}
            		else if (this.checkboxColorSelector.isSelected != null && this.checkboxColorSelector.isSelected)
            		{
            			Vector3 colorAt = this.spaceRaceData.getFlagData().getColorAt(unScaledX, unScaledY);
            			this.sliderColorR.setSliderPos(colorAt.floatX());
            			this.sliderColorG.setSliderPos(colorAt.floatY());
            			this.sliderColorB.setSliderPos(colorAt.floatZ());
            		}
            		else if (this.checkboxPaintbrush.isSelected != null && this.checkboxPaintbrush.isSelected)
            		{
                		this.setColorWithBrushSize(unScaledX, unScaledY, new Vector3(this.sliderColorR.getNormalizedValue() * 256, this.sliderColorG.getNormalizedValue() * 256, this.sliderColorB.getNormalizedValue() * 256), (int)Math.floor(this.sliderBrushSize.getNormalizedValue() * 10) + 1);
            		}
        		}
        	}
    		
            if (this.checkboxSelector != null)
            {
        		if (!this.lastMousePressed && Mouse.isButtonDown(0) && this.checkboxSelector.isSelected != null && this.checkboxSelector.isSelected)
        		{
        			if (x >= this.flagDesignerMinX && y >= this.flagDesignerMinY && x <= this.flagDesignerMinX + this.flagDesignerWidth && y <= this.flagDesignerMinY + this.flagDesignerHeight)
        	    	{
        	    		int unScaledX = (int)Math.floor((x - this.flagDesignerMinX) / this.flagDesignerScale.x);
        	    		int unScaledY = (int)Math.floor((y - this.flagDesignerMinY) / this.flagDesignerScale.y);
        				this.selectionMinX = unScaledX;
        				this.selectionMinY = unScaledY;
        	    	}
        			else
        			{
        				this.selectionMinX = this.selectionMinY = -1;
        			}
        		}
        		else if (this.lastMousePressed && !Mouse.isButtonDown(0) && this.checkboxSelector.isSelected != null && this.checkboxSelector.isSelected)
        		{
        			if (selectionMinX != -1 && selectionMinY != -1 && x >= this.flagDesignerMinX && y >= this.flagDesignerMinY && x <= this.flagDesignerMinX + this.flagDesignerWidth && y <= this.flagDesignerMinY + this.flagDesignerHeight)
        	    	{
        	    		int unScaledX = (int)Math.floor((x - this.flagDesignerMinX) / this.flagDesignerScale.x);
        	    		int unScaledY = (int)Math.floor((y - this.flagDesignerMinY) / this.flagDesignerScale.y);
        				this.selectionMaxX = Math.min(unScaledX + 1, this.spaceRaceData.getFlagData().getWidth());
        				this.selectionMaxY = Math.min(unScaledY + 1, this.spaceRaceData.getFlagData().getHeight());
        				
        				if (this.selectionMinX > this.selectionMaxX)
        				{
        					int temp = this.selectionMaxX - 1;
        					this.selectionMaxX = this.selectionMinX + 1;
        					this.selectionMinX = temp;
        				}
        				
        				if (this.selectionMinY > this.selectionMaxY)
        				{
        					int temp = this.selectionMaxY - 1;
        					this.selectionMaxY = this.selectionMinY + 1;
        					this.selectionMinY = temp;
        				}
        	    	}
        			else
        			{
        				this.selectionMaxX = this.selectionMaxY = -1;
        			}
        		}
            }
            
            if (this.sliderBrushSize != null && this.sliderBrushSize.visible)
            {
            	this.sliderBrushSize.displayString = "Brush Rad: " + ((int)Math.floor(this.sliderBrushSize.getNormalizedValue() * 10) + 1);
            }
            
            if (this.sliderEraserSize != null && this.sliderEraserSize.visible)
            {
            	this.sliderEraserSize.displayString = "Eraser Rad: " + ((int)Math.floor(this.sliderEraserSize.getNormalizedValue() * 10) + 1);
            }
        }
        else if (currentState == EnumSpaceRaceGui.MAIN)
        {
        	if (this.lastMousePressed && !Mouse.isButtonDown(0))
        	{
            	if (this.buttonFlag_hover)
            	{
            		this.currentState = EnumSpaceRaceGui.DESIGN_FLAG;
            		this.initGui();
            	}
        	}
        }
        
        this.lastMousePressed = Mouse.isButtonDown(0);
    }
    
    private void setColor(int unScaledX, int unScaledY, Vector3 color)
    {
		if (this.selectionMaxX - selectionMinX > 0 && this.selectionMaxY - selectionMinY > 0)
		{
			if (unScaledX >= this.selectionMinX && unScaledX <= this.selectionMaxX - 1 && unScaledY >= this.selectionMinY && unScaledY <= this.selectionMaxY - 1)
			{
				this.hasDataChanged = true;
	    		this.spaceRaceData.getFlagData().setColorAt(unScaledX, unScaledY, color);
			}
		}
		else
		{
			this.hasDataChanged = true;
    		this.spaceRaceData.getFlagData().setColorAt(unScaledX, unScaledY, color);
		}
    }
    
    private void setColorWithBrushSize(int unScaledX, int unScaledY, Vector3 color, int brushSize)
    {
    	for (int x = unScaledX - brushSize + 1; x < unScaledX + brushSize; x++)
    	{
        	for (int y = unScaledY - brushSize + 1; y < unScaledY + brushSize; y++)
        	{
        		if (x >= 0 && x < this.spaceRaceData.getFlagData().getWidth() && y >= 0 && y < this.spaceRaceData.getFlagData().getHeight())
        		{
        			float relativeX = (x + 0.5F) - (unScaledX + 0.5F);
        			float relativeY = (y + 0.5F) - (unScaledY + 0.5F);
        			
            		if (Math.sqrt(relativeX * relativeX + relativeY * relativeY) <= brushSize)
            		{
                		this.setColor(x, y, color);
            		}
        		}
        	}
    	}
    }
    
    public void updateSpaceRaceData()
    {
    	String playerName = FMLClientHandler.instance().getClient().thePlayer.getGameProfile().getName();
    	SpaceRace race = SpaceRaceManager.getSpaceRaceFromPlayer(playerName);
    	
    	if (race != null && !this.hasDataChanged)
    	{
    		
    	}
    }
    
    public void sendSpaceRaceData()
    {
		List<Object> objList = new ArrayList<Object>();
		objList.add(this.spaceRaceData.getSpaceRaceID());
		objList.add(this.spaceRaceData.getTeamName());
		objList.add(this.spaceRaceData.getFlagData());
		objList.add(new String[] { this.thePlayer.getGameProfile().getName() });
		GalacticraftCore.packetPipeline.sendToServer(new PacketSimple(EnumSimplePacket.S_START_NEW_SPACE_RACE, objList));
    }
    
    public void drawScreen(int par1, int par2, float par3)
    {
        this.drawDefaultBackground();
		final int var5 = (this.width - this.width / 4) / 2;
		final int var6 = (this.height - this.height / 4) / 2;
		
		if (this.initialized)
		{
			this.buttonFlag_hover = this.currentState == EnumSpaceRaceGui.MAIN && par1 >= this.buttonFlag_xPosition && par2 >= this.buttonFlag_yPosition && par1 < this.buttonFlag_xPosition + this.buttonFlag_width && par2 < this.buttonFlag_yPosition + this.buttonFlag_height;
			
	        switch (this.currentState)
	        {
	        case MAIN:
				this.drawCenteredString(this.fontRendererObj, "Space Race Manager", this.width / 2, this.height / 2 - this.height / 3 - 15, 16777215);
	        	this.drawFlagButton(par1, par2);
	        	break;
	        case ADD_PLAYER:
				this.drawCenteredString(this.fontRendererObj, "Invite Player", this.width / 2, this.height / 2 - this.height / 3 - 15, 16777215);
				this.drawCenteredString(this.fontRendererObj, "Note: Players must be within range! (25 blocks)", this.width / 2, this.height / 2 + this.height / 3 + 3, GCCoreUtil.to32BitColor(255, 180, 40, 40));
	        	break;
	        case REMOVE_PLAYER:
				this.drawCenteredString(this.fontRendererObj, "Remove Player", this.width / 2, this.height / 2 - this.height / 3 - 15, 16777215);
	        	break;
	        case DESIGN_FLAG:
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		        GL11.glDisable(GL11.GL_TEXTURE_2D);
		        GL11.glEnable(GL11.GL_BLEND);
		        GL11.glDisable(GL11.GL_ALPHA_TEST);
		        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		        GL11.glShadeModel(GL11.GL_SMOOTH);
		        Tessellator tessellator = Tessellator.instance;
    	        
	        	for (int x = 0; x < this.spaceRaceData.getFlagData().getWidth(); x++)
	        	{
	        		for (int y = 0; y < this.spaceRaceData.getFlagData().getHeight(); y++)
	        		{
	        			Vector3 color = this.spaceRaceData.getFlagData().getColorAt(x, y);
	        	        GL11.glColor4f(color.floatX(), color.floatY(), color.floatZ(), 1.0F);
	        	        tessellator.startDrawingQuads();
	        	        tessellator.addVertex((double)this.flagDesignerMinX + x * this.flagDesignerScale.x, (double)this.flagDesignerMinY + y * this.flagDesignerScale.y + 1 * this.flagDesignerScale.y, 0.0D);
	        	        tessellator.addVertex((double)this.flagDesignerMinX + x * this.flagDesignerScale.x + 1 * this.flagDesignerScale.x, (double)this.flagDesignerMinY + y * this.flagDesignerScale.y + 1 * this.flagDesignerScale.y, 0.0D);
	        	        tessellator.addVertex((double)this.flagDesignerMinX + x * this.flagDesignerScale.x + 1 * this.flagDesignerScale.x, (double)this.flagDesignerMinY + y * this.flagDesignerScale.y, 0.0D);
	        	        tessellator.addVertex((double)this.flagDesignerMinX + x * this.flagDesignerScale.x, (double)this.flagDesignerMinY + y * this.flagDesignerScale.y, 0.0D);
	        	        tessellator.draw();
	        		}
	        	}
	        	
	        	if (this.checkboxShowGrid != null && this.checkboxShowGrid.isSelected != null && this.checkboxShowGrid.isSelected)
	        	{
		        	for (int x = 0; x <= this.spaceRaceData.getFlagData().getWidth(); x++)
		        	{
			        	tessellator.startDrawing(GL11.GL_LINES);
			        	tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 1.0F);
			        	tessellator.addVertex(this.flagDesignerMinX + x * this.flagDesignerScale.x, this.flagDesignerMinY, this.zLevel);
			        	tessellator.addVertex(this.flagDesignerMinX + x * this.flagDesignerScale.x, this.flagDesignerMinY + this.flagDesignerHeight, this.zLevel);
		        		tessellator.draw();
		        	}
		        	
		        	for (int y = 0; y <= this.spaceRaceData.getFlagData().getHeight(); y++)
	        		{
			        	tessellator.startDrawing(GL11.GL_LINES);
			        	tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 1.0F);
			        	tessellator.addVertex(this.flagDesignerMinX, this.flagDesignerMinY + y * this.flagDesignerScale.y, this.zLevel);
			        	tessellator.addVertex(this.flagDesignerMinX + this.flagDesignerWidth, this.flagDesignerMinY + y * this.flagDesignerScale.y, this.zLevel);
		        		tessellator.draw();	
	        		}
	        	}
	        	
	        	if (!(this.lastMousePressed && this.checkboxSelector.isSelected != null && this.checkboxSelector.isSelected) && this.selectionMaxX - selectionMinX > 0 && this.selectionMaxY - selectionMinY > 0)
	        	{
		        	tessellator.startDrawing(GL11.GL_LINE_STRIP);
		        	float col = (float) (Math.sin(this.ticksPassed * 0.3) * 0.4 + 0.1);
		        	tessellator.setColorRGBA_F(col, col, col, 1.0F);
		        	tessellator.addVertex(this.flagDesignerMinX + this.selectionMinX * this.flagDesignerScale.x, this.flagDesignerMinY + this.selectionMinY * this.flagDesignerScale.y, this.zLevel);
		        	tessellator.addVertex(this.flagDesignerMinX + this.selectionMaxX * this.flagDesignerScale.x, this.flagDesignerMinY + this.selectionMinY * this.flagDesignerScale.y, this.zLevel);
		        	tessellator.addVertex(this.flagDesignerMinX + this.selectionMaxX * this.flagDesignerScale.x, this.flagDesignerMinY + this.selectionMaxY * this.flagDesignerScale.y, this.zLevel);
		        	tessellator.addVertex(this.flagDesignerMinX + this.selectionMinX * this.flagDesignerScale.x, this.flagDesignerMinY + this.selectionMaxY * this.flagDesignerScale.y, this.zLevel);
		        	tessellator.addVertex(this.flagDesignerMinX + this.selectionMinX * this.flagDesignerScale.x, this.flagDesignerMinY + this.selectionMinY * this.flagDesignerScale.y, this.zLevel);
	        		tessellator.draw();	
	        	}
	            
	        	int guiRight = this.width / 2 + this.width / 3;
	        	int guiBottom = this.height / 2 + this.height / 4;
		        float x1 = this.sliderColorR.xPosition;
		        float x2 = guiRight - 10;
		        float y1 = guiBottom - 10 - (x2 - x1);
		        float y2 = guiBottom - 10;
		        
		        tessellator.startDrawingQuads();
		        tessellator.setColorRGBA_F(0, 0, 0, 1.0F);
		        tessellator.addVertex((double)x2, (double)y1, (double)this.zLevel);
		        tessellator.addVertex((double)x1, (double)y1, (double)this.zLevel);
		        tessellator.addVertex((double)x1, (double)y2, (double)this.zLevel);
		        tessellator.addVertex((double)x2, (double)y2, (double)this.zLevel);
		        tessellator.draw();
		        
		        tessellator.startDrawingQuads();
		        tessellator.setColorRGBA_F(this.sliderColorR.getNormalizedValue(), this.sliderColorG.getNormalizedValue(), this.sliderColorB.getNormalizedValue(), 1.0F);
		        tessellator.addVertex((double)x2 - 1, (double)y1 + 1, (double)this.zLevel);
		        tessellator.addVertex((double)x1 + 1, (double)y1 + 1, (double)this.zLevel);
		        tessellator.addVertex((double)x1 + 1, (double)y2 - 1, (double)this.zLevel);
		        tessellator.addVertex((double)x2 - 1, (double)y2 - 1, (double)this.zLevel);
		        tessellator.draw();
		        
		        GL11.glShadeModel(GL11.GL_FLAT);
		        GL11.glDisable(GL11.GL_BLEND);
		        GL11.glEnable(GL11.GL_ALPHA_TEST);
		        GL11.glEnable(GL11.GL_TEXTURE_2D);
	        	
	        	break;
	        }
		}
		
        super.drawScreen(par1, par2, par3);
        
        if (this.currentState == EnumSpaceRaceGui.ADD_PLAYER && this.gradientListAddPlayers != null)
        {
        	this.gradientListAddPlayers.draw(par1, par2);
        }
    }
    
    private void drawFlagButton(int mouseX, int mouseY)
    {
		GL11.glPushMatrix();
        GL11.glTranslatef(this.buttonFlag_xPosition + 2.9F, this.buttonFlag_yPosition + this.buttonFlag_height + 1 - 4, 0);
        GL11.glScalef(49.0F, 47.5F, 1F);
        GL11.glTranslatef(0.0F, 0.0F, 1.0F);
        GL11.glScalef(1.0F, 1.0F, -1F);
		this.dummyFlag.flagData = this.spaceRaceData.getFlagData();
		this.dummyModel.renderFlag(this.dummyFlag, 0.0625F, this.ticksPassed);
		GL11.glColor3f(1, 1, 1);
		GL11.glPopMatrix();

		GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, 500.0F);
        int color = this.buttonFlag_hover ? 170 : 100;
        this.fontRendererObj.drawString("Customize Flag", this.buttonFlag_xPosition + this.buttonFlag_width / 2 - this.fontRendererObj.getStringWidth("Customize Flag") / 2, this.buttonFlag_yPosition + this.buttonFlag_height / 2 - 5, GCCoreUtil.to32BitColor(255, color, color, color), this.buttonFlag_hover);
        GL11.glPopMatrix();

        if (this.buttonFlag_hover)
        {
            this.drawRect(this.buttonFlag_xPosition, this.buttonFlag_yPosition, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + buttonFlag_height, GCCoreUtil.to32BitColor(255, 50, 50, 50));
        }
        
        this.drawRect(this.buttonFlag_xPosition + buttonFlag_width - 1, this.buttonFlag_yPosition, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + buttonFlag_height, GCCoreUtil.to32BitColor(255, 0, 0, 0));
        this.drawRect(this.buttonFlag_xPosition, this.buttonFlag_yPosition, buttonFlag_xPosition + 1, buttonFlag_yPosition + buttonFlag_height, GCCoreUtil.to32BitColor(255, 0, 0, 0));
        this.drawRect(this.buttonFlag_xPosition, this.buttonFlag_yPosition, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + 1, GCCoreUtil.to32BitColor(255, 0, 0, 0));
        this.drawRect(this.buttonFlag_xPosition, buttonFlag_yPosition + buttonFlag_height - 1, buttonFlag_xPosition + buttonFlag_width, buttonFlag_yPosition + buttonFlag_height, GCCoreUtil.to32BitColor(255, 0, 0, 0));
    }

    public void drawWorldBackground(int i)
    {
        if (this.mc.theWorld != null)
        {
        	int scaleX = Math.min(ticksPassed * 14, this.width / 3);
        	int scaleY = Math.min(ticksPassed * 14, this.height / 3);
        	
        	if (scaleX == this.width / 3 && scaleY == this.height / 3 && !this.initialized)
        	{
        		this.initialized = true;
        		this.initGui();
        	}
        	
            this.drawGradientRect(this.width / 2 - scaleX, this.height / 2 - scaleY, this.width / 2 + scaleX, this.height / 2 + scaleY, -1072689136, -804253680);
        }
        else
        {
            this.drawBackground(i);
        }
    }

	@Override
	public void onSelectionChanged(GuiElementCheckbox checkbox, boolean newSelected)
	{
		if (checkbox.equals(this.checkboxEraser))
		{
			if (newSelected)
			{
				this.sliderEraserSize.visible = true;
				
				if (this.checkboxPaintbrush.isSelected)
				{
					this.sliderBrushSize.visible = false;
					this.checkboxPaintbrush.isSelected = false;
				}
				else if (this.checkboxSelector.isSelected)
				{
					this.checkboxSelector.isSelected = false;
				}
				else if (this.checkboxColorSelector.isSelected)
				{
					this.checkboxColorSelector.isSelected = false;
				}
			}
			else
			{
				this.sliderEraserSize.visible = false;
			}
		}
		else if (checkbox.equals(this.checkboxPaintbrush))
		{
			if (newSelected)
			{
				this.sliderBrushSize.visible = true;
				
				if (this.checkboxEraser.isSelected)
				{
					this.sliderEraserSize.visible = false;
					this.checkboxEraser.isSelected = false;
				}
				else if (this.checkboxSelector.isSelected)
				{
					this.checkboxSelector.isSelected = false;
				}
				else if (this.checkboxColorSelector.isSelected)
				{
					this.checkboxColorSelector.isSelected = false;
				}
			}
			else
			{
				this.sliderBrushSize.visible = false;
			}
		}
		else if (checkbox.equals(this.checkboxSelector))
		{
			if (newSelected)
			{
				if (this.checkboxEraser.isSelected)
				{
					this.sliderEraserSize.visible = false;
					this.checkboxEraser.isSelected = false;
				}
				else if (this.checkboxPaintbrush.isSelected)
				{
					this.sliderBrushSize.visible = false;
					this.checkboxPaintbrush.isSelected = false;
				}
				else if (this.checkboxColorSelector.isSelected)
				{
					this.checkboxColorSelector.isSelected = false;
				}
			}
		}
		else if (checkbox.equals(this.checkboxColorSelector))
		{
			if (newSelected)
			{
				if (this.checkboxEraser.isSelected)
				{
					this.sliderEraserSize.visible = false;
					this.checkboxEraser.isSelected = false;
				}
				else if (this.checkboxPaintbrush.isSelected)
				{
					this.sliderBrushSize.visible = false;
					this.checkboxPaintbrush.isSelected = false;
				}
				else if (this.checkboxSelector.isSelected)
				{
					this.checkboxSelector.isSelected = false;
				}
			}
		}
	}

	@Override
	public boolean canPlayerEdit(GuiElementCheckbox checkbox, EntityPlayer player)
	{
		return true;
	}

	@Override
	public boolean getInitiallySelected(GuiElementCheckbox checkbox)
	{
		if (checkbox.equals(this.checkboxPaintbrush))
		{
			return true;
		}
		
		return false;
	}

	@Override
	public void onIntruderInteraction()
	{
		;
	}

	@Override
	public boolean canPlayerEdit(GuiElementTextBox textBox, EntityPlayer player) 
	{
		return true;
	}

	@Override
	public void onTextChanged(GuiElementTextBox textBox, String newText) 
	{
		if (textBox == this.textBoxRename)
		{
			if (!newText.equals(this.spaceRaceData.getTeamName()))
			{
				this.spaceRaceData.setTeamName(newText);
				this.hasDataChanged = true;
			}
		}
	}

	@Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

	@Override
	public String getInitialText(GuiElementTextBox textBox) 
	{
		if (textBox == this.textBoxRename)
		{
			return this.spaceRaceData.getTeamName();
		}
		
		return "";
	}

	@Override
	public int getTextColor(GuiElementTextBox textBox) 
	{
		return GCCoreUtil.to32BitColor(255, 255, 255, 255);
	}
}
