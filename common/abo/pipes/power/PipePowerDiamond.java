/** 
 * Copyright (C) 2011-2013 Flow86
 * 
 * AdditionalBuildcraftObjects is open-source.
 *
 * It is distributed under the terms of my Open Source License. 
 * It grants rights to read, modify, compile or run the code. 
 * It does *NOT* grant the right to redistribute this software or its 
 * modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package abo.pipes.power;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import buildcraft.BuildCraftTransport;
import buildcraft.core.network.IClientState;
import buildcraft.core.utils.Utils;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.PipeTransportLiquids;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.pipes.PipeLogicDiamond;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import abo.pipes.ABOPipe;

public class PipePowerDiamond extends ABOPipe implements IPipeTransportPowerHook, IClientState {

	public PipePowerDiamond(int itemID) {
		super(new PipeTransportPower(), new PipeLogicDiamond(), itemID);

	}
	
	@Override
	public int getTextureIndex(ForgeDirection direction) {
		int baseTexture = 13 * 16 + 0;

		if (direction == ForgeDirection.UNKNOWN)
			return baseTexture;

		return baseTexture + 1 + direction.ordinal();
	}

	@Override
	public void receiveEnergy(ForgeDirection from, double val) {
		PipeTransportPower ptransport = (PipeTransportPower) transport;
		PipeLogicDiamond logicDiamond = (PipeLogicDiamond) logic;

		if (Utils.checkPipesConnections(container.getTile(from), container)) {
			boolean filter = false;
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = logicDiamond.getStackInSlot(from.ordinal() * 9 + slot);
				if(stack != null)
					filter = true;
			}
			
			if(!filter)
			{
				if (BuildCraftTransport.usePipeLoss) {
					ptransport.internalNextPower[from.ordinal()] += val * (1 - ptransport.powerResistance);
				} else {
					ptransport.internalNextPower[from.ordinal()] += val;
				}

				/*if (ptransport.internalNextPower[from.ordinal()] >= ptransport.MAX_POWER_INTERNAL) {
					worldObj.createExplosion(null, xCoord, yCoord, zCoord, 3, false);
					worldObj.setBlockWithNotify(xCoord, yCoord, zCoord, 0);
				}*/
			}
		}
	}

	@Override
	public void requestEnergy(ForgeDirection from, int i) {
		PipeTransportPower ptransport = (PipeTransportPower) transport;
		PipeLogicDiamond logicDiamond = (PipeLogicDiamond) logic;
		
		if (Utils.checkPipesConnections(container.getTile(from), container)) {
			boolean filter = false;
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = logicDiamond.getStackInSlot(from.ordinal() * 9 + slot);
				if(stack != null)
					filter = true;
			}

			if(filter)
			{
				ptransport.step();
				ptransport.nextPowerQuery[from.ordinal()] += i;
			}
		}
	}

	// ICLIENTSTATE
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		NBTTagCompound nbt = new NBTTagCompound();
		((PipeLogicDiamond) logic).writeToNBT(nbt);
		NBTBase.writeNamedTag(nbt, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		NBTBase nbt = NBTBase.readNamedTag(data);
		if (nbt instanceof NBTTagCompound) {
			logic.readFromNBT((NBTTagCompound) nbt);
		}
	}	
}
