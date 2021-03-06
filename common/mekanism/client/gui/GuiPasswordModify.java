package mekanism.client.gui;

import mekanism.api.EnumColor;
import mekanism.api.Coord4D;
import mekanism.common.IElectricChest;
import mekanism.common.PacketHandler;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketElectricChest;
import mekanism.common.network.PacketElectricChest.ElectricChestPacketType;
import mekanism.common.tile.TileEntityElectricChest;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiPasswordModify extends GuiScreen
{
    public int xSize = 176;
    public int ySize = 95;
	
	public TileEntityElectricChest tileEntity;
	
	public ItemStack itemStack;
	
	public boolean isBlock;
	
	public GuiTextField newPasswordField;
	
	public GuiTextField confirmPasswordField;
	
	public String displayText = EnumColor.BRIGHT_GREEN + MekanismUtils.localize("gui.password.setPassword");
	
	public int ticker = 0;
	
	public GuiPasswordModify(TileEntityElectricChest tileentity)
	{
		isBlock = true;
		tileEntity = tileentity;
	}
	
	public GuiPasswordModify(ItemStack itemstack)
	{
		isBlock = false;
		itemStack = itemstack;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
		
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiWidth + 55, guiHeight + 68, 60, 20, MekanismUtils.localize("gui.confirm")));
		
		newPasswordField = new GuiTextField(fontRenderer, guiWidth + 60, guiHeight + 34, 80, 12);
		newPasswordField.setMaxStringLength(12);
		newPasswordField.setFocused(true);
		
		confirmPasswordField = new GuiTextField(fontRenderer, guiWidth + 60, guiHeight + 51, 80, 12);
		confirmPasswordField.setMaxStringLength(12);
		confirmPasswordField.setFocused(false);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button)
	{
		super.mouseClicked(mouseX, mouseY, button);
		newPasswordField.mouseClicked(mouseX, mouseY, button);
		confirmPasswordField.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public void keyTyped(char c, int i)
	{
		super.keyTyped(c, i);
		
		if(i == Keyboard.KEY_TAB)
		{
			if(!newPasswordField.isFocused() && !confirmPasswordField.isFocused())
			{
				newPasswordField.setFocused(true);
				return;
			}
			else if(newPasswordField.isFocused() && confirmPasswordField.isFocused())
			{
				newPasswordField.setFocused(true);
				confirmPasswordField.setFocused(false);
				return;
			}
			else {
				newPasswordField.setFocused(!newPasswordField.isFocused());
				confirmPasswordField.setFocused(!confirmPasswordField.isFocused());
				return;
			}
		}
		else if(i == Keyboard.KEY_RETURN)
		{
			tryModify();
		}
		
		newPasswordField.textboxKeyTyped(c, i);
		confirmPasswordField.textboxKeyTyped(c, i);
	}
	
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	@Override
	public void updateScreen()
	{
		newPasswordField.updateCursorCounter();
		
		if(ticker > 0)
		{
			ticker--;
		}
		else {
			displayText = EnumColor.BRIGHT_GREEN + MekanismUtils.localize("gui.password.setPassword");
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton)
	{
		if(guibutton.id == 0)
		{
			tryModify();
		}
	}
	
	public void tryModify()
	{
		if(newPasswordField.getText() == null || newPasswordField.getText().equals("") || confirmPasswordField.getText() == null || confirmPasswordField.getText().equals(""))
		{
			displayText = EnumColor.DARK_RED + MekanismUtils.localize("gui.password.fieldsEmpty");
			ticker = 30;
		}
		else if(!newPasswordField.getText().equals(confirmPasswordField.getText()))
		{
			displayText = EnumColor.DARK_RED + MekanismUtils.localize("gui.password.notMatching");
			ticker = 30;
		}
		else if(confirmPasswordField.getText().equals(getPassword()))
		{
			displayText = EnumColor.DARK_RED + MekanismUtils.localize("gui.password.identical");
			ticker = 30;
		}
		else {
			if(isBlock)
			{
				PacketHandler.sendPacket(Transmission.SERVER, new PacketElectricChest().setParams(ElectricChestPacketType.PASSWORD, confirmPasswordField.getText(), true, Coord4D.get(tileEntity)));
				PacketHandler.sendPacket(Transmission.SERVER, new PacketElectricChest().setParams(ElectricChestPacketType.SERVER_OPEN, false, true, Coord4D.get(tileEntity)));
			}
			else {
				((IElectricChest)itemStack.getItem()).setPassword(itemStack, confirmPasswordField.getText());
				PacketHandler.sendPacket(Transmission.SERVER, new PacketElectricChest().setParams(ElectricChestPacketType.PASSWORD, confirmPasswordField.getText(), false));
				PacketHandler.sendPacket(Transmission.SERVER, new PacketElectricChest().setParams(ElectricChestPacketType.SERVER_OPEN, false, false));
			}
		}
	}
	
	public String getPassword()
	{
		if(isBlock)
		{
			return tileEntity.password;
		}
		else {
			return ((IElectricChest)itemStack.getItem()).getPassword(itemStack);
		}
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTick)
	{
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(MekanismUtils.getResource(ResourceType.GUI, "GuiPasswordModify.png"));
        
        int guiWidth = (width - xSize) / 2;
        int guiHeight = (height - ySize) / 2;
        
        drawTexturedModalRect(guiWidth, guiHeight, 0, 0, xSize, ySize);
        
        super.drawScreen(mouseX, mouseY, partialTick);
        
        fontRenderer.drawString(MekanismUtils.localize("gui.password"), guiWidth + 64, guiHeight + 5, 0x404040);
        fontRenderer.drawString(displayText, guiWidth + 37, guiHeight + 19, 0x404040);
        fontRenderer.drawString("Enter:", guiWidth + 27, guiHeight + 37, 0x404040);
        fontRenderer.drawString("Repeat:", guiWidth + 21, guiHeight + 54, 0x404040);
        newPasswordField.drawTextBox();
        confirmPasswordField.drawTextBox();
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
}
